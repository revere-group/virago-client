package dev.revere.virago.client.notification;

import dev.revere.virago.Virago;
import dev.revere.virago.client.gui.components.InteractionComponent;
import dev.revere.virago.client.gui.components.RenderableComponent;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.util.animation.Animation;
import dev.revere.virago.util.animation.Easing;
import dev.revere.virago.util.render.ColorUtil;
import dev.revere.virago.util.render.RenderUtils;
import dev.revere.virago.util.render.RoundedUtils;
import lombok.Getter;
import net.minecraft.util.MathHelper;

import java.awt.*;

public class Notification extends RenderableComponent {

    private final NotificationType type;
    private final String title;
    private final String message;

    private final FontService fontService = Virago.getInstance().getServiceManager().getService(FontService.class);

    @Getter
    private final Animation animation = new Animation(() -> 700F, true, () -> Easing.BACK_IN_OUT);

    private long initTime = 0L;
    private long duration = 1500L;

    /**
     * Instantiates a new Notification.
     *
     * @param x       the x position
     * @param y       the y position
     * @param type    the type of notification
     * @param title   the title of the notification
     * @param message the message of the notification
     */
    public Notification(float x, float y, NotificationType type, String title, String message) {
        super(x, y, 125, 30);

        this.type = type;
        this.title = title;
        this.message = message;

        animation.resetToDefault();
    }

    /**
     * Instantiates a new Notification.
     *
     * @param x        the x position
     * @param y        the y position
     * @param type     the type of notification
     * @param title    the title of the notification
     * @param message  the message of the notification
     * @param duration the duration of the notification
     */
    public Notification(float x, float y, NotificationType type, String title, String message, long duration) {
        super(x, y, 125, 30);

        this.type = type;
        this.title = title;
        this.message = message;
        this.duration = duration;

        animation.resetToDefault();
    }

    /**
     * Draw the notification.
     *
     * @param mouseX     the mouse x
     * @param mouseY     the mouse y
     * @param mouseDelta the mouse delta
     */
    @Override
    public void draw(float mouseX, float mouseY, int mouseDelta) {
        if (initTime == 0L && animation.getFactor() >= 1) {
            initTime = System.currentTimeMillis();
        }

        long time = initTime > 0 ? System.currentTimeMillis() - initTime : 0;

        if (time >= duration) {
            animation.setState(false);
        }

        if (animation.getState()) {
            RoundedUtils.round(getX(), getY(), getWidth(), getHeight(), 5, ColorUtil.interpolate(new Color(0, 0, 0, 130), new Color(0, 0, 0, 100), 0.2f));
            RenderUtils.pushScissor(getX(), getY(), MathHelper.clamp_float(getWidth() * (time / 1500f), 0, getWidth()), getHeight());

            RoundedUtils.round(getX(), getY(), getWidth(), getHeight(), 5f, new Color(0, 0, 0, 130));
            RenderUtils.popScissor();

            if (type == NotificationType.INFO || type == NotificationType.YES) {
                RoundedUtils.outline(getX(), getY(), getWidth(), getHeight(), 5f, 2f, new Color(ColorUtil.getColor(false)));
                fontService.getIcon20().drawString(type.icon, (getX() + 8), (getY() + 10), ColorUtil.getColor(false), false);
            } else {
                RoundedUtils.outline(getX(), getY(), getWidth(), getHeight(), 5f, 2f, new Color(0x202020));
                fontService.getIcon20().drawString(type.icon, (getX() + 8), (getY() + 10), 0x202020, false);
            }
            fontService.getPoppinsMedium().drawString(title, getX() + 27, getY() + 5, -1);
            fontService.getProductSans().drawString(message, getX() + 27, getY() + 17, -1);
        }
    }

    /**
     * Called when the mouse is clicked.
     *
     * @param mouseX the mouse x
     * @param mouseY the mouse y
     * @param click  the click
     * @return the boolean
     */
    @Override
    public boolean mouseClicked(float mouseX, float mouseY, InteractionComponent click) { return false; }

    /**
     * Called when the mouse is released.
     *
     * @param mouseX the mouse x
     * @param mouseY the mouse y
     * @param click  the click
     */
    @Override
    public void mouseReleased(float mouseX, float mouseY, InteractionComponent click) {}

    /**
     * Gets the width of the notification.
     *
     * @return the width
     */
    @Override
    public float getWidth() {
        float titleWidth = fontService.getSfProTextRegular().getStringWidth(this.title);
        float descWidth = fontService.getSfProTextRegular().getStringWidth(this.message);

        return Math.max(titleWidth, descWidth) + 47;
    }

    /**
     * Should notification be hidden.
     *
     * @return the boolean
     */
    public boolean shouldNotificationHide() {
        return !animation.getState();
    }

    /**
     * Called when a key is typed.
     *
     * @param typedChar the typed char
     * @param keyCode   the key code
     */
    public void keyTyped(char typedChar, int keyCode) {}
}