package store.database.entity;

import javax.persistence.*;
import java.util.List;

/**
 * 功能: 数据库使用, 方法计数器
 * @author unknown100name
 * @date 2021.10.06
 */
@Entity
@Table(name = "beancopunter_method")
public class MethodEntity {

    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 类名
     */
    @Column(name = "method_name", unique = true, nullable = false)
    private String methodName;

    /**
     * 调用次数
     */
    @Column(name = "count", unique = false, nullable = false)
    private Long count;

    /**
     * 最新调用时间
     */
    @Column(name = "last_call_time", unique = false, nullable = true)
    private Long lastCallTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
