package advise.processor;

import advise.checker.BeanChecker;
import advise.counter.GeneralCounter;
import advise.map.ProxyMappingUtil;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import store.StoreStatus;
import store.database.manager.DataStoreManager;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 功能: 第三层检查
 * <p>1. 检查循环依赖</p>
 * <p>2. 对通过检查的 Bean 进行包装</p>
 *
 * @author unknown100name
 * @date 2021.10.06
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class BeanCounterProcessor implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(BeanCounterProcessor.class);

    /**
     * 全局计数器
     */
    private static final GeneralCounter generalCounter = new GeneralCounter();

    /**
     * 刷盘标识, 初始化为不需要刷盘防止数据库等还没有初始化就进行刷盘
     */
    public static AtomicReference<StoreStatus> storeStatus = new AtomicReference<>(StoreStatus.NO_CHANGED);

    @Resource
    private DataStoreManager dataStoreManager;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 循环依赖检查
        // 每一次到这里都需要经过检查, 因为 BPP 和 Bean 实例注入的顺序是分离不开的, 而是交织的, 从排除循环依赖的 Log 中可以看出
        BeanChecker.circularCheck();

        // 获取内部 Bean
        Object innerBean = ProxyMappingUtil.getFullOriObject(bean);
        String fullBeanName = innerBean.getClass().getName();

        // 第一层就被排除掉的 Bean
        if (BeanChecker.isIgnore(fullBeanName)){
            return bean;
        }

        // 没有被 BeanBasicCheckProcessor 扫描过的 Bean, 直接排除防止循环依赖
        if (!BeanChecker.isChecked(fullBeanName)){
            return bean;
        }

        // 代理类
        logger.warn("[beancounter-start] Proxy bean[" + fullBeanName + "]");
        return getProxy(bean, methodInvocation -> counterProxy(bean, methodInvocation));
    }

    /**
     * 获取代理类
     * @param bean 需要被代理的类
     * @param methodInterceptor 代理 Interceptor
     * @return 代理类
     */
    private Object getProxy(Object bean, MethodInterceptor methodInterceptor){
        ProxyFactory proxyFactory = new ProxyFactory(bean);
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addAdvice(methodInterceptor);
        return proxyFactory.getProxy();
    }

    /**
     * 代理方法
     * @param bean 需要被代理的类
     * @param methodInvocation 代理 Invocation
     * @return 执行方法
     */
    private Object counterProxy(Object bean, MethodInvocation methodInvocation){
        // 在整体方法中的任何操作, 如果发生错误, 不进行任何补救措施, 从而保障正常业务的进行
        try {
            // 获取底层类
            bean = ProxyMappingUtil.getFullOriObject(bean);
            // 获取调用的类和方法
            Class<?> klass = bean.getClass();
            Method method = methodInvocation.getMethod();

            // 如果计数器不包含当前类, 则需要更新计数器(启动时)
            if (!generalCounter.containsClass(klass)){
                generalCounter.put(klass);
                // 数据库双向校验
                dataStoreManager.proofreadWithDatabase(generalCounter, klass);
            }

            try {
                // 获取原始的方法进行更新
                Method oriMethod = klass.getMethod(method.getName(), method.getParameterTypes());
                generalCounter.update(klass, oriMethod);
            }catch (Throwable ignored){
                // 不进行任何补偿
            }

            // 刷库标记更新
            storeStatus.set(StoreStatus.CHANGED);
            // 继续执行方法
            return methodInvocation.proceed();
        }catch (Throwable t){
            logger.error("[beancounter-main] Update count fail in the main process!", t);
            // FIXME: 发生错误不执行
            return bean;
        }
    }

    public static GeneralCounter getGeneralCounter() {
        return generalCounter;
    }
}
