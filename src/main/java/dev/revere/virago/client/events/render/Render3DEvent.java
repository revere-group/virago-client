package dev.revere.virago.client.events.render;

import dev.revere.virago.api.event.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.ScaledResolution;

/**
 * @author Remi
 * @project Virago
 * @date 3/17/2024
 */
@Setter
@Getter
public class Render3DEvent extends Event {
    private float partialTicks;

    /**
     * Render3DEvent constructor to initialize the event.
     *
     * @param partialTicks     the partial ticks of the screen
     */
    public Render3DEvent(float partialTicks) {
        this.partialTicks = partialTicks;
    }
}
