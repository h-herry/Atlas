package com.atlas.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * Atlas 审批工作流微服务 / Atlas approval workflow microservice
 * <p>
 * 基于 Flowable 7.0 工作流引擎，提供流程启动、任务审批、待办查询和审批历史追溯。 /
 * Based on Flowable 7.0 workflow engine, provides process startup, task approval,
 * pending task queries, and approval history tracing.
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.atlas.common", "com.atlas.workflow"})
public class WorkflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkflowApplication.class, args);
    }
}
