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
import net.minecraft.potion.Potion;

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
            mc.gameSettings.keyBindLeft
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

            if(player.getSpeed() >= 0.12) {
                player.motionX = player.motionX * 0.6;
                player.motionZ = player.motionZ * 0.6;
            }

            if(player.getActivePotionEffect(Potion.moveSpeed) == null) {
                player.motionX = player.motionX * 0.74;
                player.motionZ = player.motionZ * 0.74;
                return;
            }

            int amplifier = player.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1;

            switch(amplifier) {
                case 1:
                    player.motionX = player.motionX * 0.52;
                    player.motionZ = player.motionZ * 0.52;
                    break;
                case 2:
                    player.motionX = player.motionX * 0.31;
                    player.motionZ = player.motionZ * 0.31;
                    break;
            }
        }
    };
}
