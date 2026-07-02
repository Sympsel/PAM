package server.service;

import common.entity.Announcement;
import common.dto.response.Result;
import common.entity.User;
import common.enums.Permission;
import common.exception.BusinessException;
import common.manager.UserManager;
import server.dao.AnnouncementDAO;
import java.util.List;

/**
 * 公告服务类
 */
public class AnnouncementService {

    private final AnnouncementDAO announcementDAO;

    public AnnouncementService() {
        this.announcementDAO = new AnnouncementDAO();
    }

    /**
     * 发布公告（管理员）
     */
    public Result<String> publishAnnouncement(String adminId, String title, String content) {
        try {
            // 1. 验证管理员权限
            validateAdminPermission(adminId);

            // 2. 验证参数
            if (title == null || title.trim().isEmpty()) {
                return Result.error("公告标题不能为空");
            }
            if (content == null || content.trim().isEmpty()) {
                return Result.error("公告内容不能为空");
            }

            // 3. 创建公告对象
            Announcement announcement = new Announcement(adminId, title.trim(), content.trim());

            // 4. 保存到数据库
            boolean success = announcementDAO.save(announcement);
            if (!success) {
                return Result.error("发布公告失败");
            }

            // 5. todo 广播给所有在线用户

            return Result.success("公告发布成功");
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            return Result.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 修改公告（管理员）
     */
    public Result<String> updateAnnouncement(String announcementId, String title,
                                              String content, String adminId) {
        try {
            // 1. 验证管理员权限
            validateAdminPermission(adminId);

            // 2. 查询公告是否存在
            Announcement announcement = announcementDAO.findById(announcementId);
            if (announcement == null) {
                return Result.error("公告不存在");
            }

            // 3. 更新信息
            announcement.setTitle(title);
            announcement.setContent(content);

            boolean success = announcementDAO.update(announcement);
            if (!success) {
                return Result.error("更新公告失败");
            }

            return Result.success("公告更新成功");
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            return Result.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 删除公告（管理员）
     */
    public Result<String> deleteAnnouncement(String announcementId, String adminId) {
        try {
            // 1. 验证管理员权限
            validateAdminPermission(adminId);

            // 2. 从数据库删除
            boolean success = announcementDAO.delete(announcementId);
            if (!success) {
                return Result.error("删除公告失败");
            }

            return Result.success("公告删除成功");
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            return Result.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 浏览公告（所有人）
     */
    public Result<List<Announcement>> getAllAnnouncements() {
        try {
            List<Announcement> announcements = announcementDAO.findAll();
            // 按发布时间倒序排列
            announcements.sort((a1, a2) -> Long.compare(a2.getCreateTime(), a1.getCreateTime()));
            return Result.success(announcements);
        } catch (Exception e) {
            return Result.error("查询公告失败: " + e.getMessage());
        }
    }

    /**
     * 获取公告详情
     */
    public Result<Announcement> getAnnouncementById(String id) {
        try {
            Announcement announcement = announcementDAO.findById(id);
            if (announcement == null) {
                return Result.error("公告不存在");
            }
            return Result.success(announcement);
        } catch (Exception e) {
            return Result.error("查询公告失败: " + e.getMessage());
        }
    }


    /**
     * 验证管理员权限
     */
    private void validateAdminPermission(String userId) throws BusinessException {
         User user = UserManager.getInstance().getUserById(userId);
         if (user == null || user.getPermission() != Permission.ADMIN) {
             throw new BusinessException(403, "无权限执行此操作");
         }
    }
}
