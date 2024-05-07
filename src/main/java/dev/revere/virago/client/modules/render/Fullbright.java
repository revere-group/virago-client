package dev.revere.virago.client.modules.render;

import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import net.minecraft.client.Minecraft;

@ModuleData(name = "Fullbright", displayName = "Fullbright", description = "No more black, just white.", type = EnumModuleType.RENDER)
public class Fullbright extends AbstractModule {

    private float oldGamma;

    public Fullbright() {
        setEnabledSilent(true);
    }

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
