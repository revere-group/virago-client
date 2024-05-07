package dev.revere.virago.client.gui.menu;

import lombok.Getter;

@Getter
public enum EnumSelectDesign {
    DEFAULT("/assets/minecraft/virago/shader/noise.fsh"),
    BUBBLE("/assets/minecraft/virago/shader/bubble.fsh"),
    WATER("/assets/minecraft/virago/shader/water.fsh"),
    CITY("/assets/minecraft/virago/shader/city.fsh"),
    FIRE("/assets/minecraft/virago/shader/fire.fsh"),
    RAIN("/assets/minecraft/virago/shader/rain.fsh"),

    ;

    private final String shaderPath;

    EnumSelectDesign(String shaderPath) {
        this.shaderPath = shaderPath;
    }
}
