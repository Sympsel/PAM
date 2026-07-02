package client;

import client.utils.InputFrame;
import common.entity.AdoptionApplication;
import common.entity.Announcement;
import common.entity.Pet;
import common.entity.User;
import common.utils.Config;
import common.utils.StringFormatter;
import server.api.ServerApi;
import common.dto.response.Result;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static client.utils.Utils.addAll;

public class AdminUI extends JFrame {
    private final ServerApi api = new ServerApi();
    private String adminId;
    private String username;

    private JTextArea displayArea;
    private JMenuBar menuBar;

    public AdminUI() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("宠物领养系统 - 管理员端");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font(Config.FONT, Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(displayArea);

        setJMenuBar(createMenuBar());
        add(scrollPane, BorderLayout.CENTER);

        showLoginDialog();
    }

    private JMenuBar createMenuBar() {
        menuBar = new JMenuBar();

        JMenu petMenu = new JMenu("宠物管理");
        JMenuItem viewPets = new JMenuItem("查看所有宠物");
        JMenuItem addPet = new JMenuItem("添加宠物");
        JMenuItem updatePet = new JMenuItem("修改宠物信息");
        JMenuItem deletePet = new JMenuItem("删除宠物");
        viewPets.addActionListener(e -> showAllPets());
        addPet.addActionListener(e -> showAddPetDialog());
        updatePet.addActionListener(e -> showUpdatePetDialog());
        deletePet.addActionListener(e -> showDeletePetDialog());

        JMenu applicationMenu = new JMenu("申请管理");
        JMenuItem pendingApps = new JMenuItem("查看待审核申请");
        JMenuItem allApps = new JMenuItem("查看所有申请");
        JMenuItem approveApp = new JMenuItem("通过申请");
        JMenuItem rejectApp = new JMenuItem("拒绝申请");
        pendingApps.addActionListener(e -> showPendingApplications());
        allApps.addActionListener(e -> showAllApplications());
        approveApp.addActionListener(e -> showApproveApplicationDialog());
        rejectApp.addActionListener(e -> showRejectApplicationDialog());

        JMenu announcementMenu = new JMenu("公告管理");
        JMenuItem viewAnnouncements = new JMenuItem("查看所有公告");
        JMenuItem publishAnnouncement = new JMenuItem("发布公告");
        JMenuItem updateAnnouncement = new JMenuItem("修改公告");
        JMenuItem deleteAnnouncement = new JMenuItem("删除公告");
        viewAnnouncements.addActionListener(e -> showAllAnnouncements());
        publishAnnouncement.addActionListener(e -> showPublishAnnouncementDialog());
        updateAnnouncement.addActionListener(e -> showUpdateAnnouncementDialog());
        deleteAnnouncement.addActionListener(e -> showDeleteAnnouncementDialog());

        JMenu userMenu = new JMenu("用户管理");
        JMenuItem viewUsers = new JMenuItem("查看所有用户");
        JMenuItem deleteUser = new JMenuItem("删除用户");
        JMenuItem userStatistics = new JMenuItem("查看用户统计");
        viewUsers.addActionListener(e -> showAllUsers());
        deleteUser.addActionListener(e -> showDeleteUserDialog());
        userStatistics.addActionListener(e -> showUserStatistics());

        JMenu statisticsMenu = new JMenu("数据统计");
        JMenuItem allStatistics = new JMenuItem("综合统计");
        allStatistics.addActionListener(e -> showAllStatistics());

        JMenu accountMenu = new JMenu("账户");
        JMenuItem logout = new JMenuItem("退出登录");
        logout.addActionListener(e -> logout());

        return addAll(menuBar,
                addAll(petMenu, viewPets, addPet, updatePet, deletePet),
                addAll(applicationMenu, pendingApps, allApps, approveApp, rejectApp),
                addAll(announcementMenu, viewAnnouncements, publishAnnouncement, updateAnnouncement, deleteAnnouncement),
                addAll(userMenu, viewUsers, deleteUser, userStatistics),
                addAll(statisticsMenu, allStatistics),
                addAll(accountMenu, logout));
    }

