package common.utils;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Slf4j
public class Validator {

    private static final Config CONFIG = loadConfig();
    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^1[3-9]\\d{9}$");

    private static Config loadConfig() {
        try (InputStream is = Validator.class.getClassLoader()
                .getResourceAsStream("config/validator.json")) {
            if (is == null) {
                return new Config();
            }
            return new Gson().fromJson(
                    new InputStreamReader(is, StandardCharsets.UTF_8), Config.class);
        } catch (Exception e) {
            log.warn("无法加载验证器配置文件");
            return new Config();
        }
    }

    public static boolean isValidPhone(String phone) {
        if (CONFIG.testMode) {
            return phone != null;
        }
        return phone != null  && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidUsername(String username) {
        if (username == null) return false;
        int len = username.length();
        if (len < CONFIG.username.minLength || len > CONFIG.username.maxLength) {
            return false;
        }
        String pattern = CONFIG.username.allowUnderscore
                ? "^[a-zA-Z0-9_]+$" : "^[a-zA-Z0-9]+$";
        return Pattern.matches(pattern, username);
    }

    public static boolean isValidPassword(String password) {
        if (CONFIG.testMode) {
            return true;
        }
        if (password == null) return false;

        int len = password.length();
        if (len < CONFIG.password.minLength || len > CONFIG.password.maxLength) {
            return false;
        }
        if (CONFIG.password.requireLetter && !password.matches(".*[a-zA-Z].*")) {
            return false;
        }
        if (CONFIG.password.requireDigit && !password.matches(".*\\d.*")) {
            return false;
        }
        return !CONFIG.password.requireSpecial || password.matches(".*[^a-zA-Z0-9].*");
    }

    private static class Config {
        boolean testMode = true;
        PasswordConfig password = new PasswordConfig();
        UsernameConfig username = new UsernameConfig();
    }

    private static class PasswordConfig {
        // 默认配置
        int minLength = 6;
        int maxLength = 20;
        boolean requireLetter = true;
        boolean requireDigit = true;
        boolean requireSpecial = false;
    }

    private static class UsernameConfig {
        // 默认配置
        int minLength = 4;
        int maxLength = 20;
        boolean allowUnderscore = true;
    }
}
