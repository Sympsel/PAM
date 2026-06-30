package common.entity;

import java.util.Objects;

public class User {
    public enum Permission {
        Admin,
        Normal
    }

    // we use uuid
    private final String id;
    private String username;
    // 加密后的密码
    private String password;
    // 用户实名信息，没有表示未实名
    private Profile profile;

    private Permission role;
    // 创建时间戳
    private final long createTime;

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Permission getRole() {
        return role;
    }

    public void setRole(Permission role) {
        this.role = role;
    }

    public long getCreateTime() {
        return createTime;
    }

    public User(String name, String password, Permission role) {
        this(name, password, role, null);
    }

    public User(String name, String password, Permission role, Profile profile) {
        this.id = java.util.UUID.randomUUID().toString();
        this.username = name;
        this.password = password;
        this.role = role;
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
                id, username, password, role, profile);
    }

    /**
     * @brief 用户档案信息
     */
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

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String toString() {
            return String.format("{realName=%s,phone=%s,address=%s}",
                    realName, phone, address);
        }
    }
}