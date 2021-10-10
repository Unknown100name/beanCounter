package store.database.config;

import config.BeanCounterConfig;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能: 提供 JPA 连接
 * @author unknown100name
 * @date 2021.10.06
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager",
    basePackages = {"store.database.manager"}
)
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class DefaultDataSourceConfig {

    @Resource(name = "defaultDataSource")
    private DataSource dataSource;

    @Primary
    @Bean(name = {"entityManager"})
    public EntityManager entityManager(FactoryBean<EntityManagerFactory> entityManagerFactoryFactoryBean) throws Exception{
        return entityManagerFactoryFactoryBean.getObject().createEntityManager();
    }

    @Primary
    @Bean(name = {"entityManagerFactory"})
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean() throws Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL5InnoDBDialect");
        properties.put("hibernate.show_sql", BeanCounterConfig.SHOW_SQL.toString());
        properties.put("hibernate.format_sql", BeanCounterConfig.FORMAT_SQL.toString());
        properties.put("hibernate.ddl_auto", BeanCounterConfig.DDL_AUTO.toLowerCase());
        properties.put("hibernate.hbm2ddl.auto", BeanCounterConfig.DDL_AUTO.toLowerCase());
        LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        localContainerEntityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        localContainerEntityManagerFactoryBean.setDataSource(this.dataSource);
        localContainerEntityManagerFactoryBean.setPackagesToScan("store.database.entity");
        localContainerEntityManagerFactoryBean.getJpaPropertyMap().putAll(properties);
        localContainerEntityManagerFactoryBean.setPersistenceUnitName("defaultPersistenceUnit");
        return localContainerEntityManagerFactoryBean;
    }

    @Primary
    @Bean(name = {"transactionManager"})
    public PlatformTransactionManager transactionManager(FactoryBean<EntityManagerFactory> entityManagerFactoryFactoryBean) throws Exception {
         return new JpaTransactionManager(entityManagerFactoryFactoryBean.getObject());
    }
}
