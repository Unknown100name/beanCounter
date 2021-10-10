package store;

/**
 * 功能: 刷盘标志, 防止频繁刷盘
 * @author unknown100name
 * @date 2021.10.06
 */
public enum StoreStatus {

    /**
     * 已经被改变了, 需要刷盘
     */
    CHANGED(0),

    /**
     * 没有任何改变, 不需要刷盘
     */
    NO_CHANGED(1),

    /**
     * 文件刷盘完成
     */
    FILE_DONE(2),

    /**
     * DB 刷盘完成
     */
    DB_DONE(3);

    StoreStatus(Integer status) {}
}
