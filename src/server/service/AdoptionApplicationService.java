package server.service;

import common.entity.AdoptionApplication;
import common.manager.AdoptionApplicationManager;
import common.dto.response.Result;
import common.exception.BusinessException;

import java.util.List;

/**
 * 领养申请服务类 - 服务层
 * 提供领养申请相关的业务服务，封装 Manager 层
 */
public class AdoptionApplicationService {

    private final AdoptionApplicationManager applicationManager;

    public AdoptionApplicationService() {
        this.applicationManager = AdoptionApplicationManager.getInstance();
    }

    /**
     * 提交领养申请（普通用户）
     *
     * @param userId 用户ID
     * @param petId  宠物ID
     * @return 操作结果
     */
    public Result<String> submitApplication(String userId, String petId) {
        try {
            // 1. 验证参数
            if (userId == null || userId.trim().isEmpty()) {
                return Result.error("用户ID不能为空");
            }
            if (petId == null || petId.trim().isEmpty()) {
                return Result.error("宠物ID不能为空");
            }

            // 2. 调用 Manager 层提交申请
            String applicationId = applicationManager.submitApplication(userId, petId);

            if (applicationId != null) {
                return Result.success(applicationId);
            } else {
                return Result.error("您已为该宠物提交过申请或提交失败");
            }
        } catch (Exception e) {
            return Result.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 通过申请（管理员）
     *
     * @param adminId       管理员ID
     * @param applicationId 申请ID
     * @param comment       审核意见
     * @return 操作结果
     */
    public Result<String> approveApplication(String adminId, String applicationId, String comment) {
        try {
            // 1. 验证管理员权限
            validateAdminPermission(adminId);

            // 2. 调用 Manager 层审核
            boolean success = applicationManager.approve(applicationId, adminId, comment);

            if (success) {
                return Result.success("申请已通过");
            } else {
                return Result.error("审核失败");
            }
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            return Result.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 拒绝申请（管理员）
     *
     * @param adminId       管理员ID
     * @param applicationId 申请ID
     * @param comment       审核意见
     * @return 操作结果
     */
    public Result<String> rejectApplication(String adminId, String applicationId, String comment) {
        try {
            // 1. 验证管理员权限
            validateAdminPermission(adminId);

            // 2. 调用 Manager 层审核
            boolean success = applicationManager.reject(applicationId, adminId, comment);

            if (success) {
                return Result.success("申请已拒绝");
            } else {
                return Result.error("审核失败");
            }
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            return Result.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 搁置申请（管理员）
     *
     * @param adminId       管理员ID
     * @param applicationId 申请ID
     * @return 操作结果
     */
    public Result<String> postponeApplication(String adminId, String applicationId) {
        try {
            // 1. 验证管理员权限
            validateAdminPermission(adminId);

            // 2. 调用 Manager 层搁置
            boolean success = applicationManager.postpone(applicationId);

            if (success) {
                return Result.success("申请已搁置");
            } else {
                return Result.error("搁置失败");
            }
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            return Result.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 查询所有申请（管理员）
     *
     * @return 申请列表
     */
    public Result<List<AdoptionApplication>> getAllApplications() {
        try {
            List<AdoptionApplication> applications = applicationManager.getAll();
            return Result.success(applications);
        } catch (Exception e) {
            return Result.error("查询申请失败: " + e.getMessage());
        }
    }

    /**
     * 根据 ID 查询申请详情
     *
     * @param applicationId 申请ID
     * @return 申请对象
     */
    public Result<AdoptionApplication> getApplicationById(String applicationId) {
        try {
            AdoptionApplication application = applicationManager.getById(applicationId);
            if (application == null) {
                return Result.error("申请不存在");
            }
            return Result.success(application);
        } catch (Exception e) {
            return Result.error("查询申请失败: " + e.getMessage());
        }
    }

    /**
     * 查询待审核的申请（管理员）
     *
     * @return 申请列表
     */
    public Result<List<AdoptionApplication>> getPendingApplications() {
        try {
            List<AdoptionApplication> applications = applicationManager.getPendingApplications();
            return Result.success(applications);
        } catch (Exception e) {
            return Result.error("查询申请失败: " + e.getMessage());
        }
    }

    /**
     * 查询已通过的申请（管理员）
     *
     * @return 申请列表
     */
    public Result<List<AdoptionApplication>> getApprovedApplications() {
        try {
            List<AdoptionApplication> applications = applicationManager.getApprovedApplications();
            return Result.success(applications);
        } catch (Exception e) {
            return Result.error("查询申请失败: " + e.getMessage());
        }
    }

    /**
     * 查询已拒绝的申请（管理员）
     *
     * @return 申请列表
     */
    public Result<List<AdoptionApplication>> getRejectedApplications() {
        try {
            List<AdoptionApplication> applications = applicationManager.getRejectedApplications();
            return Result.success(applications);
        } catch (Exception e) {
            return Result.error("查询申请失败: " + e.getMessage());
        }
    }

    /**
     * 查询我的申请（普通用户）
     *
     * @param userId 用户ID
     * @return 申请列表
     */
    public Result<List<AdoptionApplication>> getMyApplications(String userId) {
        try {
            List<AdoptionApplication> applications = applicationManager.getByApplicator(userId);
            return Result.success(applications);
        } catch (Exception e) {
            return Result.error("查询申请失败: " + e.getMessage());
        }
    }

    /**
     * 查询某个宠物的所有申请（管理员）
     *
     * @param petId 宠物ID
     * @return 申请列表
     */
    public Result<List<AdoptionApplication>> getApplicationsByPet(String petId) {
        try {
            List<AdoptionApplication> applications = applicationManager.getByPet(petId);
            return Result.success(applications);
        } catch (Exception e) {
            return Result.error("查询申请失败: " + e.getMessage());
        }
    }

    /**
     * 统计申请数量（管理员）
     *
     * @return 各状态申请数量
     */
    public Result<ApplicationStatistics> getApplicationStatistics() {
        try {
            ApplicationStatistics stats = new ApplicationStatistics(
                    applicationManager.count(),
                    applicationManager.countPending(),
                    applicationManager.countApproved(),
                    applicationManager.countRejected()
            );
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error("统计申请失败: " + e.getMessage());
        }
    }

    /**
     * 验证管理员权限
     */
    private void validateAdminPermission(String userId) throws BusinessException {
        // TODO: 从 UserManager 获取用户权限并验证
        // User user = UserManager.getInstance().getUserById(userId);
        // if (user == null || user.getPermission() != Permission.ADMIN) {
        //     throw new BusinessException(403, "无权限执行此操作");
        // }
    }

    /**
         * 申请统计数据
         */

    public record ApplicationStatistics(int total, int pending, int approved, int rejected) {
    }
}
