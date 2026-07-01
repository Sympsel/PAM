package common.enums;

import lombok.Getter;

@Getter
public enum PetStatus {
    AVAILABLE("待领养"),
    PENDING("申请中"),
    ADOPTED("已领养");

    private final String description;

    PetStatus(String description) {
        this.description = description;
    }

}