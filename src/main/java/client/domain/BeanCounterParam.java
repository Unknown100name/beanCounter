package client.domain;

/**
 * 功能: 对外查询参数
 * @author unknown100name
 * @date 2021.10.06
 */
public class BeanCounterParam {

    /**
     * 最大返回个数
     */
    private Integer maxReturnSize;

    /**
     * 是否返回方法
     */
    private boolean searchMethod;

    /**
     * 返回的类中调用次数最小值
     */
    private Integer minCount;

    /**
     * 返回的类中调用次数最大值
     */
    private Integer maxCount;

    /**
     * 排序方法
     */
    private String order;

    /**
     * 类名 LIKE 模糊搜索
     */
    private String className;

    public Integer getMaxReturnSize() {
        return maxReturnSize;
    }

    public void setMaxReturnSize(Integer maxReturnSize) {
        this.maxReturnSize = maxReturnSize;
    }

    public boolean isSearchMethod() {
        return searchMethod;
    }

    public void setSearchMethod(boolean searchMethod) {
        this.searchMethod = searchMethod;
    }

    public Integer getMinCount() {
        return minCount;
    }

    public void setMinCount(Integer minCount) {
        this.minCount = minCount;
    }

    public Integer getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
