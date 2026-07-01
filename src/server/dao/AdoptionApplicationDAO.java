package server.dao;

import common.entity.AdoptionApplication;
import common.entity.AdoptionApplication.Review;
import common.enums.ApplicationStatus;
import config.DatabaseConfig;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * 领养申请数据访问对象
 */
public class AdoptionApplicationDAO implements BaseDAO<AdoptionApplication, String> {

    private static final Logger logger = Logger.getLogger(AdoptionApplicationDAO.class.getName());

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                DatabaseConfig.JDBC_URL,
                DatabaseConfig.DB_USER,
                DatabaseConfig.DB_PASSWORD
        );
    }

    /**
     * 保存领养申请
     */
    @Override
    public boolean save(AdoptionApplication application) {
        if (application == null) {
            logger.warning("尝试保存空申请");
            return false;
        }

        String sql = "insert into adoption_applications (id, applicator_id, pet_id, status) " +
                     "values (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, application.getId());
            pstmt.setString(2, application.getApplicatorId());
            pstmt.setString(3, application.getPetId());
            pstmt.setString(4, application.getStatus().name());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("领养申请保存成功: " + application.getId());
                return true;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            logger.warning("申请已存在: " + application.getId());
        } catch (SQLException e) {
            logger.severe("保存领养申请失败: " + e.getMessage());
        }
        return false;
    }

    /**
     * 根据 ID 查询申请
     */
    @Override
    public AdoptionApplication findById(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }

        String sql = "select * from adoption_applications where id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return getApplicationByResultSet(rs);
                }
            }
        } catch (SQLException e) {
            logger.severe("查询申请失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 查询所有申请
     */
    @Override
    public List<AdoptionApplication> findAll() {
        List<AdoptionApplication> applications = new ArrayList<>();
        String sql = "select * from adoption_applications order by id desc";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                applications.add(getApplicationByResultSet(rs));
            }
        } catch (SQLException e) {
            logger.severe("查询所有申请失败: " + e.getMessage());
        }
        return applications;
    }

    /**
     * 更新申请信息（包括审核结果）
     */
    @Override
    public boolean update(AdoptionApplication application) {
        if (application == null) {
            logger.warning("尝试更新空申请");
            return false;
        }

        String sql = "update adoption_applications set status = ?, admin_id = ?, " +
                     "comment = ?, review_time = ? where id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, application.getStatus().name());

            Review review = application.getReview();
            if (review != null) {
                pstmt.setString(2, review.getAdminId());
                pstmt.setString(3, review.getComment());
                pstmt.setLong(4, review.getTime());
            } else {
                pstmt.setNull(2, Types.VARCHAR);
                pstmt.setNull(3, Types.VARCHAR);
                pstmt.setNull(4, Types.BIGINT);
            }

            pstmt.setString(5, application.getId());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("申请更新成功: " + application.getId());
                return true;
            }
        } catch (SQLException e) {
            logger.severe("更新申请失败: " + e.getMessage());
        }
        return false;
    }

    /**
     * 删除申请
     */
    @Override
    public boolean delete(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }

        String sql = "delete from adoption_applications where id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("申请删除成功: " + id);
                return true;
            }
        } catch (SQLException e) {
            logger.severe("删除申请失败: " + e.getMessage());
        }

        logger.warning("尝试删除不存在的申请: " + id);
        return false;
    }

    /**
     * 统计申请数量
     */
    @Override
    public int count() {
        String sql = "select count(*) from adoption_applications";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.severe("统计申请失败: " + e.getMessage());
        }
        return 0;
    }

    /**
     * 根据申请人查询申请
     */
    public List<AdoptionApplication> findByApplicator(String applicatorId) {
        List<AdoptionApplication> applications = new ArrayList<>();
        String sql = "select * from adoption_applications where applicator_id = ? order by id desc";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, applicatorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    applications.add(getApplicationByResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("按申请人查询申请失败: " + e.getMessage());
        }
        return applications;
    }

    /**
     * 根据宠物查询申请
     */
    public List<AdoptionApplication> findByPet(String petId) {
        List<AdoptionApplication> applications = new ArrayList<>();
        String sql = "select * from adoption_applications where pet_id = ? order by id desc";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, petId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    applications.add(getApplicationByResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("按宠物查询申请失败: " + e.getMessage());
        }
        return applications;
    }

    /**
     * 根据状态查询申请
     */
    public List<AdoptionApplication> findByStatus(ApplicationStatus status) {
        List<AdoptionApplication> applications = new ArrayList<>();
        String sql = "select * from adoption_applications where status = ? order by id desc";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    applications.add(getApplicationByResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("按状态查询申请失败: " + e.getMessage());
        }
        return applications;
    }

    /**
     * 根据审核人查询申请
     */
    public List<AdoptionApplication> findByAdmin(String adminId) {
        List<AdoptionApplication> applications = new ArrayList<>();
        String sql = "select * from adoption_applications where admin_id = ? order by review_time desc";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, adminId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    applications.add(getApplicationByResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.severe("按审核人查询申请失败: " + e.getMessage());
        }
        return applications;
    }

    /**
     * 统计指定状态的申请数量
     */
    public int countByStatus(ApplicationStatus status) {
        String sql = "select count(*) from adoption_applications where status = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.severe("统计申请失败: " + e.getMessage());
        }
        return 0;
    }

    /**
     * 检查用户是否已经为某个宠物提交过申请
     */
    public boolean hasApplied(String applicatorId, String petId) {
        String sql = "select count(*) from adoption_applications where applicator_id = ? and pet_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, applicatorId);
            pstmt.setString(2, petId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.severe("检查申请状态失败: " + e.getMessage());
        }
        return false;
    }

    /**
     * 将 ResultSet 转换为 AdoptionApplication 对象
     */
    private AdoptionApplication getApplicationByResultSet(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String applicatorId = rs.getString("applicator_id");
        String petId = rs.getString("pet_id");
        ApplicationStatus status = ApplicationStatus.valueOf(rs.getString("status"));

        // 构建 Review 对象
        Review review = null;
        String adminId = rs.getString("admin_id");
        String comment = rs.getString("comment");
        long reviewTime = rs.getLong("review_time");

        if (adminId != null && !adminId.isEmpty()) {
            review = Review.createFromDatabase(adminId, comment, reviewTime);
        }

        return AdoptionApplication.createFromDatabase(id, applicatorId, petId, status, review);
    }
}
