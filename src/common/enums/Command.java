package common.enums;

public enum Command {
    // 用户相关
    LOGIN,
    REGISTER,
    LOGOUT,
    UPDATE_PROFILE,

    // 宠物相关
    ADD_PET,
    UPDATE_PET,
    DELETE_PET,
    QUERY_PETS,

    // 申请相关
    SUBMIT_APPLICATION,
    REVIEW_APPLICATION,
    QUERY_APPLICATIONS,

    // 公告相关
    PUBLISH_ANNOUNCEMENT,
    UPDATE_ANNOUNCEMENT,
    DELETE_ANNOUNCEMENT,
    QUERY_ANNOUNCEMENTS,

    // 统计相关
    GET_STATISTICS;

    public static Command fromString(String cmd) {
        try {
            return valueOf(cmd.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
