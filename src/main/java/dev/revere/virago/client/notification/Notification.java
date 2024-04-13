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

    public Notification(float x, float y, NotificationType type, String title, String message) {
        super(x, y, 125, 30);

        this.type = type;
        this.title = title;
        this.message = message;

        animation.resetToDefault();
        //animation.setState(false);
    }

    @Override
    public void draw(float mouseX, float mouseY, int mouseDelta) {
        if (initTime == 0L && animation.getFactor() >= 1) {
            initTime = System.currentTimeMillis();
        }

        long time = initTime > 0 ? System.currentTimeMillis() - initTime : 0;

        if (time >= 1500) {
            animation.setState(false);
        }

        if (animation.getState()) {
            RoundedUtils.round(getX(), getY(), getWidth(), getHeight(), 5, ColorUtil.interpolate(new Color(0, 0, 0, 130), new Color(0, 0, 0, 100), 0.2f));
            RenderUtils.pushScissor(getX(), getY(), MathHelper.clamp_float(getWidth() * (time / 1500f), 0, getWidth()), getHeight());

            RoundedUtils.round(getX(), getY(), getWidth(), getHeight(), 5f, new Color(0, 0, 0, 130));
            RenderUtils.popScissor();

            RoundedUtils.outline(getX(), getY(), getWidth(), getHeight(), 5f, 2f, type.getColor());

            fontService.getIcon20().drawString(type.icon, (getX() + 8), (getY() + 10), type.color.getRGB(), false);
            fontService.getPoppinsMedium().drawString(title, getX() + 27, getY() + 5, -1);
            fontService.getProductSans().drawString(message, getX() + 27, getY() + 17, -1);
        }
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, InteractionComponent click) { return false; }

    @Override
    public void mouseReleased(float mouseX, float mouseY, InteractionComponent click) {}

    @Override
    public float getWidth() {
        float titleWidth = fontService.getSfProTextRegular().getStringWidth(this.title);
        float descWidth = fontService.getSfProTextRegular().getStringWidth(this.message);

        return Math.max(titleWidth, descWidth) + 47;
    }

    public boolean shouldNotificationHide() {
        return !animation.getState();
    }

    public void keyTyped(char typedChar, int keyCode) {}
}