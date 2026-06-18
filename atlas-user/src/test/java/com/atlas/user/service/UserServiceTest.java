package com.atlas.user.service;

import com.atlas.common.cache.lock.RedisDistributedLock;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.common.core.util.JwtUtil;
import com.atlas.common.security.service.TokenService;
import com.atlas.user.entity.User;
import com.atlas.user.mapper.UserMapper;
import com.atlas.user.model.LoginRequest;
import com.atlas.user.model.LoginResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("用户认证服务单元测试")
class UserServiceTest {

    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private TokenService tokenService;
    @Mock private RedisDistributedLock redisLock;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private LoginService loginService;

    // ======================== 登录测试 ========================

    @Test
    @DisplayName("登录成功：密码匹配、返回 Token")
    void testLogin_Success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("123456");

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("admin");
        mockUser.setRealName("管理员");
        mockUser.setPasswordHash("$2a$10$hashed");
        mockUser.setDeptId(100L);
        mockUser.setStatus(1);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("login:fail:admin")).thenReturn(null);
        when(userMapper.selectByUsername("admin")).thenReturn(mockUser);
        when(passwordEncoder.matches("123456", "$2a$10$hashed")).thenReturn(true);
        when(userMapper.selectRoleCodes(1L)).thenReturn(List.of("ADMIN"));
        when(userMapper.selectPermissionCodes(1L)).thenReturn(List.of("user:view", "user:edit"));
        when(jwtUtil.createToken(eq(1L), eq("admin"), anyMap())).thenReturn("mock-jwt-token");

        LoginResponse response = loginService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("mock-jwt-token");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getRealName()).isEqualTo("管理员");
        assertThat(response.getRoles()).contains("ADMIN");
        assertThat(response.getPermissions()).contains("user:view", "user:edit");

        verify(valueOperations).get("login:fail:admin");
        verify(redisTemplate).delete("login:fail:admin");
        verify(tokenService).cachePermissions(eq(1L), anyList());
    }

    @Test
    @DisplayName("密码错误：抛 BizException(PASSWORD_ERROR)")
    void testLogin_WrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong");

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("admin");
        mockUser.setPasswordHash("$2a$10$hashed");
        mockUser.setStatus(1);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("login:fail:admin")).thenReturn(null);
        when(userMapper.selectByUsername("admin")).thenReturn(mockUser);
        when(passwordEncoder.matches("wrong", "$2a$10$hashed")).thenReturn(false);
        when(valueOperations.increment("login:fail:admin")).thenReturn(1L);

        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.PASSWORD_ERROR.getCode());

        verify(valueOperations).increment("login:fail:admin");
    }

    @Test
    @DisplayName("账户锁定：失败计数达上限，抛 BizException(USER_LOCKED)")
    void testLogin_UserLocked() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("123456");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("login:fail:admin")).thenReturn(5); // 已达上限

        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.USER_LOCKED.getCode());

        verify(userMapper, never()).selectByUsername(anyString());
    }

    @Test
    @DisplayName("用户不存在：抛 BizException(USER_NOT_EXIST)")
    void testLogin_UserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsername("ghost");
        request.setPassword("123456");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("login:fail:ghost")).thenReturn(null);
        when(userMapper.selectByUsername("ghost")).thenReturn(null);
        when(valueOperations.increment("login:fail:ghost")).thenReturn(1L);

        assertThatThrownBy(() -> loginService.login(request))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.USER_NOT_EXIST.getCode());

        verify(valueOperations).increment("login:fail:ghost");
    }

    // ======================== Token 刷新测试 ========================

    @Test
    @DisplayName("有效 Token 刷新：返回新 Token")
    void testRefresh_Success() {
        String oldToken = "Bearer old-jwt-token";
        String token = "old-jwt-token";

        // 模拟 AuthController.refresh 的核心逻辑
        when(jwtUtil.parseToken(token)).thenReturn(
                io.jsonwebtoken.Jwts.claims()
                        .subject("1")
                        .add("username", "admin")
                        .add("deptId", 100L)
                        .add("realName", "管理员")
                        .build());
        when(jwtUtil.getUserId(any())).thenReturn(1L);
        when(jwtUtil.getUsername(any())).thenReturn("admin");
        when(jwtUtil.createToken(eq(1L), eq("admin"), anyMap())).thenReturn("new-jwt-token");

        // 解析并生成新 Token
        io.jsonwebtoken.Claims claims = jwtUtil.parseToken(token);
        Long userId = jwtUtil.getUserId(claims);
        String username = jwtUtil.getUsername(claims);
        String newToken = jwtUtil.createToken(userId, username,
                java.util.Map.of("deptId", claims.get("deptId"), "realName", claims.get("realName")));

        assertThat(newToken).isEqualTo("new-jwt-token");
        assertThat(userId).isEqualTo(1L);
        assertThat(username).isEqualTo("admin");
    }
}
