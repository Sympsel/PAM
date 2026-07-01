package common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordEncoder {

    /**
     *
     * 测试模式开关：true=不加密（仅用于测试），false=正常加密
     */
    private static final boolean TEST_MODE = true;

    public static String encode(String rawPassword) {
        if (TEST_MODE) {
            return rawPassword; // 测试模式：直接返回明文
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(rawPassword.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("加密算法不可用", e);
        }
    }

    /**
     * 验证密码
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (TEST_MODE) {
            return rawPassword.equals(encodedPassword); // 测试模式：直接比较明文
        }
        return encode(rawPassword).equals(encodedPassword);
    }
}
