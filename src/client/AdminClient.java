package client;

import common.entity.AdoptionApplication;
import common.entity.Pet;
import server.service.AdoptionApplicationService;
import server.service.AnnouncementService;
import server.service.PetService;
import server.service.UserService;
import common.dto.response.Result;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class AdminClient extends Client {
    private enum OnlineStatus {
        Selecting,

        SelectAllApply,
        Waiting,
        Handling,
        SendAnnounce,
        Back
    }

    private enum HandingStatus {
        Comment,
        Choose
    }

    private static final Logger logger = Logger.getLogger(AdminClient.class.getName());

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
    private OnlineStatus onlineStatus;
    private HandingStatus handingStatus;

    public AdminClient() {
        status = Status.LOGGING;
        onlineStatus = OnlineStatus.Selecting;
        handingStatus = HandingStatus.Comment;
        try {
            terminal = TerminalBuilder.builder().build();
            reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(completer)
                    .build();
        } catch (IOException e) {
            logger.severe("无法初始化终端: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void showMenu() {
        beginShow("menu");
        System.out.println("1. 查询所有申领请求");
        System.out.println("2. 开始处理申领请求");
        System.out.println("3. 发布公告");
        System.out.println("4. 查看所有宠物");
        System.out.println("5. 查看用户列表");
        System.out.println("6. 退出");
        endShow();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            logger.warning("等待被中断");
        }
    }

    private void handle() {
        onlineStatus = OnlineStatus.Selecting;

        while (onlineStatus != OnlineStatus.Back) {
            switch (onlineStatus) {
                case Selecting -> {
                    showMenu();
                    int op;
                    try {
                        String line = reader.readLine("> ");
                        if (line == null) {
                            logger.info("输入流结束，退出系统");
                            status = Status.EXIT;
                            return;
                        }
                        line = line.trim();
                        if (line.isEmpty()) {
                            continue;
                        }
                        op = Integer.parseInt(line);
                        if (op == 1) {
                            onlineStatus = OnlineStatus.SelectAllApply;
                        } else if (op == 2) {
                            Result<List<AdoptionApplication>> result = applicationService.getPendingApplications();
                            if (result.isSuccess() && !result.getData().isEmpty()) {
                                onlineStatus = OnlineStatus.Handling;
                            } else {
                                onlineStatus = OnlineStatus.Waiting;
                            }
                        } else if (op == 3) {
                            onlineStatus = OnlineStatus.SendAnnounce;
                        } else if (op == 4) {
                            viewAllPets();
                        } else if (op == 5) {
                            viewAllUsers();
                        } else if (op == 6) {
                            logger.info("管理员退出登录");
                            onlineStatus = OnlineStatus.Back;
                            status = Status.EXIT;
                            return;
                        } else {
                            terminal.writer().println("非法输入，请重新输入");
                            terminal.flush();
                        }
                    } catch (NumberFormatException e) {
                        terminal.writer().println("请输入有效的数字");
                        terminal.flush();
                    } catch (UserInterruptException e) {
                        logger.info("用户中断，退出系统");
                        status = Status.EXIT;
                        return;
                    } catch (EndOfFileException e) {
                        logger.info("输入流结束，退出系统");
                        status = Status.EXIT;
                        return;
                    } catch (Exception e) {
                        logger.severe("读取输入失败: " + e.getMessage());
                        status = Status.EXIT;
                        return;
                    }

                }
                case SelectAllApply -> {
                    viewAllApplications();
                    onlineStatus = OnlineStatus.Selecting;
                }
                case Waiting -> {
                    /* 每隔一秒超时一次，每次超时时检测是否输入 q，
                        如果是直接返回
                        否则检测是否有新请求
                            如果有切换到处理状态
                            否则重新打印 "等待新申请..."，进行下一轮等待
                    */
                    int waitCount = 0;
                    while (true) {
                        try {
                            // 每10秒提示一次
                            if (waitCount % 10 == 0) {
                                terminal.writer().println("等待新申请... (输入 q 返回菜单)");
                                terminal.flush();
                            }

                            Thread.sleep(1000);
                            waitCount++;

                            // 检查是否有新请求
                            Result<List<AdoptionApplication>> result = applicationService.getPendingApplications();
                            if (result.isSuccess() && !result.getData().isEmpty()) {
                                logger.info("检测到新申请，进入处理状态");
                                onlineStatus = OnlineStatus.Handling;
                                break;
                            }

                            // 检查用户是否输入了 q（非阻塞检查）
                            if (System.in.available() > 0) {
                                int ch = System.in.read();
                                if (ch == 'q' || ch == 'Q') {
                                    logger.info("用户取消等待");
                                    onlineStatus = OnlineStatus.Back;
                                    break;
                                }
                            }
                        } catch (InterruptedException e) {
                            logger.warning("等待被中断");
                            onlineStatus = OnlineStatus.Back;
                            break;
                        } catch (IOException e) {
                            logger.warning("读取输入失败");
                            onlineStatus = OnlineStatus.Back;
                            break;
                        }
                    }
                }
                case Handling -> {
                    handleApplications();
                    onlineStatus = OnlineStatus.Waiting;
                }
                case SendAnnounce -> {
                    publishAnnouncement();
                }
            }
        }
    }

    /**
     * 查看所有申请
     */
    private void viewAllApplications() {
        Result<List<AdoptionApplication>> result = applicationService.getAllApplications();
        if (result.isSuccess()) {
            List<AdoptionApplication> applications = result.getData();
            System.out.println("========== 所有申领请求 ==========");
            if (applications.isEmpty()) {
                System.out.println("暂无申请");
            } else {
                for (int i = 0; i < applications.size(); i++) {
                    AdoptionApplication app = applications.get(i);
                    System.out.printf("[%d] ID: %s\n", i + 1, app.getId());
                    System.out.printf("\t申请人: %s\n", app.getApplicatorId());
                    System.out.printf("\t宠物: %s\n", app.getPetId());
                    System.out.printf("\t状态: %s\n", app.getStatus());
                    if (app.getReview() != null) {
                        System.out.printf("\t审核人: %s\n", app.getReview().getAdminId());
                        System.out.printf("\t审核意见: %s\n", app.getReview().getComment());
                    }
                    System.out.println();
                }
            }
            System.out.println("总计: " + applications.size() + " 条");
            System.out.println("==================================");
        } else {
            System.out.println("查询失败: " + result.getMessage());
        }
    }

    /**
     * 处理申请
     */
    private void handleApplications() {
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
            AdoptionApplication currentApp = pendingApps.get(0);

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
                        onlineStatus = OnlineStatus.Back;
                        return;
                    } catch (EndOfFileException e) {
                        logger.info("输入流结束");
                        onlineStatus = OnlineStatus.Back;
                        return;
                    } catch (Exception e) {
                        logger.severe("读取输入失败: " + e.getMessage());
                        onlineStatus = OnlineStatus.Back;
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

                    logger.fine("评论输入结束");
                } catch (UserInterruptException e) {
                    logger.info("用户中断");
                    onlineStatus = OnlineStatus.Back;
                    return;
                } catch (EndOfFileException e) {
                    logger.info("输入流结束");
                    onlineStatus = OnlineStatus.Back;
                    return;
                } catch (Exception e) {
                    logger.severe("读取评论失败: " + e.getMessage());
                    onlineStatus = OnlineStatus.Back;
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
                        onlineStatus = OnlineStatus.Back;
                        return;
                    }
                } catch (UserInterruptException | EndOfFileException e) {
                    logger.info("用户中断");
                    onlineStatus = OnlineStatus.Back;
                    return;
                } catch (Exception e) {
                    logger.info("返回菜单");
                    onlineStatus = OnlineStatus.Back;
                    return;
                }
            }
        }
    }

    /**
     * 发布公告
     */
    private void publishAnnouncement() {
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
                    onlineStatus = OnlineStatus.Back;
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
                onlineStatus = OnlineStatus.Back;
                shouldEndShow = false;
                return;
            }
            logger.fine("公告内容输入结束");

            // 调用 Service 层发布公告
            Result<String> result = announcementService.publishAnnouncement(adminId, title, content);
            if (result.isSuccess()) {
                terminal.writer().println("公告发布成功！");
            } else {
                terminal.writer().println("公告发布失败: " + result.getMessage());
            }
            terminal.flush();

            // 发布成功后返回菜单
            onlineStatus = OnlineStatus.Back;
        } catch (UserInterruptException e) {
            logger.info("用户中断");
            onlineStatus = OnlineStatus.Back;
            shouldEndShow = false;
        } catch (EndOfFileException e) {
            logger.info("输入结束");
            onlineStatus = OnlineStatus.Back;
            shouldEndShow = false;
        } catch (Exception e) {
            logger.severe("发布公告失败: " + e.getMessage());
            onlineStatus = OnlineStatus.Back;
            shouldEndShow = false;
        } finally {
            if (shouldEndShow) {
                endShow();
            }
        }
    }

    /**
     * 查看所有宠物
     */
    private void viewAllPets() {
        Result<List<Pet>> result = petService.getAllPets();
        if (result.isSuccess()) {
            List<Pet> pets = result.getData();
            System.out.println("========== 宠物列表 ==========");
            if (pets.isEmpty()) {
                System.out.println("暂无宠物");
            } else {
                for (int i = 0; i < pets.size(); i++) {
                    Pet pet = pets.get(i);
                    System.out.printf("[%d] %s (%s)\n", i + 1, pet.getName(), pet.getSpecie());
                    System.out.printf("\tID: %s\n", pet.getId());
                    System.out.printf("\t年龄: %d岁\n", pet.getAge());
                    System.out.printf("\t状态: %s\n", pet.getAdoptionStatus());
                    System.out.printf("\t描述: %s\n", pet.getDescription());
                    System.out.println();
                }
            }
            System.out.println("总计: " + pets.size() + " 只");
            System.out.println("==============================");
        } else {
            System.out.println("查询失败: " + result.getMessage());
        }
    }

    /**
     * 查看所有用户
     */
    private void viewAllUsers() {
        Result<List<common.entity.User>> result = userService.getAllUsers(adminId);
        if (result.isSuccess()) {
            List<common.entity.User> users = result.getData();
            System.out.println("========== 用户列表 ==========");
            if (users.isEmpty()) {
                System.out.println("暂无用户");
            } else {
                for (int i = 0; i < users.size(); i++) {
                    common.entity.User user = users.get(i);
                    System.out.printf("[%d] %s (%s)\n", i + 1, user.getUsername(), user.getPermission());
                    System.out.printf("\tID: %s\n", user.getId());
                    if (user.getProfile() != null) {
                        System.out.printf("\t姓名: %s\n", user.getProfile().getRealName());
                        System.out.printf("\t电话: %s\n", user.getProfile().getPhone());
                        System.out.printf("\t地址: %s\n", user.getProfile().getAddress());
                    }
                    System.out.println();
                }
            }
            System.out.println("总计: " + users.size() + " 人");
            System.out.println("==============================");
        } else {
            System.out.println("查询失败: " + result.getMessage());
        }
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
                    logger.warning("该用户不是管理员，禁止登录");
                    terminal.writer().println("该用户不是管理员，禁止登录");
                    terminal.flush();
                    tryTimes++;
                    if (tryTimes >= 3) {
                        logger.severe("登录失败次数过多，退出系统");
                        status = Status.FORBIDDEN;
                    }
                    return;
                }

                logger.info(String.format("欢迎管理员: %s", user.getUsername()));
                status = Status.ONLINE;
            } else {
                logger.warning("登录失败：" + loginResult.getMessage());
                terminal.writer().println("登录失败: " + loginResult.getMessage());
                terminal.flush();
                tryTimes++;
                if (tryTimes >= 3) {
                    logger.severe("登录失败次数过多，退出系统");
                    status = Status.FORBIDDEN;
                }
            }
        } catch (Exception e) {
            logger.severe("登录时发生错误: " + e.getMessage());
        }
        endShow();
    }

    @Override
    public void start() {
        while (status != Status.EXIT) {
            switch (status) {
                case LOGGING -> login();
                case ONLINE -> {
                    handle();
                }
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
            logger.warning("关闭终端失败: " + e.getMessage());
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
