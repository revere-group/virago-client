package dev.revere.virago.client.modules.render;

import dev.revere.virago.Virago;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.client.gui.panel.PanelGUI;
import org.lwjgl.input.Keyboard;

/**
 * @author Remi
 * @project Virago
 * @date 3/20/2024
 */

@ModuleData(name = "GUI", description = "The GUI for the client.", type = EnumModuleType.MISC)
public class ClickGUI extends AbstractModule {

    public ClickGUI() {
        setKey(Keyboard.KEY_RSHIFT);
    }

    @Override
    public void onEnable() {
        this.toggle();
        mc.displayGuiScreen(Virago.getInstance().getPanelGUI());
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

}
