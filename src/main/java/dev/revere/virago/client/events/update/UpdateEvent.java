package dev.revere.virago.client.events.update;

import dev.revere.virago.api.event.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;

/**
 * @author Remi
 * @project Virago
 * @date 3/17/2024
 */
@Setter
@Getter
public class UpdateEvent extends Event {
    private boolean onGround;
    private float yaw;
    private float pitch;
    private double y;
    private boolean pre;

    /**
     * UpdateEvent constructor to initialize the event.
     *
     * @param yaw      the yaw of the player
     * @param pitch    the pitch of the player
     * @param y        the y position of the player
     * @param onGround the ground state of the player
     * @param pre      the pre state of the player
     */
    public UpdateEvent(float yaw, float pitch, double y, boolean onGround, boolean pre) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.y = y;
        this.onGround = onGround;
        this.pre = pre;
    }

    /**
     * UpdateEvent constructor to initialize the event.
     *
     * @param yaw      the yaw of the player
     * @param pitch    the pitch of the player
     * @param y        the y position of the player
     * @param onGround the ground state of the player
     */
    public UpdateEvent(float yaw, float pitch, double y, boolean onGround) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.y = y;
        this.onGround = onGround;
    }

    /**
     * Set the yaw of the player.
     *
     * @param yaw the yaw of the player
     */
    public void setYaw(float yaw) {
        Minecraft.getMinecraft().thePlayer.renderYawOffset = yaw;
        Minecraft.getMinecraft().thePlayer.rotationYawHead = yaw;
        this.yaw = yaw;
    }
}
