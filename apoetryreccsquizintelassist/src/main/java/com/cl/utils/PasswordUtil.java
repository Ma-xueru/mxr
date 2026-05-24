package com.cl.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * 密码工具 — BCrypt 加密/验证
 */
public class PasswordUtil {

    /** BCrypt 工作因子，越大越安全但越慢（推荐10-12） */
    private static final int WORKLOAD = 10;

    /** 加密密码 */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) return null;
        String salt = BCrypt.gensalt(WORKLOAD);
        return BCrypt.hashpw(plainPassword, salt);
    }

    /** 验证密码（兼容旧明文密码） */
    public static boolean verify(String plainPassword, String storedPassword) {
        if (plainPassword == null || storedPassword == null) return false;
        // 如果已加密，用 BCrypt 验证
        if (isHashed(storedPassword)) {
            try {
                return BCrypt.checkpw(plainPassword, storedPassword);
            } catch (Exception e) {
                return false;
            }
        }
        // 旧明文密码，用 equals 兜底（迁移后此分支不再进入）
        return storedPassword.equals(plainPassword);
    }

    /** 判断是否已经是 BCrypt 哈希（以 $2a$ 开头） */
    public static boolean isHashed(String password) {
        return password != null && password.startsWith("$2a$");
    }
}
