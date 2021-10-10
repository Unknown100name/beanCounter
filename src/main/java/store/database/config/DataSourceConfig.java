package store.database.config;

import config.BeanCounterConfig;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * 功能: 提供 BasicDataSource
 * @author unknown100name
 * @date 2021.10.06
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DataSourceConfig {

    @Bean(name = {"defaultDataSource"})
    @Qualifier("defaultDataSource")
    public DataSource getDefaultDataSource(){
        if (!BeanCounterConfig.supportDatabase()){
            return null;
        }

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl(BeanCounterConfig.DATA_SOURCE[0]);
        dataSource.setUsername(BeanCounterConfig.DATA_SOURCE[1]);
        dataSource.setPassword(BeanCounterConfig.DATA_SOURCE[2]);
        dataSource.setDriverClassName(BeanCounterConfig.DATA_SOURCE[3]);
        return dataSource;
    }
}
