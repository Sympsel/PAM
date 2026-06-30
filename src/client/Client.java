package client;

/**
 * 客户端抽象基类
 */
public abstract class Client {
    public enum Status {
        LOGGING,
        ONLINE,
        FORBIDDEN,
        EXIT
    }

    protected String username = null;
    protected String ip = null;
    protected short port = 0;
    protected Status status = Status.LOGGING;
    protected int tryTimes = 0;


    protected abstract void login();
    protected abstract void showMenu();
    protected abstract void exit();
    public abstract void start();
}
