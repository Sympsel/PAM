package common.utils;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 数据库配置类
 */
@Slf4j
public class DatabaseConfig {

    private static final Config CONFIG = loadConfig();

    private static Config loadConfig() {
        try (InputStream is = DatabaseConfig.class.getClassLoader().getResourceAsStream(
                "config/database.json"
        )) {
            if (is == null) {
                return new Config();
            }
            return new Gson().fromJson(
                    new InputStreamReader(is, StandardCharsets.UTF_8), Config.class
            );
        } catch (Exception e) {
            log.warn("无法加载验证器配置文件");
            return new Config();
        }
    }

    public static final String JDBC_URL = String.format(
            "jdbc:%s://%s:%d/%s?useUnicode=true&characterEncoding=utf8&useSSL=false",
            CONFIG.dbConfig.databaseType,
            CONFIG.dbConfig.host,
            CONFIG.dbConfig.port,
            CONFIG.dbConfig.name
    );

    public static final String DB_USER = CONFIG.connectConfig.user;
    public static final String DB_PASSWORD = CONFIG.connectConfig.password;
    public static final int MAX_CONNECTIONS = CONFIG.connectConfig.maxConnect;
    public static final long CONNECTION_TIMEOUT = CONFIG.connectConfig.timeout;

    private static class Config {
        DBConfig dbConfig = new DBConfig();
        ConnectConfig connectConfig = new ConnectConfig();
    }

    private static class DBConfig {
        String databaseType = "mariadb";
        String host = "127.0.0.1";
        int port = 3306;
        String name = "pam_db";
    }

    private static class ConnectConfig {
        String user = "root";
        String password = "Zx123434678.";
        int maxConnect = 10;
        long timeout = 5000;
    }
}