    private void showLoginDialog() {
        InputFrame inputFrame = new InputFrame(0.3);
        inputFrame.addInputField("用户名：", "")
                .addPasswordField("密码：", "");

        JPanel panel = inputFrame.buildPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        Object[] options = {"登录", "取消"};
        int result = JOptionPane.showOptionDialog(this, panel, "管理员登录",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

        if (result == 0) {
            String[] values = inputFrame.getFieldValues();
            String username = values[0].trim();
            String password = values[1];

            if (username.isEmpty() || password.isEmpty()) {
                showError("用户名和密码不能为空");
                showLoginDialog();
                return;
            }

            Result<User> loginResult = api.login(username, password);
            if (loginResult.isSuccess()) {
                User user = loginResult.getData();
                if (user.getPermission() != common.enums.Permission.ADMIN) {
                    showError("普通用户请使用用户端登录");
                    System.exit(0);
                    return;
                }
                this.adminId = user.getId();
                this.username = user.getUsername();
                displayMessage("欢迎，管理员 " + username + "!");
            } else {
                showError("登录失败: " + loginResult.getMessage());
                showLoginDialog();
            }
        } else {
            System.exit(0);
        }
    }

    private void showAllPets() {
        Result<List<Pet>> result = api.getAllPets();
        if (result.isSuccess()) {
            List<Pet> pets = result.getData();
            StringBuilder sb = new StringBuilder();
            sb.append("========== 所有宠物 ==========\n\n");
            if (pets.isEmpty()) {
                sb.append("暂无宠物记录\n");
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

    private void showAddPetDialog() {
        InputFrame inputFrame = new InputFrame(0.3);
        inputFrame.addInputField("宠物名称：", "")
                .addInputField("年龄：", "")
                .addInputField("描述：", "");

        JPanel panel = inputFrame.buildPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JComboBox<String> speciesBox = new JComboBox<>(new String[]{"狗", "猫", "兔子", "鸟", "其他"});
        JPanel speciesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        speciesPanel.add(new JLabel("物种："));
        speciesPanel.add(speciesBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "添加宠物",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String[] values = inputFrame.getFieldValues();
            String name = values[0].trim();
            String ageStr = values[1].trim();
            String description = values[2].trim();

            if (name.isEmpty() || ageStr.isEmpty()) {
                showError("宠物名称和年龄不能为空");
                return;
            }

            try {
                int age = Integer.parseInt(ageStr);
                String speciesStr = (String) speciesBox.getSelectedItem();
                if (speciesStr == null) {
                    speciesStr = "";
                }
                common.enums.PetSpecies specie = parseSpecies(speciesStr);

                Result<String> addResult = api.addPet(adminId, name, specie, age, description);
                if (addResult.isSuccess()) {
                    showSuccess(addResult.getMessage());
                } else {
                    showError("添加失败: " + addResult.getMessage());
                }
            } catch (NumberFormatException e) {
                showError("年龄格式不正确");
            }
        }
    }

    private void showUpdatePetDialog() {
        String petId = JOptionPane.showInputDialog(this, "请输入宠物ID：");
        if (petId == null || petId.trim().isEmpty()) {
            return;
        }

        Result<Pet> petResult = api.getPetById(petId.trim());
        if (!petResult.isSuccess()) {
            showError("宠物不存在: " + petResult.getMessage());
            return;
        }

        Pet pet = petResult.getData();
        InputFrame inputFrame = new InputFrame(0.3);
        inputFrame.addInputField("宠物名称：", pet.getName())
                .addInputField("年龄：", String.valueOf(pet.getAge()))
                .addInputField("描述：", pet.getDescription() != null ? pet.getDescription() : "");

        JPanel panel = inputFrame.buildPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        int result = JOptionPane.showConfirmDialog(this, panel, "修改宠物信息",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String[] values = inputFrame.getFieldValues();
            String name = values[0].trim();
            String ageStr = values[1].trim();
            String description = values[2].trim();

            try {
                int age = Integer.parseInt(ageStr);
                Result<String> updateResult = api.updatePet(adminId, petId.trim(), name, pet.getSpecie(), age, description);
                if (updateResult.isSuccess()) {
                    showSuccess(updateResult.getMessage());
                } else {
                    showError("修改失败: " + updateResult.getMessage());
                }
            } catch (NumberFormatException e) {
                showError("年龄格式不正确");
            }
        }
    }

    private void showDeletePetDialog() {
        String petId = JOptionPane.showInputDialog(this, "请输入要删除的宠物ID：");
        if (petId == null || petId.trim().isEmpty()) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "确认删除宠物 " + petId.trim() + "？",
                "确认", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Result<String> deleteResult = api.deletePet(adminId, petId.trim());
            if (deleteResult.isSuccess()) {
                showSuccess(deleteResult.getMessage());
            } else {
                showError("删除失败: " + deleteResult.getMessage());
            }
        }
    }

    private void showPendingApplications() {
        Result<List<AdoptionApplication>> result = api.getPendingApplications();
        if (result.isSuccess()) {
            List<AdoptionApplication> apps = result.getData();
            StringBuilder sb = new StringBuilder();
            sb.append("========== 待审核申请 ==========\n\n");
            if (apps.isEmpty()) {
                sb.append("暂无待审核申请\n");
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

    private void showAllApplications() {
        Result<List<AdoptionApplication>> result = api.getAllApplications();
        if (result.isSuccess()) {
            List<AdoptionApplication> apps = result.getData();
            StringBuilder sb = new StringBuilder();
            sb.append("========== 所有申请 ==========\n\n");
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

    private void showApproveApplicationDialog() {
        String appId = JOptionPane.showInputDialog(this, "请输入申请ID：");
        if (appId == null || appId.trim().isEmpty()) {
            return;
        }

        String comment = JOptionPane.showInputDialog(this, "请输入审核意见（可选）：");

        int confirm = JOptionPane.showConfirmDialog(this,
                "确认通过申请 " + appId.trim() + "？",
                "确认", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Result<String> approveResult = api.approveApplication(adminId, appId.trim(), comment);
            if (approveResult.isSuccess()) {
                showSuccess(approveResult.getMessage());
            } else {
                showError("操作失败: " + approveResult.getMessage());
            }
        }
    }

    private void showRejectApplicationDialog() {
        String appId = JOptionPane.showInputDialog(this, "请输入申请ID：");
        if (appId == null || appId.trim().isEmpty()) {
            return;
        }

        String comment = JOptionPane.showInputDialog(this, "请输入拒绝原因：");

        int confirm = JOptionPane.showConfirmDialog(this,
                "确认拒绝申请 " + appId.trim() + "？",
                "确认", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Result<String> rejectResult = api.rejectApplication(adminId, appId.trim(), comment);
            if (rejectResult.isSuccess()) {
                showSuccess(rejectResult.getMessage());
            } else {
                showError("操作失败: " + rejectResult.getMessage());
            }
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
                    sb.append(String.format("[%d] %s\n\n", i + 1, announcements.get(i).getShortDisplayString()));
                }
            }
            sb.append("总计: ").append(announcements.size()).append(" 条\n");
            sb.append("==============================");
            displayMessage(sb.toString());
        } else {
            showError("查询失败: " + result.getMessage());
        }
    }

    private void showPublishAnnouncementDialog() {
        InputFrame inputFrame = new InputFrame(0.3);
        inputFrame.addInputField("标题：", "")
                .addInputField("内容：", "");

        JPanel panel = inputFrame.buildPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        int result = JOptionPane.showConfirmDialog(this, panel, "发布公告",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String[] values = inputFrame.getFieldValues();
            String title = values[0].trim();
            String content = values[1].trim();

            if (title.isEmpty() || content.isEmpty()) {
                showError("标题和内容不能为空");
                return;
            }

            Result<String> publishResult = api.publishAnnouncement(adminId, title, content);
            if (publishResult.isSuccess()) {
                showSuccess(publishResult.getMessage());
            } else {
                showError("发布失败: " + publishResult.getMessage());
            }
        }
    }

    private void showUpdateAnnouncementDialog() {
        String id = JOptionPane.showInputDialog(this, "请输入公告ID：");
        if (id == null || id.trim().isEmpty()) {
            return;
        }

        Result<Announcement> annResult = api.getAnnouncementById(id.trim());
        if (!annResult.isSuccess()) {
            showError("公告不存在: " + annResult.getMessage());
            return;
        }

        Announcement announcement = annResult.getData();
        InputFrame inputFrame = new InputFrame(0.3);
        inputFrame.addInputField("标题：", announcement.getTitle())
                .addInputField("内容：", announcement.getContent() != null ? announcement.getContent() : "");

        JPanel panel = inputFrame.buildPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        int result = JOptionPane.showConfirmDialog(this, panel, "修改公告",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String[] values = inputFrame.getFieldValues();
            String title = values[0].trim();
            String content = values[1].trim();

            Result<String> updateResult = api.updateAnnouncement(id.trim(), title, content, adminId);
            if (updateResult.isSuccess()) {
                showSuccess(updateResult.getMessage());
            } else {
                showError("修改失败: " + updateResult.getMessage());
            }
        }
    }

    private void showDeleteAnnouncementDialog() {
        String id = JOptionPane.showInputDialog(this, "请输入要删除的公告ID：");
        if (id == null || id.trim().isEmpty()) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "确认删除公告 " + id.trim() + "？",
                "确认", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Result<String> deleteResult = api.deleteAnnouncement(id.trim(), adminId);
            if (deleteResult.isSuccess()) {
                showSuccess(deleteResult.getMessage());
            } else {
                showError("删除失败: " + deleteResult.getMessage());
            }
        }
    }

    private void showAllUsers() {
        Result<List<User>> result = api.getAllUsers(adminId);
        if (result.isSuccess()) {
            List<User> users = result.getData();
            StringBuilder sb = new StringBuilder();
            sb.append("========== 用户列表 ==========\n\n");
            if (users.isEmpty()) {
                sb.append("暂无用户记录\n");
            } else {
                for (int i = 0; i < users.size(); i++) {
                    sb.append(String.format("[%d] %s\n\n", i + 1, users.get(i).getDisplayString()));
                }
            }
            sb.append("总计: ").append(users.size()).append(" 人\n");
            sb.append("==============================");
            displayMessage(sb.toString());
        } else {
            showError("查询失败: " + result.getMessage());
        }
    }

    private void showDeleteUserDialog() {
        String userId = JOptionPane.showInputDialog(this, "请输入要删除的用户ID：");
        if (userId == null || userId.trim().isEmpty()) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "警告：删除用户将同时删除该用户的所有申请和公告！\n确认删除用户 " + userId.trim() + "？",
                "确认", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            Result<String> deleteResult = api.deleteUser(adminId, userId.trim());
            if (deleteResult.isSuccess()) {
                showSuccess(deleteResult.getMessage());
            } else {
                showError("删除失败: " + deleteResult.getMessage());
            }
        }
    }

    private void showUserStatistics() {
        Result<server.service.UserService.UserStatistics> result = api.getUserStatistics(adminId);
        if (result.isSuccess()) {
            server.service.UserService.UserStatistics stats = result.getData();
            displayMessage("========== 用户统计 ==========\n\n" +
                    "总用户数: " + stats.getTotal() + "\n" +
                    "管理员: " + stats.getAdminCount() + "\n" +
                    "普通用户: " + stats.getNormalCount() + "\n\n" +
                    "==============================");
        } else {
            showError("查询失败: " + result.getMessage());
        }
    }

    private void showAllStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("========== 系统统计概览 ==========\n\n");

        // 用户统计
        Result<server.service.UserService.UserStatistics> userResult = api.getUserStatistics(adminId);
        if (userResult.isSuccess()) {
            server.service.UserService.UserStatistics userStats = userResult.getData();
            sb.append("【用户统计】\n");
            sb.append("\t总用户数: ").append(userStats.getTotal()).append("\n");
            sb.append("\t管理员: ").append(userStats.getAdminCount()).append("\n");
            sb.append("\t普通用户: ").append(userStats.getNormalCount()).append("\n\n");
        } else {
            sb.append("【用户统计】查询失败: ").append(userResult.getMessage()).append("\n\n");
        }

        // 宠物统计
        Result<Integer> petCountResult = api.getPetCount();
        if (petCountResult.isSuccess()) {
            sb.append("【宠物统计】\n");
            sb.append("\t宠物总数: ").append(petCountResult.getData()).append("\n");

            Result<List<Pet>> allPets = api.getAllPets();
            if (allPets.isSuccess()) {
                long available = allPets.getData().stream()
                        .filter(p -> p.getAdoptionStatus() == common.enums.PetStatus.AVAILABLE).count();
                long pending = allPets.getData().stream()
                        .filter(p -> p.getAdoptionStatus() == common.enums.PetStatus.PENDING).count();
                long adopted = allPets.getData().stream()
                        .filter(p -> p.getAdoptionStatus() == common.enums.PetStatus.ADOPTED).count();
                sb.append("\t可领养: ").append(available).append("\n");
                sb.append("\t待审核: ").append(pending).append("\n");
                sb.append("\t已领养: ").append(adopted).append("\n");
            }
            sb.append("\n");
        } else {
            sb.append("【宠物统计】查询失败: ").append(petCountResult.getMessage()).append("\n\n");
        }

        // 申请统计
        Result<server.service.AdoptionApplicationService.ApplicationStatistics> appResult =
                api.getApplicationStatistics();
        if (appResult.isSuccess()) {
            server.service.AdoptionApplicationService.ApplicationStatistics appStats = appResult.getData();
            sb.append("【申请统计】\n");
            sb.append("\t申请总数: ").append(appStats.total()).append("\n");
            sb.append("\t待审核: ").append(appStats.pending()).append("\n");
            sb.append("\t已通过: ").append(appStats.approved()).append("\n");
            sb.append("\t已拒绝: ").append(appStats.rejected()).append("\n\n");
        } else {
            sb.append("【申请统计】查询失败: ").append(appResult.getMessage()).append("\n\n");
        }

        sb.append("==================================");
        displayMessage(sb.toString());
    }

    private void logout() {
        if (adminId != null) {
            api.logout(adminId);
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

    private String formatTime(long timeStamp) {
        return StringFormatter.timeStampToString(timeStamp);
    }

    private common.enums.PetSpecies parseSpecies(String speciesStr) {
        return switch (speciesStr) {
            case "狗" -> common.enums.PetSpecies.DOG;
            case "猫" -> common.enums.PetSpecies.CAT;
            default -> common.enums.PetSpecies.OTHER;
        };
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception ex) {
            System.err.println("Failed to initialize system Look and Feel");
        }
        SwingUtilities.invokeLater(() -> {
            new AdminUI().setVisible(true);
        });
    }
}
