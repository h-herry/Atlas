package com.atlas.common.security.service;

/**
 * Token 续期 ThreadLocal 持有器 /
 * Token renewal ThreadLocal holder
 * <p>
 * 在 TokenService.renewIfNeeded() 中设置新 Token，
 * 由 JwtAuthenticationFilter 在 doFilter 结束后读取并写入响应头。 /
 * New token is set in TokenService.renewIfNeeded(),
 * read by JwtAuthenticationFilter after doFilter and written to response header.
 */
public final class RenewTokenHolder {

    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    private RenewTokenHolder() {
    }

    public static void set(String newToken) {
        HOLDER.set(newToken);
    }

    public static String get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
