package advise.counter;

import com.alibaba.fastjson.annotation.JSONField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 功能: 内存中使用, 调用次数计数器
 * @author unknown100name
 * @date 2021.10.06
 */
public class GeneralCounter implements Serializable {

    private static final long serialVersionUID = -1934157472028906725L;

    @JSONField(serialize = false)
    private static final Logger logger = LoggerFactory.getLogger(GeneralCounter.class);

    /**
     * 下属的类计数器
     */
    private ConcurrentHashMap<String, ClassCounter> classCounters = new ConcurrentHashMap<>();

    /**
     * 添加新的类
     * @param klass class
     */
    public void put(Class<?> klass){
        this.classCounters.put(klass.getName(), new ClassCounter(klass));
    }

    /**
     * 更新 class 和 method 数据 (调用次数 + 1)
     * @param klass 调用类
     * @param method 调用方法
     */
    public void update(Class<?> klass, Method method){
        this.classCounters.get(klass.getName()).update(method);
    }

    /**
     * 是否包含某个类
     * @param klass 类
     * @return 是否包含 class
     */
    public boolean containsClass(Class<?> klass){
        return this.classCounters.containsKey(klass.getName());
    }

    public ConcurrentHashMap<String, ClassCounter> getClassCounters() {
        return classCounters;
    }

    public void setClassCounters(ConcurrentHashMap<String, ClassCounter> classCounters) {
        this.classCounters = classCounters;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        GeneralCounter clone = new GeneralCounter();
        clone.setClassCounters(new ConcurrentHashMap<>());
        for (Map.Entry<String, ClassCounter> entry : classCounters.entrySet()) {
            clone.getClassCounters().put(entry.getKey(), entry.getValue().clone());
        }
        return clone;
    }
}
