package client;

import common.mng.AdoptionApplicationManager;
import common.mng.UserManager;

import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Logger;

public class AdminClient extends Client {
    private enum OnlineStatus {
        Selecting,

        SelectAllApply,
        Waiting,
        Handling,
        Back
    }

    private enum HandingStatus {
        Comment,
        Choose
    }

    private static final Logger logger = Logger.getLogger(AdminClient.class.getName());
    private String adminId;
    private final Scanner sc = new Scanner(System.in);
    private String message;
    private OnlineStatus onlineStatus;
    private HandingStatus handingStatus;


    public AdminClient() {
        status = Status.LOGGING;
        onlineStatus = OnlineStatus.Selecting;
        handingStatus = HandingStatus.Comment;
    }

    @Override
    protected void showMenu() {
        beginShow("menu");

        // 查询所有申领请求
        System.out.println("1. 查询所有申领请求");
        System.out.println("2. 开始处理申领请求，按下 \"ESC\" 返回");
        System.out.println("3. 退出");
        endShow();
        System.out.print("> ");
    }

    private void handle() {
        AdoptionApplicationManager adoptionApplicationManager = AdoptionApplicationManager.getInstance();
        while (onlineStatus != OnlineStatus.Back) {
            switch (onlineStatus) {
                case Selecting -> {
                    int op = sc.nextInt();
                    if (op == 1) {
                        // todo selectAll
                        onlineStatus = OnlineStatus.SelectAllApply;
                    } else if (op == 2) {
                        if (adoptionApplicationManager.hasNext()) {
                            // 等待请求到来
                            onlineStatus = OnlineStatus.Waiting;
                        } else {
                            onlineStatus = OnlineStatus.Handling;
                        }
                    } else if (op == 3) {
                        onlineStatus = OnlineStatus.Back;
                    }
                }
                case Waiting -> {
                    /* 每隔一秒超时一次，每次超时时检测是否输入ctrl + D，
                        如果是直接返回
                        否则检测是否有新请求
                            如果有切换到处理状态
                            否则重新打印 "等待新申请..."，进行下一轮等待
                    */
                    while (onlineStatus == OnlineStatus.Waiting) {
                        System.out.println("等待新申请... (按 Ctrl+D 返回菜单)");

                        try {
                            // 等待 1 秒
                            Thread.sleep(1000);

                            // 检查是否有 Ctrl+D 输入
                            if (!sc.hasNext()) {
                                onlineStatus = OnlineStatus.Back;
                                break;
                            }

                            // 检查是否有新请求
                            if (adoptionApplicationManager.hasNext()) {
                                logger.info("检测到新申请，进入处理状态");
                                onlineStatus = OnlineStatus.Handling;
                                break;
                            }

                            // 没有新请求，继续循环，重新打印提示

                        } catch (InterruptedException e) {
                            logger.warning("等待被中断");
                            onlineStatus = OnlineStatus.Back;
                            break;
                        }
                    }
                }
                case Handling -> {
                    int op = -1;
                    String comment = "";

                    while (adoptionApplicationManager.hasNext()) {
                        switch (handingStatus) {
                            case Comment -> {
                                boolean flag = true;

                                while (flag) {
                                    flag = false;
                                    System.out.println("[== 1. 接受该条 2. 拒绝该条 3. 搁置该条 ==]");
                                    System.out.print(">> ");

                                    op = sc.nextInt();
                                    if (op == 3) {
                                        // 将当前请求移到队列末尾，继续处理下一个
                                        adoptionApplicationManager.handleLater();
                                        handingStatus = HandingStatus.Comment;
                                        // 检查是否还有下一个
                                        if (!adoptionApplicationManager.hasNext()) {
                                            break;
                                        }
                                        continue;
                                    } else if (op != 1 && op != 2) {
                                        System.out.println("无效输入，请重新输入");
                                        flag = true;
                                    }
                                }

                                System.out.println("请输入审核意见（Ctrl+D 结束输入）：");
                                sc.nextLine(); // 消耗换行符

                                try {
                                    StringBuilder commentBuilder = new StringBuilder();
                                    while (sc.hasNextLine()) {
                                        String line = sc.nextLine();
                                        commentBuilder.append(line).append("\n");
                                    }
                                    comment = commentBuilder.toString().trim();
                                } catch (NoSuchElementException e) {
                                    // Ctrl+D 触发，正常结束输入
                                    logger.fine("评论输入结束");
                                }

                                handingStatus = HandingStatus.Choose;
                            }
                            case Choose -> {
                                if (op == 1) {
                                    adoptionApplicationManager.passOne(adminId, comment);
                                } else if (op == 2) {
                                    adoptionApplicationManager.rejectOne(adminId, comment);
                                }

                                handingStatus = HandingStatus.Comment;
                                System.out.println("处理完成，回车继续 或按 Ctrl+D 退出");
                                if (!sc.hasNextInt()) {
                                    logger.info("检测到 EOF，返回菜单");
                                    onlineStatus = OnlineStatus.Back;
                                    return;
                                }
                            }
                        }
                    }
                    // 队列为空，继续等待
                    onlineStatus = OnlineStatus.Waiting;
                }
            }
        }
    }

    @Override
    protected void login() {
        beginShow("login");
        System.out.println("请输入管理员用户名：");
        String username = sc.next();
        // todo 接收要访问的服务器地址和端口
        System.out.println("请输入密码：");
        String password = sc.next();
        String adminId = UserManager.getInstance().login(username, password);

        if (adminId != null) {
            this.adminId = adminId;
            logger.info(String.format("欢迎管理员: %s", UserManager.getInstance().getUserById(adminId).getUsername()));
            status = Status.ONLINE;
        } else {
            logger.warning("登录失败：用户名或密码错误");
            tryTimes++;
            if (tryTimes >= 3) {
                logger.severe("登录失败次数过多，退出系统");
                status = Status.FORBIDDEN;
            }
        }
        endShow();
    }

    @Override
    public void start() {
        while (status != Status.EXIT) {
            switch (status) {
                case LOGGING -> login();
                case ONLINE -> {
                    showMenu();
                    handle();
                }
            }
        }
    }

    @Override
    public void exit() {

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
