package advise.map;

import advise.counter.MethodCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Field;

/**
 * 功能: 映射关系工具类
 *
 * @author unknown100name
 * @date 2021.10.06
 */
public class ProxyMappingUtil {

    private static final Logger logger = LoggerFactory.getLogger(ProxyMappingUtil.class);

    /**
     * 映射关系存储类
     */
    private static final ProxyMappingBuffer proxyMappingBuffer = ProxyMappingBuffer.getInstance();

    /**
     * 获取单层代理关系
     * @param proxyObject 代理类
     * @return 底层类
     */
    private static Object getOriObject(Object proxyObject){
        if (!AopUtils.isAopProxy(proxyObject)){
            return proxyObject;
        }

        // 查询缓存
        Object target = proxyMappingBuffer.getTargetFromMap(proxyObject);
        if (target != null){
            return target;
        }

        // 是否被 JDK 代理
        if (AopUtils.isJdkDynamicProxy(proxyObject)){
            return getOriObjectFromJdkProxy(proxyObject);
        }
        // 是否被 CGLIB 代理
        if (AopUtils.isCglibProxy(proxyObject)){
            return getOriObjectFromCglibProxy(proxyObject);
        }
        // 返回默认原值
        return proxyObject;
    }

    /**
     * 获取多层底层类
     * @param proxyObject 代理类
     * @return 最底层类
     */
    public static Object getFullOriObject(Object proxyObject){
        while(AopUtils.isAopProxy(proxyObject)){
            proxyObject = getOriObject(proxyObject);
        }
        return proxyObject;
    }

    /**
     * 从 JDK 代理的类中获取底层类
     * @param proxyObject 代理类
     * @return 底层类
     */
    private static Object getOriObjectFromJdkProxy(Object proxyObject) {
        Field callback = null;

        try{
            callback = proxyObject.getClass().getDeclaredField("h");
        }catch (NoSuchFieldException e) {
            logger.error("[beancounter-start] JDK proxy object[" + proxyObject.getClass() + "] doesn't have filed [h]", e);
            return null;
        }

        callback.setAccessible(true);
        AopProxy aopProxy = null;

        try {
            aopProxy = (AopProxy) callback.get(proxyObject);
        } catch (IllegalAccessException e) {
            logger.error("[beancounter-start] JDK proxy object[" + proxyObject.getClass() + "]#[h] is null", e);
            return null;
        }

        Field advised = null;

        try {
            advised = aopProxy.getClass().getDeclaredField("advised");
        } catch (NoSuchFieldException e) {
            logger.error("[beancounter-start] JDK proxy object[" + proxyObject.getClass() + "]#[h] doesn't have filed [advised]", e);
            return null;
        }

        advised.setAccessible(true);
        Object oriObject = null;

        try {
            oriObject = ((AdvisedSupport) advised.get(aopProxy)).getTargetSource().getTarget();
            return oriObject;
        } catch (Exception e) {
            logger.error("[beancounter-start] JDK proxy object[" + proxyObject.getClass() + "]#[h]#[advised] doesn't have field [" + aopProxy + "] to get the targetSource", e);
            return null;
        }
    }

    /**
     * 从 CGLIB 代理的类中获取底层类
     * @param proxyObject 代理类
     * @return 底层类
     */
    private static Object getOriObjectFromCglibProxy(Object proxyObject) {
        Field callback = null;

        try{
            callback = proxyObject.getClass().getDeclaredField("CGLIB$CALLBACK_0");
        }catch (NoSuchFieldException e) {
            logger.error("[beancounter-start] Cglib proxy object[" + proxyObject.getClass() + "] doesn't have filed [CGLIB$CALLBACK_0]", e);
            return null;
        }

        callback.setAccessible(true);
        Object dynamicAdvisedInterceptor = null;

        try {
            dynamicAdvisedInterceptor = callback.get(proxyObject);
        } catch (IllegalAccessException e) {
            logger.error("[beancounter-start] Cglib proxy object[" + proxyObject.getClass() + "]#[CGLIB$CALLBACK_0] is null", e);
            return null;
        }

        Field advised = null;

        try {
            advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
        } catch (NoSuchFieldException e) {
            logger.error("[beancounter-start] Cglib proxy object[" + proxyObject.getClass() + "]#[CGLIB$CALLBACK_0] doesn't have filed [advised]", e);
            return null;
        }

        advised.setAccessible(true);
        Object oriObject = null;

        try {
            oriObject = ((AdvisedSupport) advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget();
            return oriObject;
        } catch (Exception e) {
            logger.error("[beancounter-start] Cglib proxy object[" + proxyObject.getClass() + "]#[CGLIB$CALLBACK_0]#[advised] doesn't have field [" + dynamicAdvisedInterceptor + "] to get the targetSource", e);
            return null;
        }
    }

}
