package dev.revere.virago.client.modules.movement;

import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.client.events.player.SafeWalkEvent;

/**
 * @author Zion
 * @project Virago
 * @date 02/05/2024
 */

@ModuleData(name = "Safewalk", displayName = "Safe Walk", description = "", type = EnumModuleType.MOVEMENT)
public class Safewalk extends AbstractModule {
    @EventHandler
    private final Listener<SafeWalkEvent> safeWalkEventListener = e -> {
        e.setCancelled(true);
    };
}
