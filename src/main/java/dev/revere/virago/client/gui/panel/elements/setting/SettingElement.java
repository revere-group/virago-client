package dev.revere.virago.client.gui.panel.elements.setting;

import dev.revere.virago.Virago;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.gui.components.InteractionComponent;
import dev.revere.virago.client.gui.components.RenderableComponent;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.util.animation.Animation;
import dev.revere.virago.util.animation.Easing;
import dev.revere.virago.util.render.ColorUtil;
import dev.revere.virago.util.render.RenderUtils;
import lombok.Getter;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Remi
 * @project Virago
 * @date 3/21/2024
 */
@Getter
public class SettingElement<T> extends RenderableComponent {

    private final Animation expandAnimation = new Animation(() -> 200F, false, () -> Easing.CUBIC_IN_OUT);
    private final List<SettingElement<?>> subSettings = new ArrayList<>();
    private final Setting<T> setting;

    public SettingElement(Setting<T> setting, float x, float y, float width, float height) {
        super(x, y, width, height);
        this.setting = setting;

        for (Setting<?> child : setting.getChildren()) {
            if (child.getValue() instanceof Enum) {
                subSettings.add(new SettingEnumElement((Setting<Enum<?>>) child, getX(), getY(), getWidth() - 2, getHeight()));
            } else if (child.getValue() instanceof Color) {
                subSettings.add(new SettingColorElement((Setting<Color>) child, getX(), getY(), getWidth() - 2, getHeight() * 5));
            } else if (setting.getValue() instanceof Boolean) {
                subSettings.add(new SettingBooleanElement((Setting<Boolean>) child, getX(), getY(), getWidth(), getHeight()));
            } else if (setting.getValue() instanceof Number) {
                subSettings.add(new SettingSliderElement((Setting<Number>) child, getX(), getY(), getWidth() - 2, getHeight()));
            } else if (child.getValue() != null) {
                subSettings.add(new SettingElement<>(child, getX(), getY(), getWidth() - 2, getHeight()));
            }
        }
    }

    @Override
    public void draw(float mouseX, float mouseY, int mouseDelta) {
        float offset = 0f;

        if (subSettings.stream().anyMatch(subsetting -> subsetting.getSetting().isVisible())) {
            offset = 8f;
            Virago.getInstance().getServiceManager().getService(FontService.class).getProductSans().drawString("+", getX() + 3.5f, getY() + 5f, new Color(0xff8f8f8f).getRGB(), false);
        }

        Virago.getInstance().getServiceManager().getService(FontService.class).getProductSans().drawString(setting.getName(), getX() + 4f + offset, getY() + 6f, new Color(0xff8f8f8f).getRGB(), false);

        if (expandAnimation.getFactor() > 0 && subSettings.stream().anyMatch(subsetting -> subsetting.getSetting().isVisible())) {
            float settingOffset = getY() + getHeight() + 80;
            for (SettingElement<?> settingElement : subSettings) {
                if (settingElement.getSetting().isVisible()) {
                    settingElement.setX(getX() + 1);
                    settingElement.setY(settingOffset);

                    settingElement.draw(mouseX, mouseY, mouseDelta);

                    settingOffset += settingElement.getOffset();
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, InteractionComponent click) {
        if (hovered(mouseX, mouseY) && click.equals(InteractionComponent.RIGHT)) {
            expandAnimation.setState(!expandAnimation.getState());
        }

        if (expandAnimation.getState()) {
            subSettings.forEach(subsetting -> {
                if (subsetting.getSetting().isVisible()) {
                    subsetting.mouseClicked(mouseX, mouseY, click);
                }
            });
        }

        return false;
    }

    @Override
    public void mouseReleased(float mouseX, float mouseY, InteractionComponent click) {
        if (expandAnimation.getState()) {
            subSettings.forEach(subsetting -> {
                if (subsetting.getSetting().isVisible()) {
                    subsetting.mouseReleased(mouseX, mouseY, click);
                }
            });
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (expandAnimation.getState()) {
            subSettings.forEach(subsetting -> {
                if (subsetting.getSetting().isVisible()) {
                    subsetting.keyTyped(typedChar, keyCode);
                }
            });
        }
    }

    private float getSubsettingHeight() {
        float subsettingHeight = 0f;

        for (SettingElement<?> subsetting : subSettings) {
            if (subsetting.getSetting().isVisible()) {
                subsettingHeight += subsetting.getOffset();
            }
        }

        return (float) (subsettingHeight * expandAnimation.getFactor());
    }

    @Override
    public float getOffset() {
        return getHeight() + getSubsettingHeight();
    }
}
