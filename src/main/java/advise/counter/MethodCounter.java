package advise.counter;

import com.alibaba.fastjson.annotation.JSONField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.LongAdder;

/**
 * 功能: 内存中使用, 方法调用次数计数器
 *
 * @author unknown100name
 * @date 2021.10.06
 */
public class MethodCounter implements Serializable {

    private static final long serialVersionUID = -7333434052158223853L;

    @JSONField(serialize = false)
    private static final Logger logger = LoggerFactory.getLogger(MethodCounter.class);

    /**
     * 方法调用次数
     */
    private LongAdder count;

    /**
     * 方法最近调用时间
     */
    private Long lastCallTime;

    /**
     * 对应的 method
     */
    @JSONField(serialize = false)
    private Method method;

    public MethodCounter() {
    }

    public MethodCounter(Method method) {
        this.method = method;
        this.count = new LongAdder();
    }

    /**
     * 更新 method 数据 (调用次数 + 1)
     */
    public void update() {
        this.count.increment();
        this.lastCallTime = System.currentTimeMillis();
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

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    @Override
    protected MethodCounter clone() throws CloneNotSupportedException {
        MethodCounter clone = new MethodCounter();
        LongAdder cloneCount = new LongAdder();
        cloneCount.add(this.count.longValue());
        clone.setCount(cloneCount);
        clone.setLastCallTime(this.lastCallTime);
        clone.setMethod(this.method);
        return clone;
    }
}
