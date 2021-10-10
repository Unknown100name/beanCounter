package config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 功能: 基础配置类, 可以通过 YML, Properties, beancounter.properties 或者其他能被 Environment 检测的方式进行配置
 * <p>BeanFactoryPostProcessor 保证 Config 被提前执行而不是延后执行</p>
 * <p>如果 Config 延后执行可能导致数据库初始化或文件初始化时, 发生 NPE</p>
 * <p></p>
 * <p>配置表</p>
 * <p>beancounter:</p>
 * <p>&nbsp;&nbsp;&nbsp;&nbsp;path: {@link BeanCounterConfig#SCAN_PATH} 扫描目录</p>
 * <p>&nbsp;&nbsp;&nbsp;&nbsp;search-source: {@link BeanCounterConfig#SEARCH_SOURCE} 查询来源: DATABASE / MEMORY</p>
 * <p>&nbsp;&nbsp;&nbsp;&nbsp;store:</p>
 * <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;interval: {@link BeanCounterConfig#STORE_FILE_INTERVAL} 文件持久化间隔时间(s)</p>
 * <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;path: {@link BeanCounterConfig#STORE_FILE_PATH} 文件持久化路径(不需要后缀名, 格式为 json)</p>
 * <p>&nbsp;&nbsp;&nbsp;&nbsp;database:</p>
 * <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;interval: {@link BeanCounterConfig#STORE_DATABASE_INTERVAL} 数据库时间花时间间隔(s)</p>
 * <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;show-sql: {@link BeanCounterConfig#SHOW_SQL} 是否打印数据库 SQL: true / false</p>
 * <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;format-sql: {@link BeanCounterConfig#FORMAT_SQL} 是否格式化数据库 SQL: true / false</p>
 * <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ddl-auto: {@link BeanCounterConfig#DDL_AUTO} 数据库初始化方式: CREATE / UPDATE</p>
 * <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;datasource: </p>
 * <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;url: {@link BeanCounterConfig#DATA_SOURCE} 数据库 url</p>
 * <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;username: {@link BeanCounterConfig#DATA_SOURCE} 数据库应用名</p>
 * <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;password: {@link BeanCounterConfig#DATA_SOURCE} 数据库密码</p>
 * <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;driver-class-name: {@link BeanCounterConfig#DATA_SOURCE} 数据库驱动</p>
 * <p></p>
 * @author unknown100name
 * @date 2021.10.7
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BeanCounterConfig implements EnvironmentAware, PriorityOrdered, BeanFactoryPostProcessor {

    public static final Logger logger = LoggerFactory.getLogger(BeanCounterConfig.class);

    /**
     * 扫描路径
     * <p>默认为 !@#$%^&*()_ 从而达到什么都不扫描的目的</p>
     */
    public static String SCAN_PATH;

    /**
     * 查询来源: DATABASE / MEMORY
     * <p>默认为 MEMORY</p>
     */
    public static SearchSource SEARCH_SOURCE;

    /**
     * 持久化方法: DATABASE / FILE
     * <p>多种持久化方式用 , 隔离</p>
     * <p>默认为空</p>
     */
    public static Set<String> STORE_TYPE;

    /**
     * 文件持久化时间间隔(s)
     * <p>默认为 10s</p>
     */
    public static Integer STORE_FILE_INTERVAL;

    /**
     * 文件持久化路径(不需要后缀, 格式为 json)
     * <p>默认为当前路径的 default-beancounter-store</p>
     */
    public static String STORE_FILE_PATH;

    /**
     * 数据库时间花时间间隔(s)
     * <p>默认为 10s</p>
     */
    public static Integer STORE_DATABASE_INTERVAL;

    /**
     * 是否打印数据库 SQL: true/false
     * <p>默认为 false</p>
     */
    public static Boolean SHOW_SQL;

    /**
     * 是否格式化数据库 SQL: true/false
     * <p>默认为 false</p>
     */
    public static Boolean FORMAT_SQL;

    /**
     * 数据库初始化方式 CREATE / UPDATE
     * <p>默认为 CREATE</p>
     */
    public static String DDL_AUTO;

    /**
     * 数据库信息
     * <p>DATA_SOURCE[0]: url</p>
     * <p>DATA_SOURCE[1]: username</p>
     * <p>DATA_SOURCE[2]: password</p>
     * <p>DATA_SOURCE[3]: driver</p>
     * <p>无默认值</p>
     */
    public static String[] DATA_SOURCE;

    /**
     * 获取持久化文件名
     * @return 持久化文件名 = 路径 + "-" + 日期 + "-" + ".json"
     */
    public static String getStoreFileName(){
        LocalDate time = LocalDate.now();
        String fileTime = (new DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_DATE)).toFormatter().format(time);
        return STORE_FILE_PATH + "-" + fileTime + ".json";
    }

    /**
     * @return 是否支持数据库持久化
     */
    public static boolean supportDatabase(){
        return STORE_TYPE.contains("DATABASE");
    }

    /**
     * @return 是否支持文件持久化
     */
    public static boolean supportFile(){
        return STORE_TYPE.contains("FILE");
    }

    /**
     * @return 启动时是否需要和数据库进行双向校验
     */
    public static boolean needProofreadWithDatabase(){
        return supportDatabase() && "UPDATE".equalsIgnoreCase(DDL_AUTO);
    }

    public static void setScanPath(String scanPath) {
        SCAN_PATH = scanPath;
    }

    public static void setSearchSource(SearchSource searchSource) {
        SEARCH_SOURCE = searchSource;
    }

    public static void setStoreType(Set<String> storeType) {
        STORE_TYPE = storeType;
    }

    public static void setStoreFileInterval(Integer storeFileInterval) {
        STORE_FILE_INTERVAL = storeFileInterval;
    }

    public static void setStoreFilePath(String storeFilePath) {
        STORE_FILE_PATH = storeFilePath;
    }

    public static void setStoreDatabaseInterval(Integer storeDatabaseInterval) {
        STORE_DATABASE_INTERVAL = storeDatabaseInterval;
    }

    public static void setShowSql(Boolean showSql) {
        SHOW_SQL = showSql;
    }

    public static void setFormatSql(Boolean formatSql) {
        FORMAT_SQL = formatSql;
    }

    public static void setDdlAuto(String ddlAuto) {
        DDL_AUTO = ddlAuto;
    }

    public static void setDataSource(String[] dataSource) {
        DATA_SOURCE = dataSource;
    }

    @Override
    public void setEnvironment(Environment environment) {
        // yml
        Properties ymlProperties = null;

        try{
            YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
            factory.setResources(new ClassPathResource("application.yml"));
            ymlProperties = factory.getObject();
        }catch (Throwable t){
            logger.warn("[beancounter-config] No application.yml in resource, beancounter will ignore it");
        }

        // properties
        Properties customizeProperties = new Properties();

        try {
            customizeProperties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("beancounter.properties"));
        }catch (Throwable t){
            logger.warn("[beancounter-config] No beancounter.properties in resource, beancounter will ignore it");
        }

        setScanPath(getFromProperties(environment, ymlProperties, customizeProperties, "beancounter.path", String.class, "!@#$%^&*()_"));
        String[] supportEnduranceProperties = getFromProperties(environment, ymlProperties, customizeProperties, "beancounter.store.support", String.class, "").split(",");
        setStoreType(Arrays.stream(supportEnduranceProperties).collect(Collectors.toSet()));

        if (supportFile()) {
            setStoreFileInterval(getFromProperties(environment, ymlProperties, customizeProperties, "beancounter.store.file.interval", Integer.class, 10));
            setStoreFilePath(getFromProperties(environment, ymlProperties, customizeProperties, "beancounter.store.file.path", String.class, "default-beancounter-store"));
        }

        if (supportDatabase()){
            setStoreDatabaseInterval(getFromProperties(environment, ymlProperties, customizeProperties, "beancounter.store.database.interval", Integer.class, 10));
            setShowSql(getFromProperties(environment, ymlProperties, customizeProperties, "beancounter.store.database.show-sql", Boolean.class, false));
            setFormatSql(getFromProperties(environment, ymlProperties, customizeProperties, "beancounter.store.database.format-sql", Boolean.class, false));
            setDdlAuto(getFromProperties(environment, ymlProperties, customizeProperties, "beancounter.store.database.ddl-auto", String.class, "CREATE"));
            if (!"CREATE".equalsIgnoreCase(DDL_AUTO) && !"UPDATE".equalsIgnoreCase(DDL_AUTO)){
                logger.warn("[beancounter-config] Unsupported ddl-auto choice [" + DDL_AUTO + "], beancounter set the default value [CREATE]");
                setDdlAuto("CREATE");
            }

            String url = getFromProperties(environment, ymlProperties, customizeProperties, "beancounter.store.database.datasource.url", String.class, null);
            String username = getFromProperties(environment, ymlProperties, customizeProperties, "beancounter.store.database.datasource.username", String.class, null);
            String password = getFromProperties(environment, ymlProperties, customizeProperties, "beancounter.store.database.datasource.password", String.class, null);
            String driver = getFromProperties(environment, ymlProperties, customizeProperties, "beancounter.store.database.datasource.driver-class-name", String.class, null);
            if (StringUtils.isAnyBlank(url, username, password, driver)){
                logger.error("[beancounter-config] No config for the [datasource url/username/password/driver-class-name, beanCounter reject database endurance!");
                STORE_TYPE.remove("DATABASE");
            }else {
                setDataSource(new String[]{url, username, password, driver});
            }

            SearchSource searchSource = SearchSource.get(getFromProperties(environment, ymlProperties, customizeProperties, "beancounter.search-source", String.class, "MEMORY"));
            if (searchSource != null){
                logger.warn("[beancounter-config] Unsupported search-source choice [" + SEARCH_SOURCE + "], beanCounter set the defaultValue [MEMORY]");
                searchSource = SearchSource.MEMORY;
            }

            setSearchSource(searchSource);
        }
    }

    private <TYPE> TYPE getFromProperties(Environment environment, Properties ymlProperties, Properties customizeProperties, String key, Class<TYPE> klass, TYPE defaultValue){
        TYPE propertiesValue = null;
        TYPE yamlValue = null;
        TYPE customizeValue = null;

        if (environment != null){
            propertiesValue = environment.getProperty(key, klass);
        }

        if (ymlProperties != null && !"null".equalsIgnoreCase(String.valueOf(ymlProperties.get(key)))){
            yamlValue = (TYPE) this.getPropertiesWithCorrectType(ymlProperties, key, klass);
        }

        if (customizeProperties != null && !"null".equalsIgnoreCase(String.valueOf(customizeProperties.get(key)))){
            customizeValue = (TYPE) this.getPropertiesWithCorrectType(customizeProperties, key, klass);
        }

        if (propertiesValue == null && yamlValue == null && customizeValue == null){
            logger.warn("[beancounter-config] No config for the [" + key + "], beancounter set hte defaultValue [" + defaultValue + "]");
            return defaultValue;
        }

        if ((propertiesValue != null && yamlValue != null && !propertiesValue.toString().equals(yamlValue.toString()))
            || (propertiesValue != null && customizeValue != null && !propertiesValue.toString().equals(customizeValue.toString()))
            || (yamlValue != null && customizeValue != null && !yamlValue.toString().equals(customizeValue.toString()))){
            logger.warn("[beancounter-config] Conflict config for the [" + key + "], [" + propertiesValue + "] in application.properties and [" + yamlValue + "] in application.yml and[" + customizeValue + "] in beancounter.properties, beancounter set hte defaultValue [" + defaultValue + "]");
            return defaultValue;
        }

        TYPE returnValue = defaultValue;
        if (propertiesValue != null){
            returnValue = propertiesValue;
        }else if (yamlValue != null){
            returnValue = yamlValue;
        }else if (customizeValue != null){
            returnValue = customizeValue;
        }

        logger.info("[beancounter-config] Config for the [" + key + "] set the value [" + returnValue + "]");
        return returnValue;

    }

    private Object getPropertiesWithCorrectType(Properties properties, String key, Class<?> klass){
        if (klass == Integer.class){
            return Integer.valueOf(String.valueOf(properties.get(key)));
        }else if (klass == Long.class){
            return Long.valueOf(String.valueOf(properties.get(key)));
        }else if (klass == Double.class){
            return Double.valueOf(String.valueOf(properties.get(key)));
        }else if (klass == Float.class){
            return Float.valueOf(String.valueOf(properties.get(key)));
        }else if (klass == Boolean.class){
            return Boolean.valueOf(String.valueOf(properties.get(key)));
        }else if (klass == Character.class){
            return properties.get(key);
        }else if (klass == String.class){
            return String.valueOf(properties.get(key));
        }else{
            return null;
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
