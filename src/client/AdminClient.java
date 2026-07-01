package client;

import common.entity.AdoptionApplication;
import common.entity.Announcement;
import common.entity.Pet;
import common.enums.PetSpecies;
import common.utils.Menu;
import server.service.AdoptionApplicationService;
import server.service.AnnouncementService;
import server.service.PetService;
import server.service.UserService;
import common.dto.response.Result;

import java.io.IOException;
import java.util.List;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminClient extends Client {
    private enum HandingStatus {
        Comment,
        Choose
    }

    private static final Logger logger = LoggerFactory.getLogger(AdminClient.class.getName());

    Terminal terminal;
    StringsCompleter completer = new StringsCompleter(
            java.util.Arrays.asList("help", "hello", "world", "exit", "quit", "status")
    );
    LineReader reader;

    // Service 层实例
    private final UserService userService = new UserService();
    private final AdoptionApplicationService applicationService = new AdoptionApplicationService();
    private final AnnouncementService announcementService = new AnnouncementService();
    private final PetService petService = new PetService();

    private String adminId;
    private String message;
    private HandingStatus handingStatus;

    public AdminClient() {
        status = Status.LOGGING;
        handingStatus = HandingStatus.Comment;
        try {
            terminal = TerminalBuilder.builder().build();
            reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(completer)
                    .build();
        } catch (IOException e) {
            logger.warn("无法初始化终端: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Menu createMainMenu() {
        Menu mainMenu = new Menu("主菜单", reader, terminal);
        mainMenu.addItem(1, "申领请求管理", this::showApplicationMenu)
                .addItem(2, "宠物管理", this::showPetMenu)
                .addItem(3, "公告管理", this::showAnnouncementMenu)
                .addItem(4, "用户管理", this::showUserMenu)
                .addItem(5, "系统统计", this::showStatisticsMenu)
                .addItem(6, "退出登录", () -> {
                    logger.info("退出登录");
                    status = Status.EXIT;
                });
        return mainMenu;
    }

    private void showStatisticsMenu() {
        Menu statisticsMenu = new Menu("系统统计", reader, terminal);
        statisticsMenu.addItem(1, "综合统计概览", () -> {
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

        });
        statisticsMenu.addBackItem();
        statisticsMenu.run();
    }

    private void showUserMenu() {
        Menu userMenu = new Menu("用户管理", reader, terminal);
        userMenu.addItem(1, "查看所有用户", () -> {
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
        });
        userMenu.addItem(2, "用户统计", () -> {
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
        });
        userMenu.addBackItem();
        userMenu.run();
    }

    private void showAnnouncementMenu() {
        Menu announcementMenu = new Menu("公告管理", reader, terminal);
        announcementMenu.addItem(1, "发布新公告", () -> {
            String title = "";
            String content = "";
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
                logger.warn("发布公告失败: " + e.getMessage());
                shouldEndShow = false;
            } finally {
                if (shouldEndShow) {
                    endShow();
                }
            }
        });
        announcementMenu.addItem(2, "查看所有公告", () -> {
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
        });
        announcementMenu.addItem(3, "查看公告详情", this::queryAnnouncementById);
        announcementMenu.addItem(4, "筛选公告", this::queryApplicationsByStatus);
        announcementMenu.addItem(5, "修改公告", () -> {
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
        });
        announcementMenu.addItem(6, "删除公告", () -> {
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
        }).addBackItem();
        announcementMenu.run();
    }

    private void showApplicationMenu() {
        Menu application = new Menu("申领请求管理", reader, terminal);
        application.addItem(1, "查看所有申领", () -> {
            Result<List<AdoptionApplication>> result = applicationService.getAllApplications();
            showApplicationList(result, "所有申领请求");
        });
        application.addItem(2, "查看指定申请", this::queryApplicationById);
        application.addItem(3, "处理待审核申请", () -> {
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
                while (handingStatus == HandingStatus.Comment) {
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
                                handingStatus = HandingStatus.Comment;
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
                            logger.info("输入流结束");
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
                    handingStatus = HandingStatus.Choose;
                }

                // 执行审核
                if (handingStatus == HandingStatus.Choose) {
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

                    handingStatus = HandingStatus.Comment;
                    try {
                        terminal.writer().println("处理完成，按回车继续");
                        terminal.flush();
                        String line = reader.readLine();
                        if (line == null) {
                            logger.info("输入流结束");
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
        });
        application.addBackItem();
        application.run();
    }

    /**
     * 按状态查询申请
     */
    private void queryApplicationsByStatus() {
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
        showApplicationList(result, title);
    }

    /**
     * 查询申请详情
     */
    private void queryApplicationById() {
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

    /**
     * 显示申请列表
     */
    private void showApplicationList(Result<List<AdoptionApplication>> result, String title) {
        if (result.isSuccess()) {
            List<AdoptionApplication> applications = result.getData();
            System.out.println("========== " + title + " ==========");
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

    private void showPetMenu() {
        Menu petMenu = new Menu("宠物管理", reader, terminal);
        petMenu.addItem(1, "查看所有宠物", () -> {
            Result<List<Pet>> result = petService.getAllPets();
            showPetList(result);
        });

        petMenu.addItem(2, "添加新宠物", () -> {
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
                        terminal.writer().println("无效选项");
                        terminal.flush();
                        return;
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
        });

        petMenu.addItem(3, "删除宠物", () -> {
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
        });

        petMenu.addItem(4, "宠物统计", () -> {
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
        });
        petMenu.addBackItem();
        petMenu.run();
    }

    /**
     * 显示宠物列表
     */
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

    /**
     * 查询公告详情
     */
    private void queryAnnouncementById() {
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

    private void handle() {
        try {
            // 延迟等待启动日志打完
            Thread.sleep(500);
        } catch (InterruptedException e) {
            logger.warn("等待被中断");
        }
        Menu mainMenu = createMainMenu();
        mainMenu.run();
    }


    @Override
    protected void login() {
        beginShow("login");
        try {
            String username = reader.readLine("请输入管理员用户名：");
            if (username == null) {
                return;
            }

            String password = reader.readLine("请输入密码：", '*');
            if (password == null) {
                return;
            }

            Result<common.entity.User> loginResult = userService.login(username, password);

            if (loginResult.isSuccess()) {
                common.entity.User user = loginResult.getData();
                this.adminId = user.getId();

                // 验证是否为管理员
                if (user.getPermission() != common.enums.Permission.ADMIN) {
                    logger.warn("该用户不是管理员，禁止登录");
                    terminal.writer().println("该用户不是管理员，禁止登录");
                    terminal.flush();
                    tryTimes++;
                    if (tryTimes >= 3) {
                        logger.warn("登录失败次数过多，退出系统");
                        status = Status.FORBIDDEN;
                    }
                    return;
                }

                logger.info("欢迎管理员: {}", user.getUsername());
                status = Status.ONLINE;
            } else {
                logger.warn("登录失败：{}", loginResult.getMessage());
                terminal.writer().println("登录失败: " + loginResult.getMessage());
                terminal.flush();
                tryTimes++;
                if (tryTimes >= 3) {
                    logger.warn("登录失败次数过多，退出系统");
                    status = Status.FORBIDDEN;
                }
            }
        } catch (Exception e) {
            logger.warn("登录时发生错误: {}", e.getMessage());
        }
        endShow();
    }

    @Override
    public void start() {
        while (status != Status.EXIT) {
            switch (status) {
                case LOGGING -> login();
                case ONLINE -> handle();
            }
        }
        exit();
    }

    @Override
    public void exit() {
        try {
            // 登出
            if (adminId != null) {
                userService.logout(adminId);
            }

            if (terminal != null) {
                terminal.close();
            }
        } catch (Exception e) {
            logger.warn("关闭终端失败: {}", e.getMessage());
        }
    }


    private void beginShow(String message) {
        this.message = message;
        System.out.println("========= " + message + " =========");
    }

    private void endShow() {
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
