package dev.revere.virago.api.event.handler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Remi
 * @project nigger
 * @date 3/28/2024
 */
public class EventExecutable {
    private final Method method;
    private final Field field;

    public EventExecutable(final Method method, final Field field) {
        this.method = method;
        this.field = field;
    }

    public EventExecutable(final Method method) {
        this(method, null);
    }

    public EventExecutable(final Field field) {
        this(null, field);
    }

    public Field getField() {
        return field;
    }

    public Method getMethod() {
        return method;
    }
}
