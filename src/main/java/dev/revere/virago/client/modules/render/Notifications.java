package dev.revere.virago.client.modules.render;

import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;

@ModuleData(name = "Notifications", type = EnumModuleType.RENDER, description = "Render notifications for modules and errors")
public class Notifications extends AbstractModule {

    public final Setting<Boolean> moduleNotifications = new Setting<>("Module Notifications", true);

    public Notifications() {
        setEnabledSilent(true);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
