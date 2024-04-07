package dev.revere.virago.client.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.*;

@Getter
@AllArgsConstructor
public enum NotificationType {
    INFO(new Color(255, 255, 255, 195), "j"),
    YES(new Color(56, 182, 42, 195), "j"),
    NO(new Color(182, 42, 42, 195), "j"),
    WARNING(new Color(194, 161, 7, 194), "j"),
    ERROR(new Color(168, 20, 20, 194), "j");

    final Color color;
    final String icon;
}