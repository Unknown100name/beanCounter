package advise.checker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 功能: 负责对依赖图进行检查 \ 添加 \ 排除 bean
 * @author unknown100name
 * @date 2021.10.06
 */
public class BeanChecker {

    private static final Logger logger = LoggerFactory.getLogger(BeanChecker.class);

    private static final CircularMap circularMap = CircularMap.getInstance();

    /**
     * 排除掉的 Bean
     */
    private static final Set<String> ignoreBeanName = new HashSet<>();

    /**
     * 检查过的 Bean (有一些 Bean 不会经过 BeanFactoryPostProcessor 但会经过 BeanPostProcessor 造成了这个问题)
     */
    private static final Set<String> checkedBeanName = new HashSet<>();

    /**
     * 是否需要忽略该 Bean
     */
    public static boolean isIgnore(String beanName){
        return ignoreBeanName.contains(beanName);
    }

    /**
     * 是否检查过该 Bean
     */
    public static boolean isChecked(String beanName){
        return checkedBeanName.contains(beanName);
    }

    /**
     * 标记忽略该 Bean
     */
    public static void ignoredBean(String beanName){
        ignoreBeanName.add(beanName);
    }

    /**
     * 标记检查该 Bean
     */
    public static void checkedBean(String beanName){
        checkedBeanName.add(beanName);
    }

    /**
     * <p>循环依赖检查</p>
     * <p>核心思想是 DFS 检查</p>
     */
    public static void circularCheck(){
        Map<String, BeanWrapperNode> allBeans = circularMap.getBeanMap();
        // 通过的 Bean
        Set<BeanWrapperNode> acceptBeans = new HashSet<>();
        // 拒绝的 Bean
        Set<BeanWrapperNode> rejectBeans = new HashSet<>();
        // 依赖链条
        LinkedList<BeanWrapperNode> dependencyLine = new LinkedList<>();

        // 对每个 bean 进行检查
        for (BeanWrapperNode bean : allBeans.values()) {
            // 如果没有被依赖, 则证明是 "头结点"
            if (bean.getBeDependencyMap().size() == 0){
                dependencyLine.add(bean);
                // 开始递归搜索, 结果保存在 acceptBeans 和 rejectBeans 中
                findDependencyFromBean(dependencyLine, acceptBeans, rejectBeans);
            }
        }

        Iterator<Map.Entry<String, BeanWrapperNode>> allBeansIterator = allBeans.entrySet().iterator();

        // 将结果保存进 ignoredBeanName 中
        while (allBeansIterator.hasNext()) {
            Map.Entry<String, BeanWrapperNode> entry = allBeansIterator.next();
            if (!acceptBeans.contains(entry.getValue())){
                logger.warn("[beancounter-start] Exclude bean[" + entry.getKey() + "] because it's in a circular dependence");
                ignoreBeanName.add(entry.getKey());
                allBeansIterator.remove();
            }
        }
    }

    /**
     * 递归对以来进行检查
     * @param dependencyLine 依赖链
     * @param acceptBeans 通过的 Bean
     * @param rejectBeans 不通过的 Bean
     */
    private static void findDependencyFromBean(LinkedList<BeanWrapperNode> dependencyLine, Set<BeanWrapperNode> acceptBeans, Set<BeanWrapperNode> rejectBeans) {
        // 获取当前 Bean
        BeanWrapperNode currentNode = dependencyLine.getLast();

        // 如果发现 ignored 中包含了, 那么之后就必然走向循环依赖, 直接排除
        if (ignoreBeanName.contains(currentNode.getBeanName())){
            dependencyLine.removeLast();
        }

        // 如果当前的 Bean 依赖为空, 则直接弹出, 走到下一个依赖去(没有依赖就不会有循环依赖)
        if (currentNode.getDependencyMap().isEmpty()){
            acceptBeans.add(currentNode);
            dependencyLine.removeLast();
            return;
        }

        // 遍历他的依赖
        for (Map.Entry<String, BeanWrapperNode> entry : currentNode.getDependencyMap().entrySet()) {
            // 如果他的依赖之前有过, 那证明这里有循环依赖了
            if (dependencyLine.contains(entry.getValue())){
                // 找到循环依赖的位置
                int index = dependencyLine.indexOf(entry.getValue());
                // 中间的所有 Bean 都会被 Reject
                for (int i = index; i < dependencyLine.size(); i++) {
                    rejectBeans.add(dependencyLine.get(i));
                }
            }
            // 如果不包含, 则通过该 Bean 进行递归前进
            else{
                dependencyLine.addLast(entry.getValue());
                findDependencyFromBean(dependencyLine, acceptBeans, rejectBeans);
            }
        }

        // 如果整个一圈下来, 该 Bean 都没有被排除, 则证明该 Bean 不存在循环依赖
        if (!rejectBeans.contains(currentNode)){
            acceptBeans.add(currentNode);
        }
    }
}
