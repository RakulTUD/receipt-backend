package com.example.ead.be;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

@Configuration
@PropertySource("classpath:test.properties")
public class TestConfig {

    @Bean
    public StandardEnvironment environment() throws IOException {
        StandardEnvironment environment = new StandardEnvironment();
        Properties properties = PropertiesLoaderUtils.loadProperties(new ClassPathResource("test.properties"));
        environment.getPropertySources().addFirst(new org.springframework.core.env.PropertiesPropertySource("testProperties", properties));
        return environment;
    }
} 