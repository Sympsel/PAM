package common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@Slf4j
public class Validator {


    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^1[3-9]\\d{9}$");

    public static boolean isValidPhone(String phone) {
        if (Config.TEST_MODE) {
            return phone != null;
        }
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidUsername(String username) {
        if (username == null) return false;
        int len = username.length();
        if (len < Config.UsernameRules.MIN_LENGTH || len > Config.UsernameRules.MAX_LENGTH) {
            return false;
        }
        String pattern = Config.UsernameRules.ALLOW_UNDERSCORE
                ? "^[a-zA-Z0-9_]+$" : "^[a-zA-Z0-9]+$";
        return Pattern.matches(pattern, username);
    }

    public static boolean isValidPassword(String password) {
        if (Config.TEST_MODE) {
            return true;
        }
        if (password == null) return false;

        int len = password.length();
        if (len < Config.PasswordRules.MIN_LENGTH || len > Config.PasswordRules.MAX_LENGTH) {
            return false;
        }
        if (Config.PasswordRules.REQUIRE_LETTER && !password.matches(".*[a-zA-Z].*")) {
            return false;
        }
        if (Config.PasswordRules.REQUIRE_DIGIT && !password.matches(".*\\d.*")) {
            return false;
        }
        return !Config.PasswordRules.REQUIRE_SPECIAL || password.matches(".*[^a-zA-Z0-9].*");
    }
}
