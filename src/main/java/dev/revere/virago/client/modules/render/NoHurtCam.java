package dev.revere.virago.client.modules.render;

import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.client.events.game.TickEvent;
import dev.revere.virago.client.events.packet.PacketEvent;
import dev.revere.virago.client.events.player.PreMotionEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.S42PacketCombatEvent;
import org.lwjgl.Sys;


@ModuleData(name = "NoHurtCam", displayName = "No Hurt Cam", description = "Disable hurt camera", type = EnumModuleType.RENDER)
public class NoHurtCam extends AbstractModule { // TODO: No clue if this works.

    @EventHandler
    private final Listener<PreMotionEvent> preMotionEventListener = event -> {
        if (this.isEnabled() && Minecraft.getMinecraft().thePlayer != null) {
            Minecraft.getMinecraft().thePlayer.hurtTime = 0;
            Minecraft.getMinecraft().thePlayer.hurtResistantTime = 0;
        }
    };

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

}
