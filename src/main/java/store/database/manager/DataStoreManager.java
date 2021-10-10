package store.database.manager;

import advise.counter.ClassCounter;
import advise.counter.GeneralCounter;
import advise.counter.MethodCounter;
import advise.processor.BeanCounterProcessor;
import client.domain.BeanCounterParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import config.BeanCounterConfig;
import store.AbstractStoreManager;
import store.StoreManager;
import store.StoreStatus;
import store.database.entity.ClassEntity;
import store.database.entity.MethodEntity;
import util.ConvertUtil;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 功能: 数据库持久类
 * @author unknown100name
 * @date 2021.10.06
 */
public class DataStoreManager extends AbstractStoreManager implements StoreManager {

    private static final Logger logger = LoggerFactory.getLogger(DataStoreManager.class);

    @Resource
    private DataStoreRepository dataStoreRepository;

    @Override
    @PostConstruct
    public void init() {
        super.init();

        // 是否开启了双向校验
        if (BeanCounterConfig.needProofreadWithDatabase()){
            logger.warn("[beancounter-config] Beancounter will read your record from database to proofread with the newest code if exists");
        }
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
                // 刷盘
                flushDatabase(copy);

                // CAS
                BeanCounterProcessor.storeStatus.compareAndSet(StoreStatus.CHANGED, StoreStatus.DB_DONE);
                BeanCounterProcessor.storeStatus.compareAndSet(StoreStatus.FILE_DONE, StoreStatus.NO_CHANGED);
            }catch (Throwable t){
                logger.error("[beancounter-database] Flush disk fail!", t);
            }
        };
    }

    /**
     * 刷盘实际操作
     * @param generalCounter 计数器
     */
    private void flushDatabase(GeneralCounter generalCounter){
        // 获取类名
        List<String> classNames = new ArrayList<>(generalCounter.getClassCounters().keySet());
        // 获取数据库数据
        List<ClassEntity> classCounterList = dataStoreRepository.getByClassNameIn(classNames);
        Map<String, ClassEntity> classEntityMap = new HashMap<>();
        classCounterList.forEach(classEntity ->
                classEntityMap.put(classEntity.getClassName(), classEntity));

        // 将内存中的数据刷入数据库中
        List<ClassEntity> classEntities = ConvertUtil.convertToClassEntityListWithDatabase(generalCounter, classEntityMap);
        dataStoreRepository.save(classEntities);
    }

    @Override
    public void initFlushSwitch() {
        flushSwitched = BeanCounterConfig.supportDatabase();
    }

    @Override
    public void initFlushInterval() {
        flushInterval = (long) BeanCounterConfig.STORE_DATABASE_INTERVAL;
    }

    @Override
    @PreDestroy
    public void destroy() {
        super.destroy();
    }


    // ---------------------------------------------- 其他方法 ------------------------------------------------


    /**
     * 从数据库中拿出信息放入 GeneralCounter 中
     * @param generalCounter 当前内存的 GeneralCounter
     * @param klass 需要被检索出来的 Class
     */
    public void proofreadWithDatabase(GeneralCounter generalCounter, Class<?> klass){
        // 获取内存中的信息
        ClassCounter classCounter = generalCounter.getClassCounters().get(klass.getName());
        // 获取数据库中的信息
        ClassEntity classEntity = dataStoreRepository.getByClassNameEquals(klass.getName());

        if (classEntity != null) {

            // 总量更新
            classCounter.getCount().add(classEntity.getCount());
            // 数据库中的方法 List
            ListIterator<MethodEntity> methodEntityIterator = classEntity.getMethodEntityList().listIterator();
            // 内存中的方法 Map
            ConcurrentHashMap<String, MethodCounter> newMethodCounter = classCounter.getMethodCounters();

            for (MethodEntity methodEntity : classEntity.getMethodEntityList()) {
                // 如果包含, 则将数据添加进内存中
                if (newMethodCounter.containsKey(methodEntity.getMethodName())){
                    newMethodCounter.get(methodEntity.getMethodName()).getCount().add(methodEntity.getCount());
                }
                // 如果不包含, 则证明这个方法被删除掉了, 这届排除
                else {
                    methodEntityIterator.remove();
                }
            }

            // 更新数据库
            try {
                dataStoreRepository.save(classEntity);
            }catch (Throwable t){
                logger.error("[beancounter-database] Update classEntity to database fail", t);
            }
        }
    }

    /**
     * 从数据库中查询计数器结果
     * @param beanCounterParam 查询参数
     * @return 计数器
     */
    public GeneralCounter getGeneralCounter(BeanCounterParam beanCounterParam){
        List<ClassEntity> classCounterList = dataStoreRepository.getByCountBetweenAndClassNameLike(
                (long) Optional.ofNullable(beanCounterParam.getMinCount()).orElse(Integer.MIN_VALUE),
                (long) Optional.ofNullable(beanCounterParam.getMaxCount()).orElse(Integer.MAX_VALUE),
                "%" + Optional.ofNullable(beanCounterParam.getClassName()).orElse("") + "%");
        return ConvertUtil.convertToGeneralCounter(classCounterList);
    }

}
