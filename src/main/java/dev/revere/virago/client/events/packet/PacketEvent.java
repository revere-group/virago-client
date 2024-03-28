package dev.revere.virago.client.events.packet;

import dev.revere.virago.api.event.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.Packet;

/**
 * @author Remi
 * @project nigger
 * @date 3/28/2024
 */
@Setter
@Getter
public class PacketEvent extends Event {
    private final EventState eventState;
    private Packet packet;

    public PacketEvent(EventState eventState, Packet packet) {
        this.eventState = eventState;
        this.packet = packet;
    }

    public enum EventState{
        RECEIVING,
        SENDING
    }
    public <T extends Packet> T getPacket() {
        return (T) packet;
    }
}
