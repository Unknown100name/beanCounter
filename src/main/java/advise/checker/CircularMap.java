package advise.checker;

import advise.map.ProxyMappingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 功能: 依赖图实现类
 * <p>1. 实例化 BeanWrapper</p>
 * <p>2. 填充 DependsOn 依赖</p>
 * <p>3. 填充 @Resource 和 @Autowired 依赖</p>
 * @author unknown100name
 * @date 2021.10.06
 */
public class CircularMap {

    private static final Logger logger = LoggerFactory.getLogger(CircularMap.class);

    private static final CircularMap instance = new CircularMap();

    public static CircularMap getInstance() {
        return instance;
    }

    private CircularMap() {

    }

    /**
     * Bean 依赖图
     * Full Bean Name -> BeanWrapper
     */
    private final Map<String, BeanWrapperNode> beanMap = new HashMap<>();

    /**
     * 通过　Full Bean Name 构建 BeanWrarpper 并添加进 BeanWrapper 中
     * @param fullBeanName Bean 全量名
     * @return BeanWrapper
     */
    public BeanWrapperNode initBeanWrapper(String fullBeanName){
        BeanWrapperNode currentBeanWrapper = getOrCreateBeanWrapperNode(fullBeanName);
        beanMap.put(currentBeanWrapper.getBeanName(), currentBeanWrapper);
        return currentBeanWrapper;
    }

    /**
     * 检索 Bean DependsOn 依赖, 并添加进 BeanWrapper 中
     * @param simpleBeanName Bean 简称
     * @param fullBeanName Bean 全量名
     * @param beanFactory BeanFactory
     */
    public void populateDependence(String simpleBeanName, String fullBeanName, ConfigurableListableBeanFactory beanFactory){
        // 获取当前的 BeanWrapper
        BeanWrapperNode currentBeanWrapper = getOrCreateBeanWrapperNode(fullBeanName);
        // 获取 dependsOn
        BeanDefinition beanDefinition = beanFactory.getBeanDefinition(simpleBeanName);
        String[] dependsOn= beanDefinition.getDependsOn();
        if (dependsOn != null){
            for (String dependsOnBeanName : dependsOn) {
                // 获取依赖的 BeanWrapper
                BeanWrapperNode dependsOnBeanWrapper = initBeanWrapper(dependsOnBeanName);
                // 添加映射关系
                currentBeanWrapper.getDependencyMap().put(dependsOnBeanName, dependsOnBeanWrapper);
                dependsOnBeanWrapper.getBeDependencyMap().put(fullBeanName, currentBeanWrapper);
            }
        }
    }

    /**
     * 添加 Bean @Resource 和 @Autowired 依赖, 并添加进 BeanWrapper 中
     * @param fullBeanName Bean 全量名
     * @param bean Bean 实例
     */
    public void populateBeanInstance(String fullBeanName, Object bean){
        // 获取当前的 BeanWrapper
        BeanWrapperNode currentBeanWrapper = beanMap.get(fullBeanName);

        // 排除没有被扫描到的 Bean
        if (currentBeanWrapper == null){
            logger.warn("[beancounter-start] Exclude bean[" + fullBeanName + "] because it's not in the beanMap, so that it may cause circular dependency");
            return;
        }

        // 排除没有实例的 Bean (由于 Spring 对 Bean 实例的添加与 BPP 是交替进行的关系, 所以很有可能经过 BPP 时对应的类还没有进行注入)
        if (bean == null){
            return;
        }

        currentBeanWrapper.setBean(bean);
        Class<?> klass = bean.getClass();
        Field[] fields = klass.getDeclaredFields();

        // 遍历所有的字段属性
        for (Field field : fields) {
            // 如果不是自动注入可以直接排除
            if (field.isAnnotationPresent(Autowired.class) || field.isAnnotationPresent(Resource.class)){
                // 全局 try 防止获取环节发生错误直接退出
                try {
                    // 获取值
                    field.setAccessible(true);
                    Object injectBean = field.get(bean);
                    // 对应的信息
                    BeanWrapperNode injectBeanWrapper = null;
                    String injectFullBeanName = null;
                    Class<?> injectClass = null;

                    // 首先尝试从值上获取, 值 === 实例, 所以一定是精确地
                    if (injectBean != null){
                        injectBean = ProxyMappingUtil.getFullOriObject(injectBean);
                        injectClass = injectBean.getClass();
                        injectFullBeanName = injectClass.getName();
                        injectBeanWrapper = beanMap.get(injectFullBeanName);
                    }

                    // 如果没有值, 则尝试从类型上获取, 类型有可能有对应的继承实现, 所以不一定是正确的, 但是依赖关系多了总比少好
                    if (injectBeanWrapper == null){
                        injectClass = field.getType();
                        injectFullBeanName = injectClass.getName();
                        injectBeanWrapper = beanMap.get(injectFullBeanName);
                    }

                    // 如果啥都没有, 则证明这个不存在于 BeanMap 中
                    if (injectBeanWrapper == null){
                        logger.warn("[beancounter-start] Bean[" + fullBeanName + "]#[" + field.getName()  +"]'s type is " + injectFullBeanName + ", but it doesn't exist in beanMap");
                        continue;
                    }

                    // 如果之前记录过, 直接退出
                    if (currentBeanWrapper.getDependencyMap().containsKey(injectFullBeanName) && injectBeanWrapper.getBeDependencyMap().containsKey(fullBeanName)){
                        continue;
                    }

                    // 否则添加关系
                    currentBeanWrapper.getDependencyMap().put(injectFullBeanName, injectBeanWrapper);
                    injectBeanWrapper.getBeDependencyMap().put(fullBeanName, currentBeanWrapper);
                    // 递归查询
                    this.populateBeanInstance(injectFullBeanName, injectBean);
                }catch (Throwable t){
                    logger.error("[beancounter-start] Bean[" + fullBeanName + "] occurs error", t);
                }
            }
        }
    }

    public Map<String, BeanWrapperNode> getBeanMap() {
        return beanMap;
    }

    private BeanWrapperNode getOrCreateBeanWrapperNode(String fullBeanName) {
        return Optional.of(beanMap.get(fullBeanName)).orElse(new BeanWrapperNode(fullBeanName));
    }
}
