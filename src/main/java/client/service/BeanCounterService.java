package client.service;

import advise.counter.ClassCounter;
import advise.counter.GeneralCounter;
import advise.processor.BeanCounterProcessor;
import client.domain.BeanCounterParam;
import client.domain.BeanCounterResult;
import client.domain.ClassResult;
import client.util.ComparatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import config.BeanCounterConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import store.database.manager.DataStoreManager;
import util.ConvertUtil;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 功能: 对外提供查询接口
 * @author unknown100name
 * @date 2021.10.06
 */
@Service
public class BeanCounterService {

    private static final Logger logger = LoggerFactory.getLogger(BeanCounterService.class);

    @Resource
    private DataStoreManager dataStoreManager;

    /**
     * 对外查询方法
     * @param beanCounterParam 查询参数
     * @return 查询结果
     */
    public BeanCounterResult search(BeanCounterParam beanCounterParam){

        BeanCounterResult beanCounterResult = new BeanCounterResult();

        // 校验查询参数
        verifyParam(beanCounterParam, beanCounterResult);
        if (beanCounterResult.getErrorMessage() != null){
            return beanCounterResult;
        }

        GeneralCounter generalCounter = null;
        switch (BeanCounterConfig.SEARCH_SOURCE){
            case MEMORY:
                generalCounter = BeanCounterProcessor.getGeneralCounter();
                break;
            case DATABASE:
                generalCounter = dataStoreManager.getGeneralCounter(beanCounterParam);
                break;
            default:
                logger.error("[beancounater-config] No config for the [beancounter.search-source] when running!");
                beanCounterResult.setErrorMessage("No config for the [beancounter.search-source] when running!");
                return beanCounterResult;
        }

        // 过滤计数器
        filter(beanCounterParam, generalCounter);

        // 构建结果
        List<ClassResult> classResultList = ConvertUtil.convertToClassResultList(generalCounter, beanCounterParam.isSearchMethod());
        buildResult(beanCounterParam, beanCounterResult, classResultList);
        return beanCounterResult;
    }

    /**
     * 校验查询参数
     * @param beanCounterParam 查询参数
     * @param beanCounterResult 查询结果
     */
    private void verifyParam(BeanCounterParam beanCounterParam, BeanCounterResult beanCounterResult) {
        // 最大值与最小值判定
        if ((beanCounterParam.getMinCount() == null && beanCounterParam.getMaxCount() != null)
            || (beanCounterParam.getMinCount() != null && beanCounterParam.getMaxCount() == null)){
            beanCounterResult.setErrorMessage("MinCount and maxCount need both null or not null");
            return ;
        }

        if (beanCounterParam.getMaxCount() != null && beanCounterParam.getMinCount() > beanCounterParam.getMaxCount()){
            beanCounterResult.setErrorMessage("MinCount is larger than maxCount");
            return;
        }

        // 排序方法判定
        Comparator<ClassResult> comparator = ComparatorFactory.getComparator(beanCounterParam.getOrder());
        if (comparator == null){
            beanCounterResult.setErrorMessage("Have no comparator for order [" + beanCounterParam.getOrder() + "]");
        }
    }

    /**
     * 过滤不需要的数据
     * FIXME: 使用内存时, 如果进行过滤就会删除内存中的数据
     * @param beanCounterParam 查询参数
     * @param generalCounter 内存计数器
     */
    private void filter(BeanCounterParam beanCounterParam, GeneralCounter generalCounter) {
        Iterator<Map.Entry<String, ClassCounter>> iterator = generalCounter.getClassCounters().entrySet().stream().iterator();
        if (iterator.hasNext()) {
            Map.Entry<String, ClassCounter> entry = iterator.next();
            // 类名过滤
            if (beanCounterParam.getClassName() != null && !StringUtils.containsAny(entry.getKey(), beanCounterParam.getClassName())){
                iterator.remove();
            }
            // 调用次数过滤
            if (beanCounterParam.getMinCount() != null && beanCounterParam.getMinCount() != null &&
                    ( entry.getValue().getCount().longValue() > (long) beanCounterParam.getMaxCount()
                    || entry.getValue().getCount().longValue() < (long) beanCounterParam.getMinCount())){
                iterator.remove();
            }
        }
    }

    /**
     * 构建查询结果
     * @param beanCounterParam 查询参数
     * @param beanCounterResult 查询结果
     * @param classResultList 查询数据
     */
    private void buildResult(BeanCounterParam beanCounterParam, BeanCounterResult beanCounterResult, List<ClassResult> classResultList) {
        Comparator<ClassResult> comparator = ComparatorFactory.getComparator(beanCounterParam.getOrder());
        classResultList.sort(comparator);

        beanCounterResult.setTotalClass(classResultList.size());
        if (beanCounterParam.getMaxReturnSize() != null){
            beanCounterResult.setTotalClass(Math.min(beanCounterParam.getMaxReturnSize(), classResultList.size()));
            classResultList = classResultList.subList(0, beanCounterParam.getMaxCount());
        }

        beanCounterResult.setBeanCounterParam(beanCounterParam);
        beanCounterResult.setClassResultList(classResultList);
    }
}