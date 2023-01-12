package com.fit2cloud.devops.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fit2cloud.commons.utils.CommonThreadPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 */
@PropertySource(value = {
        "classpath:properties/global.properties",
        "classpath:properties/quartz.properties",
        "classpath:fit2cloud.properties",
}, encoding = "UTF-8", ignoreResourceNotFound = true)
@Configuration
public class CommonConfig {

    @Resource
    private Environment env;

    @Bean
    public AsyncTaskExecutor syncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(200);
        executor.setMaxPoolSize(500);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("async-service-sync");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    public AsyncTaskExecutor adhocExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("async-service-adhoc");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
     }

    @Bean(destroyMethod = "shutdown")
    public CommonThreadPool connectCheckPool() {
        CommonThreadPool commonThreadPool = new CommonThreadPool();
        commonThreadPool.setCorePoolSize(20);
        commonThreadPool.setKeepAliveSeconds(3600);
        return commonThreadPool;
    }

    @Bean(destroyMethod = "shutdown")
    public CommonThreadPool appDeployPool() {
        CommonThreadPool commonThreadPool = new CommonThreadPool();
        commonThreadPool.setCorePoolSize(20);
        commonThreadPool.setKeepAliveSeconds(3600);
        return commonThreadPool;
    }

    @Bean(destroyMethod = "shutdown")
    public CommonThreadPool devopsJobPool() {
        CommonThreadPool commonThreadPool = new CommonThreadPool();
        commonThreadPool.setCorePoolSize(150);
        commonThreadPool.setMaxQueueSize(2048);
        commonThreadPool.setKeepAliveSeconds(3600);
        return commonThreadPool;
    }

    @Bean(destroyMethod = "shutdown")
    public CommonThreadPool appTestPool() {
        CommonThreadPool commonThreadPool = new CommonThreadPool();
        commonThreadPool.setCorePoolSize(20);
        commonThreadPool.setMaxQueueSize(2048);
        commonThreadPool.setKeepAliveSeconds(3600);
        return commonThreadPool;
    }

    public String getJenkinsSecret(){
        return env.getProperty("jenkins.secret");
    }

}
