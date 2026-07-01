package common.entity;

import common.enums.Permission;
import lombok.*;

import java.util.Objects;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {
    /**
     * 用户 UUID
     */
    private final String id;
    /**
     * 用户名
     */
    private String username;
    /**
     * 加密后的密码
     */
    private String password;
    /**
     * 权限
     */
    private Permission permission;
    /**
     * 用户实名信息，没有表示未实名
     */
    private Profile profile;


    /**
     * 创建时间戳
     */
    private final long createTime;

    /**
     * 从数据库记录创建 User 对象
     *
     * @warn 仅供 DAO 层使用
     */
    public static User createFromDatabase(String id, String username, String password,
                                          Permission permission, Profile profile, long createTime) {
        return User.builder()
                .id(id)
                .username(username)
                .password(password)
                .permission(permission)
                .profile(profile)
                .createTime(createTime)
                .build();
    }

    public User(String name, String password, Permission permission) {
        this(name, password, permission, null);
    }

    public User(String name, String password, Permission permission, Profile profile) {
        this.id = java.util.UUID.randomUUID().toString();
        this.username = name;
        this.password = password;
        this.permission = permission;
        this.profile = profile;
        this.createTime = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);  // UUID 是唯一标识
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String toString() {
        return String.format("{id=%s,name=%s,password=%s,role=%s, profile={%s}}",
                id, username, password, permission, profile);
    }

    /**
     * @brief 用户档案信息
     */
    @Getter
    @Setter
    public static class Profile {
        private String realName;
        private String phone;
        private String address;

        public Profile(String realName, String phone, String address) {
            this.realName = realName;
            this.phone = phone;
            if (address == null || address.isEmpty()) {
                this.address = "未知";
            } else {
                this.address = address;
            }
        }

        public Profile(String realName, String phone) {
            this(realName, phone, "未知");
        }

        public String toString() {
            return String.format("{realName=%s,phone=%s,address=%s}",
                    realName, phone, address);
        }
    }
}