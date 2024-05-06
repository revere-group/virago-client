package dev.revere.virago.client.modules.render;

import dev.revere.virago.Virago;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import org.lwjgl.input.Keyboard;

/**
 * @author Remi
 * @project Virago
 * @date 3/20/2024
 */

@ModuleData(name = "GUI", displayName = "GUI", description = "The GUI for the client.", type = EnumModuleType.MISC)
public class ClickGUI extends AbstractModule {

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.DROPDOWN);

    public ClickGUI() {
        setKey(Keyboard.KEY_RSHIFT);
    }

    @Override
    public void onEnable() {
        this.toggle();

        switch (mode.getValue()) {
            case DROPDOWN:
                mc.displayGuiScreen(Virago.getInstance().getPanelGUI());
                break;
            case MATERIAL:
                mc.displayGuiScreen(Virago.getInstance().getMenuImpl());
                break;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    private enum Mode {
        DROPDOWN, MATERIAL
    }
}
