package dev.revere.virago.client.notification;

import lombok.Getter;

@Getter
public enum NotificationType {
    ERROR,
    WARNING,
    INFO,
    CRITICAL;
}
