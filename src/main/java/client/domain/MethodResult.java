package client.domain;

/**
 * 功能: 返回结果中使用, 方法计数器查询结果
 * @author unknown100name
 * @date 2021.10.06
 */
public class MethodResult {

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 方法调用次数
     */
    private Long count;

    /**
     * 方法最新调用时间 (ms)
     */
    private Long lastCallTime;

    public MethodResult(String methodName, Long count, Long lastCallTime) {
        this.methodName = methodName;
        this.count = count;
        this.lastCallTime = lastCallTime;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
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
}
