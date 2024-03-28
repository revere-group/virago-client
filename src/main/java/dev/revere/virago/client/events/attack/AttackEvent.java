package dev.revere.virago.client.events.attack;

import dev.revere.virago.api.event.Event;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;

@Getter
@Setter
public class AttackEvent extends Event {
    private Entity target;

    public AttackEvent(Entity target) {
        this.target = target;
    }
}
