package com.atlas.common.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务线程池配置 / Async task thread pool configuration
 *
 * <p>线程池参数 / Thread pool parameters:</p>
 * <ul>
 *   <li>核心线程: 8 / Core threads: 8</li>
 *   <li>最大线程: 16 / Max threads: 16</li>
 *   <li>队列容量: 200 / Queue capacity: 200</li>
 *   <li>拒绝策略: CallerRunsPolicy（避免丢任务）/ Rejection policy: CallerRunsPolicy (avoids task loss)</li>
 * </ul>
 *
 * <p>适用场景: 物料批量导入、订单批量导出、消息批量推送等重操作 /
 * Applicable scenarios: batch material import, order export, batch message push, etc.</p>
 *
 * @author Atlas Team
 * @since 1.2.21
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("asyncTaskExecutor")
    public Executor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("async-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        log.info("异步任务线程池已初始化: core=8, max=16, queue=200 / Async task thread pool initialized");
        return executor;
    }
}
