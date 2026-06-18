package com.atlas.common.core.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtUtil JWT 工具类测试")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String secret = Base64.getEncoder().encodeToString(
            "atlas-test-secret-key-for-unit-testing-2024".getBytes());

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "base64Secret", secret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 7200L);
    }

    @Test
    @DisplayName("应成功创建 Token")
    void should_create_token_successfully() {
        String token = jwtUtil.createToken(1001L, "admin", null);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("应成功解析 Token 获取 userId")
    void should_parse_user_id_from_token() {
        String token = jwtUtil.createToken(1001L, "admin", null);
        Claims claims = jwtUtil.parseToken(token);

        assertThat(claims).isNotNull();
        assertThat(jwtUtil.getUserId(claims)).isEqualTo(1001L);
    }

    @Test
    @DisplayName("应成功解析 Token 获取 username")
    void should_parse_username_from_token() {
        String token = jwtUtil.createToken(1001L, "admin", null);
        Claims claims = jwtUtil.parseToken(token);

        assertThat(jwtUtil.getUsername(claims)).isEqualTo("admin");
    }

    @Test
    @DisplayName("应携带额外 claims")
    void should_carry_extra_claims() {
        Map<String, Object> extra = new HashMap<>();
        extra.put("deptId", 5L);
        extra.put("role", "MANAGER");
        String token = jwtUtil.createToken(1001L, "admin", extra);
        Claims claims = jwtUtil.parseToken(token);

        assertThat(claims.get("deptId", Long.class)).isEqualTo(5L);
        assertThat(claims.get("role", String.class)).isEqualTo("MANAGER");
    }

    @Test
    @DisplayName("null extraClaims 不应抛异常")
    void should_not_throw_when_extra_claims_null() {
        String token = jwtUtil.createToken(1001L, "admin", null);

        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("空 extraClaims 不应抛异常")
    void should_not_throw_when_extra_claims_empty() {
        String token = jwtUtil.createToken(1001L, "admin", new HashMap<>());

        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("无效 Token 解析应返回 null")
    void should_return_null_when_invalid_token() {
        Claims claims = jwtUtil.parseToken("invalid.token.here");

        assertThat(claims).isNull();
    }

    @Test
    @DisplayName("null Token 解析应返回 null")
    void should_return_null_when_null_token() {
        Claims claims = jwtUtil.parseToken(null);

        assertThat(claims).isNull();
    }

    @Test
    @DisplayName("过期 Token 解析应返回 null")
    void should_return_null_when_expired_token() throws InterruptedException {
        ReflectionTestUtils.setField(jwtUtil, "expiration", 0L);
        String token = jwtUtil.createToken(1001L, "admin", null);
        Thread.sleep(10);

        Claims claims = jwtUtil.parseToken(token);

        assertThat(claims).isNull();
    }

    @Test
    @DisplayName("不同用户的 Token 应不同")
    void should_differ_by_user() {
        String token1 = jwtUtil.createToken(1001L, "admin", null);
        String token2 = jwtUtil.createToken(1002L, "user", null);

        assertThat(token1).isNotEqualTo(token2);
    }
}
