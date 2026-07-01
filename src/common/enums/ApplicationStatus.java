package common.enums;

import lombok.Getter;

@Getter
public enum ApplicationStatus {
    PENDING("待审核"),
    APPROVED("已通过"),
    REJECTED("已拒绝");

    private final String description;

    ApplicationStatus(String description) {
        this.description = description;
    }

}