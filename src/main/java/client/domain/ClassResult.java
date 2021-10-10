package client.domain;

import java.util.List;

/**
 * 功能: 返回结果中使用, 类计数器查询结果
 * @author unknown100name
 * @date 2021.10.06
 */
public class ClassResult {

    /**
     * 类名
     */
    private String className;

    /**
     * 类调用次数
     */
    private Long count;

    /**
     * 类最新调用时间 (ms)
     */
    private Long lastCallTime;

    /**
     * 类下属方法计数器查询结果
     */
    private List<MethodResult> methodResultList;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Long getLastCallTime() {
        return lastCallTime;
    }

    public void setLastCallTime(Long lastCallTime) {
        this.lastCallTime = lastCallTime;
    }

    public List<MethodResult> getMethodResultList() {
        return methodResultList;
    }

    public void setMethodResultList(List<MethodResult> methodResultList) {
        this.methodResultList = methodResultList;
    }
}
