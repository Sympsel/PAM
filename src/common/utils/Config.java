package common.utils;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
public class Config {

    private static final AppConfig CONFIG = loadConfig();

    private static AppConfig loadConfig() {
        try (InputStream is = Config.class.getClassLoader().getResourceAsStream("config/config.json")) {
            if (is == null) {
                log.warn("未找到配置文件 config.json，使用默认配置");
                return new AppConfig();
            }
            return new Gson().fromJson(
                    new InputStreamReader(is, StandardCharsets.UTF_8), AppConfig.class);
        } catch (Exception e) {
            log.warn("无法加载配置文件: {}", e.getMessage());
            return new AppConfig();
        }
    }

    // ==================== 全局配置 ====================
    public static final String FONT = CONFIG.font;
    public static final boolean TEST_MODE = CONFIG.testMode;

    // ==================== 数据库配置 ====================
    public static final String JDBC_URL = String.format(
            "jdbc:%s://%s:%d/%s?useUnicode=true&characterEncoding=utf8&useSSL=false",
            CONFIG.database.DB.databaseType,
            CONFIG.database.DB.host,
            CONFIG.database.DB.port,
            CONFIG.database.DB.name
    );
    public static final String DB_USER = CONFIG.database.Connect.user;
    public static final String DB_PASSWORD = CONFIG.database.Connect.password;
    public static final int MAX_CONNECTIONS = CONFIG.database.Connect.maxConnect;
    public static final long CONNECTION_TIMEOUT = CONFIG.database.Connect.timeout;

    // ==================== 验证器配置 ====================
    public static final boolean VALIDATOR_TEST_MODE = CONFIG.validator.testMode;

    public static class UsernameRules {
        public static final int MIN_LENGTH = CONFIG.validator.username.minLength;
        public static final int MAX_LENGTH = CONFIG.validator.username.maxLength;
        public static final boolean ALLOW_UNDERSCORE = CONFIG.validator.username.allowUnderscore;
    }

    public static class PasswordRules {
        public static final int MIN_LENGTH = CONFIG.validator.password.minLength;
        public static final int MAX_LENGTH = CONFIG.validator.password.maxLength;
        public static final boolean REQUIRE_LETTER = CONFIG.validator.password.requireLetter;
        public static final boolean REQUIRE_DIGIT = CONFIG.validator.password.requireDigit;
        public static final boolean REQUIRE_SPECIAL = CONFIG.validator.password.requireSpecial;
    }

    // ==================== 内部配置类 ====================
    private static class AppConfig {
        String font = "JetBrainsMono";
        boolean testMode = false;
        DatabaseSection database = new DatabaseSection();
        ValidatorSection validator = new ValidatorSection();
    }

    private static class DatabaseSection {
        DBConfig DB = new DBConfig();
        ConnectConfig Connect = new ConnectConfig();
    }

    private static class DBConfig {
        String databaseType = "mariadb";
        String host = "localhost";
        int port = 3306;
        String name = "pam_db";
    }

    private static class ConnectConfig {
        String user = "root";
        String password = "Zx123434678.";
        int maxConnect = 3;
        long timeout = 5000;
    }

    private static class ValidatorSection {
        boolean testMode = false;
        PasswordConfig password = new PasswordConfig();
        UsernameConfig username = new UsernameConfig();
    }

    private static class PasswordConfig {
        int minLength = 6;
        int maxLength = 20;
        boolean requireLetter = true;
        boolean requireDigit = true;
        boolean requireSpecial = false;
    }

    private static class UsernameConfig {
        int minLength = 4;
        int maxLength = 20;
        boolean allowUnderscore = true;
    }
}
