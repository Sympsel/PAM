package common.enums;

import lombok.Getter;

@Getter
public enum PetSpecies {
    CAT("猫"),
    DOG("狗"),
    OTHER("其他");

    private final String description;

    PetSpecies(String description) {
        this.description = description;
    }

}
