package com.atlas.supplier.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 定时任务线程池配置 / Scheduled task thread pool configuration
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Configuration
@EnableAsync
public class ScheduleConfig {

    /**
     * 定时任务执行器 Bean / Task executor bean
     */
    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数 2 / Core pool size: 2
        executor.setCorePoolSize(2);
        // 最大线程数 5 / Max pool size: 5
        executor.setMaxPoolSize(5);
        // 队列容量 100 / Queue capacity: 100
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("scheduled-");
        // 拒绝策略：由调用线程执行 / Rejection policy: CallerRunsPolicy
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
