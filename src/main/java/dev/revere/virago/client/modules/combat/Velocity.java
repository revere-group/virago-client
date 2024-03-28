package dev.revere.virago.client.modules.combat;

import com.google.common.eventbus.Subscribe;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.client.events.packet.PacketEvent;
import dev.revere.virago.client.events.update.PostMotionEvent;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import org.lwjgl.input.Keyboard;

/**
 * @author Remi
 * @project Virago
 * @date 3/18/2024
 */
@ModuleData(name = "Velocity", type = EnumModuleType.COMBAT, description = "Avoid taking knockback from other players")
public class Velocity extends AbstractModule {
    public Velocity() {
        setKey(Keyboard.KEY_H);
    }

    /**
     * Handles the receive packet event
     *
     * @param event The event to handle
     */
    @EventHandler
    private final Listener<PacketEvent> packetEventListener = event -> {
        if (event.getEventState() == PacketEvent.EventState.RECEIVING) {
            Packet<INetHandlerPlayClient> packet = (Packet<INetHandlerPlayClient>) event.getPacket();
            if (packet instanceof S12PacketEntityVelocity) {
                handleEntityVelocityPacket((S12PacketEntityVelocity) packet);
            } else if (packet instanceof S27PacketExplosion) {
                handleExplosionPacket((S27PacketExplosion) packet);
            }
        }
    };

    /**
     * Handles the entity velocity packet
     *
     * @param packet The packet to handle
     */
    private void handleEntityVelocityPacket(S12PacketEntityVelocity packet) {
        if (packet.getEntityID() == mc.thePlayer.getEntityId()) {
            packet.setMotionX(0);
            packet.setMotionY(0);
            packet.setMotionZ(0);
        }
    }

    /**
     * Handles the explosion packet
     *
     * @param packet The packet to handle
     */
    private void handleExplosionPacket(S27PacketExplosion packet) {
        packet.setField_149152_f(0);
        packet.setField_149153_g(0);
        packet.setField_149159_h(0);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
