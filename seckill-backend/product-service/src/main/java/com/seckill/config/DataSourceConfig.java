package com.seckill.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class DataSourceConfig {

    // 从 application.yml 绑定 spring.datasource.master.*
    @Bean
    @ConfigurationProperties("spring.datasource.master")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    // 从 application.yml 绑定 spring.datasource.slave.*
    @Bean
    @ConfigurationProperties("spring.datasource.slave")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    // ✅ 这是 MyBatis-Plus 使用的 DataSource — 它会动态路由
    @Bean
    @Primary
    public DataSource routingDataSource(DataSource masterDataSource,
                                        DataSource slaveDataSource) {
        RoutingDataSource routing = new RoutingDataSource();
        routing.setTargetDataSources(Map.of(
                DataSourceContextHolder.MASTER, masterDataSource,
                DataSourceContextHolder.SLAVE,  slaveDataSource
        ));
        routing.setDefaultTargetDataSource(masterDataSource);
        return routing;
    }
}