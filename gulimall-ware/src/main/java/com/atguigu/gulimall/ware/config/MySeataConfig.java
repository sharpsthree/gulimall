package com.atguigu.gulimall.ware.config;

import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

/**
 * @Description TODO
 * @Author 鲁班不会飞
 * @Date 2020/5/5 18:08
 * @Version 1.0
 **/
@Configuration
public class MySeataConfig {

    @Autowired
    DataSourceProperties dataSourceProperties;

    /**
     * 需要将 DataSourceProxy 设置为主数据源，否则事务无法回滚
     * @param dataSourceProperties
     * @return
     */
    @Bean
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {

        HikariDataSource dataSource = dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
        if (StringUtils.hasText(dataSourceProperties.getName())) {
            dataSource.setPoolName(dataSourceProperties.getName());
        }

        return new DataSourceProxy(dataSource);
    }

}
