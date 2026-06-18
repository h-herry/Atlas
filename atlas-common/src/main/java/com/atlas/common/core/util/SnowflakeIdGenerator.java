package com.atlas.common.core.util;

import cn.hutool.core.util.IdUtil;

/**
 * 雪花算法 ID 生成器 — 基于 Hutool 封装 /
 * Snowflake ID generator — based on Hutool wrapper
 * <p>
 * 面试场景说明 / Interview explanation：
 * 1. 核心原理：1位符号 + 41位时间戳(ms) + 10位机器ID(5位机房+5位机器) + 12位序列号 / Core: 1b sign + 41b timestamp(ms) + 10b machine ID + 12b sequence
 * 2. 时间回拨问题：Hutool 内置时钟回拨检测，超过容忍阈值抛出异常；生产环境还可借助 NTP + Nacos 统一分配 workerId / Clock rollback: Hutool has built-in detection; production can use NTP + Nacos for workerId allocation
 * 3. 不同部署方式的 workerId 策略：单体可写死；集群用 Redis 递增；K8s 用 StatefulSet 序号 / workerId strategies: hardcode for monolith; Redis increment for cluster; StatefulSet ordinal for K8s
 * 4. 与 UUID 对比：有序、可索引、数据库 B+Tree 友好；UUID 无序导致页分裂 / vs UUID: ordered, indexable, B+Tree friendly; UUID's disorder causes page splits
 */
public class SnowflakeIdGenerator {

    /**
     * 生成一个 Snowflake 长整型 ID，线程安全 / Generate a Snowflake long ID, thread-safe
     */
    public static long nextId() {
        return IdUtil.getSnowflakeNextId();
    }

    /**
     * 生成 Snowflake ID 的字符串形式 / Generate Snowflake ID as string
     */
    public static String nextIdStr() {
        return IdUtil.getSnowflakeNextIdStr();
    }

    /**
     * 自定义 workerId + dataCenterId / Custom workerId + dataCenterId
     */
    public static long nextId(long workerId, long dataCenterId) {
        return IdUtil.getSnowflake(workerId, dataCenterId).nextId();
    }
}
