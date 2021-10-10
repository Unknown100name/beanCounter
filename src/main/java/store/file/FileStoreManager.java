package store.file;

import advise.counter.GeneralCounter;
import advise.processor.BeanCounterProcessor;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import config.BeanCounterConfig;
import org.springframework.stereotype.Component;
import store.AbstractStoreManager;
import store.StoreManager;
import store.StoreStatus;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 功能: 文件持久化类
 * @author unknown100name
 * @date 2021.10.06
 */
@Component
public class FileStoreManager extends AbstractStoreManager implements StoreManager {

    private static final Logger logger = LoggerFactory.getLogger(FileStoreManager.class);

    private final FileStoreRepository fileStoreRepository = FileStoreRepository.getInstance();

    @Override
    @PostConstruct
    public void init() {
        super.init();
    }

    @Override
    public void initFlush() {
        flush = () -> {
            try {
                // 当不需要刷盘的时候直接跳过
                if (BeanCounterProcessor.storeStatus.get() == StoreStatus.NO_CHANGED){
                    return ;
                }

                // 获取计数器
                GeneralCounter copy = BeanCounterProcessor.getGeneralCounter();
                String json = JSONObject.toJSONString(copy);
                String fileName = BeanCounterConfig.getStoreFileName();
                // 刷盘
                flushDisk(fileName, json);

                // CAS
                BeanCounterProcessor.storeStatus.compareAndSet(StoreStatus.CHANGED, StoreStatus.FILE_DONE);
                BeanCounterProcessor.storeStatus.compareAndSet(StoreStatus.DB_DONE, StoreStatus.NO_CHANGED);
            }catch (Throwable t){
                logger.error("[beancounter-file] Flush disk fail!", t);
            }
        };
    }

    private void flushDisk(String fileName, String json) throws IOException {
        File file = fileStoreRepository.getOrCreateFileByName(fileName, "");
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(json.getBytes());
        fos.flush();
        fos.close();
    }

    @Override
    public void initFlushSwitch() {
        flushSwitched = BeanCounterConfig.supportFile();
    }

    @Override
    public void initFlushInterval() {
        flushInterval = (long) BeanCounterConfig.STORE_FILE_INTERVAL;
    }

    @Override
    @PreDestroy
    public void destroy() {
        super.destroy();
    }
}
