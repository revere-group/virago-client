package dev.revere.virago.api.event.handler;

import dev.revere.virago.api.event.core.EventPriority;

import java.util.ArrayList;

/**
 * @author Remi
 * @project nigger
 * @date 3/28/2024
 */

public class EventObject {
    private final Object object;
    private final EventPriority eventPriority;
    private final ArrayList<EventExecutable> eventExecutables;

    public EventObject(Object object, EventPriority eventPriority) {
        this.object = object;
        this.eventPriority = eventPriority;
        this.eventExecutables = new ArrayList<>();
    }

    public Object getObject() {
        return object;
    }

    public EventPriority getEventPriority() {
        return eventPriority;
    }

    public ArrayList<EventExecutable> getEventExecutables() {
        return eventExecutables;
    }
}
