package server.api;

import common.dto.response.Result;
import common.entity.AdoptionApplication;
import common.entity.Announcement;
import common.entity.Pet;
import common.entity.User;
import common.enums.Permission;
import common.enums.PetSpecies;
import common.exception.BusinessException;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.service.AdoptionApplicationService;
import server.service.AnnouncementService;
import server.service.PetService;
import server.service.UserService;

import java.io.IOException;
import java.util.List;

/**
 *
 * todo 添加网络中间层，客户端发送HTTP请求到服务端调用服务端 api/*
 */
public class ServerApi {
    private enum HandingStatus {
        Comment,
        Choose
    }

    protected static final Logger logger = LoggerFactory.getLogger(ServerApi.class.getName());
    protected Terminal terminal;
    protected StringsCompleter completer = new StringsCompleter(
            java.util.Arrays.asList("help", "exit", "status")
    );
    private String message;
    protected LineReader reader;

    private final UserService userService = new UserService();
    private final AdoptionApplicationService applicationService = new AdoptionApplicationService();
    private final AnnouncementService announcementService = new AnnouncementService();
    private final PetService petService = new PetService();
    private ServerApi.HandingStatus handingStatus;

    public ServerApi() {
        handingStatus = ServerApi.HandingStatus.Comment;
        try {
            terminal = TerminalBuilder.builder().build();
        } catch (IOException e) {
            logger.error("终端创建异常: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(completer)
                .build();
    }

    /**
     * 用户注册接口
     */
    public Result<String> register(String username, String password, String phone, String address) {
        try {
            return userService.register(username, password, null, phone, address);
        } catch (Exception e) {
            logger.error("注册异常: {}", e.getMessage());
            return Result.error("注册失败: " + e.getMessage());
        }
    }

    /**
     * 用户注册接口（带姓名）
     */
    public Result<String> register(String username, String password, String realName, String phone, String address) {
        try {
            return userService.register(username, password, realName, phone, address);
        } catch (Exception e) {
            logger.error("注册异常: {}", e.getMessage());
            return Result.error("注册失败: " + e.getMessage());
        }
    }

    /**
     * 用户自我注销账户（无需管理员权限）
     */
    public Result<String> deleteAccount(String userId) {
        try {
            if (userId == null || userId.isEmpty()) {
                return Result.error("用户ID为空");
            }
            return userService.deleteAccount(userId);
        } catch (Exception e) {
            logger.error("账户注销异常: {}", e.getMessage());
            return Result.error("账户注销失败: " + e.getMessage());
        }
    }

    /**
     * 统一登录接口（管理员和普通用户共用）
     */
    public Result<User> login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (password == null || password.isEmpty()) {
            return Result.error("密码不能为空");
        }

        try {
            Result<User> result = userService.login(username.trim(), password);
            if (result.isSuccess()) {
                User user = result.getData();
                logger.info("用户 {} 登录成功，权限: {}", user.getUsername(), user.getPermission());
            } else {
                logger.warn("用户 {} 登录失败: {}", username, result.getMessage());
            }
            return result;
        } catch (Exception e) {
            logger.error("登录异常: {}", e.getMessage());
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 统一登出接口
     */
    public Result<String> logout(String userId) {
        if (userId == null || userId.isEmpty()) {
            return Result.error("用户ID为空");
        }

        try {
            Result<String> result = userService.logout(userId);
            if (result.isSuccess()) {
                logger.info("用户 {} 已登出", userId);
            }
            return result;
        } catch (Exception e) {
            logger.error("登出异常: {}", e.getMessage());
            return Result.error("登出失败: " + e.getMessage());
        }
    }

    /**
     * 统一的管理员权限验证
     */
    protected User validateAdmin(String userId) throws BusinessException {
        if (userId == null || userId.isEmpty()) {
            throw new BusinessException(401, "未登录或会话已过期");
        }

        Result<User> userResult = userService.getUserById(userId);
        if (!userResult.isSuccess()) {
            throw new BusinessException(401, "用户不存在: " + userResult.getMessage());
        }

        User user = userResult.getData();
        if (user.getPermission() != Permission.ADMIN) {
            logger.warn("非管理员尝试执行管理操作: {} ({})", user.getUsername(), userId);
            throw new BusinessException(403, "无权限执行此操作，需要管理员权限");
        }

        return user;
    }

    protected void checkAdmin(String userId) throws BusinessException {
        validateAdmin(userId);
    }

    // ======================= 宠物管理

    public Result<List<Pet>> getAllPets() {
        try {
            return petService.getAllPets();
        } catch (Exception e) {
            logger.error("查询所有宠物失败: {}", e.getMessage());
            return Result.error("查询宠物失败: " + e.getMessage());
        }
    }

    public Result<Pet> getPetById(String petId) {
        try {
            return petService.getPetById(petId);
        } catch (Exception e) {
            logger.error("查询宠物失败: {}", e.getMessage());
            return Result.error("查询宠物失败: " + e.getMessage());
        }
    }

    public Result<List<Pet>> getAvailablePets() {
        try {
            return petService.getAvailablePets();
        } catch (Exception e) {
            logger.error("查询可领养宠物失败: {}", e.getMessage());
            return Result.error("查询宠物失败: " + e.getMessage());
        }
    }

    public Result<String> addPet(String adminId, String name, PetSpecies specie, int age, String description) {
        try {
            validateAdmin(adminId);
            return petService.addPet(adminId, name, specie, age, description);
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("添加宠物失败: {}", e.getMessage());
            return Result.error("添加宠物失败: " + e.getMessage());
        }
    }

    public Result<String> updatePet(String adminId, String petId, String name, PetSpecies specie, int age, String description) {
        try {
            validateAdmin(adminId);
            return petService.updatePet(adminId, petId, name, specie, age, description);
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("修改宠物失败: {}", e.getMessage());
            return Result.error("修改宠物失败: " + e.getMessage());
        }
    }

    public Result<String> deletePet(String adminId, String petId) {
        try {
            validateAdmin(adminId);
            return petService.deletePet(adminId, petId);
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("删除宠物失败: {}", e.getMessage());
            return Result.error("删除宠物失败: " + e.getMessage());
        }
    }

    public Result<Integer> getPetCount() {
        try {
            return petService.getPetCount();
        } catch (Exception e) {
            logger.error("统计宠物失败: {}", e.getMessage());
            return Result.error("统计宠物失败: " + e.getMessage());
        }
    }

    // ======================= 公告管理

    public Result<List<Announcement>> getAllAnnouncements() {
        try {
            return announcementService.getAllAnnouncements();
        } catch (Exception e) {
            logger.error("查询公告失败: {}", e.getMessage());
            return Result.error("查询公告失败: " + e.getMessage());
        }
    }

    public Result<Announcement> getAnnouncementById(String id) {
        try {
            return announcementService.getAnnouncementById(id);
        } catch (Exception e) {
            logger.error("查询公告失败: {}", e.getMessage());
            return Result.error("查询公告失败: " + e.getMessage());
        }
    }

    public Result<String> publishAnnouncement(String adminId, String title, String content) {
        try {
            validateAdmin(adminId);
            return announcementService.publishAnnouncement(adminId, title, content);
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("发布公告失败: {}", e.getMessage());
            return Result.error("发布公告失败: " + e.getMessage());
        }
    }

    public Result<String> updateAnnouncement(String id, String title, String content, String adminId) {
        try {
            validateAdmin(adminId);
            return announcementService.updateAnnouncement(id, title, content, adminId);
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("修改公告失败: {}", e.getMessage());
            return Result.error("修改公告失败: " + e.getMessage());
        }
    }

    public Result<String> deleteAnnouncement(String id, String adminId) {
        try {
            validateAdmin(adminId);
            return announcementService.deleteAnnouncement(id, adminId);
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("删除公告失败: {}", e.getMessage());
            return Result.error("删除公告失败: " + e.getMessage());
        }
    }

    // ======================= 领养申请

    public Result<List<AdoptionApplication>> getAllApplications() {
        try {
            return applicationService.getAllApplications();
        } catch (Exception e) {
            logger.error("查询所有申请失败: {}", e.getMessage());
            return Result.error("查询申请失败: " + e.getMessage());
        }
    }

    public Result<AdoptionApplication> getApplicationById(String id) {
        try {
            return applicationService.getApplicationById(id);
        } catch (Exception e) {
            logger.error("查询申请失败: {}", e.getMessage());
            return Result.error("查询申请失败: " + e.getMessage());
        }
    }

    public Result<List<AdoptionApplication>> getPendingApplications() {
        try {
            return applicationService.getPendingApplications();
        } catch (Exception e) {
            logger.error("查询待审核申请失败: {}", e.getMessage());
            return Result.error("查询申请失败: " + e.getMessage());
        }
    }

    public Result<List<AdoptionApplication>> getApprovedApplications() {
        try {
            return applicationService.getApprovedApplications();
        } catch (Exception e) {
            logger.error("查询已通过申请失败: {}", e.getMessage());
            return Result.error("查询申请失败: " + e.getMessage());
        }
    }

    public Result<List<AdoptionApplication>> getRejectedApplications() {
        try {
            return applicationService.getRejectedApplications();
        } catch (Exception e) {
            logger.error("查询已拒绝申请失败: {}", e.getMessage());
            return Result.error("查询申请失败: " + e.getMessage());
        }
    }

    public Result<List<AdoptionApplication>> getApplicationsByApplicator(String applicatorId) {
        try {
            return applicationService.getApplicationsByApplicator(applicatorId);
        } catch (Exception e) {
            logger.error("查询用户申请失败: {}", e.getMessage());
            return Result.error("查询申请失败: " + e.getMessage());
        }
    }

    public Result<String> submitApplication(String applicatorId, String petId) {
        return applicationService.submitApplication(applicatorId, petId);
    }

    public Result<String> approveApplication(String adminId, String applicationId, String comment) {
        try {
            validateAdmin(adminId);
            return applicationService.approveApplication(adminId, applicationId, comment);
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("审核申请失败: {}", e.getMessage());
            return Result.error("审核申请失败: " + e.getMessage());
        }
    }

    public Result<String> rejectApplication(String adminId, String applicationId, String comment) {
        try {
            validateAdmin(adminId);
            return applicationService.rejectApplication(adminId, applicationId, comment);
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("拒绝申请失败: {}", e.getMessage());
            return Result.error("拒绝申请失败: " + e.getMessage());
        }
    }

    public Result<String> postponeApplication(String adminId, String applicationId) {
        try {
            validateAdmin(adminId);
            return applicationService.postponeApplication(adminId, applicationId);
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("搁置申请失败: {}", e.getMessage());
            return Result.error("搁置申请失败: " + e.getMessage());
        }
    }

    public Result<server.service.AdoptionApplicationService.ApplicationStatistics> getApplicationStatistics() {
        try {
            return applicationService.getApplicationStatistics();
        } catch (Exception e) {
            logger.error("统计申请失败: {}", e.getMessage());
            return Result.error("统计申请失败: " + e.getMessage());
        }
    }

    // ======================= 用户管理

    public Result<List<User>> getAllUsers(String adminId) {
        try {
            validateAdmin(adminId);
            return userService.getAllUsers(adminId);
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("查询用户列表失败: {}", e.getMessage());
            return Result.error("查询用户失败: " + e.getMessage());
        }
    }

    public Result<String> deleteUser(String adminId, String userId) {
        try {
            validateAdmin(adminId);
            return userService.deleteUser(adminId, userId);
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("删除用户失败: {}", e.getMessage());
            return Result.error("删除用户失败: " + e.getMessage());
        }
    }

    public Result<server.service.UserService.UserStatistics> getUserStatistics(String adminId) {
        try {
            validateAdmin(adminId);
            return userService.getUserStatistics(adminId);
        } catch (BusinessException e) {
            return Result.error(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            logger.error("统计用户失败: {}", e.getMessage());
            return Result.error("统计用户失败: " + e.getMessage());
        }
    }

    public Result<User> getUserById(String userId) {
        try {
            return userService.getUserById(userId);
        } catch (Exception e) {
            logger.error("查询用户失败: {}", e.getMessage());
            return Result.error("查询用户失败: " + e.getMessage());
        }
    }

    public Result<String> changePassword(String userId, String oldPassword, String newPassword) {
        try {
            return userService.changePassword(userId, oldPassword, newPassword);
        } catch (Exception e) {
            logger.error("修改密码失败: {}", e.getMessage());
            return Result.error("修改密码失败: " + e.getMessage());
        }
    }

    // =================== 用户状态
    public void exit(String userId) {
        logger.info("退出登录");
        try {
            // 登出
            if (userId != null) {
                userService.logout(userId);
            }
        } catch (Exception e) {
            logger.warn("关闭终端失败: {}", e.getMessage());
        }
    }


    // =================== 用户管理
    public void deleteUser(String adminId) {
        try {
            User admin = validateAdmin(adminId);  // 统一验证
            logger.info("管理员 {} 开始添加宠物", admin.getUsername());

            beginShow("添加宠物");
            // ... 业务逻辑
        } catch (BusinessException e) {
            System.out.println("权限验证失败: " + e.getMessage());
        }
        // 先显示用户列表供选择
        Result<List<common.entity.User>> listResult = userService.getAllUsers(adminId);
        if (!listResult.isSuccess()) {
            System.out.println("查询用户列表失败: " + listResult.getMessage());
            return;
        }

        List<common.entity.User> users = listResult.getData();
        if (users.isEmpty()) {
            System.out.println("暂无用户");
            return;
        }

        System.out.println("========== 选择要删除的用户 ==========");
        for (int i = 0; i < users.size(); i++) {
            common.entity.User u = users.get(i);
            System.out.printf("[%d] %s (%s)%s\n",
                    i + 1, u.getUsername(), u.getPermission(),
                    u.getId().equals(adminId) ? " ← 当前账户" : "");
        }
        System.out.println("[0] 取消");
        System.out.println("======================================");

        String input = reader.readLine("请选择要删除的用户编号：");
        if (input == null || input.trim().isEmpty()) return;

        int choice;
        try {
            choice = Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            System.out.println("无效输入");
            return;
        }

        if (choice == 0) return;
        if (choice < 1 || choice > users.size()) {
            System.out.println("无效选项");
            return;
        }

        common.entity.User targetUser = users.get(choice - 1);

        if (targetUser.getId().equals(adminId)) {
            System.out.println("不能删除自己的账户，已返回");
            return;
        }

        String confirm = reader.readLine("确认删除用户 " + targetUser.getUsername() + "？(y/n)：");
        if (confirm == null || !confirm.trim().equalsIgnoreCase("y")) {
            System.out.println("已取消");
            return;
        }

        Result<String> result = userService.deleteUser(adminId, targetUser.getId());
        if (result.isSuccess()) {
            System.out.println(result.getMessage());
        } else {
            System.out.println("删除失败: " + result.getMessage());
        }
    }

    public void showAllUser(String adminId) {
        Result<List<common.entity.User>> result = userService.getAllUsers(adminId);
        if (result.isSuccess()) {
            List<common.entity.User> users = result.getData();
            System.out.println("========== 用户列表 ==========");
            if (users.isEmpty()) {
                System.out.println("暂无用户");
            } else {
                for (int i = 0; i < users.size(); i++) {
                    common.entity.User user = users.get(i);
                    System.out.printf("[%d] %s\n", i + 1, user.getDisplayString());
                }
            }
            System.out.println("总计: " + users.size() + " 人");
            System.out.println("==============================");
        } else {
            System.out.println("查询失败: " + result.getMessage());
        }
    }

    public void showUserStatistics(String adminId) {
        try {
            User admin = validateAdmin(adminId);  // 统一验证
            logger.info("管理员 {} 开始添加宠物", admin.getUsername());

            beginShow("添加宠物");
            // ... 业务逻辑
        } catch (BusinessException e) {
            System.out.println("权限验证失败: " + e.getMessage());
        }
        Result<server.service.UserService.UserStatistics> result = userService.getUserStatistics(adminId);
        if (result.isSuccess()) {
            server.service.UserService.UserStatistics stats = result.getData();
            System.out.println("========== 用户统计 ==========");
            System.out.println("总用户数: " + stats.getTotal());
            System.out.println("管理员: " + stats.getAdminCount());
            System.out.println("普通用户: " + stats.getNormalCount());
            System.out.println("==============================");
        } else {
            System.out.println("统计失败: " + result.getMessage());
        }
    }

    // ======================= 申请

    public void applicationHandler(String adminId) {
        int op = -1;
        String comment = "";

        while (true) {
            // 获取待审核申请
            Result<List<AdoptionApplication>> result = applicationService.getPendingApplications();
            if (!result.isSuccess() || result.getData().isEmpty()) {
                System.out.println("暂无待审核的申请");
                break;
            }

            List<AdoptionApplication> pendingApps = result.getData();
            AdoptionApplication currentApp = pendingApps.getFirst();

            // 显示当前申请详情
            System.out.println("========== 当前申请 ==========");
            System.out.println("申请ID: " + currentApp.getId());
            System.out.println("申请人: " + currentApp.getApplicatorId());
            System.out.println("宠物: " + currentApp.getPetId());

            // 显示宠物信息
            Result<Pet> petResult = petService.getPetById(currentApp.getPetId());
            if (petResult.isSuccess()) {
                Pet pet = petResult.getData();
                System.out.println("宠物名称: " + pet.getName());
                System.out.println("物种: " + pet.getSpecie());
                System.out.println("年龄: " + pet.getAge() + "岁");
            }
            System.out.println("==============================");

            // 选择操作
            while (handingStatus == ServerApi.HandingStatus.Comment) {
                boolean flag = true;
                while (flag) {
                    flag = false;
                    try {
                        System.out.println("[== 1. 接受该条 2. 拒绝该条 3. 搁置该条 ==]");
                        String line = reader.readLine(">> ");

                        op = Integer.parseInt(line.trim());
                        if (op == 3) {
                            // 搁置
                            applicationService.postponeApplication(adminId, currentApp.getId());
                            System.out.println("已搁置该申请");
                            handingStatus = ServerApi.HandingStatus.Comment;
                            break;
                        } else if (op != 1 && op != 2) {
                            terminal.writer().println("无效输入，请重新输入");
                            terminal.flush();
                            flag = true;
                        }
                    } catch (NumberFormatException e) {
                        terminal.writer().println("请输入有效的数字");
                        terminal.flush();
                        flag = true;
                    } catch (UserInterruptException e) {
                        logger.info("用户中断");
                        return;
                    } catch (EndOfFileException e) {
                        logger.info("输入结束");
                        return;
                    } catch (Exception e) {
                        logger.warn("读取输入失败: {}", e.getMessage());
                        return;
                    }
                }

                if (op == 3) {
                    // 搁置后继续下一个
                    continue;
                }

                // 读取审核意见
                try {
                    terminal.writer().println("请输入审核意见（新行输入 EOF 结束）：");
                    terminal.flush();

                    StringBuilder commentBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.trim().equals("EOF")) {
                            break;
                        }
                        commentBuilder.append(line).append("\n");
                    }
                    comment = commentBuilder.toString().trim();

                    if (comment.isEmpty()) {
                        comment = "无";
                    }

                    logger.info("评论输入结束");
                } catch (UserInterruptException e) {
                    logger.info("用户中断");
                    return;
                } catch (EndOfFileException e) {
                    logger.info("输入流结束");
                    return;
                } catch (Exception e) {
                    logger.warn("读取评论失败: {}", e.getMessage());
                    return;
                }
                handingStatus = ServerApi.HandingStatus.Choose;
            }

            // 执行审核
            if (handingStatus == ServerApi.HandingStatus.Choose) {
                Result<String> reviewResult;
                if (op == 1) {
                    reviewResult = applicationService.approveApplication(adminId, currentApp.getId(), comment);
                } else {
                    reviewResult = applicationService.rejectApplication(adminId, currentApp.getId(), comment);
                }

                if (reviewResult.isSuccess()) {
                    System.out.println(reviewResult.getMessage());
                } else {
                    System.out.println("审核失败: " + reviewResult.getMessage());
                }

                handingStatus = ServerApi.HandingStatus.Comment;
                try {
                    terminal.writer().println("处理完成，按回车继续");
                    terminal.flush();
                    String line = reader.readLine();
                    if (line == null) {
                        logger.info("输入异常");
                        return;
                    }
                } catch (UserInterruptException | EndOfFileException e) {
                    logger.info("用户中断");
                    return;
                } catch (Exception e) {
                    logger.info("返回菜单");
                    return;
                }
            }
        }
    }

    public void queryApplicationById() {
        String id = reader.readLine("请输入申请ID：");
        if (id == null || id.trim().isEmpty()) return;

        Result<AdoptionApplication> result = applicationService.getApplicationById(id.trim());
        if (result.isSuccess()) {
            System.out.println("========== 申请详情 ==========");
            System.out.println(result.getData().getDisplayString());
            System.out.println("==============================");
        } else {
            System.out.println("查询失败: " + result.getMessage());
        }
    }

    public void queryApplicationsByStatus() {
        System.out.println("查询状态：1.待审核 2.已通过 3.已拒绝");
        String input = reader.readLine("请选择：");
        if (input == null || input.trim().isEmpty()) return;

        Result<List<AdoptionApplication>> result;
        String title;
        switch (input.trim()) {
            case "1" -> {
                result = applicationService.getPendingApplications();
                title = "待审核申请";
            }
            case "2" -> {
                result = applicationService.getApprovedApplications();
                title = "已通过申请";
            }
            case "3" -> {
                result = applicationService.getRejectedApplications();
                title = "已拒绝申请";
            }
            default -> {
                System.out.println("无效选项");
                return;
            }
        }
        showApplicationList(result);
    }

    private void showApplicationList(Result<List<AdoptionApplication>> result) {
        if (result.isSuccess()) {
            List<AdoptionApplication> applications = result.getData();
            System.out.println("========== " + "申领请求列表" + " ==========");
            if (applications.isEmpty()) {
                System.out.println("暂无申请");
            } else {
                for (int i = 0; i < applications.size(); i++) {
                    System.out.printf("[%d] %s\n\n", i + 1, applications.get(i).getDisplayString());
                }
            }
            System.out.println("总计: " + applications.size() + " 条");
            System.out.println("==============================");
        } else {
            System.out.println("查询失败: " + result.getMessage());
        }
    }

    public void showAllApplication() {
        Result<List<AdoptionApplication>> result = applicationService.getAllApplications();
        showApplicationList(result);
    }

    // ======================= 宠物

    public void showAllPets() {
        Result<List<Pet>> result = petService.getAllPets();
        showPetList(result);
    }

    private void showPetList(Result<List<Pet>> result) {
        if (result.isSuccess()) {
            List<Pet> pets = result.getData();
            System.out.println("========== " + "宠物列表" + " ==========");
            if (pets.isEmpty()) {
                System.out.println("暂无宠物");
            } else {
                for (int i = 0; i < pets.size(); i++) {
                    System.out.printf("[%d] %s\n\n", i + 1, pets.get(i).getDisplayString());
                }
            }
            System.out.println("总计: " + pets.size() + " 只");
            System.out.println("==============================");
        } else {
            System.out.println("查询失败: " + result.getMessage());
        }
    }

    public void addPets(String adminId) {
        try {
            User admin = validateAdmin(adminId);  // 统一验证
            logger.info("管理员 {} 开始添加宠物", admin.getUsername());

            beginShow("添加宠物");
            // ... 业务逻辑
        } catch (BusinessException e) {
            System.out.println("权限验证失败: " + e.getMessage());
        }
        beginShow("添加宠物");
        try {
            System.out.println("请输入宠物信息：");

            String name = "";
            while (name.isEmpty()) {
                name = reader.readLine("宠物名称：");
                if (name == null) {
                    logger.info("取消添加");
                    return;
                }
                name = name.trim();
            }

            System.out.println("请选择物种：1.狗 2.猫 3.其他");
            String specieInput = reader.readLine("物种（1/2/3）：");
            if (specieInput == null) return;

            String specie;
            switch (specieInput.trim()) {
                case "1" -> specie = "DOG";
                case "2" -> specie = "CAT";
                case "3" -> specie = "OTHER";
                default -> {
                    System.out.println("无效选项，默认为 OTHER 类型");
                    specie = "OTHER";
                }
            }

            String ageInput = reader.readLine("年龄：");
            if (ageInput == null) return;
            int age = Integer.parseInt(ageInput.trim());

            String description = reader.readLine("描述（可选）：");
            if (description == null) description = "无";

            petService.addPet(adminId, name, PetSpecies.valueOf(specie.toUpperCase()), age, description);

        } catch (Exception e) {
            logger.warn("添加宠物失败: {}", e.getMessage());
        } finally {
            endShow();
        }
    }

    public void modifyPetsInfo(String adminId) {
        try {
            User admin = validateAdmin(adminId);  // 统一验证
            logger.info("管理员 {} 开始添加宠物", admin.getUsername());

            beginShow("添加宠物");
            // ... 业务逻辑
        } catch (BusinessException e) {
            System.out.println("权限验证失败: " + e.getMessage());
        }
        Result<List<Pet>> listResult = petService.getAllPets();
        if (!listResult.isSuccess() || listResult.getData().isEmpty()) {
            System.out.println("暂无宠物可修改");
            return;
        }

        List<Pet> pets = listResult.getData();
        System.out.println("========== 选择要修改的宠物 ==========");
        for (int i = 0; i < pets.size(); i++) {
            Pet p = pets.get(i);
            System.out.printf("[%d] %s (%s, %d岁)\n",
                    i + 1, p.getName(), p.getSpecie(), p.getAge());
        }
        System.out.println("[0] 取消");
        System.out.println("======================================");

        String input = reader.readLine("请选择：");
        if (input == null || input.trim().isEmpty()) return;
        int choice;
        try {
            choice = Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            System.out.println("无效输入");
            return;
        }
        if (choice == 0) return;
        if (choice < 1 || choice > pets.size()) {
            System.out.println("无效选项");
            return;
        }

        Pet target = pets.get(choice - 1);
        System.out.println("\n当前信息：" + target.getDisplayString());

        try {
            String name = reader.readLine("新名称（回车保持 " + target.getName() + "）：");
            if (name == null || name.trim().isEmpty()) name = target.getName();
            else name = name.trim();

            System.out.println("当前物种：" + target.getSpecie() + "  1.狗 2.猫 3.其他（回车保持）");
            String specieInput = reader.readLine("物种：");
            PetSpecies specie = target.getSpecie();
            if (specieInput != null && !specieInput.trim().isEmpty()) {
                switch (specieInput.trim()) {
                    case "1" -> specie = PetSpecies.DOG;
                    case "2" -> specie = PetSpecies.CAT;
                    case "3" -> specie = PetSpecies.OTHER;
                    default -> System.out.println("无效选项，保持原物种");
                }
            }

            String ageInput = reader.readLine("新年龄（回车保持 " + target.getAge() + "）：");
            int age = target.getAge();
            if (ageInput != null && !ageInput.trim().isEmpty()) {
                age = Integer.parseInt(ageInput.trim());
            }

            String description = reader.readLine("新描述（回车保持原样）：");
            if (description == null || description.trim().isEmpty()) {
                description = target.getDescription();
            }

            Result<String> result = petService.updatePet(
                    adminId, target.getId(), name, specie, age, description);
            if (result.isSuccess()) {
                System.out.println(result.getMessage());
            } else {
                System.out.println("修改失败: " + result.getMessage());
            }
        } catch (NumberFormatException e) {
            System.out.println("年龄格式错误");
        } catch (Exception e) {
            logger.warn("修改宠物失败: {}", e.getMessage());
        }
    }

    public void deletePet(String adminId) {
        try {
            User admin = validateAdmin(adminId);  // 统一验证
            logger.info("管理员 {} 开始添加宠物", admin.getUsername());

            beginShow("添加宠物");
            // ... 业务逻辑
        } catch (BusinessException e) {
            System.out.println("权限验证失败: " + e.getMessage());
        }
        beginShow("删除宠物");
        try {
            String petId = reader.readLine("请输入要删除的宠物ID：");
            if (petId == null) {
                logger.info("取消删除");
                return;
            }

            Result<String> result = petService.deletePet(adminId, petId.trim());
            if (result.isSuccess()) {
                System.out.println("删除成功: " + result.getMessage());
            } else {
                System.out.println("删除失败: " + result.getMessage());
            }

        } catch (Exception e) {
            logger.warn("删除宠物失败: {}", e.getMessage());
        } finally {
            endShow();
        }
    }

    public void showPetStatistics() {
        // 宠物统计
        Result<Integer> petCountResult = petService.getPetCount();
        if (petCountResult.isSuccess()) {
            System.out.println("\n【宠物统计】");
            System.out.println("  宠物总数: " + petCountResult.getData());

            // 按状态统计
            Result<List<Pet>> allPets = petService.getAllPets();
            if (allPets.isSuccess()) {
                long available = allPets.getData().stream()
                        .filter(p -> p.getAdoptionStatus() == common.enums.PetStatus.AVAILABLE).count();
                long pending = allPets.getData().stream()
                        .filter(p -> p.getAdoptionStatus() == common.enums.PetStatus.PENDING).count();
                long adopted = allPets.getData().stream()
                        .filter(p -> p.getAdoptionStatus() == common.enums.PetStatus.ADOPTED).count();
                System.out.println("\t可领养: " + available);
                System.out.println("\t待审核: " + pending);
                System.out.println("\t已领养: " + adopted);
            }
        } else {
            System.out.println("\n【宠物统计】查询失败: " + petCountResult.getMessage());
        }
    }

    // ====================== 公告
    public void sendAnnouncement(String adminId) {
        try {
            User admin = validateAdmin(adminId);  // 统一验证
            logger.info("管理员 {} 开始添加宠物", admin.getUsername());

            beginShow("添加宠物");
            // ... 业务逻辑
        } catch (BusinessException e) {
            System.out.println("权限验证失败: " + e.getMessage());
        }
        String title = "";
        String content;
        beginShow("publish announce");

        boolean shouldEndShow = true;
        try {
            // 读取标题
            while (title.isEmpty()) {
                title = reader.readLine("请输入公告标题：");
                if (title == null) {
                    logger.info("取消发布");
                    shouldEndShow = false;
                    break;
                }
                title = title.trim();
                if (title.isEmpty()) {
                    terminal.writer().println("标题不能为空，请重新输入");
                    terminal.flush();
                }
            }

            if (!shouldEndShow) {
                return;
            }

            // 读取内容
            terminal.writer().println("请输入公告内容（新行输入 EOF 结束）：");
            terminal.flush();
            StringBuilder contentBuilder = new StringBuilder();
            String line;
            boolean hasContent = false;

            while ((line = reader.readLine()) != null) {
                if (line.trim().equals("EOF")) {
                    break;
                }
                if (!hasContent) {
                    if (!line.trim().isEmpty()) {
                        hasContent = true;
                        contentBuilder.append(line);
                    }
                } else {
                    contentBuilder.append("\n").append(line);
                }
            }

            content = contentBuilder.toString().trim();
            if (content.isEmpty()) {
                terminal.writer().println("公告内容为空，已取消发布");
                terminal.flush();
                logger.info("返回菜单");
                shouldEndShow = false;
                return;
            }
            logger.info("公告内容输入结束");

            // 调用 Service 层发布公告
            Result<String> result = announcementService.publishAnnouncement(adminId, title, content);
            if (result.isSuccess()) {
                terminal.writer().println("公告发布成功！");
            } else {
                terminal.writer().println("公告发布失败: " + result.getMessage());
            }
            terminal.flush();

            // 发布成功后返回菜单
        } catch (UserInterruptException e) {
            logger.info("用户中断");
            shouldEndShow = false;
        } catch (EndOfFileException e) {
            logger.info("输入结束");
            shouldEndShow = false;
        } catch (Exception e) {
            logger.warn("发布公告失败: {}", e.getMessage());
            shouldEndShow = false;
        } finally {
            if (shouldEndShow) {
                endShow();
            }
        }
    }

    public void showAllAnnouncement() {
        Result<List<Announcement>> result = announcementService.getAllAnnouncements();
        if (result.isSuccess()) {
            List<Announcement> announcements = result.getData();
            System.out.println("========== 公告列表 ==========");
            if (announcements.isEmpty()) {
                System.out.println("暂无公告");
            } else {
                for (int i = 0; i < announcements.size(); i++) {
                    System.out.printf("[%d] %s\n\n", i + 1, announcements.get(i).getDisplayString());
                }
            }
            System.out.println("总计: " + announcements.size() + " 条");
            System.out.println("==============================");
        } else {
            System.out.println("查询失败: " + result.getMessage());
        }
    }

    public void queryAnnouncementById() {
        String id = reader.readLine("请输入公告ID：");
        if (id == null || id.trim().isEmpty()) return;

        Result<Announcement> result = announcementService.getAnnouncementById(id.trim());
        if (result.isSuccess()) {
            System.out.println("========== 公告详情 ==========");
            System.out.println(result.getData().getDisplayString());
            System.out.println("==============================");
        } else {
            System.out.println("查询失败: " + result.getMessage());
        }
    }

    public void modifyAnnouncement(String adminId) {
        try {
            User admin = validateAdmin(adminId);  // 统一验证
            logger.info("管理员 {} 开始添加宠物", admin.getUsername());

            beginShow("添加宠物");
            // ... 业务逻辑
        } catch (BusinessException e) {
            System.out.println("权限验证失败: " + e.getMessage());
        }
        beginShow("修改公告");
        try {
            String id = reader.readLine("请输入要修改的公告ID：");
            if (id == null || id.trim().isEmpty()) return;

            // 先查询公告是否存在
            Result<Announcement> existResult = announcementService.getAnnouncementById(id.trim());
            if (!existResult.isSuccess()) {
                System.out.println("公告不存在: " + id);
                return;
            }

            Announcement existing = existResult.getData();
            System.out.println("当前标题: " + existing.getTitle());

            String title = reader.readLine("新标题（回车保持原样）：");
            if (title == null || title.trim().isEmpty()) {
                title = existing.getTitle();
            } else {
                title = title.trim();
            }

            terminal.writer().println("新内容（新行输入 EOF 结束，回车保持原样）：");
            terminal.flush();
            StringBuilder contentBuilder = new StringBuilder();
            String line;
            boolean hasContent = false;

            while ((line = reader.readLine()) != null) {
                if (line.trim().equals("EOF")) break;
                if (!hasContent) {
                    if (!line.trim().isEmpty()) {
                        hasContent = true;
                        contentBuilder.append(line);
                    }
                } else {
                    contentBuilder.append("\n").append(line);
                }
            }

            String content = contentBuilder.toString().trim();
            if (content.isEmpty()) {
                content = existing.getContent();
            }

            Result<String> result = announcementService.updateAnnouncement(id.trim(), title, content, adminId);
            if (result.isSuccess()) {
                System.out.println("修改成功: " + result.getMessage());
            } else {
                System.out.println("修改失败: " + result.getMessage());
            }

        } catch (UserInterruptException e) {
            logger.info("用户中断");
        } catch (EndOfFileException e) {
            logger.info("输入结束");
        } catch (Exception e) {
            logger.warn("修改公告失败: {}", e.getMessage());
        } finally {
            endShow();
        }
    }

    public void deleteAnnouncement(String adminId) {
        try {
            User admin = validateAdmin(adminId);  // 统一验证
            logger.info("管理员 {} 开始添加宠物", admin.getUsername());

            beginShow("添加宠物");
            // ... 业务逻辑
        } catch (BusinessException e) {
            System.out.println("权限验证失败: " + e.getMessage());
        }
        beginShow("删除公告");
        try {
            String id = reader.readLine("请输入要删除的公告ID：");
            if (id == null || id.trim().isEmpty()) return;

            Result<String> result = announcementService.deleteAnnouncement(id.trim(), adminId);
            if (result.isSuccess()) {
                System.out.println("删除成功: " + result.getMessage());
            } else {
                System.out.println("删除失败: " + result.getMessage());
            }

        } catch (Exception e) {
            logger.warn("删除公告失败: {}", e.getMessage());
        } finally {
            endShow();
        }
    }

    // ================== 综合统计
    public void showAllStatistics(String adminId) {
        try {
            User admin = validateAdmin(adminId);  // 统一验证
            logger.info("管理员 {} 开始添加宠物", admin.getUsername());

            beginShow("添加宠物");
            // ... 业务逻辑
        } catch (BusinessException e) {
            System.out.println("权限验证失败: " + e.getMessage());
        }
        System.out.println("========== 系统统计概览 ==========");

        // 用户统计
        Result<server.service.UserService.UserStatistics> userResult = userService.getUserStatistics(adminId);
        if (userResult.isSuccess()) {
            server.service.UserService.UserStatistics userStats = userResult.getData();
            System.out.println("\n【用户统计】");
            System.out.println("\t总用户数: " + userStats.getTotal());
            System.out.println("\t管理员: " + userStats.getAdminCount());
            System.out.println("\t普通用户: " + userStats.getNormalCount());
        } else {
            System.out.println("\n【用户统计】查询失败: " + userResult.getMessage());
        }

        // 宠物统计
        Result<Integer> petCountResult = petService.getPetCount();
        if (petCountResult.isSuccess()) {
            System.out.println("\n【宠物统计】");
            System.out.println("\t宠物总数: " + petCountResult.getData());

            // 按状态统计
            Result<List<Pet>> allPets = petService.getAllPets();
            if (allPets.isSuccess()) {
                long available = allPets.getData().stream()
                        .filter(p -> p.getAdoptionStatus() == common.enums.PetStatus.AVAILABLE).count();
                long pending = allPets.getData().stream()
                        .filter(p -> p.getAdoptionStatus() == common.enums.PetStatus.PENDING).count();
                long adopted = allPets.getData().stream()
                        .filter(p -> p.getAdoptionStatus() == common.enums.PetStatus.ADOPTED).count();
                System.out.println("\t可领养: " + available);
                System.out.println("\t待审核: " + pending);
                System.out.println("\t已领养: " + adopted);
            }
        } else {
            System.out.println("\n【宠物统计】查询失败: " + petCountResult.getMessage());
        }

        // 申请统计
        Result<server.service.AdoptionApplicationService.ApplicationStatistics> appResult =
                applicationService.getApplicationStatistics();
        if (appResult.isSuccess()) {
            server.service.AdoptionApplicationService.ApplicationStatistics appStats = appResult.getData();
            System.out.println("\n【申请统计】");
            System.out.println("\t申请总数: " + appStats.total());
            System.out.println("\t待审核: " + appStats.pending());
            System.out.println("\t已通过: " + appStats.approved());
            System.out.println("\t已拒绝: " + appStats.rejected());
        } else {
            System.out.println("\n【申请统计】查询失败: " + appResult.getMessage());
        }

        System.out.println("\n==================================");

    }


    public Result<String> updateProfile(String userId, String phone, String address) {
        Result<User> userResult = userService.getUserById(userId);
        if (!userResult.isSuccess()) {
            return Result.error(userResult.getMessage());
        }
        return userService.updateProfile(userId, new User.Profile(userResult.getData().getProfile().getRealName(), phone, address));
    }
    // ====================== 工具

    public void beginShow(String message) {
        this.message = message;
        System.out.println("========= " + message + " =========");
    }


    public void endShow() {
        if (message == null) {
            System.out.println("========= error =========");
            return;
        }
        System.out.print("========= ");
        for (int i = 0; i < message.length(); ++i) {
            System.out.print("=");
        }
        System.out.println(" =========");
        message = null;

    }
}