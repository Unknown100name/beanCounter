package advise.checker;

import java.util.HashMap;
import java.util.Objects;

/**
 * 功能: 用于对 Bean 进行包装, 获取其信息
 * @author unknown100name
 * @date 2021.10.06
 */
public class BeanWrapperNode {

    private String beanName;

    private Object bean;

    private HashMap<String, BeanWrapperNode> dependencyMap;

    private HashMap<String, BeanWrapperNode> beDependencyMap;

    public BeanWrapperNode(String beanName) {
        this.beanName = beanName;
        this.dependencyMap = new HashMap<>();
        this.beDependencyMap = new HashMap<>();
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public HashMap<String, BeanWrapperNode> getDependencyMap() {
        return dependencyMap;
    }

    public void setDependencyMap(HashMap<String, BeanWrapperNode> dependencyMap) {
        this.dependencyMap = dependencyMap;
    }

    public HashMap<String, BeanWrapperNode> getBeDependencyMap() {
        return beDependencyMap;
    }

    public void setBeDependencyMap(HashMap<String, BeanWrapperNode> beDependencyMap) {
        this.beDependencyMap = beDependencyMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BeanWrapperNode that = (BeanWrapperNode) o;
        return Objects.equals(beanName, that.beanName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beanName);
    }
}
