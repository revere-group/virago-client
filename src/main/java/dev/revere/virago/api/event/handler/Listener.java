package dev.revere.virago.api.event.handler;

import dev.revere.virago.api.event.Event;

/**
 * @author Remi
 * @project nigger
 * @date 3/28/2024
 */

public interface Listener<T extends Event> {

    /**
     * Method for calling an event
     *
     * @param event event that should be called
     */
    void call(final T event);
}
