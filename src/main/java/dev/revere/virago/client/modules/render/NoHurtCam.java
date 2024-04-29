package dev.revere.virago.client.modules.render;

import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;


@ModuleData(name = "NoHurtCam", displayName = "No Hurt Cam", description = "Disable hurt camera", type = EnumModuleType.RENDER)
public class NoHurtCam extends AbstractModule {

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
