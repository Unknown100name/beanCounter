package store;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 功能: 持久化顶层类
 * @author unknown100name
 * @date 2021.10.06
 */
public interface StoreManager {

    /**
     * 初始化方法, 在 bean 完成之后
     */
    @PostConstruct
    void init();

    /**
     * 销毁方法, 在 bean 销毁前
     */
    @PreDestroy
    void destroy();
}
