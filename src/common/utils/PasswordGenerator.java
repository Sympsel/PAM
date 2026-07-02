package common.utils;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

/**
 * 密码生成工具 - 交互式生成加密后的管理员密码
 */
public class PasswordGenerator {

    public static void main(String[] args) {

        LineReader reader;
        try (Terminal terminal = TerminalBuilder.builder().build()) {
            try {
                reader = LineReaderBuilder.builder()
                        .terminal(terminal)
                        .build();

                System.out.println("========== 管理员密码加密工具 ==========");
                System.out.println();

                // 读取明文密码
                String password = reader.readLine("请输入要加密的密码: ", '*');

                if (password == null || password.trim().isEmpty()) {
                    System.out.println("\n密码不能为空");
                    return;
                }

                String confirmPassword = reader.readLine("请再次输入密码: ", '*');

                if (!password.equals(confirmPassword)) {
                    System.out.println("\n两次输入的密码不一致！");
                    return;
                }

                // 加密
                String encodedPassword = PasswordEncoder.encode(password);

                // 显示结果
                System.out.println();
                System.out.println("========== 密码加密结果 ==========");
                System.out.println("明文密码: " + password);
                System.out.println("加密密码: " + encodedPassword);
                System.out.println("==================================");
                System.out.println();

                // 询问是否生成 SQL
                String generateSQL = reader.readLine("是否生成 SQL 插入语句？(y/n): ");

                if (generateSQL != null && generateSQL.trim().equalsIgnoreCase("y")) {
                    String username = reader.readLine("请输入管理员用户名 [admin]: ");
                    if (username == null || username.trim().isEmpty()) {
                        username = "admin";
                    }

                    String realName = reader.readLine("请输入真实姓名 [管理员]: ");
                    if (realName == null || realName.trim().isEmpty()) {
                        realName = "管理员";
                    }

                    System.out.println();
                    System.out.println("========== SQL 插入语句 ==========");
                    System.out.printf("""
                            insert into users (id, username, password_hash, permission, real_name, phone, address, is_online, create_time)
                            values (uuid(), '%s', '%s', 'admin', '%s', null, null, false,  unix_timestamp() * 1000);
                            """, username, encodedPassword, realName
                    );
                    System.out.println("==================================");
                    System.out.println();
                    System.out.println("复制上面的 SQL 语句在数据库中执行即可创建管理员账户");
                }

            } catch (Exception e) {
                System.err.println("错误: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            // 忽略关闭异常
        }
    }
}
