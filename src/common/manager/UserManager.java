package common.manager;

import common.entity.User;
import common.enums.Permission;
import common.utils.PasswordEncoder;
import common.utils.Validator;
import server.dao.AdoptionApplicationDAO;
import server.dao.AnnouncementDAO;
import server.dao.UserDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class UserManager {
    private static final Logger logger = LoggerFactory.getLogger(UserManager.class);

    private static UserManager instance;
    private final UserDAO userDAO;
    private final AdoptionApplicationDAO applicationDAO;
    private final AnnouncementDAO announcementDAO;
    private final List<String> onlineUserIds;

    private UserManager() {
        userDAO = new UserDAO();
        applicationDAO = new AdoptionApplicationDAO();
        announcementDAO = new AnnouncementDAO();
        onlineUserIds = new ArrayList<>();
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }


    /**
     * 用户注册
     *
     * @param username   用户名
     * @param password   明文密码
     * @param permission 权限
     * @param profile    个人信息
     * @return 注册成功返回用户ID，失败返回null
     */
    public String register(String username, String password, Permission permission, User.Profile profile) {
        // 参数检验
        if (!Validator.isValidUsername(username)) {
            logger.warn("注册失败：用户名格式不正确: {}", username);
            return null;
        }

        if (!Validator.isValidPassword(password)) {
            logger.warn("注册失败：密码复杂度不符合要求");
            return null;
        }

        if (userDAO.existsByUsername(username)) {
            logger.warn("用户名已被占用: {}", username);
            return null;
        }

        String encodedPassword = PasswordEncoder.encode(password);
        User newUser = new User(username, encodedPassword, permission, profile);

        // 保存到数据库
        boolean success = userDAO.save(newUser);
        if (!success) {
            logger.warn("注册失败：保存用户信息失败");
            return null;
        }
        logger.info("用户注册成功: {}, ID: {}", username, newUser.getId());
        return newUser.getId();
    }

    /**
     * 删除用户
     *
     * @param userId 要删除的用户ID
     * @return 是否删除成功
     */
    public boolean deleteUser(String userId) {
        if (userId == null || userId.isEmpty()) {
            logger.warn("删除用户失败：用户ID为空");
            return false;
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            logger.warn("删除用户失败：用户不存在: {}", userId);
            return false;
        }

        // 如果用户在线，先登出
        onlineUserIds.remove(userId);

        // 级联删除用户的申请和公告
        int apps = applicationDAO.deleteByApplicator(userId);
        int announcements = announcementDAO.deleteBySender(userId);
        if (apps > 0 || announcements > 0) {
            logger.info("～ 删除用户 {} 的 {} 条申请和 {} 条公告",
                    user.getUsername(), apps, announcements);
        }

        boolean success = userDAO.delete(userId);
        if (success) {
            logger.info("～ 用户删除成功: {}", user.getUsername());
        } else {
            logger.warn("用户删除失败: {}", user.getUsername());
        }
        return success;
    }

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 明文密码
     * @return 登录成功返回用户ID，失败返回null
     */
    public String login(String username, String password) {
        // 参数验证
        if (username == null || username.trim().isEmpty()) {
            logger.warn("登录失败：用户名为空");
            return null;
        }
        if (password == null || password.isEmpty()) {
            logger.warn("登录失败：密码为空");
            return null;
        }

        // 2. 查询用户
        User user = userDAO.findByUsername(username);
        if (user == null) {
            logger.warn("登录失败：用户不存在: {}", username);
            return null;
        }

        if (!PasswordEncoder.matches(password, user.getPassword())) {
            logger.warn("登录失败：密码错误: {}", username);
            return null;
        }

        // 4. 检查是否已在线
        if (onlineUserIds.contains(user.getId()) && user.isOnline()) {
            logger.warn("用户已在线: {}", username);
            return null;
        }

        // 5. 添加到在线列表
        onlineUserIds.add(user.getId());
        userDAO.setOnlineStatus(user.getId(), true);

        logger.info("用户登录成功: username={}", username);
        return user.getId();
    }


    /**
     * 用户登出
     *
     * @param userId 用户ID
     */
    public void logout(String userId) {
        if (userId != null && onlineUserIds.remove(userId)) {
            userDAO.setOnlineStatus(userId, false);
            logger.info("用户登出: {}", userId);
        }
    }

    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户对象
     */
    public User getUserById(String id) {
        return userDAO.findById(id);
    }

    /**
     * 更新用户信息
     *
     * @param user 用户对象
     * @return 是否更新成功
     */
    public boolean updateUser(User user) {
        if (user == null) {
            return false;
        }
        return userDAO.update(user);
    }

    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isOnline(String userId) {
        return onlineUserIds.contains(userId);
    }

    /**
     * 获取所有在线用户
     *
     * @return 用户列表
     */
    public List<User> getOnlineUsers() {
        List<User> onlineUsers = new ArrayList<>();
        for (String userId : onlineUserIds) {
            User user = userDAO.findById(userId);
            if (user != null) {
                onlineUsers.add(user);
            }
        }
        return onlineUsers;
    }

    /**
     * 显示所有在线用户
     */
    public void showAllUserOnline() {
        List<User> users = getOnlineUsers();
        System.out.println("[在线用户列表]");
        for (User user : users) {
            System.out.println("\t" + user);
        }
        System.out.println("总计: " + users.size());
    }

    /**
     * 显示所有注册用户
     */
    public void showAllUserRegistered() {
        List<User> users = getAllRegisteredUsers();
        System.out.println("[注册用户列表]");
        for (User user : users) {
            System.out.println("\t" + user);
        }
        System.out.println("总计: " + users.size());
    }

    /**
     * 获取所有注册用户
     *
     * @return 用户列表
     */
    public List<User> getAllRegisteredUsers() {
        return userDAO.findAll();
    }
}
