package server.dao;

import common.entity.Announcement;
import config.DatabaseConfig;

import java.sql.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 公告数据访问对象
 */
public class AnnouncementDAO implements BaseDAO<Announcement, String> {

    private static final Logger logger = LoggerFactory.getLogger(AnnouncementDAO.class);

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.JDBC_URL,
                DatabaseConfig.DB_USER,
                DatabaseConfig.DB_PASSWORD
        );
    }

    /**
     * 保存公告
     */
    @Override
    public boolean save(Announcement announcement) {
        if (announcement == null) {
            logger.warn("尝试保存空公告");
            return false;
        }

        String sql = "insert into announcements (id, sender_id, title, content, create_time) " +
                "values (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, announcement.getId());
            pstmt.setString(2, announcement.getSenderId());
            pstmt.setString(3, announcement.getTitle());
            pstmt.setString(4, announcement.getContent());
            pstmt.setLong(5, announcement.getCreateTime());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("公告保存成功: {}", announcement.getTitle());
                return true;
            }
        } catch (SQLException e) {
            logger.info("保存公告失败: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 根据 ID 查询公告
     */
    @Override
    public Announcement findById(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }

        String sql = "select * from announcements where id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return getAnnouncementByResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.warn("查询公告失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 查询所有公告
     */
    @Override
    public List<Announcement> findAll() {
        List<Announcement> announcements = new ArrayList<>();
        String sql = "select * from announcements order by create_time desc";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                announcements.add(getAnnouncementByResultSet(rs));
            }
        } catch (SQLException e) {
            logger.warn("查询所有公告失败: {}", e.getMessage());
        }
        return announcements;
    }

    /**
     * 更新公告信息
     */
    @Override
    public boolean update(Announcement announcement) {
        if (announcement == null) {
            logger.warn("尝试更新空公告");
            return false;
        }

        String sql = "update announcements set title = ?, content = ? where id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, announcement.getTitle());
            pstmt.setString(2, announcement.getContent());
            pstmt.setString(3, announcement.getId());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("公告更新成功: {}", announcement.getTitle());
                return true;
            }
        } catch (SQLException e) {
            logger.warn("更新公告失败: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 删除公告
     */
    @Override
    public boolean delete(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }

        String sql = "delete from announcements where id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("公告删除成功: {}", id);
                return true;
            }
        } catch (SQLException e) {
            logger.info("删除公告失败: {}", e.getMessage());
        }

        logger.warn("尝试删除不存在的公告: {}", id);
        return false;
    }

    /**
     * 统计公告数量
     */
    @Override
    public int count() {
        String sql = "select count(*) from announcements";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.warn("统计公告失败: {}", e.getMessage());
        }
        return 0;
    }

    /**
     * 根据发布者查询公告
     */
    public List<Announcement> findBySender(String senderId) {
        List<Announcement> announcements = new ArrayList<>();
        String sql = "select * from announcements where senderId = ? order by create_time desc";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, senderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    announcements.add(getAnnouncementByResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.info("按发布者查询公告失败: {}", e.getMessage());
        }
        return announcements;
    }

    /**
     * 根据标题模糊查询公告
     */
    public List<Announcement> findByTitle(String title) {
        List<Announcement> announcements = new ArrayList<>();
        String sql = "select * from announcements where title like ? order by create_time desc";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + title + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    announcements.add(getAnnouncementByResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.warn("按标题查询公告失败: {}", e.getMessage());
        }
        return announcements;
    }

    /**
     * 查询最新的 N 条公告
     */
    public List<Announcement> findLatest(int limit) {
        List<Announcement> announcements = new ArrayList<>();
        String sql = "select * from announcements order by create_time desc limit ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    announcements.add(getAnnouncementByResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.warn("查询最新公告失败: {}", e.getMessage());
        }
        return announcements;
    }

    /**
     * 删除指定发布者的所有公告
     *
     * @param senderId 发布者ID
     * @return 删除的记录数
     */
    public int deleteBySender(String senderId) {
        String sql = "delete from announcements where sender_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, senderId);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                logger.info("删除用户 {} 的 {} 条公告", senderId, rows);
            }
            return rows;
        } catch (SQLException e) {
            logger.warn("删除用户公告失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 将 ResultSet 转换为 Announcement 对象
     */
    private Announcement getAnnouncementByResultSet(ResultSet rs) throws SQLException {
        return Announcement.createFromDatabase(
                rs.getString("id"),
                rs.getString("sender_id"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getLong("create_time")
        );
    }
}
