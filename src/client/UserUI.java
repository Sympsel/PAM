package client;

import common.entity.AdoptionApplication;
import common.entity.Announcement;
import common.entity.Pet;
import common.entity.User;
import common.utils.StringFormatter;
import server.api.ServerApi;
import common.dto.response.Result;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserUI extends JFrame {
    private final ServerApi api = new ServerApi();
    private String userId;
    private String username;

    private JTextArea displayArea;
    private JMenuBar menuBar;

    public UserUI() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("宠物领养系统 - 用户端 - 丑就丑，时间紧任务重，将就着用");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(displayArea);

        setJMenuBar(createMenuBar());
        add(scrollPane, BorderLayout.CENTER);

        showLoginDialog();
    }

    private JMenuBar createMenuBar() {
        menuBar = new JMenuBar();

        JMenu petMenu = new JMenu("浏览宠物");
        JMenuItem viewPets = new JMenuItem("查看所有可领养宠物");
        JMenuItem submitApplication = new JMenuItem("提交领养申请");
        viewPets.addActionListener(e -> showAvailablePets());
        submitApplication.addActionListener(e -> showSubmitApplicationDialog());

        JMenu applicationMenu = new JMenu("我的申请");
        JMenuItem viewApplications = new JMenuItem("查看我的申请列表");
        JMenuItem viewApplicationDetail = new JMenuItem("查看申请详情");
        viewApplications.addActionListener(e -> showMyApplications());
        viewApplicationDetail.addActionListener(e -> showApplicationDetailDialog());

        JMenu announcementMenu = new JMenu("系统公告");
        JMenuItem viewAnnouncements = new JMenuItem("查看所有公告");
        JMenuItem viewAnnouncementDetail = new JMenuItem("查看公告详情");
        viewAnnouncements.addActionListener(e -> showAllAnnouncements());
        viewAnnouncementDetail.addActionListener(e -> showAnnouncementDetailDialog());

        JMenu profileMenu = new JMenu("个人信息");
        JMenuItem viewProfile = new JMenuItem("查看个人信息");
        JMenuItem updateProfile = new JMenuItem("修改个人信息");
        JMenuItem changePassword = new JMenuItem("修改密码");
        viewProfile.addActionListener(e -> showProfile());
        updateProfile.addActionListener(e -> showUpdateProfileDialog());
        changePassword.addActionListener(e -> showChangePasswordDialog());

        JMenu accountMenu = new JMenu("账户");
        JMenuItem logout = new JMenuItem("退出登录");
        JMenuItem deleteAccount = new JMenuItem("注销账户");
        logout.addActionListener(e -> logout());
        deleteAccount.addActionListener(e -> showDeleteAccountDialog());

        return addAll(menuBar,
                addAll(petMenu, viewPets, submitApplication),
                addAll(applicationMenu, viewApplications, viewApplicationDetail),
                addAll(announcementMenu, viewAnnouncements, viewAnnouncementDetail),
                addAll(profileMenu, viewProfile, updateProfile, changePassword),
                addAll(accountMenu, logout, deleteAccount));
    }

    private JMenu addAll(JMenu menu, JMenuItem... menuItems) {
        for (JMenuItem menuItem : menuItems) {
            menu.add(menuItem);
        }
        return menu;
    }

    private JMenuBar addAll(JMenuBar menuBar, JMenu... menus) {
        for (JMenu menu : menus) {
            menuBar.add(menu);
        }
        return menuBar;
    }

    private void showProfile() {
        Result<User> result = api.getUserById(userId);
        if (result.isSuccess()) {
            displayMessage("========== 个人信息 ==========\n\n" +
                    result.getData().getDisplayString() + "\n\n==============================");
        } else {
            showError("查询失败: " + result.getMessage());
        }
    }

    private void showAnnouncementDetailDialog() {
        // 先显示公告列表让用户选择
        Result<List<Announcement>> result = api.getAllAnnouncements();
        if (!result.isSuccess()) {
            showError("获取公告列表失败: " + result.getMessage());
            return;
        }

        List<Announcement> announcements = result.getData();
        if (announcements.isEmpty()) {
            showError("暂无公告");
            return;
        }

        // 构建选择列表
        String[] options = new String[announcements.size()];
        for (int i = 0; i < announcements.size(); i++) {
            options[i] = String.format("[%d] %s (%s)",
                    i + 1,
                    announcements.get(i).getTitle(),
                    formatTime(announcements.get(i).getCreateTime()));
        }

        String selection = (String) JOptionPane.showInputDialog(this,
                "请选择公告：",
                "查看公告详情",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        if (selection == null) {
            return;
        }

        // 提取索引
        int index = Integer.parseInt(selection.substring(1, selection.indexOf("]"))) - 1;
        Announcement selectedAnnouncement = announcements.get(index);

        // 显示完整详情（不包含 ID 和发布者）
        displayMessage("========== 公告详情 ==========\n\n" +
                "标题: " + selectedAnnouncement.getTitle() + "\n\n" +
                "发布时间: " + formatTime(selectedAnnouncement.getCreateTime()) + "\n\n" +
                "内容:\n" + selectedAnnouncement.getContent() + "\n\n" +
                "==============================");
    }

    private String formatTime(long timeStamp) {
        return StringFormatter.timeStampToString(timeStamp);
    }

    private String truncateContent(String content, int maxLength) {
        if (content == null) {
            return "无";
        }
        // 移除换行符，截断内容
        String singleLine = content.replaceAll("\\s+", " ");
        if (singleLine.length() > maxLength) {
            return singleLine.substring(0, maxLength) + "...";
        }
        return singleLine;
    }

    private void showDeleteAccountDialog() {
        int confirm = JOptionPane.showConfirmDialog(this,
                """
                        !!! 警告：注销账户将永久删除您的所有数据！
                        
                        包括：
                        • 您的个人信息
                        • 您提交的所有领养申请
                        • 您发布的所有公告
                        
                        此操作不可恢复！
                        
                        确定要注销账户吗？""",
                "注销账户确认",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            String password = JOptionPane.showInputDialog(this,
                    "请输入密码以确认身份：");

            if (password == null || password.isEmpty()) {
                showError("已取消操作");
                return;
            }

            // 直接使用 deleteAccount，不需要重新登录验证
            Result<String> deleteResult = api.deleteAccount(userId);
            if (deleteResult.isSuccess()) {
                showSuccess("账户已注销\n感谢使用宠物领养系统！");
                dispose();
                System.exit(0);
            } else {
                showError("注销失败: " + deleteResult.getMessage());
            }
        }
    }

    private void showLoginDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);

        usernameField.setPreferredSize(new Dimension(200, 30));
        passwordField.setPreferredSize(new Dimension(200, 30));

        JLabel userLabel = new JLabel("用户名：");
        JLabel passLabel = new JLabel("密码：");
        userLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        panel.add(userLabel);
        panel.add(usernameField);
        panel.add(passLabel);
        panel.add(passwordField);

        Object[] options = {"登录", "注册", "取消"};
        int result = JOptionPane.showOptionDialog(this, panel, "用户登录",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        if (result == 0) { // 登录
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "用户名和密码不能为空",
                        "错误", JOptionPane.ERROR_MESSAGE);
                showLoginDialog();
                return;
            }

            Result<User> loginResult = api.login(username, password);
            if (loginResult.isSuccess()) {
                User user = loginResult.getData();
                if (user.getPermission() == common.enums.Permission.ADMIN) {
                    JOptionPane.showMessageDialog(this,
                            "管理员账户请使用管理员客户端登录",
                            "错误", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                    return;
                }
                this.userId = user.getId();
                this.username = user.getUsername();
                displayMessage("欢迎, " + username + "!");
            } else {
                JOptionPane.showMessageDialog(this,
                        "登录失败: " + loginResult.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
                showLoginDialog();
            }
        } else if (result == 1) { // 注册
            showRegisterDialog();
        } else { // 取消
            System.exit(0);
        }
    }

    private void showRegisterDialog() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JPasswordField confirmPasswordField = new JPasswordField(20);
        JTextField realNameField = new JTextField(20);
        JTextField phoneField = new JTextField(20);
        JTextField addressField = new JTextField(20);

        Dimension fieldSize = new Dimension(200, 30);
        usernameField.setPreferredSize(fieldSize);
        passwordField.setPreferredSize(fieldSize);
        confirmPasswordField.setPreferredSize(fieldSize);
        realNameField.setPreferredSize(fieldSize);
        phoneField.setPreferredSize(fieldSize);
        addressField.setPreferredSize(fieldSize);

        JLabel userLabel = new JLabel("用户名：");
        JLabel passLabel = new JLabel("密码：");
        JLabel confirmPassLabel = new JLabel("确认密码：");
        JLabel realNameLabel = new JLabel("姓名（可选）：");
        JLabel phoneLabel = new JLabel("手机号（可选）：");
        JLabel addressLabel = new JLabel("地址（可选）：");

        Font labelFont = new Font("微软雅黑", Font.PLAIN, 14);
        userLabel.setFont(labelFont);
        passLabel.setFont(labelFont);
        confirmPassLabel.setFont(labelFont);
        realNameLabel.setFont(labelFont);
        phoneLabel.setFont(labelFont);
        addressLabel.setFont(labelFont);

        panel.add(userLabel);
        panel.add(usernameField);
        panel.add(passLabel);
        panel.add(passwordField);
        panel.add(confirmPassLabel);
        panel.add(confirmPasswordField);
        panel.add(realNameLabel);
        panel.add(realNameField);
        panel.add(phoneLabel);
        panel.add(phoneField);
        panel.add(addressLabel);
        panel.add(addressField);

        int result = JOptionPane.showConfirmDialog(this, panel, "用户注册",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            String realName = realNameField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                showError("用户名和密码不能为空");
                showRegisterDialog();
                return;
            }

            if (!password.equals(confirmPassword)) {
                showError("两次输入的密码不一致");
                showRegisterDialog();
                return;
            }

            if (realName.isEmpty()) {
                realName = null;
            }
            if (phone.isEmpty()) {
                phone = null;
            }
            if (address.isEmpty()) {
                address = null;
            }

            Result<String> registerResult = api.register(username, password, realName, phone, address);
            if (registerResult.isSuccess()) {
                showSuccess("注册成功！\n用户名: " + username + "\n\n请使用新账户登录");
                showLoginDialog();
            } else {
                showError("注册失败: " + registerResult.getMessage());
                showRegisterDialog();
            }
        } else {
            showLoginDialog();
        }
    }

    private void showAvailablePets() {
        Result<List<Pet>> result = api.getAvailablePets();
        if (result.isSuccess()) {
            List<Pet> pets = result.getData();
            StringBuilder sb = new StringBuilder();
            sb.append("========== 可领养宠物 ==========\n\n");
            if (pets.isEmpty()) {
                sb.append("暂无可领养宠物\n");
            } else {
                for (int i = 0; i < pets.size(); i++) {
                    sb.append(String.format("[%d] %s\n\n", i + 1, pets.get(i).getDisplayString()));
                }
            }
            sb.append("总计: ").append(pets.size()).append(" 只\n");
            sb.append("==============================");
            displayMessage(sb.toString());
        } else {
            showError("查询失败: " + result.getMessage());
        }
    }

    private void showSubmitApplicationDialog() {
        String petId = JOptionPane.showInputDialog(this, "请输入宠物ID：");
        if (petId == null || petId.trim().isEmpty()) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "确认申请宠物 " + petId.trim() + "？",
                "确认", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Result<String> result = api.submitApplication(userId, petId.trim());
            if (result.isSuccess()) {
                showSuccess(result.getMessage() + "\n您可以在【我的申请】中查看审核进度");
            } else {
                showError(result.getMessage());
            }
        }
    }

    private void showMyApplications() {
        Result<List<AdoptionApplication>> result = api.getApplicationsByApplicator(userId);
        if (result.isSuccess()) {
            List<AdoptionApplication> apps = result.getData();
            StringBuilder sb = new StringBuilder();
            sb.append("========== 我的申请 ==========\n\n");
            if (apps.isEmpty()) {
                sb.append("暂无申请记录\n");
            } else {
                for (int i = 0; i < apps.size(); i++) {
                    sb.append(String.format("[%d] %s\n\n", i + 1, apps.get(i).getDisplayString()));
                }
            }
            sb.append("总计: ").append(apps.size()).append(" 条\n");
            sb.append("==============================");
            displayMessage(sb.toString());
        } else {
            showError("查询失败: " + result.getMessage());
        }
    }

    private void showApplicationDetailDialog() {
        String appId = JOptionPane.showInputDialog(this, "请输入申请ID：");
        if (appId == null || appId.trim().isEmpty()) {
            return;
        }

        Result<AdoptionApplication> result = api.getApplicationById(appId.trim());
        if (result.isSuccess()) {
            AdoptionApplication app = result.getData();
            if (!app.getApplicatorId().equals(userId)) {
                showError("无权查看此申请");
                return;
            }
            displayMessage("========== 申请详情 ==========\n\n" +
                    app.getDisplayString() + "\n\n==============================");
        } else {
            showError("查询失败: " + result.getMessage());
        }
    }

    private void showAllAnnouncements() {
        Result<List<Announcement>> result = api.getAllAnnouncements();
        if (result.isSuccess()) {
            List<Announcement> announcements = result.getData();
            StringBuilder sb = new StringBuilder();
            sb.append("========== 公告列表 ==========\n\n");
            if (announcements.isEmpty()) {
                sb.append("暂无公告\n");
            } else {
                for (int i = 0; i < announcements.size(); i++) {
                    sb.append(String.format("[%d] %s\n\n", i + 1, announcements.get(i).getDisplayString()));
                }
            }
            sb.append("总计: ").append(announcements.size()).append(" 条\n");
            sb.append("==============================");
            displayMessage(sb.toString());
        } else {
            showError("查询失败: " + result.getMessage());
        }
    }


    private void showUpdateProfileDialog() {
        Result<User> userResult = api.getUserById(userId);
        if (!userResult.isSuccess()) {
            showError("获取用户信息失败");
            return;
        }

        User currentUser = userResult.getData();
        String currentPhone = currentUser.getProfile().getPhone() != null ?
                currentUser.getProfile().getPhone() : "未设置";
        String currentAddress = currentUser.getProfile().getAddress() != null ?
                currentUser.getProfile().getAddress() : "未设置";

        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(new JLabel("当前手机号："));
        panel.add(new JLabel(currentPhone));
        JTextField phoneField = new JTextField();
        panel.add(new JLabel("新手机号（留空保持不变）："));
        panel.add(phoneField);
        panel.add(new JLabel("当前地址："));
        panel.add(new JLabel(currentAddress));
        JTextField addressField = new JTextField();
        panel.add(new JLabel("新地址（留空保持不变）："));
        panel.add(addressField);

        int result = JOptionPane.showConfirmDialog(this, panel, "修改个人信息",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();

            if (phone.isEmpty()) {
                phone = currentUser.getProfile().getPhone();
            }
            if (address.isEmpty()) {
                address = currentUser.getProfile().getAddress();
            }

            Result<String> updateResult = api.updateProfile(userId, phone, address);
            if (updateResult.isSuccess()) {
                showSuccess(updateResult.getMessage());
            } else {
                showError("修改失败: " + updateResult.getMessage());
            }
        }
    }

    private void showChangePasswordDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        JPasswordField oldPasswordField = new JPasswordField();
        JPasswordField newPasswordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();

        panel.add(new JLabel("旧密码："));
        panel.add(oldPasswordField);
        panel.add(new JLabel("新密码："));
        panel.add(newPasswordField);
        panel.add(new JLabel("确认新密码："));
        panel.add(confirmPasswordField);

        int result = JOptionPane.showConfirmDialog(this, panel, "修改密码",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String oldPassword = new String(oldPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (oldPassword.isEmpty() || newPassword.isEmpty()) {
                showError("密码不能为空");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                showError("两次输入的密码不一致");
                return;
            }

            Result<String> changeResult = api.changePassword(userId, oldPassword, newPassword);
            if (changeResult.isSuccess()) {
                showSuccess(changeResult.getMessage());
            } else {
                showError("修改失败: " + changeResult.getMessage());
            }
        }
    }

    private void logout() {
        if (userId != null) {
            api.logout(userId);
        }
        dispose();
        System.exit(0);
    }

    private void displayMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            displayArea.setText(message);
            displayArea.setCaretPosition(0);
        });
    }

    private void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "✗ " + message,
                    "错误", JOptionPane.ERROR_MESSAGE);
        });
    }

    private void showSuccess(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "✓ " + message,
                    "成功", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new UserUI().setVisible(true);
        });
    }
}
