package dev.revere.virago.api.anticheat;

import lombok.Getter;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/19/2024
 */
@Getter
public enum EnumCheckType {

    COMBAT("Combat"),
    MOVEMENT("Movement"),
    OTHER("Other")

    ;

    private final String name;

    /**
     * Instantiates a new Enum check type.
     *
     * @param name the name
     */
    EnumCheckType(String name) {
        this.name = name;
    }

}
