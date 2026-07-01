package config;

/**
 * 数据库配置类
 */
public class DatabaseConfig {

    // 数据库连接配置
    public static final String DB_HOST = "localhost";
    public static final int DB_PORT = 3306;
    public static final String DB_NAME = "pam_db";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "Zx123434678.";

    // JDBC URL
    public static final String JDBC_URL =
            String.format("jdbc:mariadb://%s:%d/%s?useUnicode=true&characterEncoding=utf8&useSSL=false",
                    DB_HOST, DB_PORT, DB_NAME);

    // 连接池配置
    public static final int MAX_CONNECTIONS = 10;
    public static final long CONNECTION_TIMEOUT = 5000; // 5秒
}
