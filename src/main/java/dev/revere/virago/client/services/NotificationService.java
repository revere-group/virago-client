package dev.revere.virago.client.services;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.service.IService;
import dev.revere.virago.client.events.render.Render2DEvent;
import dev.revere.virago.client.notification.Notification;
import dev.revere.virago.util.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotificationService implements IService {

    List<Notification> notifications = new ArrayList<>();

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
        //clear notification list
    }

    public void addNotification(Notification notification) {
        this.notifications.add(notification);
    }

    public void drawNotification(Notification notification) {
        String title = notification.getTitle();
        String message = notification.getMessage();
        Color color = notification.getColor();

        //render shit here with animations remi
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

            this.drawNotification(notification);

            if (notification.ended()) {
                iterator.remove();
            }
            i++;
        }

        notifications.removeIf(Notification::ended);
    };
}
