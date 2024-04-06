package dev.revere.virago.client.events.player;

import dev.revere.virago.api.event.Event;
import lombok.Data;

/**
 * @author Remi
 * @project Virago
 * @date 3/26/2024
 */
@Data
public class StrafeEvent extends Event {
    private float forward, strafe, friction, attributeSpeed, yaw, pitch;

    public StrafeEvent(float forward, float strafe, float friction, float attributeSpeed, float yaw, float pitch) {
        this.forward = forward;
        this.strafe = strafe;
        this.friction = friction;
        this.attributeSpeed = attributeSpeed;
        this.yaw = yaw;
        this.pitch = pitch;
    }
}
