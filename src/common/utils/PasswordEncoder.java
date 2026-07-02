package common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordEncoder {


    public static String encode(String rawPassword) {
        if (Config.TEST_MODE) {
            return rawPassword;
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
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (Config.TEST_MODE) {
            return rawPassword.equals(encodedPassword);
        }
        return encode(rawPassword).equals(encodedPassword);
    }
}
