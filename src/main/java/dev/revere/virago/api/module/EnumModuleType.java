package dev.revere.virago.api.module;

import lombok.Getter;

/**
 * @author Remi
 * @project Virago
 * @date 3/17/2024
 */
@Getter
public enum EnumModuleType {

    COMBAT("Combat"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    PLAYER("Player"),
    MISC("Misc");

    private final String name;

    /**
     * EnumModuleType constructor to initialize the module type.
     *
     * @param name the name of the module type
     */
    EnumModuleType(String name) {
        this.name = name;
    }
}
