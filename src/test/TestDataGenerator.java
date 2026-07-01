// test/TestDataGenerator.java
package test;

import config.DatabaseConfig;

import java.sql.*;
import java.util.UUID;

/**
 * 测试数据生成器
 * 为 AdminClient 测试生成完整的测试数据
 */
public class TestDataGenerator {

    private static final String ADMIN_PASSWORD = "admin123";
    private static final String USER_PASSWORD = "user123";

    public static void main(String[] args) {
        try {
            System.out.println("========== 开始生成测试数据 ==========");

            // 建立数据库连接
            Connection conn = DriverManager.getConnection(
                    DatabaseConfig.JDBC_URL,
                    DatabaseConfig.DB_USER,
                    DatabaseConfig.DB_PASSWORD
            );

            // 清空现有数据（按依赖顺序）
            clearData(conn);

            // 生成测试数据
            String adminId = createAdminUser(conn);
            String userId = createNormalUser(conn);
            String pet1Id = createPet(conn, "小白", "dog", 2, "可爱的小狗，很温顺");
            String pet2Id = createPet(conn, "咪咪", "cat", 1, "活泼的小猫，喜欢玩耍");
            String pet3Id = createPet(conn, "旺财", "dog", 3, "忠诚的看门狗");
            createApplications(conn, userId, pet1Id, pet2Id);
            createAnnouncement(conn, adminId);

            // 关闭连接
            conn.close();

            System.out.println("\n========== 测试数据生成完成 ==========");
            System.out.println("管理员账号: admin / " + ADMIN_PASSWORD);
            System.out.println("普通用户: user / " + USER_PASSWORD);
            System.out.println("待审核申请数量: 2");
            System.out.println("宠物数量: 3");
            System.out.println("公告数量: 1");
            System.out.println("=====================================\n");

        } catch (SQLException e) {
            System.err.println("生成测试数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 清空现有数据
     */
    private static void clearData(Connection conn) throws SQLException {
        System.out.println("清空现有数据...");

        Statement stmt = conn.createStatement();

        // 按依赖顺序删除（先删除子表，再删除父表）
        stmt.executeUpdate("delete from adoption_applications");
        stmt.executeUpdate("delete from announcements");
        stmt.executeUpdate("delete from pets");
        stmt.executeUpdate("delete from users");

        stmt.close();
        System.out.println("数据清空完成");
    }

    /**
     * 创建管理员用户
     */
    private static String createAdminUser(Connection conn) throws SQLException {
        System.out.println("创建管理员用户...");

        String id = UUID.randomUUID().toString();
        String sql = "insert into users (id, username, password_hash, permission, real_name, phone, address, is_online, create_time) " +
                     "values (?, ?, ?, 'admin', '管理员', '13800138000', '管理中心', false, ?)";

        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, id);
        pstmt.setString(2, "admin");
        pstmt.setString(3, ADMIN_PASSWORD); // 实际应该加密，这里简化处理
        pstmt.setLong(4, System.currentTimeMillis());
        pstmt.executeUpdate();
        pstmt.close();

        System.out.println("  管理员 ID: " + id);
        return id;
    }

    /**
     * 创建普通用户
     */
    private static String createNormalUser(Connection conn) throws SQLException {
        System.out.println("创建普通用户...");

        String id = UUID.randomUUID().toString();
        String sql = "insert into users (id, username, password_hash, permission, real_name, phone, address, is_online, create_time) " +
                     "values (?, ?, ?, 'normal', '张三', '13900139000', '北京市朝阳区', false, ?)";

        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, id);
        pstmt.setString(2, "user");
        pstmt.setString(3, USER_PASSWORD); // 实际应该加密
        pstmt.setLong(4, System.currentTimeMillis());
        pstmt.executeUpdate();
        pstmt.close();

        System.out.println("  用户 ID: " + id);
        return id;
    }

    /**
     * 创建宠物
     */
    private static String createPet(Connection conn, String name, String specie, int age, String description) throws SQLException {
        System.out.println("创建宠物: " + name);

        String id = UUID.randomUUID().toString();
        String sql = "insert into pets (id, name, specie, age, description, adoption_status, create_time) " +
                     "values (?, ?, ?, ?, ?, 'available', ?)";

        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, id);
        pstmt.setString(2, name);
        pstmt.setString(3, specie);
        pstmt.setInt(4, age);
        pstmt.setString(5, description);
        pstmt.setLong(6, System.currentTimeMillis());
        pstmt.executeUpdate();
        pstmt.close();

        System.out.println("  宠物 ID: " + id + ", 物种: " + specie + ", 年龄: " + age);
        return id;
    }

    /**
     * 创建领养申请
     */
    private static void createApplications(Connection conn, String userId, String pet1Id, String pet2Id) throws SQLException {
        System.out.println("创建领养申请...");

        // 申请 1
        String appId1 = UUID.randomUUID().toString();
        String sql1 = "insert into adoption_applications (id, applicator_id, pet_id, status) " +
                      "values (?, ?, ?, 'pending')";

        PreparedStatement pstmt1 = conn.prepareStatement(sql1);
        pstmt1.setString(1, appId1);
        pstmt1.setString(2, userId);
        pstmt1.setString(3, pet1Id);
        pstmt1.executeUpdate();
        pstmt1.close();

        System.out.println("  申请 1 - ID: " + appId1 + ", 申请人: " + userId + ", 宠物: " + pet1Id);

        // 申请 2
        String appId2 = UUID.randomUUID().toString();
        String sql2 = "insert into adoption_applications (id, applicator_id, pet_id, status) " +
                      "values (?, ?, ?, 'pending')";

        PreparedStatement pstmt2 = conn.prepareStatement(sql2);
        pstmt2.setString(1, appId2);
        pstmt2.setString(2, userId);
        pstmt2.setString(3, pet2Id);
        pstmt2.executeUpdate();
        pstmt2.close();

        System.out.println("  申请 2 - ID: " + appId2 + ", 申请人: " + userId + ", 宠物: " + pet2Id);
    }

    /**
     * 创建公告
     */
    private static void createAnnouncement(Connection conn, String adminId) throws SQLException {
        System.out.println("创建公告...");

        String id = UUID.randomUUID().toString();
        String sql = "insert into announcements (id, sender_id, title, content, create_time) " +
                     "values (?, ?, ?, ?, ?)";

        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, id);
        pstmt.setString(2, adminId);
        pstmt.setString(3, "欢迎使用宠物领养系统");
        pstmt.setString(4, "亲爱的用户们：\n\n" +
                "欢迎使用我们的宠物领养系统！我们致力于为流浪动物找到温暖的家。\n\n" +
                "系统功能：\n" +
                "1. 浏览可领养的宠物\n" +
                "2. 提交领养申请\n" +
                "3. 查看申请状态\n" +
                "4. 接收最新公告\n\n" +
                "如果您有任何问题，请联系管理员。\n\n" +
                "祝好！\n" +
                "宠物领养管理团队");
        pstmt.setLong(5, System.currentTimeMillis());
        pstmt.executeUpdate();
        pstmt.close();

        System.out.println("  公告 ID: " + id);
    }
}
