package store.database.entity;

import javax.persistence.*;
import java.util.List;

/**
 * 功能: 数据库使用, 类计数器
 * @author unknown100name
 * @date 2021.10.06
 */
@Entity
@Table(name = "beancopunter_class")
public class ClassEntity {

    /**
     * id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 类名
     */
    @Column(name = "class_name", unique = true, nullable = false)
    private String className;

    /**
     * 下属方法
     */
    @OneToMany(targetEntity = MethodEntity.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<MethodEntity> methodEntityList;

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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<MethodEntity> getMethodEntityList() {
        return methodEntityList;
    }

    public void setMethodEntityList(List<MethodEntity> methodEntityList) {
        this.methodEntityList = methodEntityList;
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
