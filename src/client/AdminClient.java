package client;


import common.utils.Menu;
import common.dto.response.Result;

import java.io.IOException;

import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.TerminalBuilder;

public class AdminClient extends Client {
    private enum HandingStatus {
        Comment,
        Choose
    }

    private String adminId;
    private final Menu mainMenu;

    public AdminClient() {
        status = Status.LOGGING;
        try {
            terminal = TerminalBuilder.builder().build();
            reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(completer)
                    .build();
            mainMenu = createMainMenu();
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
                .addItem(0, "退出登录", this::exit);
        return mainMenu;
    }

    private void showStatisticsMenu() {
        Menu statisticsMenu = new Menu("系统统计", reader, terminal);
        statisticsMenu.addItem(1, "综合统计概览", () -> api.showAllStatistics(adminId));
        statisticsMenu.addBackItem();
        statisticsMenu.run();
    }

    private void showUserMenu() {
        Menu userMenu = new Menu("用户管理", reader, terminal);
        userMenu.addItem(1, "查看所有用户", () -> api.showAllUser(adminId));
        userMenu.addItem(2, "用户统计", () -> api.showUserStatistics(adminId));
        userMenu.addItem(3, "删除用户", () -> api.deleteUser(adminId));
        userMenu.addBackItem();
        userMenu.run();
    }


    private void showApplicationMenu() {
        Menu application = new Menu("申领请求管理", reader, terminal);
        application.addItem(1, "查看所有申领", api::showAllApplication);
        application.addItem(2, "查看指定申请", api::queryApplicationById);
        application.addItem(3, "处理待审核申请", () -> api.applicationHandler(adminId)).addBackItem();
        application.run();
    }

    private void showPetMenu() {
        Menu petMenu = new Menu("宠物管理", reader, terminal);
        petMenu.addItem(1, "查看所有宠物", api::showAllPets);
        petMenu.addItem(2, "添加新宠物", () -> api.addPets(adminId));
        petMenu.addItem(3, "修改宠物信息", () -> api.modifyPetsInfo(adminId));
        petMenu.addItem(4, "删除宠物", () -> api.deletePet(adminId));
        petMenu.addItem(5, "宠物统计", api::showPetStatistics);
        petMenu.addBackItem();
        petMenu.run();
    }

    private void handle() {
        try {
            // 延迟等待启动日志打完
            Thread.sleep(500);
        } catch (InterruptedException e) {
            logger.warn("等待被中断");
        }
        mainMenu.run();
    }


    @Override
    protected void login() {
        api.beginShow("login");
        try {
            String username = reader.readLine("请输入管理员用户名：");
            if (username == null) return;

            String password = reader.readLine("请输入密码：", '*');
            if (password == null) return;

            Result<common.entity.User> loginResult = api.login(username, password);

            if (loginResult.isSuccess()) {
                common.entity.User user = loginResult.getData();

                // 验证是否为管理员
                if (user.getPermission() != common.enums.Permission.ADMIN) {
                    logger.warn("该用户不是管理员，无权访问专用客户端");
                    terminal.writer().println("该用户不是管理员，无权访问专用客户端");
                    terminal.flush();
                    tryTimes++;
                    if (tryTimes >= 3) {
                        logger.warn("管理员登录失败次数过多，退出系统");
                        status = Status.FORBIDDEN;
                    }
                    return;
                }

                this.adminId = user.getId();
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
        api.endShow();
    }

    public void showAnnouncementMenu() {
        Menu announcementMenu = new Menu("公告管理", reader, terminal);
        announcementMenu.addItem(1, "发布新公告", () -> api.sendAnnouncement(adminId));
        announcementMenu.addItem(2, "查看所有公告", api::showAllAnnouncement);
        announcementMenu.addItem(3, "查看公告详情", api::queryAnnouncementById);
        announcementMenu.addItem(4, "筛选公告", api::queryApplicationsByStatus);
        announcementMenu.addItem(5, "修改公告", () -> api.modifyAnnouncement(adminId));
        announcementMenu.addItem(6, "删除公告", () -> api.deleteAnnouncement(adminId)).addBackItem();
        announcementMenu.run();
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
        logger.info("退出登录");
        status = Status.EXIT;
        mainMenu.stop();
    }

    public static void main(String[] args) {
        new AdminClient().start();
    }
}
