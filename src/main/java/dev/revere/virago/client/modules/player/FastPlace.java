package dev.revere.virago.client.modules.player;

import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.client.events.player.UpdateEvent;

/**
 * @author Zion
 * @project Virago-Client
 * @date 25/04/2024
 */

@ModuleData(name = "FastPlace", description = "Place blocks instantly", type = EnumModuleType.PLAYER)
public class FastPlace extends AbstractModule {
    @EventHandler
    private final Listener<UpdateEvent> onUpdate = event -> {
        if (!event.isPre()) return;
        mc.rightClickDelayTimer = 0;
    };
}
