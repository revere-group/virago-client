package dev.revere.virago.client.services;

import com.google.common.eventbus.Subscribe;
import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.service.IService;
import dev.revere.virago.client.events.input.KeyDownEvent;
import dev.revere.virago.client.events.render.Render2DEvent;
import dev.revere.virago.client.notification.Notification;
import dev.revere.virago.client.notification.NotificationType;
import dev.revere.virago.client.notification.impl.CriticalNotification;
import dev.revere.virago.client.notification.impl.ErrorNotification;
import dev.revere.virago.client.notification.impl.InfoNotification;
import dev.revere.virago.client.notification.impl.WarningNotification;
import dev.revere.virago.util.Logger;

import java.util.ArrayList;
import java.util.Iterator;

public class NotificationService implements IService {
    public final ArrayList<Notification> notifications = new ArrayList<>();

    /**
     * Initializes the service by registering the service to the event bus.
     */
    @Override
    public void initService() {
        Virago.getInstance().getEventBus().register(this);
        Logger.info("Notification service initialized!", getClass());
    }

    /**
     * Destroys the service by clearing the notifications list.
     */
    @Override
    public void destroyService() {
        notifications.clear();
    }

    /**
     * Draws a notification with the given title, message, and type.
     *
     * @param title   the title of the notification
     * @param message the message of the notification
     * @param type    the type of the notification
     */
    public void drawNotification(String title, String message, NotificationType type) {
        long length = Virago.getInstance().getServiceManager().getService(FontService.class).getRalewaySemiBold14().getStringWidth(message) * 30L;
        Notification notification;

        switch (type) {
            case CRITICAL:
                notification = new CriticalNotification(title, message, length);
                break;
            case WARNING:
                notification = new WarningNotification(title, message, length);
                break;
            case ERROR:
                notification = new ErrorNotification(title, message, length);
                break;
            default:
                notification = new InfoNotification(title, message, length);
                break;
        }

        notifications.add(notification);
    }

    /**
     * Renders all the notifications in the list.
     *
     * @param event the render event
     */
    @EventHandler
    private final Listener<Render2DEvent> render2DEventListener = event -> {
        int i = 0;

        for (Iterator<Notification> iterator = notifications.iterator(); iterator.hasNext(); ) {
            final Notification notification = iterator.next();

            notification.show(i);

            if (notification.ended()) {
                iterator.remove();
            }
            i++;
        }

        notifications.removeIf(Notification::ended);
    };
}
