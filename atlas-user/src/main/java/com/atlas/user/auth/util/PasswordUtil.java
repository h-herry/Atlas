package com.atlas.user.auth.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 密码加密/校验工具类 /
 * BCrypt password encryption & verification utility
 */
public final class PasswordUtil {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtil() {
    }

    /**
     * 对明文密码进行 BCrypt 加密 /
     * Encrypt plaintext password with BCrypt
     *
     * @param rawPassword 明文密码 / Plaintext password
     * @return BCrypt 哈希 / BCrypt hash
     */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 校验明文密码是否匹配 BCrypt 哈希 /
     * Verify plaintext password against BCrypt hash
     *
     * @param rawPassword     明文密码 / Plaintext password
     * @param encodedPassword BCrypt 哈希 / BCrypt hash
     * @return true 匹配 / matches, false 不匹配 / does not match
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}
