package advise.processor;

import advise.checker.BeanChecker;
import advise.checker.CircularMap;
import advise.map.ProxyMappingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 功能: 第二层检查:
 * <p>1. 检查循环依赖</p>
 *
 * @author unknown100name
 * @date 2021.10.06
 */
@Component
@Order(1)
public class BeanCircularCheckProcessor implements BeanPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(BeanCircularCheckProcessor.class);

    /**
     * 依赖图
     */
    private static final CircularMap circularMap = CircularMap.getInstance();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
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

        // 扫描 Bean 依赖
        circularMap.populateBeanInstance(fullBeanName, innerBean);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
