package com.atlas.common.cache.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Cache 配置 — 统一缓存策略 / Spring Cache config — unified caching strategy
 *
 * <p>缓存过期策略 / Cache TTL policy:</p>
 * <pre>
 * | 缓存域 / Cache Domain   | TTL     | 说明 / Description          |
 * |------------------------|---------|-----------------------------|
 * | material               | 24h     | 物料主数据 / Material master  |
 * | supplier:info          | 12h     | 供应商档案 / Supplier profile |
 * | dict                   | 48h     | 数据字典 / Data dictionary   |
 * | cert                   | 24h     | 供应商资质 / Supplier cert    |
 * | default                | 30min   | 默认 / Default               |
 * </pre>
 *
 * <p>序列化: Jackson2JsonRedisSerializer，避免 GenericJackson2JsonRedisSerializer 的 @class 污染 /
 * Serialization: Jackson2JsonRedisSerializer, avoids @class pollution from GenericJackson2JsonRedisSerializer</p>
 *
 * @author Atlas Team
 * @since 1.2.21
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /** 物料缓存 TTL: 24 小时 / Material cache TTL: 24 hours */
    private static final Duration MATERIAL_TTL = Duration.ofHours(24);

    /** 供应商档案缓存 TTL: 12 小时 / Supplier info cache TTL: 12 hours */
    private static final Duration SUPPLIER_INFO_TTL = Duration.ofHours(12);

    /** 数据字典缓存 TTL: 48 小时 / Dictionary cache TTL: 48 hours */
    private static final Duration DICT_TTL = Duration.ofHours(48);

    /** 供应商资质缓存 TTL: 24 小时 / Certification cache TTL: 24 hours */
    private static final Duration CERT_TTL = Duration.ofHours(24);

    /** 默认缓存 TTL: 30 分钟 / Default cache TTL: 30 minutes */
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        // JSON 序列化器 / JSON serializer
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        jsonSerializer.setObjectMapper(objectMapper);

        // 通用配置: String Key + JSON Value / Common config: String Key + JSON Value
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(DEFAULT_TTL)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        // 多域 TTL 映射 / Multi-domain TTL mapping
        Map<String, RedisCacheConfiguration> ttlConfigs = new HashMap<>();
        ttlConfigs.put("material", defaultConfig.entryTtl(MATERIAL_TTL));
        ttlConfigs.put("supplier:info", defaultConfig.entryTtl(SUPPLIER_INFO_TTL));
        ttlConfigs.put("dict", defaultConfig.entryTtl(DICT_TTL));
        ttlConfigs.put("cert", defaultConfig.entryTtl(CERT_TTL));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(ttlConfigs)
                .build();
    }
}
