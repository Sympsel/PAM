package server.dao;

import common.entity.User;
import common.enums.Permission;
import config.DatabaseConfig;

import java.sql.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户数据访问对象
 * 负责用户的增删改查操作
 */
public class UserDAO implements BaseDAO<User, String> {

    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    /**
     * 获取数据库连接
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.JDBC_URL,
                DatabaseConfig.DB_USER,
                DatabaseConfig.DB_PASSWORD
        );
    }

    /**
     * 保存新用户
     * @param user 要保存的用户对象
     * @return 是否保存成功
     */
    @Override
    public boolean save(User user) {
        if (user == null) {
            logger.warn("尝试保存空用户");
            return false;
        }
        String sql = "insert into users (" +
                "id, username, password_hash, real_name, phone, address, permission, create_time" +
                ") values (" +
                "?, ?, ?, ?, ?, ?, ?, ?" +
                ")";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getId());
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword());

            User.Profile profile = user.getProfile();
            if (profile != null) {
                pstmt.setString(4, profile.getRealName());
                pstmt.setString(5, profile.getPhone());
                pstmt.setString(6, profile.getAddress());
            } else {
                pstmt.setNull(4, Types.VARCHAR);
                pstmt.setNull(5, Types.VARCHAR);
                pstmt.setString(6, "未知");
            }
            pstmt.setString(7, user.getPermission().name().toLowerCase());
            pstmt.setLong(8, user.getCreateTime());
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("用户保存成功: {}", user.getUsername());
                return true;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            logger.warn("用户名已存在: {}", user.getUsername());
        } catch (SQLException e) {
            logger.warn("保存用户失败: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 根据 ID 查询用户
     * @param id 用户 ID
     * @return 用户对象，不存在则返回 null
     */
    @Override
    public User findById(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        String sql = "select * from users where id = ?";
        try (Connection conn = getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return getUserByResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.warn("查询用户失败: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 查询所有用户
     * @return 所有用户列表
     */
    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "select * from users order by create_time desc";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(getUserByResultSet(rs));
            }
        } catch (SQLException e) {
            logger.warn("查询所有用户失败: {}", e.getMessage());
        }

        return users;
    }

    /**
     * 更新用户信息
     * @param user 要更新的用户对象
     * @return 是否更新成功
     */
    @Override
    public boolean update(User user) {
        if (user == null) {
            logger.warn("尝试更新空用户");
            return false;
        }

        String sql = "update users set username = ?, password_hash = ?, real_name = ?, " +
                "phone = ?, address = ?, permission = ? where id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());

            User.Profile profile = user.getProfile();
            if (profile != null) {
                pstmt.setString(3, profile.getRealName());
                pstmt.setString(4, profile.getPhone());
                pstmt.setString(5, profile.getAddress());
            } else {
                pstmt.setNull(3, Types.VARCHAR);
                pstmt.setNull(4, Types.VARCHAR);
                pstmt.setString(5, "未知");
            }

            pstmt.setString(6, user.getPermission().name().toLowerCase());
            pstmt.setString(7, user.getId());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("用户更新成功: {}", user.getUsername());
                return true;
            }
        } catch (SQLException e) {
            logger.warn("更新用户失败: {}", e.getMessage());
        }

        return false;
    }

    /**
     * 删除用户
     * @param id 用户 ID
     * @return 是否删除成功
     */
    @Override
    public boolean delete(String id) {
        String sql = "delete from users where id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("用户删除成功: {}", id);
                return true;
            }
        } catch (SQLException e) {
            logger.warn("删除用户失败: {}", e.getMessage());
        }

        logger.warn("尝试删除不存在的用户: {}", id);
        return false;
    }

    /**
     * 统计用户数量
     * @return 用户总数
     */
    @Override
    public int count() {
        String sql = "select count(*) from users";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.warn("统计用户失败: {}", e.getMessage());
        }

        return 0;
    }

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户对象，不存在则返回 null
     */
    public User findByUsername(String username) {
        if (username == null || username.isEmpty()) {
            return null;
        }

        String sql = "select * from users where username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return getUserByResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.warn("查询用户失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 根据权限查询用户
     * @param permission 权限类型
     * @return 符合条件的用户列表
     */
    public List<User> findByPermission(Permission permission) {
        List<User> users = new ArrayList<>();
        String sql = "select * from users where permission = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, permission.name().toLowerCase());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(getUserByResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.warn("按权限查询用户失败: {}", e.getMessage());
        }

        return users;
    }

    /**
     * 统计指定权限的用户数量
     * @param permission 权限类型
     * @return 用户数量
     */
    public int countByPermission(Permission permission) {
        String sql = "select count(*) from users where permission = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, permission.name().toLowerCase());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.warn("统计用户失败: {}", e.getMessage());
        }

        return 0;
    }

    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 是否存在
     */
    public boolean existsByUsername(String username) {
        String sql = "select count(*) from users where username = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.warn("检查用户名失败: {}", e.getMessage());
        }

        return false;
    }

    /**
     * 设置用户在线状态
     * @param userId 用户 ID
     * @param online 是否在线
     */
    public void setOnlineStatus(String userId, boolean online) {
        String sql = "update users set is_online = ? where id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, online);
            pstmt.setString(2, userId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            logger.warn("更新在线状态失败: {}", e.getMessage());
        }
    }

    /**
     * 清空所有数据（测试用）
     */
    public void clearAll() {
        String sql = "delete from users";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);
            logger.warn("所有用户数据已清空");

        } catch (SQLException e) {
            logger.warn("清空用户数据失败: {}", e.getMessage());
        }
    }

    private User getUserByResultSet(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String username = rs.getString("username");
        String password = rs.getString("password_hash");
        String realName = rs.getString("real_name");
        String phone = rs.getString("phone");
        String address = rs.getString("address");
        Permission permission = Permission.valueOf(rs.getString("permission").toUpperCase());
        boolean isOnline = rs.getBoolean("is_online");
        long createTime = rs.getLong("create_time");

        User.Profile profile = null;
        if (realName != null || phone != null) {
            profile = new User.Profile(
                    realName != null ? realName : "未知",
                    phone != null ? phone : "未知",
                    address != null ? address : "未知"
            );
        }
        return User.createFromDatabase(id, username, password, permission, profile, isOnline, createTime);
    }
}
