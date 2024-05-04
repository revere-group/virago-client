package dev.revere.virago.client.modules.movement;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.client.events.player.UpdateEvent;
import dev.revere.virago.client.modules.combat.KillAura;
import dev.revere.virago.client.modules.player.Scaffold;
import dev.revere.virago.client.services.ModuleService;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

/**
 * @author Zion
 * @project Virago
 * @date 03/05/2024
 */

@ModuleData(name = "InventoryMove", displayName = "Inventory Move", description = "N/A", type = EnumModuleType.MOVEMENT)
public class InventoryMove extends AbstractModule {

    @EventHandler
    private final Listener<UpdateEvent> onUpdate = event -> {
        if (mc.currentScreen != null) {
            ModuleService moduleService = Virago.getInstance().getServiceManager().getService(ModuleService.class);

            if (mc.currentScreen instanceof GuiChat || moduleService.getModule(Scaffold.class).isEnabled() || moduleService.getModule(KillAura.class).getSingleTarget() != null) {
                return;
            }

            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()));
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()));
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode()));
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()));
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()));
            EntityPlayerSP player = mc.thePlayer;

            if (Keyboard.isKeyDown(208) && mc.thePlayer.rotationPitch < 90.0F) {
                player.rotationPitch += 6.0F;
            }

            if (Keyboard.isKeyDown(200) && mc.thePlayer.rotationPitch > -90.0F) {
                player.rotationPitch -= 6.0F;
            }

            if (Keyboard.isKeyDown(205)) {
                player.rotationYaw += 6.0F;
            }

            if (Keyboard.isKeyDown(203)) {
                player.rotationYaw -= 6.0F;
            }
        }
    };
}
