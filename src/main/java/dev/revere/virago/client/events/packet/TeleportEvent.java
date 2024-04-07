package dev.revere.virago.client.events.packet;

import dev.revere.virago.api.event.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.play.client.C03PacketPlayer;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/7/2024
 */
@Setter
@Getter
public class TeleportEvent extends Event {

    private C03PacketPlayer packetPlayer;
    private double posX;
    private double posY;
    private double posZ;
    private float yaw;
    private float pitch;

    public TeleportEvent(C03PacketPlayer packetPlayer, double posX, double posY, double posZ, float yaw, float pitch) {
        this.packetPlayer = packetPlayer;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.yaw = yaw;
        this.pitch = pitch;
    }
}
