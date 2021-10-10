package advise.processor;

import advise.checker.BeanChecker;
import advise.checker.CircularMap;
import config.BeanCounterConfig;
import config.BeanCounterExclude;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;
import util.ConvertUtil;

import java.util.Arrays;

/**
 * 功能: 第一层检查
 * <p>1. 构建 BeanWrapper</p>
 * <p>2. 扫描 DependsOn 并添加依赖关系</p>
 *
 * @author unknown100name
 * @date 2021.10.06
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class BeanBasicCheckProcessor implements BeanFactoryPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(BeanBasicCheckProcessor.class);

    /**
     * 依赖图
     */
    private static final CircularMap circularMap = CircularMap.getInstance();

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
        // 获取所有 Bean 的名称
        String[] beanNames = configurableListableBeanFactory.getBeanDefinitionNames();
        for (String simpleBeanName : beanNames) {
            // SimpleBeanName 用于从 beanFactory 中获取 Bean
            // FullBeanName 用于 beanCounter 全局唯一定位
            String fullBeanName = ConvertUtil.convertToFullName(simpleBeanName, configurableListableBeanFactory);

            // 没有 FullBeanName
            if (fullBeanName == null){
                logger.info("[beancounter-start] Exclude bean[" + simpleBeanName + "] because it's not a annotatedBeanDefinition beanFactory");
                BeanChecker.ignoredBean(simpleBeanName);
                continue;
            }

            BeanDefinition beanDefinition = configurableListableBeanFactory.getBeanDefinition(simpleBeanName);
            AnnotationMetadata metadata = ((AnnotatedBeanDefinition) beanDefinition).getMetadata();

            // Final 类无法被 CGLIB 继承
            if (metadata.isFinal()){
                logger.info("[beancounter-start] Exclude bean[" + fullBeanName + "] because it's a final class");
                BeanChecker.ignoredBean(fullBeanName);
                continue;
            }

            // 接口和抽象类不需要扩展
            if (metadata.isAbstract() || metadata.isInterface()){
                logger.info("[beancounter-start] Exclude bean[" + fullBeanName + "] because it's a abstract class or interface");
                BeanChecker.ignoredBean(fullBeanName);
                continue;
            }

            // 注解类不需要扩展
            if (metadata.isAnnotation()){
                logger.info("[beancounter-start] Exclude bean[" + fullBeanName + "] because it's a annotation class");
                BeanChecker.ignoredBean(fullBeanName);
                continue;
            }

            // 不是单例模式, 之后的 Bean 实例不好确定, 这里直接排除
            if (!beanDefinition.isSingleton()){
                logger.info("[beancounter-start] Exclude bean[" + fullBeanName + "] because it's not a singleton class");
                BeanChecker.ignoredBean(fullBeanName);
                continue;
            }

            // 人工排除
            if (metadata.isAnnotated(BeanCounterExclude.class.getName())){
                logger.info("[beancounter-start] Exclude bean[" + fullBeanName + "] because it's a manual exclude class");
                BeanChecker.ignoredBean(fullBeanName);
                continue;
            }

            // 不是我们所需要扫描的路径
            if (!fullBeanName.startsWith(BeanCounterConfig.SCAN_PATH)){
                logger.info("[beancounter-start] Exclude bean[" + fullBeanName + "] because it's not a project class");
                BeanChecker.ignoredBean(fullBeanName);
                continue;
            }

            // BFPP 和 BPP 不需要扩展
            if (Arrays.stream(metadata.getInterfaceNames()).anyMatch(interfaceName -> interfaceName.startsWith("BeanFactoryPostProcessor") || interfaceName.startsWith("BeanPostProcessor"))){
                logger.info("[beancounter-start] Exclude bean[" + fullBeanName + "] because it's a BeanPostProcessor or BeanFactoryPostProcessor class");
                BeanChecker.ignoredBean(fullBeanName);
                continue;
            }

            // 初始化 BeanWrapper
            circularMap.initBeanWrapper(fullBeanName);
            // 扫描 DependsOn 信息
            circularMap.populateDependence(simpleBeanName, fullBeanName, configurableListableBeanFactory);
            // 表示自己 Checked 了这个 Bean
            BeanChecker.checkedBean(fullBeanName);

        }
    }
}
