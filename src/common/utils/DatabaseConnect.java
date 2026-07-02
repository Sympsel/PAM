package common.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnect {
    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                Config.JDBC_URL,
                Config.DB_USER,
                Config.DB_PASSWORD
        );
    }
}
