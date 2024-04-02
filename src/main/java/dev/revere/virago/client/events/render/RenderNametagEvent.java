package dev.revere.virago.client.events.render;

import dev.revere.virago.api.event.Event;
import lombok.Getter;
import net.minecraft.entity.EntityLivingBase;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/2/2024
 */
@Getter
public class RenderNametagEvent extends Event {
    private final EntityLivingBase entity;

    public RenderNametagEvent(EntityLivingBase entity) {
        this.entity = entity;
    }
}
