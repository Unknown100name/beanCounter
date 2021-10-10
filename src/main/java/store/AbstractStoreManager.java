package store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 功能: 持久化基础实现类
 * @author unknown100name
 * @date 2021.10.06
 */
public abstract class AbstractStoreManager implements StoreManager{

    private static final Logger logger = LoggerFactory.getLogger(AbstractStoreManager.class);

    /**
     * 刷盘定时任务执行器
     */
    protected final ScheduledExecutorService flashDiskExecutor = Executors.newSingleThreadScheduledExecutor();

    /**
     * 刷盘定时任务
     */
    protected Runnable flush;

    /**
     * 刷盘时间间隔
     */
    protected Long flushInterval;

    /**
     * 刷盘开关
     */
    protected  Boolean flushSwitched;

    @Override
    public void init() {
        initFlush();
        initFlushSwitch();
        initFlushInterval();

        // 添加定时任务
        if (flushSwitched){
            flashDiskExecutor.scheduleWithFixedDelay(flush, 0L, flushInterval, TimeUnit.SECONDS);
        }
    }

    /**
     * 更新刷盘任务
     */
    public abstract void initFlush();

    /**
     * 更新刷盘开关
     */
    public abstract void initFlushSwitch();

    /**
     * 更新刷盘时间间隔
     */
    public abstract void initFlushInterval();

    /**
     * 当 bean 删除的时候最后执行一次持久化
     */
    @Override
    public void destroy() {
        if (flushSwitched){
            try {
                flashDiskExecutor.awaitTermination(2L, TimeUnit.SECONDS);
                Thread lastDance = new Thread(flush);
                lastDance.start();
                lastDance.join();
            } catch (InterruptedException e) {
                logger.warn("[beancounter-end] Last save fail when bean is destroyed", e);
            }
        }
    }
}
