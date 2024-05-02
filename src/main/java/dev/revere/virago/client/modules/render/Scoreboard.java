package dev.revere.virago.client.modules.render;

import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/1/2024
 */
@ModuleData(name = "Scoreboard", displayName = "Scoreboard", description = "Renders a scoreboard", type = EnumModuleType.RENDER)
public class Scoreboard extends AbstractModule {

    public final Setting<Boolean> numbers = new Setting<>("Numbers", false)
            .describedBy("Whether to render numbers on the scoreboard");

    public final Setting<Integer> yOffset = new Setting<>("Y Offset", 0)
            .minimum(0)
            .maximum(250)
            .incrementation(1)
            .describedBy("The Y offset of the scoreboard");


    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
