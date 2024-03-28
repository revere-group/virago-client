package dev.revere.virago.client.notification.impl;

import dev.revere.virago.Virago;
import dev.revere.virago.client.notification.Notification;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.util.render.RoundedUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public class ErrorNotification extends Notification {

    private boolean end;

    /**
     * Constructs a new error notification with the given title, message and length.
     *
     * @param title   the title of the notification
     * @param message the message of the notification
     * @param length  the length of the notification
     */
    public ErrorNotification(String title, String message, long length) {
        super(title, message, length, new Color(255, 108, 108));
    }

    /**
     * Draws the notification with the given amount of notifications.
     *
     * @param amount the amount of notifications
     */
    @Override
    public void show(int amount) {
        ScaledResolution resolution = new ScaledResolution(Minecraft.getMinecraft());
        FontService font = Virago.getInstance().getServiceManager().getService(FontService.class);

        this.end = ((this.getInit() + this.getLength()) - System.currentTimeMillis()) < 600;

        int width = getWidth();
        int height = 35;

        int x = resolution.getScaledWidth() - 5 - width;
        int y = resolution.getScaledHeight() - (height * (amount + 1)) - (5 * amount + 1) - 5;

        RoundedUtils.round(x, y, width, height, 2, new Color(20, 20, 20, 170));
        RoundedUtils.outline(x, y, width, height, 3, 1, new Color(0, 0, 0, 200));
        RoundedUtils.shadowGradient(x, y, width, height, 3, 3, 2, this.getColor(), this.getColor(), this.getColor(), this.getColor(), false);

        font.getProductSans().drawString(this.getTitle(), resolution.getScaledWidth() - font.getProductSans().getStringWidth(this.getTitle()) - 10, y + 5, Color.WHITE.getRGB());
        font.getRalewaySemiBold14().drawString(this.getMessage(), x + 5, y + 20, Color.WHITE.getRGB());
    }

    @Override
    public boolean ended() {
        return this.end;
    }

    private int getWidth() {
        FontService font = Virago.getInstance().getServiceManager().getService(FontService.class);
        int titleWidth = font.getProductSans().getStringWidth(this.getTitle());
        int messageWidth = font.getRalewaySemiBold14().getStringWidth(this.getMessage());
        return Math.max(titleWidth, messageWidth) + 10;
    }
}
