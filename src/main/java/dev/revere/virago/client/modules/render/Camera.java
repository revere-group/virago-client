package dev.revere.virago.client.modules.render;

import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;

@ModuleData(name = "Camera", description = "Change camera options", type = EnumModuleType.RENDER)
public class Camera extends AbstractModule {

    public final Setting<Boolean> hurtCam = new Setting<>("Hurt Camera", false);

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
