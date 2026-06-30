import common.mng.UserManager;

public class Main {
    public static void main(String[] args) {
        UserManager userManager = UserManager.getInstance();
//        userManager.register(new User("admin", "x", User.Permission.Admin, "123456"));
//        userManager.register(new User("zzzz", "admin", User.Permission.Normal, "123457"));
//        userManager.register(new User("zzzz", "admin", User.Permission.Normal, "123457"));
        userManager.showAllUserRegistered();
        userManager.login("admin", "x");
        userManager.showAllUserOnline();
    }
}
