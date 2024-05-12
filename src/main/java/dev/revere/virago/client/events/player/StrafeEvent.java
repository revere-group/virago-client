package dev.revere.virago.client.events.player;

import dev.revere.virago.api.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;

/**
 * @author Remi
 * @project Virago
 * @date 3/26/2024
 */
@Getter
@Setter
@AllArgsConstructor
public class StrafeEvent extends Event {
    private float forward;
    private float strafe;
    private float friction;
    private float yaw;

    public void setSpeed(final double speed, final double motionMultiplier) {
        setFriction((float) (getForward() != 0 && getStrafe() != 0 ? speed * 0.98F : speed));
        Minecraft.getMinecraft().thePlayer.motionX *= motionMultiplier;
        Minecraft.getMinecraft().thePlayer.motionZ *= motionMultiplier;
    }

    public void setSpeed(final double speed) {
        setFriction((float) (getForward() != 0 && getStrafe() != 0 ? speed * 0.98F : speed));
        Minecraft.getMinecraft().thePlayer.motionX = 0;
        Minecraft.getMinecraft().thePlayer.motionZ = 0;
    }
}
