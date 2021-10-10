package client.domain;

import java.util.List;

/**
 * 功能: 对外查询结果
 * @author unknown100name
 * @date 2021.10.06
 */
public class BeanCounterResult {

    /**
     * 类结果
     */
    private List<ClassResult> classResultList;

    /**
     * 总返回个数
     */
    private Integer totalClass;

    /**
     * 查询参数
     */
    private BeanCounterParam beanCounterParam;

    /**
     * 错误信息
     */
    private String errorMessage;

    public BeanCounterResult() {
    }

    public List<ClassResult> getClassResultList() {
        return classResultList;
    }

    public void setClassResultList(List<ClassResult> classResultList) {
        this.classResultList = classResultList;
    }

    public Integer getTotalClass() {
        return totalClass;
    }

    public void setTotalClass(Integer totalClass) {
        this.totalClass = totalClass;
    }

    public BeanCounterParam getBeanCounterParam() {
        return beanCounterParam;
    }

    public void setBeanCounterParam(BeanCounterParam beanCounterParam) {
        this.beanCounterParam = beanCounterParam;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
