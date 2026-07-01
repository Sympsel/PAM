package common.utils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
public class Menu {
    private final String title;
    private final List<MenuItem> items;
    private final LineReader reader;
    private final Terminal terminal;

    /**
     * 构造函数
     *
     * @param title 菜单标题
     * @param reader JLine 阅读器
     * @param terminal 终端
     */
    public Menu(String title, LineReader reader, Terminal terminal) {
        this.title = title;
        this.items = new ArrayList<>();
        this.reader = reader;
        this.terminal = terminal;
    }

    /**
     * 添加菜单项
     *
     * @param number 选项编号，在调用 addBackItem 前后，不能占用用编号0
     * @param label 选项标签
     * @param action 执行动作
     * @return 当前菜单对象（链式调用）
     */
    public Menu addItem(int number, String label, Runnable action) {
        items.add(new MenuItem(number, label, action));
        return this;
    }

    /**
     * 添加返回上级菜单项
     *
     * @return 当前菜单对象（链式调用）
     */
    public Menu addBackItem() {
        return addItem(0, "返回上级菜单", null);
    }

    /**
     * 显示菜单
     */
    public void display() {
        System.out.println("========= " + title + " =========");
        for (MenuItem item : items) {
            System.out.println(item);
        }
        System.out.println("===============================");
    }

    /**
     * 读取用户选择并执行
     *
     * @return true 表示继续显示菜单，false 表示退出
     */
    public boolean promptAndExecute() {
        try {
            String line = reader.readLine("> ");
            if (line == null) {
                return false; // EOF，退出
            }

            line = line.trim();
            if (line.isEmpty()) {
                return true; // 空输入，继续
            }

            int choice;
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                terminal.writer().println("请输入有效的数字");
                terminal.flush();
                return true;
            }

            // 查找匹配的菜单项
            for (MenuItem item : items) {
                if (item.getNumber() == choice) {
                    if (choice == 0) {
                        return false; // 返回上级
                    }
                    item.execute();
                    return true; // 继续显示当前菜单
                }
            }

            terminal.writer().println("无效选项，请重新选择");
            terminal.flush();
            return true;

        } catch (Exception e) {
            terminal.writer().println("读取输入失败: " + e.getMessage());
            terminal.flush();
            return false;
        }
    }

    /**
     * 循环显示菜单直到用户选择退出
     */
    public void run() {
        do {
            display();
        } while (promptAndExecute());
    }

    /**
     * 创建子菜单
     *
     * @param title 子菜单标题
     * @return 新的菜单对象
     */
    public Menu createSubMenu(String title) {
        return new Menu(title, reader, terminal);
    }

    @Data
    public static class MenuItem {
        private final int number;
        private final String label;
        private final Runnable action;

        public void execute() {
            if (action != null) {
                action.run();
            }
        }

        @Override
        public String toString() {
            return String.format("%d. %s", number, label);
        }
    }
}
