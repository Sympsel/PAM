import client.AdminClient;
import common.entity.User;
import common.manager.UserManager;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class Main {
    public static void main(String[] args) {
//        demo1();
//        demo2();
//        demoJLine();
//        demo3();
        demo4();
    }


    private static void demo4() {
        AdminClient adminClient = new AdminClient();
        adminClient.start();
    }

//    private static void demo3() {
//        UserManager userManager = UserManager.getInstance();
//        userManager.register(new User("admin", "x", User.Permission.Admin, new User.Profile(
//                "admin", "123456", "上海"
//        )));
//        AdminClient adminClient = new AdminClient();
//        adminClient.start();
//    }

//    private static void demo1() {
//        UserManager userManager = UserManager.getInstance();
//        userManager.register(new User("admin", "x", User.Permission.Admin, "123456"));
//        userManager.register(new User("zzzz", "admin", User.Permission.Normal, "123457"));
//        userManager.register(new User("zzzz", "admin", User.Permission.Normal, "123457"));
//        userManager.showAllUserRegistered();
//        userManager.login("admin", "x");
//        userManager.showAllUserOnline();
//}
//    private static void demo2() {
//        UserManager userManager = UserManager.getInstance();
//        userManager.register(new User("admin", "admin", User.Permission.Admin,
//                new User.Profile("admin", "123456", "上海")));
//
//        AdminClient adminClient = new AdminClient();
//        adminClient.start();
//    }

    /**
     * JLine 3 完整使用示例
     * 展示各种常用功能
     */
    private static void demoJLine() {
        try {
            Terminal terminal = TerminalBuilder.builder().build();

            // 创建命令补全器
            StringsCompleter completer = new StringsCompleter(
                    java.util.Arrays.asList("help", "hello", "world", "exit", "quit", "status")
            );

            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(completer)
                    .build();

            terminal.writer().println("========================================");
            terminal.writer().println("  JLine 3 功能演示");
            terminal.writer().println("========================================");
            terminal.writer().println();

            // 示例1: 基本输入
            terminal.writer().println("【示例1】基本输入");
            String name = reader.readLine("请输入您的姓名: ");
            terminal.writer().println("您好, " + name + "!");
            terminal.writer().println();

            // 示例2: 密码输入（隐藏输入内容）
            terminal.writer().println("【示例2】密码输入（输入时会显示 *）");
            String password = reader.readLine("请输入密码: ", '*');
            terminal.writer().println("密码已接收（长度: " + password.length() + "）");
            terminal.writer().println();

            // 示例3: 带提示的输入
            terminal.writer().println("【示例3】带默认值的输入");
            String email = reader.readLine("请输入邮箱 (默认: user@example.com): ");
            if (email == null || email.trim().isEmpty()) {
                email = "user@example.com";
            }
            terminal.writer().println("邮箱: " + email);
            terminal.writer().println();

            // 示例4: 数字输入和验证
            terminal.writer().println("【示例4】数字输入验证");
            int age = 0;
            boolean valid = false;
            while (!valid) {
                try {
                    String input = reader.readLine("请输入年龄: ");
                    age = Integer.parseInt(input);
                    if (age < 0 || age > 150) {
                        terminal.writer().println("年龄必须在 0-150 之间，请重新输入");
                        continue;
                    }
                    valid = true;
                } catch (NumberFormatException e) {
                    terminal.writer().println("请输入有效的数字");
                }
            }
            terminal.writer().println("年龄: " + age);
            terminal.writer().println();

            // 示例5: 多行输入（Ctrl+D 结束）
            terminal.writer().println("【示例5】多行输入（按 Ctrl+D 结束）");
            terminal.writer().println("请输入一段文字:");
            StringBuilder multiLine = new StringBuilder();
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    multiLine.append(line).append("\n");
                }
            } catch (EndOfFileException e) {
                // Ctrl+D 触发，正常结束多行输入
                terminal.writer().println("(输入结束)");
            }
            terminal.writer().println("您输入了 " + multiLine.toString().split("\n").length + " 行文字");
            terminal.writer().println();

            // 示例6: 命令补全功能
            terminal.writer().println("【示例6】命令补全（尝试输入 hel/wo/按Tab键）");

            for (int i = 0; i < 3; i++) {
                String cmd = reader.readLine("命令> ");
                if (cmd == null || cmd.equalsIgnoreCase("exit")) {
                    break;
                }
                terminal.writer().println("执行命令: " + cmd);
            }
            terminal.writer().println();

            // 示例7: 处理用户中断（Ctrl+C）
            terminal.writer().println("【示例7】处理 Ctrl+C 中断");
            try {
                String input = reader.readLine("按 Ctrl+C 试试: ");
                terminal.writer().println("你输入了: " + input);
            } catch (UserInterruptException e) {
                terminal.writer().println("检测到 Ctrl+C！程序继续运行...");
            }
            terminal.writer().println();

            // 示例8: 跳过空行直到有内容
            terminal.writer().println("【示例8】跳过空行，直到有真实输入");
            String content = "";
            while (content.isEmpty()) {
                terminal.writer().print("请输入内容（空行无效）: ");
                terminal.flush();
                content = reader.readLine();
                if (content == null) {
                    terminal.writer().println("取消输入");
                    break;
                }
                content = content.trim();
                if (content.isEmpty()) {
                    terminal.writer().println("输入不能为空，请重试");
                }
            }
            if (!content.isEmpty()) {
                terminal.writer().println("收到内容: " + content);
            }
            terminal.writer().println();

            // 示例9: 菜单选择
            terminal.writer().println("【示例9】菜单选择");
            terminal.writer().println("1. 选项A");
            terminal.writer().println("2. 选项B");
            terminal.writer().println("3. 选项C");
            String choice = reader.readLine("请选择 (1-3): ");
            switch (choice) {
                case "1" -> terminal.writer().println("选择了 A");
                case "2" -> terminal.writer().println("选择了 B");
                case "3" -> terminal.writer().println("选择了 C");
                default -> terminal.writer().println("无效选择");
            }
            terminal.writer().println();

            // 示例10: 带超时的等待
            terminal.writer().println("【示例10】倒计时演示");
            for (int i = 5; i > 0; i--) {
                terminal.writer().print("\r倒计时: " + i + " 秒...");
                terminal.flush();
                Thread.sleep(1000);
            }
            terminal.writer().println("\r倒计时结束！              ");
            terminal.writer().println();

            terminal.writer().println("========================================");
            terminal.writer().println("  演示结束！感谢体验 JLine 3");
            terminal.writer().println("========================================");
            terminal.flush();

            terminal.close();

        } catch (UserInterruptException e) {
            System.out.println("\n程序被用户中断");
        } catch (EndOfFileException e) {
            System.out.println("\n输入流结束");
        } catch (Exception e) {
            System.err.println("发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
