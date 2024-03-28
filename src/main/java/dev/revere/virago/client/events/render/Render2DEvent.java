package dev.revere.virago.client.events.render;

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
public class Render2DEvent extends Event {
    private float width;
    private float height;
    private float partialTicks;

    /**
     * Render2DEvent constructor to initialize the event.
     *
     * @param width        the width of the screen
     * @param height       the height of the screen
     * @param partialTicks the partial ticks of the screen
     */
    public Render2DEvent(float width, float height, float partialTicks) {
        this.width = width;
        this.height = height;
        this.partialTicks = partialTicks;
    }
}
