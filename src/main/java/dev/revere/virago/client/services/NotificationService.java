package dev.revere.virago.client.services;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.service.IService;
import dev.revere.virago.client.events.render.Render2DEvent;
import dev.revere.virago.client.modules.render.Notifications;
import dev.revere.virago.client.notification.Notification;
import dev.revere.virago.client.notification.NotificationType;
import dev.revere.virago.util.Logger;
import dev.revere.virago.util.animation.Animation;
import dev.revere.virago.util.animation.Easing;
import javafx.scene.transform.Scale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotificationService implements IService {

    private final List<Notification> notifications = new ArrayList<>();

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

    public void notify(NotificationType type, String title, String message) {
        if(!Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(Notifications.class).isEnabled()) {
            return;
        }

        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        notifications.add(new Notification(scaledResolution.getScaledWidth() - 205, scaledResolution.getScaledHeight() - 30 * notifications.size(), type, title, message));
    }



    /**
     * Renders all the notifications in the list.
     *
     * @param event the render event
     */
    @EventHandler
    private final Listener<Render2DEvent> render2DEventListener = event -> {
        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        notifications.removeIf(Notification::shouldNotificationHide);

        float offset = 0;

        for(Notification notification : notifications) {
            notification.setX(resolution.getScaledWidth() - (float) ((notification.getWidth() + 5) * notification.getAnimation().getFactor()));
            notification.setY(resolution.getScaledHeight() - (offset * 1.1f) - notification.getHeight() - 5);

            notification.draw(0, 0, 0);

            offset += notification.getHeight() * notification.getAnimation().getFactor();
        }
    };
}
