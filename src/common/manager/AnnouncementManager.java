package common.manager;

import common.entity.Announcement;
import common.utils.StringFormatter;
import server.dao.AnnouncementDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * 公告管理器 - 业务逻辑层
 * 负责公告相关的业务逻辑处理
 */
public class AnnouncementManager {

    private static final Logger log = LoggerFactory.getLogger(AnnouncementManager.class);

    private static volatile AnnouncementManager instance = null;

    // DAO 用于数据持久化
    private final AnnouncementDAO announcementDAO;

    private AnnouncementManager() {
        this.announcementDAO = new AnnouncementDAO();
    }

    /**
     * 获取单例实例
     */
    public static AnnouncementManager getInstance() {
        if (instance == null) {
            synchronized (AnnouncementManager.class) {
                if (instance == null) {
                    instance = new AnnouncementManager();
                }
            }
        }
        return instance;
    }

    /**
     * 发布公告
     *
     * @param senderId 发布者ID
     * @param title    标题
     * @param content  内容
     * @return 公告ID，失败返回null
     */
    public String publish(String senderId, String title, String content) {
        // 参数验证
        if (senderId == null || senderId.trim().isEmpty()) {
            log.warn("发布失败：发布者ID为空");
            return null;
        }

        if (title == null || title.trim().isEmpty()) {
            log.warn("发布失败：标题为空");
            return null;
        }

        if (content == null || content.trim().isEmpty()) {
            log.warn("发布失败：内容为空");
            return null;
        }

        // 创建公告对象
        Announcement announcement = new Announcement(senderId, title.trim(), content.trim());

        // 保存到数据库
        boolean success = announcementDAO.save(announcement);
        if (success) {
            log.info("公告发布成功: {} by {}", announcement.getTitle(), senderId);
            return announcement.getId();
        } else {
            log.error("公告发布失败: {}", title);
            return null;
        }
    }

    /**
     * 修改公告
     *
     * @param id      公告ID
     * @param title   新标题
     * @param content 新内容
     * @return 是否修改成功
     */
    public boolean update(String id, String title, String content) {
        // 查询公告是否存在
        Announcement announcement = announcementDAO.findById(id);
        if (announcement == null) {
            log.warn("更新失败：公告不存在: {}", id);
            return false;
        }

        // 参数验证
        if (title == null || title.trim().isEmpty()) {
            log.warn("更新失败：标题为空");
            return false;
        }

        if (content == null || content.trim().isEmpty()) {
            log.warn("更新失败：内容为空");
            return false;
        }

        // 更新字段
        announcement.setTitle(title.trim());
        announcement.setContent(content.trim());

        // 保存到数据库
        boolean success = announcementDAO.update(announcement);
        if (success) {
            log.info("公告更新成功: {}", id);
        } else {
            log.error("公告更新失败: {}", id);
        }
        return success;
    }

    /**
     * 删除公告
     *
     * @param id 公告ID
     * @return 是否删除成功
     */
    public boolean remove(String id) {
        if (id == null || id.isEmpty()) {
            log.warn("删除失败：ID为空");
            return false;
        }

        // 检查公告是否存在
        Announcement announcement = announcementDAO.findById(id);
        if (announcement == null) {
            log.warn("删除失败：公告不存在: {}", id);
            return false;
        }

        // 从数据库删除
        boolean success = announcementDAO.delete(id);
        if (success) {
            log.info("公告删除成功: {}", id);
        } else {
            log.error("公告删除失败: {}", id);
        }
        return success;
    }

    /**
     * 根据ID查询公告
     *
     * @param id 公告ID
     * @return 公告对象，不存在返回null
     */
    public Announcement getById(String id) {
        return announcementDAO.findById(id);
    }

    /**
     * 查询所有公告（按时间倒序）
     *
     * @return 公告列表
     */
    public List<Announcement> getAll() {
        return announcementDAO.findAll();
    }


    /**
     * 根据发布者查询公告
     *
     * @param senderId 发布者ID
     * @return 公告列表
     */
    public List<Announcement> getBySender(String senderId) {
        if (senderId == null || senderId.isEmpty()) {
            log.warn("查询失败：发布者ID为空");
            return List.of();
        }
        return announcementDAO.findBySender(senderId);
    }

    /**
     * 根据标题模糊查询公告
     *
     * @param title 标题关键词
     * @return 公告列表
     */
    public List<Announcement> searchByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            log.warn("查询失败：标题为空");
            return List.of();
        }
        return announcementDAO.findByTitle(title.trim());
    }


    /**
     * 显示所有公告
     */
    public void showAll() {
        List<Announcement> announcements = getAll();
        System.out.println("========== 公告列表 ==========");
        if (announcements.isEmpty()) {
            System.out.println("暂无公告");
        } else {
            for (int i = 0; i < announcements.size(); i++) {
                System.out.printf("[%d] %s\n\n", i + 1, announcements.get(i).getShortDisplayString());
            }
        }
        System.out.println("总计: " + announcements.size() + " 条");
        System.out.println("==============================");
    }

    /**
     * 显示公告详情
     *
     * @param id 公告ID
     */
    public void showDetail(String id) {
        Announcement announcement = getById(id);
        if (announcement == null) {
            System.out.println("公告不存在: " + id);
            return;
        }

        System.out.println("========== 公告详情 ==========");
        System.out.println("ID: " + announcement.getId());
        System.out.println("标题: " + announcement.getTitle());
        System.out.println("发布者: " + announcement.getSenderId());
        System.out.println("时间: " + StringFormatter.timeStampToString(announcement.getCreateTime()));
        System.out.println("内容:");
        System.out.println(announcement.getContent());
        System.out.println("==============================");
    }
}