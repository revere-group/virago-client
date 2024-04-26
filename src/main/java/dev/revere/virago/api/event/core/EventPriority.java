package dev.revere.virago.api.event.core;

import lombok.Getter;

/**
 * @author Remi
 * @project nigger
 * @date 3/28/2024
 */
@Getter
public enum EventPriority {
    LOWEST(0),
    LOWER(1),
    DEFAULT(2),
    HIGHER(3),
    HIGHEST(4);

    private final int priority;

    EventPriority(int priority) {
        this.priority = priority;
    }
}