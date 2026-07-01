package common.manager;

import common.entity.AdoptionApplication;
import common.entity.AdoptionApplication.Review;
import common.enums.ApplicationStatus;
import server.dao.AdoptionApplicationDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 领养申请管理器 - 业务逻辑层
 * 负责领养申请相关的业务逻辑处理
 */
public class AdoptionApplicationManager {

    private static final Logger log = LoggerFactory.getLogger(AdoptionApplicationManager.class);

    private static volatile AdoptionApplicationManager instance = null;

    // DAO 用于数据持久化
    private final AdoptionApplicationDAO applicationDAO;

    private AdoptionApplicationManager() {
        this.applicationDAO = new AdoptionApplicationDAO();
    }

    /**
     * 获取单例实例（线程安全）
     */
    public static AdoptionApplicationManager getInstance() {
        if (instance == null) {
            synchronized (AdoptionApplicationManager.class) {
                if (instance == null) {
                    instance = new AdoptionApplicationManager();
                }
            }
        }
        return instance;
    }

    /**
     * 提交领养申请
     *
     * @param applicatorId 申请人ID
     * @param petId        宠物ID
     * @return 申请ID，失败返回null
     */
    public String submitApplication(String applicatorId, String petId) {
        // 参数验证
        if (applicatorId == null || applicatorId.trim().isEmpty()) {
            log.warn("提交失败：申请人ID为空");
            return null;
        }

        if (petId == null || petId.trim().isEmpty()) {
            log.warn("提交失败：宠物ID为空");
            return null;
        }

        // 检查是否已经提交过申请
        if (applicationDAO.hasApplied(applicatorId, petId)) {
            log.warn("提交失败：用户已为该宠物提交过申请: {} -> {}", applicatorId, petId);
            return null;
        }

        // 创建申请对象
        AdoptionApplication application = new AdoptionApplication(applicatorId, petId);

        // 保存到数据库
        boolean success = applicationDAO.save(application);
        if (success) {
            log.info("申请提交成功: {} by user {}", application.getId(), applicatorId);
            return application.getId();
        } else {
            log.error("申请提交失败: user {} -> pet {}", applicatorId, petId);
            return null;
        }
    }

    /**
     * 通过申请
     *
     * @param applicationId 申请ID
     * @param adminId       管理员ID
     * @param comment       审核意见
     * @return 是否操作成功
     */
    public boolean approve(String applicationId, String adminId, String comment) {
        return reviewApplication(applicationId, adminId, comment, ApplicationStatus.APPROVED);
    }

    /**
     * 拒绝申请
     *
     * @param applicationId 申请ID
     * @param adminId       管理员ID
     * @param comment       审核意见
     * @return 是否操作成功
     */
    public boolean reject(String applicationId, String adminId, String comment) {
        return reviewApplication(applicationId, adminId, comment, ApplicationStatus.REJECTED);
    }

    /**
     * 搁置申请（稍后处理）
     *
     * @param applicationId 申请ID
     * @return 是否操作成功
     */
    public boolean postpone(String applicationId) {
        if (applicationId == null || applicationId.isEmpty()) {
            log.warn("搁置失败：申请ID为空");
            return false;
        }

        AdoptionApplication application = applicationDAO.findById(applicationId);
        if (application == null) {
            log.warn("搁置失败：申请不存在: {}", applicationId);
            return false;
        }

        // 保持 PENDING 状态，不做修改
        log.info("申请已搁置: {}", applicationId);
        return true;
    }

    /**
     * 审核申请（内部方法）
     */
    private boolean reviewApplication(String applicationId, String adminId, String comment, ApplicationStatus status) {
        // 参数验证
        if (applicationId == null || applicationId.isEmpty()) {
            log.warn("审核失败：申请ID为空");
            return false;
        }

        if (adminId == null || adminId.isEmpty()) {
            log.warn("审核失败：管理员ID为空");
            return false;
        }

        // 查询申请
        AdoptionApplication application = applicationDAO.findById(applicationId);
        if (application == null) {
            log.warn("审核失败：申请不存在: {}", applicationId);
            return false;
        }

        // 检查申请状态
        if (application.getStatus() != ApplicationStatus.PENDING) {
            log.warn("审核失败：申请状态不是待审核: {} (当前状态: {})",
                    applicationId, application.getStatus());
            return false;
        }

        // 创建审核信息
        Review review = new Review(adminId, comment != null ? comment : "无");
        application.setReview(review);
        application.setStatus(status);

        // 更新到数据库
        boolean success = applicationDAO.update(application);
        if (success) {
            log.info("申请审核成功: {} -> {} by admin {}", applicationId, status, adminId);
        } else {
            log.error("申请审核失败: {}", applicationId);
        }
        return success;
    }

