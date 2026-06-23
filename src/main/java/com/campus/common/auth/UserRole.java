package com.campus.common.auth;

import java.util.Locale;
import java.util.Optional;

/**
 * 系统支持的登录角色。
 */
public enum UserRole {
    STUDENT,
    MERCHANT,
    ADMIN;

    /**
     * 将 JWT 中的角色文本转换为枚举，忽略大小写和首尾空格。
     */
    public static Optional<UserRole> fromToken(String role) {
        if (role == null || role.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UserRole.valueOf(role.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
