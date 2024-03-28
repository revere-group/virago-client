package dev.revere.virago.client.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.*;

@Getter
@RequiredArgsConstructor
public abstract class Notification {
    private final String title;
    private final String message;
    private final long init = System.currentTimeMillis();
    private final long length;
    private final Color color;

    public abstract void show(int amount);
    public abstract boolean ended();

}
