package dev.revere.virago.client.gui.panel.elements.setting;

import dev.revere.virago.Virago;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.gui.components.InteractionComponent;
import dev.revere.virago.client.modules.render.HUD;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.util.render.ColorUtil;
import dev.revere.virago.util.render.RenderUtils;
import dev.revere.virago.util.render.RoundedUtils;
import net.optifine.util.MathUtils;
import org.lwjgl.input.Mouse;

import java.awt.*;

/**
 * @author Remi
 * @project Virago
 * @date 3/22/2024
 */
public class SettingSliderElement extends SettingElement<Number> {
    private boolean dragging = false;

    public SettingSliderElement(Setting<Number> set, float x, float y, float width, float height) {
        super(set, x, y, width, height);
    }

    @Override
    public void draw(float mouseX, float mouseY, int mouseDelta) {
        super.draw(mouseX, mouseY, mouseDelta);
        FontService font = Virago.getInstance().getServiceManager().getService(FontService.class);

        if (!Mouse.isButtonDown(0)) {
            dragging = false;
        }

        float offset = 4;

        float sliderWidth = getWidth() - offset * 2;
        float diff = Math.min(sliderWidth, Math.max(0, mouseX - (getX() + offset)));

        float min = getSetting().getMinimum().floatValue();
        float max = getSetting().getMaximum().floatValue();
        float step = getSetting().getIncrementation().floatValue();
        float current = getSetting().getValue().floatValue();

        float renderWidth = (sliderWidth) * (current - min) / (max - min);

        font.getProductSans().drawString("" + current, getX() + getWidth() - 4 - font.getProductSans().getStringWidth("" + current), getY() + 6f, new Color(180, 180, 180).getRGB(), false);

        RoundedUtils.round(getX() + offset, getY() + getHeight() - 3, sliderWidth, 2, 1f, new Color(0x1C1C1C));
        RoundedUtils.round(getX() + offset, getY() + getHeight() - 3, renderWidth, 2, 1f, new Color(ColorUtil.getColor(false)));

        if (dragging) {
            float value = (float) MathUtils.round(((diff / sliderWidth) * (max - min) + min), 2);
            value = Math.round(Math.max(min, Math.min(max, value)) * (1 / step)) / (1 / step);

            float finalValue = diff == 0 ? min : value;

            if (getSetting().getValue() instanceof Double) {
                getSetting().setValue((double) finalValue);
            } else if (getSetting().getValue() instanceof Float) {
                getSetting().setValue(finalValue);
            } else if (getSetting().getValue() instanceof Integer) {
                getSetting().setValue((int) finalValue);
            } else if (getSetting().getValue() instanceof Long) {
                getSetting().setValue((long) finalValue);
            } else if (getSetting().getValue() instanceof Short) {
                getSetting().setValue((short) finalValue);
            } else if (getSetting().getValue() instanceof Byte) {
                getSetting().setValue((byte) finalValue);
            }
        }
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, InteractionComponent click) {
        if (sliderHovered(mouseX, mouseY) && click.equals(InteractionComponent.LEFT)) {
            dragging = true;
        }

        return super.mouseClicked(mouseX, mouseY, click);
    }

    private boolean sliderHovered(float mouseX, float mouseY) {
        return mouseX >= getX() + 4 && mouseY >= getY() && mouseX <= getX() + getWidth() - 4 && mouseY <= getY() + getHeight();
    }
}
