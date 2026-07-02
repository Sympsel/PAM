package client;

import common.entity.AdoptionApplication;
import common.entity.Announcement;
import common.entity.Pet;
import common.utils.Menu;
import server.api.ServerApi;
import common.dto.response.Result;

import java.io.IOException;
import java.util.List;

import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.TerminalBuilder;

public class UserClient extends Client {

    private final ServerApi api = new ServerApi();
    private String userId;
    private String message;
    private final Menu mainMenu;

    public UserClient() {
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
        mainMenu.addItem(1, "浏览宠物", this::showPetBrowseMenu)
                .addItem(2, "我的申请", this::showMyApplicationsMenu)
                .addItem(3, "系统公告", this::showAnnouncementMenu)
                .addItem(4, "个人信息", this::showProfileMenu)
                .addItem(5, "退出登录", () -> {
                    logger.info("退出登录");
                    status = Status.EXIT;
                    mainMenu.stop();
                });
        return mainMenu;
    }

    private void showPetBrowseMenu() {
        Menu menu = new Menu("浏览宠物", reader, terminal);
        menu.addItem(1, "查看所有可领养宠物", () -> {
            Result<List<Pet>> result = api.getAvailablePets();
            if (result.isSuccess()) {
                List<Pet> pets = result.getData();
                System.out.println("========== 可领养宠物 ==========");
                if (pets.isEmpty()) {
                    System.out.println("暂无可领养宠物");
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
        });
        menu.addItem(2, "提交领养申请", () -> {
            String petId = reader.readLine("请输入宠物ID：");
            if (petId == null || petId.trim().isEmpty()) {
                System.out.println("已取消");
                return;
            }

            String confirm = reader.readLine("确认申请宠物 " + petId.trim() + "？(y/n)：");
            if (confirm == null || !confirm.trim().equalsIgnoreCase("y")) {
                System.out.println("已取消");
                return;
            }

            Result<String> result = api.submitApplication(userId, petId.trim());
            if (result.isSuccess()) {
                System.out.println(result.getMessage());
            } else {
                System.out.println("申请失败: " + result.getMessage());
            }
        });
        menu.addBackItem();
        menu.run();
    }

    private void showMyApplicationsMenu() {
        Menu menu = new Menu("我的申请", reader, terminal);
        menu.addItem(1, "查看我的申请列表", () -> {
            Result<List<AdoptionApplication>> result = api.getApplicationsByApplicator(userId);
            if (result.isSuccess()) {
                List<AdoptionApplication> apps = result.getData();
                System.out.println("========== 我的申请 ==========");
                if (apps.isEmpty()) {
                    System.out.println("暂无申请记录");
                } else {
                    for (int i = 0; i < apps.size(); i++) {
                        System.out.printf("[%d] %s\n\n", i + 1, apps.get(i).getDisplayString());
                    }
                }
                System.out.println("总计: " + apps.size() + " 条");
                System.out.println("==============================");
            } else {
                System.out.println("查询失败: " + result.getMessage());
            }
        });
        menu.addItem(2, "查看申请详情", () -> {
            String appId = reader.readLine("请输入申请ID：");
            if (appId == null || appId.trim().isEmpty()) return;

            Result<AdoptionApplication> result = api.getApplicationById(appId.trim());
            if (result.isSuccess()) {
                AdoptionApplication app = result.getData();
                if (!app.getApplicatorId().equals(userId)) {
                    System.out.println("无权查看此申请");
                    return;
                }
                System.out.println("========== 申请详情 ==========");
                System.out.println(app.getDisplayString());
                System.out.println("==============================");
            } else {
                System.out.println("查询失败: " + result.getMessage());
            }
        });
        menu.addBackItem();
        menu.run();
    }

    private void showAnnouncementMenu() {
        Menu menu = new Menu("系统公告", reader, terminal);
        menu.addItem(1, "查看所有公告", () -> {
            Result<List<Announcement>> result = api.getAllAnnouncements();
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
        menu.addItem(2, "查看公告详情", () -> {
            String id = reader.readLine("请输入公告ID：");
            if (id == null || id.trim().isEmpty()) return;

            Result<Announcement> result = api.getAnnouncementById(id.trim());
            if (result.isSuccess()) {
                System.out.println("========== 公告详情 ==========");
                System.out.println(result.getData().getDisplayString());
                System.out.println("==============================");
            } else {
                System.out.println("查询失败: " + result.getMessage());
            }
        });
        menu.addBackItem();
        menu.run();
    }

    private void showProfileMenu() {
        Menu menu = new Menu("个人信息", reader, terminal);
        menu.addItem(1, "查看个人信息", () -> {
            Result<common.entity.User> result = api.getUserById(userId);
            if (result.isSuccess()) {
                System.out.println("========== 个人信息 ==========");
                System.out.println(result.getData().getDisplayString());
                System.out.println("==============================");
            } else {
                System.out.println("查询失败: " + result.getMessage());
            }
        });
        menu.addItem(2, "修改个人信息", () -> {
            beginShow("修改个人信息");
            try {
                Result<common.entity.User> userResult = api.getUserById(userId);
                if (!userResult.isSuccess()) {
                    System.out.println("获取用户信息失败");
                    return;
                }

                common.entity.User currentUser = userResult.getData();
                System.out.println("当前手机号: " + (currentUser.getProfile().getPhone() != null ? currentUser.getProfile().getPhone() : "未设置"));
                String phone = reader.readLine("新手机号（回车保持）：");
                if (phone != null && phone.trim().isEmpty()) {
                    phone = currentUser.getProfile().getPhone();
                }

                System.out.println("当前地址: " + (currentUser.getProfile().getAddress() != null ? currentUser.getProfile().getAddress() : "未设置"));
                String address = reader.readLine("新地址（回车保持）：");
                if (address != null && address.trim().isEmpty()) {
                    address = currentUser.getProfile().getAddress();
                }

                Result<String> result = api.updateProfile(userId, phone, address);
                if (result.isSuccess()) {
                    System.out.println(result.getMessage());
                } else {
                    System.out.println("修改失败: " + result.getMessage());
                }
            } catch (Exception e) {
                logger.warn("修改个人信息失败: {}", e.getMessage());
            } finally {
                endShow();
            }
        });
        menu.addItem(3, "修改密码", () -> {
            beginShow("修改密码");
            try {
                String oldPassword = reader.readLine("旧密码：", '*');
                if (oldPassword == null || oldPassword.isEmpty()) {
                    System.out.println("已取消");
                    return;
                }

                String newPassword = reader.readLine("新密码：", '*');
                if (newPassword == null || newPassword.isEmpty()) {
                    System.out.println("已取消");
                    return;
                }

                String confirmPassword = reader.readLine("确认新密码：", '*');
                if (confirmPassword == null || !confirmPassword.equals(newPassword)) {
                    System.out.println("两次输入的密码不一致");
                    return;
                }

                Result<String> result = api.changePassword(userId, oldPassword, newPassword);
                if (result.isSuccess()) {
                    System.out.println(result.getMessage());
                } else {
                    System.out.println("修改失败: " + result.getMessage());
                }
            } catch (Exception e) {
                logger.warn("修改密码失败: {}", e.getMessage());
            } finally {
                endShow();
            }
        });
        menu.addBackItem();
        menu.run();
    }

    @Override
    protected void login() {
        beginShow("用户登录");
        try {
            String username = reader.readLine("用户名：");
            if (username == null) {
                return;
            }

            String password = reader.readLine("密码：", '*');
            if (password == null) {
                return;
            }

            Result<common.entity.User> loginResult = api.login(username, password);

            if (loginResult.isSuccess()) {
                common.entity.User user = loginResult.getData();

                // 验证是否为普通用户（非管理员）
                if (user.getPermission() == common.enums.Permission.ADMIN) {
                    logger.warn("管理员账户不能从普通用户端登录");
                    terminal.writer().println("管理员账户请使用管理员客户端登录");
                    terminal.flush();
                    tryTimes++;
                    if (tryTimes >= 3) {
                        logger.warn("登录失败次数过多，退出系统");
                        status = Status.FORBIDDEN;
                    }
                    return;
                }

                this.userId = user.getId();
                logger.info("欢迎用户: {}", user.getUsername());
                status = Status.ONLINE;
            } else {
                logger.warn("登录失败：{}", loginResult.getMessage());
                terminal.writer().println("登录失败: " + loginResult.getMessage());
                terminal.flush();
                tryTimes++;
                if (tryTimes >= 3) {
                    logger.warn("失败次数过多，退出系统");
                    status = Status.FORBIDDEN;
                }
            }
        } catch (Exception e) {
            logger.warn("登录时发生错误: {}", e.getMessage());
        }
        endShow();
    }

    @Override
    protected void exit() {
        logger.info("退出登录");
        status = Status.EXIT;
        mainMenu.stop();
        try {
            if (userId != null) {
                api.logout(userId);
            }

            if (terminal != null) {
                terminal.close();
            }
        } catch (Exception e) {
            logger.warn("关闭终端失败: {}", e.getMessage());
        }
    }

    @Override
    public void start() {
        while (status != Status.EXIT) {
            switch (status) {
                case LOGGING -> login();
                case ONLINE -> mainMenu.run();
            }
        }
        exit();
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

    public static void main(String[] args) {
        new UserClient().start();
    }
}
