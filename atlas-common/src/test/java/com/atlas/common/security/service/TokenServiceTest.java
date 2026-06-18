package com.atlas.common.security.service;

import com.atlas.common.core.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("TokenService Token 服务测试")
@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("应从 Redis 获取用户权限列表")
    void should_get_permissions_from_redis() {
        List<String> expected = Arrays.asList("purchase:order:create", "purchase:order:view");
        when(valueOperations.get("user:perm:1001")).thenReturn(expected);

        List<String> permissions = tokenService.getPermissions(1001L);

        assertThat(permissions).containsExactlyElementsOf(expected);
    }

    @Test
    @DisplayName("用户无权限时应返回 null")
    void should_return_null_when_no_permissions() {
        when(valueOperations.get("user:perm:1001")).thenReturn(null);

        List<String> permissions = tokenService.getPermissions(1001L);

        assertThat(permissions).isNull();
    }

    @Test
    @DisplayName("应缓存用户权限到 Redis 2 小时")
    void should_cache_permissions_for_2_hours() {
        List<String> permissions = Collections.singletonList("admin");

        tokenService.cachePermissions(1001L, permissions);

        verify(valueOperations).set(eq("user:perm:1001"), eq(permissions), eq(2L), eq(TimeUnit.HOURS));
    }

    @Test
    @DisplayName("应检查 Token 是否在黑名单")
    void should_check_blacklist() {
        when(redisTemplate.hasKey("token:blacklist:expired-token")).thenReturn(true);

        assertThat(tokenService.isBlacklisted("expired-token")).isTrue();
    }

    @Test
    @DisplayName("不在黑名单的 Token 应返回 false")
    void should_return_false_when_not_blacklisted() {
        when(redisTemplate.hasKey("token:blacklist:valid-token")).thenReturn(false);

        assertThat(tokenService.isBlacklisted("valid-token")).isFalse();
    }

    @Test
    @DisplayName("应正确将 Token 加入黑名单")
    void should_add_token_to_blacklist() {
        tokenService.blacklist("token123", 3600L);

        verify(valueOperations).set(eq("token:blacklist:token123"), eq("1"), eq(3600L), eq(TimeUnit.SECONDS));
    }
}