    /**
     * 根据ID查询申请
     *
     * @param id 申请ID
     * @return 申请对象，不存在返回null
     */
    public AdoptionApplication getById(String id) {
        return applicationDAO.findById(id);
    }

    /**
     * 查询所有申请
     *
     * @return 申请列表
     */
    public List<AdoptionApplication> getAll() {
        return applicationDAO.findAll();
    }

    /**
     * 查询待审核的申请
     *
     * @return 申请列表
     */
    public List<AdoptionApplication> getPendingApplications() {
        return applicationDAO.findByStatus(ApplicationStatus.PENDING);
    }

    /**
     * 查询已通过的申请
     *
     * @return 申请列表
     */
    public List<AdoptionApplication> getApprovedApplications() {
        return applicationDAO.findByStatus(ApplicationStatus.APPROVED);
    }

    /**
     * 查询已拒绝的申请
     *
     * @return 申请列表
     */
    public List<AdoptionApplication> getRejectedApplications() {
        return applicationDAO.findByStatus(ApplicationStatus.REJECTED);
    }

    /**
     * 根据申请人查询申请
     *
     * @param applicatorId 申请人ID
     * @return 申请列表
     */
    public List<AdoptionApplication> getByApplicator(String applicatorId) {
        if (applicatorId == null || applicatorId.isEmpty()) {
            log.warn("查询失败：申请人ID为空");
            return List.of();
        }
        return applicationDAO.findByApplicator(applicatorId);
    }

    /**
     * 根据宠物查询申请
     *
     * @param petId 宠物ID
     * @return 申请列表
     */
    public List<AdoptionApplication> getByPet(String petId) {
        if (petId == null || petId.isEmpty()) {
            log.warn("查询失败：宠物ID为空");
            return List.of();
        }
        return applicationDAO.findByPet(petId);
    }

    /**
     * 根据审核人查询申请
     *
     * @param adminId 管理员ID
     * @return 申请列表
     */
    public List<AdoptionApplication> getByAdmin(String adminId) {
        if (adminId == null || adminId.isEmpty()) {
            log.warn("查询失败：管理员ID为空");
            return List.of();
        }
        return applicationDAO.findByAdmin(adminId);
    }

    /**
     * 检查是否有待审核的申请
     *
     * @return 是否有待审核申请
     */
    public boolean hasPendingApplications() {
        return !getPendingApplications().isEmpty();
    }

    /**
     * 统计申请总数
     *
     * @return 申请数量
     */
    public int count() {
        return applicationDAO.count();
    }

    /**
     * 统计待审核申请数量
     *
     * @return 待审核数量
     */
    public int countPending() {
        return applicationDAO.countByStatus(ApplicationStatus.PENDING);
    }

    /**
     * 统计已通过申请数量
     *
     * @return 已通过数量
     */
    public int countApproved() {
        return applicationDAO.countByStatus(ApplicationStatus.APPROVED);
    }

    /**
     * 统计已拒绝申请数量
     *
     * @return 已拒绝数量
     */
    public int countRejected() {
        return applicationDAO.countByStatus(ApplicationStatus.REJECTED);
    }

    /**
     * 显示所有申请
     */
    public void showAll() {
        List<AdoptionApplication> applications = getAll();
        System.out.println("========== 领养申请列表 ==========");
        if (applications.isEmpty()) {
            System.out.println("暂无申请");
        } else {
            for (int i = 0; i < applications.size(); i++) {
                System.out.printf("[%d] %s\n\n", i + 1, applications.get(i).getDisplayString());
            }
        }
        System.out.println("总计: " + applications.size() + " 条");
        System.out.println("==================================");
    }

    /**
     * 显示申请详情
     *
     * @param id 申请ID
     */
    public void showDetail(String id) {
        AdoptionApplication application = getById(id);
        if (application == null) {
            System.out.println("申请不存在: " + id);
            return;
        }

        System.out.println("========== 申请详情 ==========");
        System.out.println(application.getDisplayString());
        System.out.println("==============================");
    }
}
