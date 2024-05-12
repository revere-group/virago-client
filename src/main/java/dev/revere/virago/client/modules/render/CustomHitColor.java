package dev.revere.virago.client.modules.render;

import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;

import java.awt.*;

/**
 * @author Remi
 * @project Virago
 * @date 5/12/2024
 */
@ModuleData(name = "CustomHitColor", displayName = "Custom Hit Color", description = "Change the color of the hit color", type = EnumModuleType.RENDER)
public class CustomHitColor extends AbstractModule {

    public final Setting<Color> colorSetting = new Setting<>("Color", new Color(100, 100, 200, 255));
}
