package dev.revere.virago.client.events.input;

import dev.revere.virago.api.event.Event;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Remi
 * @project Virago
 * @date 3/17/2024
 */
@Getter
@Setter
public class KeyUpEvent extends Event {
    private int key;

    /**
     * KeyUpEvent constructor to initialize the event.
     *
     * @param key the key that was released
     */
    public KeyUpEvent(int key) {
        this.key = key;
    }
}
