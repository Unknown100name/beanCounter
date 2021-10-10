package util;

import advise.counter.ClassCounter;
import advise.counter.GeneralCounter;
import advise.counter.MethodCounter;
import client.domain.ClassResult;
import client.domain.MethodResult;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.type.AnnotationMetadata;
import store.database.entity.ClassEntity;
import store.database.entity.MethodEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * 功能: 数据库格式 / 内存格式 / 透出格式 转换器
 * @author unknown100name
 * @date 2021.10.06
 */
public class ConvertUtil {

    /**
     * 将数据库格式转换为内存计数器格式
     * @param classEntityList 数据库格式
     * @return 内存格式
     */
    public static GeneralCounter convertToGeneralCounter(List<ClassEntity> classEntityList){
        GeneralCounter generalCounter = new GeneralCounter();
        generalCounter.setClassCounters(new ConcurrentHashMap<>());
        for (ClassEntity classEntity : classEntityList) {
            ClassCounter classCounter = new ClassCounter();
            classCounter.setCount(new LongAdder());
            classCounter.getCount().add(classEntity.getCount());
            classCounter.setLastCallTime(classEntity.getLastCallTime());
            classCounter.setMethodCounters(new ConcurrentHashMap<>());

            for (MethodEntity methodEntity : classEntity.getMethodEntityList()) {
                MethodCounter methodCounter = new MethodCounter();
                methodCounter.setCount(new LongAdder());
                methodCounter.getCount().add(methodEntity.getCount());
                methodCounter.setLastCallTime(methodEntity.getLastCallTime());
                classCounter.getMethodCounters().put(methodEntity.getMethodName(), methodCounter);
            }

            generalCounter.getClassCounters().put(classEntity.getClassName(), classCounter);
        }

        return generalCounter;
        
    }

    /**
     * 将内存格式转换为结果格式
     * @param generalCounter 内存格式
     * @param containsMethod 是否包含方法
     * @return 结果格式
     */
    public static List<ClassResult> convertToClassResultList(GeneralCounter generalCounter, boolean containsMethod){
        List<ClassResult> classResultList = new ArrayList<>();

        ClassResult classResult = null;
        for (Map.Entry<String, ClassCounter> entry : generalCounter.getClassCounters().entrySet()) {
            classResult = new ClassResult();
            classResult.setClassName(entry.getKey());
            classResult.setCount(entry.getValue().getCount().longValue());
            classResult.setLastCallTime(entry.getValue().getLastCallTime());
            if (containsMethod){
                List<MethodResult> methodResultList = entry.getValue().getMethodCounters().entrySet().stream()
                        .map(methodEntry -> new MethodResult(methodEntry.getKey(), methodEntry.getValue().getCount().longValue(), methodEntry.getValue().getLastCallTime()))
                        .collect(Collectors.toList());
                classResult.setMethodResultList(methodResultList);
             }
            classResultList.add(classResult);
        }

        return classResultList;
    }

    /**
     * 将内存格式转换为数据库个事
     * @param generalCounter 内存格式
     * @param classEntityMap 数据库格式源
     * @return 数据库格式
     */
    public static List<ClassEntity> convertToClassEntityListWithDatabase(GeneralCounter generalCounter, Map<String, ClassEntity> classEntityMap){
        List<ClassEntity> classEntityList = new ArrayList<>();

        for (Map.Entry<String, ClassCounter> entry : generalCounter.getClassCounters().entrySet()) {
            ClassCounter classCounter = entry.getValue();
            ClassEntity classEntity = Optional.ofNullable(classEntityMap.get(entry.getKey())).orElse(new ClassEntity());
            classEntity.setClassName(entry.getKey());
            classEntity.setCount(classCounter.getCount().longValue());
            classEntity.setLastCallTime(classCounter.getLastCallTime());
            classEntity.setMethodEntityList(Optional.ofNullable(classEntity.getMethodEntityList()).orElse(new ArrayList<>()));

            for (Map.Entry<String, MethodCounter> methodEntry : classCounter.getMethodCounters().entrySet()) {
                MethodCounter methodCounter = methodEntry.getValue();
                // 找到对应的 method, 如果没有则创建一个
                MethodEntity methodEntity = classEntity.getMethodEntityList().stream()
                        .filter(m -> m.getMethodName().equals(methodEntry.getKey())).findFirst()
                        .orElse(new MethodEntity());
                methodEntity.setMethodName(methodEntry.getKey());
                methodEntity.setCount(methodCounter.getCount().longValue());
                methodEntity.setLastCallTime(methodCounter.getLastCallTime());

                if (methodEntity.getId() == null){
                    classEntity.getMethodEntityList().add(methodEntity);
                }
            }
            classEntityList.add(classEntity);
        }

        return classEntityList;
    }

    /**
     * 获取 Bean 的全量名
     * @param simpleBeanName bean 的简称
     * @param beanFactory beanFactory
     * @return Bean 的全量名
     */
    public static String convertToFullName(String simpleBeanName, ConfigurableListableBeanFactory beanFactory){
        try {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(simpleBeanName);
            // 防止强转出错
            if (!(beanDefinition instanceof AnnotatedBeanDefinition)){
                return null;
            }
            AnnotationMetadata metadata = ((AnnotatedBeanDefinition) beanDefinition).getMetadata();
            return metadata.getClassName();
        }catch (Throwable t){
            return null;
        }
    }
}