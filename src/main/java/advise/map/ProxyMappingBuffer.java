package advise.map;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 功能: 类映射关系缓存类, 防止每一次都要通过被包装的类找到原类
 *
 * @author unknown100name
 * @date 2021.10.06
 */
public class ProxyMappingBuffer {

    private static final ProxyMappingBuffer instance = new ProxyMappingBuffer();

    private ProxyMappingBuffer() { }

    public static ProxyMappingBuffer getInstance() {
        return instance;
    }

    /**
     * 映射关系信息
     * 被包装类 -> 底层类
     */
    private final ConcurrentHashMap<Object, Object> proxyMap = new ConcurrentHashMap<>();

    /**
     * 将信息添加进 Map 中
     * @param proxy 代理类
     * @param target 底层类
     * @throws Exception Exception
     */
    public void addProxyMap(Object proxy, Object target) throws Exception{
        // 包含多个底层类
        if (this.proxyMap.containsKey(proxy) && this.proxyMap.get(proxy) != target){
            throw new Exception("Conflict \"proxy -> target\" found in ProxyMap, Class[" + proxy + "] have two target Object, one is [" + this.proxyMap.get(proxy) + "]. and another is [" + target + "]");
        }
        // 包含代理类为 null 或底层类为 null
        if (proxy == null || target == null){
            throw new NullPointerException((proxy == null ? "proxy" : "target") + "is null");
        }
        // 正常情况
        this.proxyMap.put(proxy, target);
    }

    /**
     * 根据代理类获取底层类
     * @param object 代理类
     * @return 底层类
     */
    public Object getTargetFromMap(Object object){
        return this.proxyMap.get(object);
    }
}
