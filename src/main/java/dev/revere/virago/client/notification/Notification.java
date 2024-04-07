package dev.revere.virago.client.notification;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.awt.*;

@Data
@AllArgsConstructor
public class Notification {
    public String title;
    public String message;
    public Color color;

    long endTime;

    public boolean ended() {
        if(System.currentTimeMillis() >= endTime) return true;
        return false;
    }
}
