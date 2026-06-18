package com.atlas.user.auth.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.user.auth.entity.LoginLog;
import com.atlas.user.auth.entity.UserCredential;
import com.atlas.user.auth.mapper.LoginLogMapper;
import com.atlas.user.auth.mapper.UserCredentialMapper;
import com.atlas.user.auth.config.JwtProperties;
import com.atlas.user.auth.util.JwtTokenUtil;
import com.atlas.user.auth.util.PasswordUtil;
import com.atlas.user.entity.User;
import com.atlas.user.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证服务 — 登录/登出/Token 刷新/校验 /
 * Authentication service — login/logout/token refresh/validate
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final UserCredentialMapper userCredentialMapper;
    private final LoginLogMapper loginLogMapper;
    private final JwtTokenUtil jwtTokenUtil;
    private final TokenBlacklistService tokenBlacklistService;
    private final LoginAttemptService loginAttemptService;
    private final JwtProperties jwtProperties;

    // ==================== 登录 / Login ====================

    /**
     * 双通道登录认证 /
     * Dual-channel login authentication
     *
     * @param username 用户名
     * @param password 明文密码
     * @param channel  通道: enterprise / supplier
     * @param ip       客户端 IP
     * @return JWT claims 及角色权限信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> login(String username, String password, String channel, String ip) {
        // 1. 查用户 / Look up user
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            log.warn("登录失败-用户不存在: username={}", username);
            writeLoginLog(null, ip, false, "用户不存在");
            throw new BizException(ErrorCode.USER_NOT_EXIST);
        }

        // 2. 检查用户状态 / Check user status
        if (user.getStatus() == 0) {
            log.warn("登录失败-用户已禁用: userId={}", user.getId());
            writeLoginLog(user.getId(), ip, false, "用户已禁用");
            throw new BizException(ErrorCode.USER_DISABLED);
        }

        // 3. 检查登录失败锁定 / Check login attempt lock
        if (loginAttemptService.isLocked(user.getId())) {
            long remainSec = loginAttemptService.getRemainingLockSeconds(user.getId());
            log.warn("登录失败-账户已锁定: userId={}, 剩余={}s", user.getId(), remainSec);
            writeLoginLog(user.getId(), ip, false, "账户已锁定");
            throw new BizException(ErrorCode.USER_LOCKED);
        }

        // 4. 查凭据并验密码 / Look up credential & verify password
        UserCredential credential = userCredentialMapper.selectByUserIdAndChannel(user.getId(), channel);
        if (credential == null || !PasswordUtil.matches(password, credential.getPasswordHash())) {
            log.warn("登录失败-密码错误: userId={}, channel={}", user.getId(), channel);
            loginAttemptService.recordFailure(user.getId());
            writeLoginLog(user.getId(), ip, false, "密码错误");
            throw new BizException(ErrorCode.PASSWORD_ERROR);
        }

        // 5. 检查凭据状态 / Check credential status
        if (credential.getStatus() == 0) {
            log.warn("登录失败-凭据已禁用: userId={}", user.getId());
            writeLoginLog(user.getId(), ip, false, "凭据已禁用");
            throw new BizException(ErrorCode.USER_DISABLED);
        }
        if (credential.getLockedUntil() != null && credential.getLockedUntil().isAfter(LocalDateTime.now())) {
            log.warn("登录失败-凭据已锁定至: userId={}, lockedUntil={}", user.getId(), credential.getLockedUntil());
            writeLoginLog(user.getId(), ip, false, "凭据已锁定至 " + credential.getLockedUntil());
            throw new BizException(ErrorCode.USER_LOCKED);
        }

        // 6. 登录成功 / Login success
        loginAttemptService.clearAttempts(user.getId());

        // 7. 查角色权限 / Look up roles & permissions
        List<String> roles = userMapper.selectRoleCodes(user.getId());
        List<String> permissions = userMapper.selectPermissionCodes(user.getId());

        // 8. 构建 extra claims / Build extra claims
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("deptId", user.getDeptId());
        extraClaims.put("realName", user.getRealName());
        extraClaims.put("roles", roles);
        extraClaims.put("permissions", permissions);

        // 9. 生成 JWT / Generate JWT
        String accessToken;
        if ("supplier".equals(channel)) {
            accessToken = jwtTokenUtil.createSupplierToken(user.getId(), user.getUsername(), extraClaims);
        } else {
            accessToken = jwtTokenUtil.createEnterpriseToken(user.getId(), user.getUsername(), extraClaims);
        }

        // 10. 写登录日志 / Write login log
        writeLoginLog(user.getId(), ip, true, null);

        // 11. 组装响应 / Assemble response
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("channel", channel);
        result.put("deptId", user.getDeptId());
        result.put("roles", roles);
        result.put("permissions", permissions);
        return result;
    }

    // ==================== 登出 / Logout ====================

    /**
     * 登出：将 Token 加入黑名单 /
     * Logout: add token to blacklist
     */
    public void logout(String token) {
        Claims claims = jwtTokenUtil.parseToken(token);
        if (claims != null) {
            String tokenHash = jwtTokenUtil.hashToken(token);
            long remaining = jwtTokenUtil.getRemainingSeconds(claims);
            tokenBlacklistService.blacklist(tokenHash, remaining);
            log.info("用户登出: userId={}", jwtTokenUtil.getUserId(claims));
        }
    }

    // ==================== Token 刷新 / Token Refresh ====================

    /**
     * 刷新 Token：校验旧 Token 有效且未在黑名单，签发新 Token /
     * Refresh token: validate old token is valid & not blacklisted, issue new token
     */
    public String refresh(String oldToken) {
        // 1. 检查黑名单 / Check blacklist
        String tokenHash = jwtTokenUtil.hashToken(oldToken);
        if (tokenBlacklistService.isBlacklisted(tokenHash)) {
            log.warn("Token 刷新失败-已被拉黑");
            throw new BizException(ErrorCode.TOKEN_INVALID);
        }

        // 2. 解析旧 Token / Parse old token
        Claims claims = jwtTokenUtil.parseToken(oldToken);
        if (claims == null) {
            throw new BizException(ErrorCode.TOKEN_EXPIRED);
        }

        // 3. 签发新 Token / Issue new token
        Long userId = jwtTokenUtil.getUserId(claims);
        String username = jwtTokenUtil.getUsername(claims);
        String channel = jwtTokenUtil.getChannel(claims);

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("deptId", claims.get("deptId"));
        extraClaims.put("realName", claims.get("realName"));
        extraClaims.put("roles", claims.get("roles"));
        extraClaims.put("permissions", claims.get("permissions"));

        String newToken = jwtTokenUtil.createToken(userId, username, channel,
                jwtProperties.getExpiration(), extraClaims);

        // 4. 旧 Token 加入黑名单 / Blacklist old token
        long oldRemaining = jwtTokenUtil.getRemainingSeconds(claims);
        tokenBlacklistService.blacklist(tokenHash, oldRemaining);

        return newToken;
    }

    // ==================== Token 校验 / Token Check ====================

    /**
     * 校验 Token 有效性（未过期 + 未在黑名单） /
     * Validate token (not expired + not blacklisted)
     *
     * @return Claims 或 null
     */
    public Claims checkToken(String token) {
        // 1. 检查黑名单 / Check blacklist
        String tokenHash = jwtTokenUtil.hashToken(token);
        if (tokenBlacklistService.isBlacklisted(tokenHash)) {
            return null;
        }
        // 2. 解析校验 / Parse & validate
        return jwtTokenUtil.parseToken(token);
    }

    // ==================== 内部方法 / Internal Methods ====================

    private void writeLoginLog(Long userId, String ip, boolean success, String failReason) {
        LoginLog logEntry = new LoginLog();
        logEntry.setUserId(userId);
        logEntry.setLoginTime(LocalDateTime.now());
        logEntry.setIp(ip);
        logEntry.setResult(success ? 1 : 0);
        logEntry.setFailReason(failReason);
        loginLogMapper.insert(logEntry);
    }
}
