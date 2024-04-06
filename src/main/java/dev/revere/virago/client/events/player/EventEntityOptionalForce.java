package dev.revere.virago.client.events.player;

import dev.revere.virago.api.event.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

/**
 * @author Remi
 * @project Virago
 * @date 3/18/2024
 */
@Setter
@Getter
public class EventEntityOptionalForce extends Event {
    private Entity entity;
    private Vec3 minor;

    /**
     * EventEntityOptionalForce constructor to initialize the event.
     *
     * @param e    the entity that was forced
     * @param vec3 the vector that was forced
     */
    public EventEntityOptionalForce(Entity e, Vec3 vec3) {
        this.entity = e;
        this.minor = vec3;
    }
}
