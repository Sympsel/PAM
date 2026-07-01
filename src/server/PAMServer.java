package server;

import common.manager.AdoptionApplicationManager;
import common.manager.AnnouncementManager;
import common.manager.UserManager;

import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;

public class PAMServer {
    private ServerSocket serverSocket;
    private int port;
    private ExecutorService threadPool;
    private UserManager userManager;
//    private PetManager petManager;
    private AdoptionApplicationManager applicationManager;
    private AnnouncementManager announcementManager;

    public void start() {
        // 启动服务器，监听端口
        // 接受客户端连接，为每个客户端创建独立线程
    }
}