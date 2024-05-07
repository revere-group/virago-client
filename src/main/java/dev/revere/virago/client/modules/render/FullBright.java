package dev.revere.virago.client.modules.render;

import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import net.minecraft.client.Minecraft;

@ModuleData(name = "FullBright", displayName = "Full Bright", description = "No more black, just white.", type = EnumModuleType.RENDER)
public class FullBright extends AbstractModule {

    private float oldGamma;

    @Override
    public void onEnable() {
        if (Minecraft.getMinecraft().gameSettings.gammaSetting != 1000) {
            oldGamma = Minecraft.getMinecraft().gameSettings.gammaSetting;
        }

        Minecraft.getMinecraft().gameSettings.gammaSetting = 1000;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        Minecraft.getMinecraft().gameSettings.gammaSetting = oldGamma;
        super.onDisable();
    }
}
