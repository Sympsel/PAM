package client;

import org.jline.reader.LineReader;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.api.ServerApi;

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

    protected final ServerApi api = new ServerApi();

    protected static final Logger logger = LoggerFactory.getLogger(Client.class.getName());
    protected Terminal terminal;
    protected StringsCompleter completer = new StringsCompleter(
            java.util.Arrays.asList("help", "exit", "status")
    );
    protected LineReader reader;

    protected String username = null;
    protected String ip = null;
    protected short port = 0;
    protected Status status = Status.LOGGING;
    protected int tryTimes = 0;


    protected abstract void login();

    protected abstract void exit();

    public abstract void start();
}
