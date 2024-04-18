package dev.revere.virago.client.events.player;

import dev.revere.virago.api.event.Event;
import lombok.Getter;

@Getter
public class IRCEvent extends Event {
    private final String message;

    public IRCEvent(String message) {
        this.message = message;
    }
}
