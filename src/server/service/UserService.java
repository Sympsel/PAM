package server.service;

import common.entity.User;
import common.enums.Permission;
import common.manager.UserManager;
import common.dto.response.Result;
import common.exception.BusinessException;

/**
 * 用户服务类 - 服务层
 * 提供用户相关的业务服务，封装 Manager 层
 */
public class UserService {

    private final UserManager userManager;

    public UserService() {
        this.userManager = UserManager.getInstance();
    }

    /**
     * 用户注册
     *
     * @param username 用户名
     * @param password 密码
     * @param realName 真实姓名
     * @param phone    手机号
     * @param address  地址
     * @return 操作结果（返回用户ID）
     */
    public Result<String> register(String username, String password, String realName,
                                   String phone, String address) {
        try {
            // 1. 验证参数
            if (username == null || username.trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            if (password == null || password.isEmpty()) {
                return Result.error("密码不能为空");
            }
            if (password.length() < 6) {
                return Result.error("密码长度不能少于6位");
            }

            // 2. 创建个人资料
            User.Profile profile = new User.Profile(
                    realName != null ? realName : "未知",
                    phone != null ? phone : "未知",
                    address != null ? address : "未知"
            );

            // 3. 调用 Manager 层注册用户（默认权限为 NORMAL）
            String userId = userManager.register(username, password, Permission.NORMAL, profile);

            if (userId != null) {
                return Result.success(userId);
            } else {
                return Result.error("用户名已存在或注册失败");
            }
        } catch (Exception e) {
            return Result.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 管理员注册用户（可以指定权限）
     *
     * @param adminId    管理员ID
     * @param username   用户名
     * @param password   密码
     * @param realName   真实姓名
     * @param phone      手机号
     * @param address    地址
     * @param permission 权限
     * @return 操作结果
     */
    public Result<String> registerByAdmin(String adminId, String username, String password,
                                          String realName, String phone, String address,
                                          Permission permission) {
        try {
            // 1. 验证管理员权限
            validateAdminPermission(adminId);

            // 2. 验证参数
            if (username == null || username.trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            if (password == null || password.isEmpty()) {
                return Result.error("密码不能为空");
            }

            // 3. 创建个人资料
            User.Profile profile = new User.Profile(
                    realName != null ? realName : "未知",
                    phone != null ? phone : "未知",
                    address != null ? address : "未知"
            );

            // 4. 调用 Manager 层注册用户
            String userId = userManager.register(username, password, permission, profile);

            if (userId != null) {
                return Result.success(userId);
            } else {
                return Result.error("用户名已存在或注册失败");
            }
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            return Result.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 操作结果（返回用户对象）
     */
    public Result<User> login(String username, String password) {
        try {
            // 1. 验证参数
            if (username == null || username.trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            if (password == null || password.isEmpty()) {
                return Result.error("密码不能为空");
            }

            // 2. 调用 Manager 层登录（内部已经设置了在线状态）
            String userId = userManager.login(username, password);

            if (userId != null) {
                User user = userManager.getUserById(userId);
                return Result.success(user);
            } else {
                return Result.error("用户名或密码错误");
            }
        } catch (Exception e) {
            return Result.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 用户登出
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    public Result<String> logout(String userId) {
        try {
            if (userId == null || userId.isEmpty()) {
                return Result.error("用户ID不能为空");
            }

            // 设置离线状态
            userManager.logout(userId);
            return Result.success("登出成功");
        } catch (Exception e) {
            return Result.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 修改个人信息
     *
     * @param userId  用户ID
     * @param profile 新的个人资料
     * @return 操作结果
     */
    public Result<String> updateProfile(String userId, User.Profile profile) {
        try {
            // 1. 验证参数
            if (userId == null || userId.isEmpty()) {
                return Result.error("用户ID不能为空");
            }
            if (profile == null) {
                return Result.error("个人资料不能为空");
            }

            // 2. 查询用户是否存在
            User user = userManager.getUserById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 3. 更新个人资料
            user.setProfile(profile);
            boolean success = userManager.updateUser(user);

            if (success) {
                return Result.success("个人信息更新成功");
            } else {
                return Result.error("个人信息更新失败");
            }
        } catch (Exception e) {
            return Result.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 修改密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 操作结果
     */
    public Result<String> changePassword(String userId, String oldPassword, String newPassword) {
        try {
            // 1. 验证参数
            if (userId == null || userId.isEmpty()) {
                return Result.error("用户ID不能为空");
            }
            if (oldPassword == null || oldPassword.isEmpty()) {
                return Result.error("旧密码不能为空");
            }
            if (newPassword == null || newPassword.isEmpty()) {
                return Result.error("新密码不能为空");
            }
            if (newPassword.length() < 6) {
                return Result.error("新密码长度不能少于6位");
            }

            // 2. 验证旧密码
            User user = userManager.getUserById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // TODO: 验证旧密码是否正确
            // if (!PasswordEncoder.matches(oldPassword, user.getPassword())) {
            //     return Result.error("旧密码错误");
            // }

            // 3. 更新密码
            user.setPassword(newPassword);
            boolean success = userManager.updateUser(user);

            if (success) {
                return Result.success("密码修改成功");
            } else {
                return Result.error("密码修改失败");
            }
        } catch (Exception e) {
            return Result.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 根据 ID 查询用户信息
     *
     * @param userId 用户ID
     * @return 用户对象
     */
    public Result<User> getUserById(String userId) {
        try {
            if (userId == null || userId.isEmpty()) {
                return Result.error("用户ID不能为空");
            }

            User user = userManager.getUserById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("查询用户失败: " + e.getMessage());
        }
    }

    /**
     * 查询所有用户（管理员）
     *
     * @param adminId 管理员ID
     * @return 用户列表
     */
    public Result<java.util.List<User>> getAllUsers(String adminId) {
        try {
            // 1. 验证管理员权限
            validateAdminPermission(adminId);

            // 2. 查询所有用户
            java.util.List<User> users = userManager.getAllRegisteredUsers();
            return Result.success(users);
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            return Result.error("查询用户失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户（管理员）
     *
     * @param adminId 管理员ID
     * @param userId  要删除的用户ID
     * @return 操作结果
     */
    public Result<String> deleteUser(String adminId, String userId) {
        try {
            // 1. 验证管理员权限
            validateAdminPermission(adminId);

            // 2. 不能删除自己
            if (adminId.equals(userId)) {
                return Result.error("不能删除自己的账户");
            }

            // 3. 先登出用户（如果在线）
            userManager.logout(userId);

            // 4. 从数据库删除用户
            // TODO: UserManager 需要添加 deleteUser 方法
            // boolean success = userManager.deleteUser(userId);

            // 临时方案：直接返回错误
            return Result.error("删除用户功能尚未实现，需要在 UserManager 中添加 deleteUser 方法");
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            return Result.error("系统错误: " + e.getMessage());
        }
    }

    /**
     * 获取用户统计信息（管理员）
     *
     * @param adminId 管理员ID
     * @return 统计数据
     */
    public Result<UserStatistics> getUserStatistics(String adminId) {
        try {
            // 1. 验证管理员权限
            validateAdminPermission(adminId);

            // 2. 获取所有用户
            java.util.List<User> allUsers = userManager.getAllRegisteredUsers();
            int total = allUsers.size();

            long adminCount = allUsers.stream()
                    .filter(u -> u.getPermission() == Permission.ADMIN)
                    .count();

            long normalCount = allUsers.stream()
                    .filter(u -> u.getPermission() == Permission.NORMAL)
                    .count();

            UserStatistics stats = new UserStatistics(
                    total,
                    (int) adminCount,
                    (int) normalCount
            );
            return Result.success(stats);
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            return Result.error("统计用户失败: " + e.getMessage());
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

    /**
         * 用户统计数据
         */
        public record UserStatistics(int total, int adminCount, int normalCount) {
    }
}
