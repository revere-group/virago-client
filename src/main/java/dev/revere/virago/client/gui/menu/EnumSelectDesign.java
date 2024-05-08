package dev.revere.virago.client.gui.menu;

import lombok.Getter;

@Getter
public enum EnumSelectDesign {
    DEFAULT("/assets/minecraft/virago/shader/background/noise.fsh", "virago/shader/background/preview/default.png"),
    BUBBLE("/assets/minecraft/virago/shader/background/bubble.fsh", "virago/shader/background/preview/bubble.png"),
    WATER("/assets/minecraft/virago/shader/background/water.fsh", "virago/shader/background/preview/water.png"),
    CITY("/assets/minecraft/virago/shader/background/city.fsh", "virago/shader/background/preview/city.png"),
    FIRE("/assets/minecraft/virago/shader/background/fire.fsh", "virago/shader/background/preview/fire.png"),
    RAIN("/assets/minecraft/virago/shader/background/rain.fsh", "virago/shader/background/preview/rain.png"),

    ;

    private final String shaderPath;
    private final String shaderPreviewPath;

    EnumSelectDesign(String shaderPath, String shaderPreviewPath) {
        this.shaderPath = shaderPath;
        this.shaderPreviewPath = shaderPreviewPath;
    }
}
