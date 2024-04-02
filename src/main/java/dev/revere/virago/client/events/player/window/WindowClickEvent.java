package dev.revere.virago.client.events.player.window;

import dev.revere.virago.api.event.Event;
import lombok.Getter;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/2/2024
 */
@Getter
public class WindowClickEvent extends Event {

    private final int windowId;
    private final int slot;
    private final int button;
    private final int mode;

    public WindowClickEvent(int windowId, int slot, int button, int mode) {
        this.windowId = windowId;
        this.slot = slot;
        this.button = button;
        this.mode = mode;
    }
}
