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
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

/**
 * @author Zion
 * @project Virago
 * @date 03/05/2024
 */

@ModuleData(name = "InventoryMove", displayName = "Inventory Move", description = "N/A", type = EnumModuleType.MOVEMENT)
public class InventoryMove extends AbstractModule {

    private final KeyBinding[] bindings = new KeyBinding[]{
            mc.gameSettings.keyBindForward,
            mc.gameSettings.keyBindBack,
            mc.gameSettings.keyBindRight,
            mc.gameSettings.keyBindLeft,
            mc.gameSettings.keyBindJump
    };

    @EventHandler
    private final Listener<UpdateEvent> onUpdate = event -> {
        if (mc.currentScreen != null) {
            ModuleService moduleService = Virago.getInstance().getServiceManager().getService(ModuleService.class);

            if (mc.currentScreen instanceof GuiChat
                    || moduleService.getModule(Scaffold.class).isEnabled()
                    || moduleService.getModule(KillAura.class).getSingleTarget() != null
                    || moduleService.getModule(Speed.class).isEnabled())

                return;

            EntityPlayerSP player = mc.thePlayer;

            for (final KeyBinding bind : bindings) {
                bind.setPressed(GameSettings.isKeyDown(bind));
            }

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

            if(player.getSpeed() >= 0.12) {
                player.motionX = player.motionX * 0.6;
                player.motionZ = player.motionZ * 0.6;
            }

            if(player.onGround) {
                player.motionX = player.motionX * 0.76;
                player.motionZ = player.motionZ * 0.76;
            }
        }
    };
}
