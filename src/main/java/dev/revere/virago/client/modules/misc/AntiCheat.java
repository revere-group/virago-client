package dev.revere.virago.client.modules.misc;

import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/21/2024
 */
@ModuleData(name = "AntiCheat", description = "Prevents you from getting banned", type = EnumModuleType.MISC)
public class AntiCheat extends AbstractModule {

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
