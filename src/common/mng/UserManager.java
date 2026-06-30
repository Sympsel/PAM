package common.mng;

import common.entity.User;

import java.util.logging.Logger;

import java.util.*;

public class UserManager {
    private static final Logger logger = Logger.getLogger(UserManager.class.getName());

    private static UserManager instance;
    private final List<User> usersRegistered;
    private final List<User> usersOnline;
    private final Map<String, User> mapOfIdToUser;

    private UserManager() {
        usersRegistered = new ArrayList<>();
        usersOnline = new ArrayList<>();
        mapOfIdToUser = new HashMap<>();
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public void register(User userInfo) {
        if (userInfo == null) {
            logger.severe("注册失败：用户信息为空");
            throw new IllegalArgumentException("用户信息不能为空");
        }
        // todo 字段合法性检查
        if (mapOfIdToUser.containsKey(userInfo.getId())) {
            logger.warning("用户已存在: " + userInfo.getId());
            return;
        } else {
            logger.info("用户注册成功: " + userInfo.getUsername());
        }
        mapOfIdToUser.put(userInfo.getId(), userInfo);
        usersRegistered.add(userInfo);
    }

    public String login(String username, String password) {
        // 参数验证
        if (username == null || username.trim().isEmpty()) {
            logger.warning("登录失败：用户名为空");
            return null;
        }
        if (password == null || password.isEmpty()) {
            logger.warning("登录失败：密码为空");
            return null;
        }

        Optional<User> currUser = usersRegistered.stream().filter(
                user -> user.getUsername().equals(username) && user.getPassword().equals(password)
        ).findFirst();

        if (currUser.isPresent()) {
            User user = currUser.get();
            if (!usersOnline.contains(user)) {
                usersOnline.add(user);
                logger.info(String.format("用户登录成功: username=%s, id=%s", username, user.getId()));
            } else {
                logger.warning(String.format("用户已在线: username=%s", username));
            }
            return user.getId();
        }

        logger.warning(String.format("用户名或密码错误: username=%s", username));
        return null;
    }

    public User getUserById(String id) {
        return mapOfIdToUser.get(id);
    }

    public void showAllUserRegistered() {
        showAll(usersRegistered);
    }

    public void showAllUserOnline() {
        showAll(usersOnline);
    }

    private void showAll(List<User> users) {
        System.out.println("[");
        for (User user : users) {
            System.out.println("\t" + user);
        }
        System.out.println("]");
    }
}
