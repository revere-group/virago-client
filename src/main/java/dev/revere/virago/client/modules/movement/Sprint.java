package dev.revere.virago.client.modules.movement;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.update.UpdateEvent;
import dev.revere.virago.client.modules.player.Scaffold;
import dev.revere.virago.client.services.ModuleService;
import net.minecraft.client.entity.EntityPlayerSP;

@ModuleData(name = "Sprint", description = "Keep you sprinting", type = EnumModuleType.MOVEMENT)
public class Sprint extends AbstractModule {
    private final Setting<Boolean> omniDirectional = new Setting<>("Omni", false);

    @EventHandler
    private final Listener<UpdateEvent> onUpdate = event -> {
        EntityPlayerSP player = mc.thePlayer;
        if(player.isMoving() && !Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(Scaffold.class).isEnabled()) {
            if(omniDirectional.getValue()) {
                player.setSprinting(true);
            } else if(player.isMoving() && player.moveForward >= Math.abs(player.moveStrafing)) {
                player.setSprinting(true);
            }
        }
    };
}
