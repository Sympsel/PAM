package common.utils;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordEncoder {

    /**
     *
     * 测试模式开关：true=不加密（仅用于测试），false=正常加密
     */
    private static final PasswordEncoder.Config CONFIG = loadConfig();

    private static PasswordEncoder.Config loadConfig() {
        try (InputStream is = Validator.class.getClassLoader()
                .getResourceAsStream("config/validator.json")) {
            if (is == null) {
                return new PasswordEncoder.Config();
            }
            return new Gson().fromJson(
                    new InputStreamReader(is, StandardCharsets.UTF_8), PasswordEncoder.Config.class);
        } catch (Exception e) {
            return new PasswordEncoder.Config();
        }
    }

    public static String encode(String rawPassword) {
        if (CONFIG.testMode) {
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
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (CONFIG.testMode) {
            // 测试模式：直接比较明文
            return rawPassword.equals(encodedPassword);
        }
        return encode(rawPassword).equals(encodedPassword);
    }

    private static class Config {
        boolean testMode = true;
    }
}
