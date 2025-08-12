package com.yunhao.superai.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.yunhao.superai.repository")
@EntityScan(basePackages = "com.yunhao.superai.entity")
@EnableTransactionManagement
public class JpaConfig {
    // JPA配置类，Spring Boot会自动配置DataSource和EntityManager
}
