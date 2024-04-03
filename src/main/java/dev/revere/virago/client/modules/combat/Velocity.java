package dev.revere.virago.client.modules.combat;

import com.google.common.eventbus.Subscribe;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.packet.PacketEvent;
import dev.revere.virago.client.events.update.PostMotionEvent;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
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
    
    private final Setting<Float> horizontal = new Setting<>("Horizontal", 0.0f)
            .minimum(0.0f)
            .maximum(100.0f)
            .incrementation(1.0f)
            .describedBy("The amount of horizontal knockback to take");
    
    private final Setting<Float> vertical = new Setting<>("Vertical", 0.0f)
            .minimum(0.0f)
            .maximum(100.0f)
            .incrementation(1.0f)
            .describedBy("The amount of vertical knockback to take");
    
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
            Packet<INetHandlerPlayClient> packet = event.getPacket();
            if (packet instanceof S12PacketEntityVelocity) {
                handleEntityVelocityPacket((S12PacketEntityVelocity) packet, event);
            } else if (packet instanceof S27PacketExplosion) {
                handleExplosionPacket((S27PacketExplosion) packet, event);
            }
        }
    };

    /**
     * Handles the entity velocity packet
     *
     * @param packet The packet to handle
     */
    private void handleEntityVelocityPacket(S12PacketEntityVelocity packet, PacketEvent event) {
        if (packet.getEntityID() == mc.thePlayer.getEntityId()) {
            if (horizontal.getValue() == 0.0f && vertical.getValue() == 0.0f) {
                event.setCancelled(true);
            } else {
                packet.setMotionX((int)((double)((float)packet.getMotionX() * horizontal.getValue()) / 100.0));
                packet.setMotionY((int)((double)((float)packet.getMotionY() * vertical.getValue()) / 100.0));
                packet.setMotionZ((int)((double)((float)packet.getMotionZ() * horizontal.getValue()) / 100.0));
            }
        }
    }

    /**
     * Handles the explosion packet
     *
     * @param packet The packet to handle
     */
    private void handleExplosionPacket(S27PacketExplosion packet, PacketEvent event) {
        if (horizontal.getValue() == 0.0f && vertical.getValue() == 0.0f) {
            event.setCancelled(true);
        } else {
            packet.setField_149152_f((float)((int)((double)(packet.func_149149_c() * horizontal.getValue()) / 100.0)));
            packet.setField_149153_g((float)((int)((double)(packet.func_149144_d() * vertical.getValue()) / 100.0)));
            packet.setField_149159_h((float)((int)((double)(packet.func_149147_e() * horizontal.getValue()) / 100.0)));
        }
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
