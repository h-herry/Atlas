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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户登录认证服务 / User login authentication service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final RedisDistributedLock redisLock;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String LOGIN_FAIL_KEY = "login:fail:";
    private static final int MAX_LOGIN_FAIL = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    public LoginResponse login(LoginRequest request) {
        String failKey = LOGIN_FAIL_KEY + request.getUsername();

        // 1. 检查是否已被锁定 / Check if already locked
        Integer failCount = (Integer) redisTemplate.opsForValue().get(failKey);
        if (failCount != null && failCount >= MAX_LOGIN_FAIL) {
            log.warn("登录失败-账户已锁定: {}", request.getUsername());
            throw new BizException(ErrorCode.USER_LOCKED);
        }

        // 2. 查用户 / Look up user
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            log.warn("登录失败-用户不存在: {}", request.getUsername());
            incrementFailCount(failKey);
            throw new BizException(ErrorCode.USER_NOT_EXIST);
        }
        if (user.getStatus() == 0) {
            log.warn("登录失败-用户已禁用: {}", request.getUsername());
            throw new BizException(ErrorCode.USER_DISABLED);
        }

        // 3. 验密码 / Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("登录失败-密码错误: {}", request.getUsername());
            incrementFailCount(failKey);
            throw new BizException(ErrorCode.PASSWORD_ERROR);
        }

        // 4. 登录成功，清除失败计数 / Login success, clear failure count
        redisTemplate.delete(failKey);

        // 5. 查角色权限 / Look up roles & permissions
        List<String> roles = userMapper.selectRoleCodes(user.getId());
        List<String> permissions = userMapper.selectPermissionCodes(user.getId());

        // 4. 生成 JWT / Generate JWT
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("deptId", user.getDeptId());
        extraClaims.put("realName", user.getRealName());
        String accessToken = jwtUtil.createToken(user.getId(), user.getUsername(), extraClaims);

        // 5. 缓存权限 / Cache permissions
        tokenService.cachePermissions(user.getId(), permissions);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .deptId(user.getDeptId())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    /**
     * 登录失败计数 +1，首次失败设置 15 分钟 TTL /
     * Increment login failure count; set 15-min TTL on first failure
     */
    private void incrementFailCount(String failKey) {
        Long count = redisTemplate.opsForValue().increment(failKey);
        if (count != null && count == 1) {
            redisTemplate.expire(failKey, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }
    }
}
