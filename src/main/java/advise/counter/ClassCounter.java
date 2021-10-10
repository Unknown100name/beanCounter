package advise.counter;

import com.alibaba.fastjson.annotation.JSONField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * 功能: 内存中使用, 类调用次数计数器
 * @author unknown100name
 * @date 2021.10.06
 */
public class ClassCounter implements Serializable {

    private static final long serialVersionUID = -5052357341849489156L;

    @JSONField(serialize = false)
    private static final Logger logger = LoggerFactory.getLogger(ClassCounter.class);

    /**
     * 类调用次数
     */
    private LongAdder count;

    /**
     * 类最近调用时间
     */
    private Long lastCallTime;

    /**
     * 对应的 Class
     */
    @JSONField(serialize = false)
    private Class<?> klass;

    /**
     * 下属的方法计数器
     */
    private ConcurrentHashMap<String, MethodCounter> methodCounters;

    /**
     * 排出的方法
     */
    private HashSet<String> ignoredSet;

    public ClassCounter() {
    }

    public ClassCounter(Class<?> klass) {
        this.klass = klass;
        this.initEmpty();
    }

    /**
     * 初始化 ClassCounter
     */
    private void initEmpty() {
        this.count = new LongAdder();
        this.methodCounters = new ConcurrentHashMap<>();
        this.ignoredSet = new HashSet<>();

        Method[] methods = klass.getDeclaredMethods();
        for (Method method : methods) {
            // Final 方法无法被 CGLIB 代理, 所以无法进行统计
            if (Modifier.isFinal(method.getModifiers())){
                logger.warn("[beancounter-start] Exclude bean[" + klass + "]#[" + method.getName() + "] because it's a final method");
                this.ignoredSet.add(method.getName());
            }else {
                this.methodCounters.put(method.getName(), new MethodCounter(method));
            }
        }
    }

    /**
     * 更新 method 数据 (调用次数 + 1)
     * @param method 调用的方法
     */
    public void update(Method method){
        this.count.increment();
        this.lastCallTime = System.currentTimeMillis();
        if (!this.ignoredSet.contains(method.getName())){
            this.methodCounters.get(method.getName()).update();
        }
    }

    public static Logger getLogger() {
        return logger;
    }

    public LongAdder getCount() {
        return count;
    }

    public void setCount(LongAdder count) {
        this.count = count;
    }

    public Long getLastCallTime() {
        return lastCallTime;
    }

    public void setLastCallTime(Long lastCallTime) {
        this.lastCallTime = lastCallTime;
    }

    public Class<?> getKlass() {
        return klass;
    }

    public void setKlass(Class<?> klass) {
        this.klass = klass;
    }

    public ConcurrentHashMap<String, MethodCounter> getMethodCounters() {
        return methodCounters;
    }

    public void setMethodCounters(ConcurrentHashMap<String, MethodCounter> methodCounters) {
        this.methodCounters = methodCounters;
    }

    public HashSet<String> getIgnoredSet() {
        return ignoredSet;
    }

    public void setIgnoredSet(HashSet<String> ignoredSet) {
        this.ignoredSet = ignoredSet;
    }

    @Override
    protected ClassCounter clone() throws CloneNotSupportedException {
        ClassCounter clone = new ClassCounter();
        LongAdder cloneCount = new LongAdder();
        cloneCount.add(this.count.longValue());
        clone.setCount(cloneCount);
        clone.setKlass(this.klass);
        clone.setLastCallTime(this.lastCallTime);
        clone.setMethodCounters(new ConcurrentHashMap<>(this.methodCounters.size()));
        for (Map.Entry<String, MethodCounter> entry : methodCounters.entrySet()) {
            clone.getMethodCounters().put(entry.getKey(), entry.getValue().clone());
        }
        clone.setIgnoredSet(new HashSet<>(this.ignoredSet));
        return clone;
    }
}
