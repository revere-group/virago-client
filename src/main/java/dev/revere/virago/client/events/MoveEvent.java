package dev.revere.virago.client.events;

import dev.revere.virago.api.event.Event;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Remi
 * @project Virago
 * @date 3/17/2024
 */
@Setter
@Getter
public class MoveEvent extends Event {
    private double x;
    private double y;
    private double z;

    /**
     * MoveEvent constructor to initialize the event.
     *
     * @param x the x position of the player
     * @param y the y position of the player
     * @param z the z position of the player
     */
    public MoveEvent(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
