package config;

import java.util.Arrays;

/**
 * 功能: 外部查询来源
 * @author unknown100name
 * @date 2021.10.06
 */
public enum SearchSource {

    /**
     * 内存
     */
    MEMORY(1, "MEMORY"),

    /**
     * 数据库
     */
    DATABASE(2, "DATABASE");

    private final Integer id;
    private final String source;

    SearchSource(Integer id, String source) {
        this.id = id;
        this.source = source;
    }

    /**
     * 根据查询方式获取对应的枚举值
     * @param datasource 查询来源
     * @return 枚举值
     */
    public static SearchSource get(String datasource){
        return Arrays.stream(values())
                .filter(valve -> valve.source.equalsIgnoreCase(datasource)).findFirst()
                .orElse(null);
    }
}
