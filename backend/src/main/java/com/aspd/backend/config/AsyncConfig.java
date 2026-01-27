package com.aspd.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executor;

/**
 * Configuration for async task execution.
 * Enables async method execution and scheduled tasks.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
    
    /**
     * Thread pool for async optimization tasks.
     * Max 3 concurrent optimizations (one for each phase type).
     */
    @Bean(name = "optimizationExecutor")
    public Executor optimizationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("optimization-");
        executor.initialize();
        return executor;
    }
}
