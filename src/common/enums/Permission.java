package common.enums;


import lombok.Getter;

@Getter
public enum Permission {
    ADMIN("管理员"),
    NORMAL("普通用户");

    private final String description;

    Permission(String description) {
        this.description = description;
    }

}
