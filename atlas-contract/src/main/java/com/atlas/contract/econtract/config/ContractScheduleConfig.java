package com.atlas.contract.econtract.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 合同模块定时任务配置 / Contract module scheduled task configuration
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Configuration
@EnableScheduling
@EnableAsync
public class ContractScheduleConfig {

    /**
     * 合同定时任务执行器 / Contract task executor
     */
    @Bean("contractTaskExecutor")
    public Executor contractTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数 1 / Core pool size: 1
        executor.setCorePoolSize(1);
        // 最大线程数 3 / Max pool size: 3
        executor.setMaxPoolSize(3);
        // 队列容量 50 / Queue capacity: 50
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("contract-scheduled-");
        // 拒绝策略：由调用线程执行 / Rejection policy: CallerRunsPolicy
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
