package dev.revere.virago.client.gui.panel.elements;

import dev.revere.virago.Virago;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.gui.components.InteractionComponent;
import dev.revere.virago.client.gui.components.RenderableComponent;
import dev.revere.virago.client.gui.panel.elements.setting.*;
import dev.revere.virago.client.modules.render.HUD;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.util.animation.Animation;
import dev.revere.virago.util.animation.Easing;
import dev.revere.virago.util.render.ColorUtil;
import dev.revere.virago.util.render.RenderUtils;
import lombok.Getter;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author Remi
 * @project Virago
 * @date 3/21/2024
 */
@Getter
public class ModuleElement extends RenderableComponent {
    private final Animation expandAnimation = new Animation(() -> 400F, false, () -> Easing.CUBIC_IN_OUT);
    private final ArrayList<SettingElement<?>> settings = new ArrayList<>();
    private final AbstractModule module;

    /**
     * Constructor for the ModuleElement.
     *
     * @param module The module.
     * @param x      The x position.
     * @param y      The y position.
     * @param width  The width.
     * @param height The height.
     */
    public ModuleElement(AbstractModule module, float x, float y, float width, float height) {
        super(x, y, width, height);
        this.module = module;

        for (Setting<?> setting : module.getSettings()) {
            if (setting.getValue() instanceof Enum) {
                settings.add(new SettingEnumElement((Setting<Enum<?>>) setting, getX(), getY(), getWidth(), getHeight()));
            } else if (setting.getValue() instanceof Color) {
                settings.add(new SettingColorElement((Setting<Color>) setting, getX(), getY(), getWidth(), getHeight() * 5));
            } else if (setting.getValue() instanceof Boolean) {
                settings.add(new SettingBooleanElement((Setting<Boolean>) setting, getX(), getY(), getWidth(), getHeight()));
            } else if (setting.getValue() instanceof Number) {
                settings.add(new SettingSliderElement((Setting<Number>) setting, getX(), getY(), getWidth(), getHeight()));
            } else if (setting.getValue() != null) {
                settings.add(new SettingElement<>(setting, getX(), getY(), getWidth(), getHeight()));
            }
        }
    }

    /**
     * Draws the module element.
     *
     * @param mouseX     The x position of the mouse.
     * @param mouseY     The y position of the mouse.
     * @param mouseDelta The delta of the mouse.
     */
    @Override
    public void draw(float mouseX, float mouseY, int mouseDelta) {
        FontService font = Virago.getInstance().getServiceManager().getService(FontService.class);
        if (expandAnimation.getFactor() > 0) {
            float settingOffset = getY() + getHeight();

            for (SettingElement<?> settingElement : settings) {
                if (settingElement.getSetting().isVisible()) {
                    settingElement.setX(getX());
                    settingElement.setY(settingOffset);

                    settingElement.draw(mouseX, mouseY, mouseDelta);

                    settingOffset += settingElement.getOffset();
                }
            }
        }

        int color = module.isEnabled() ? ColorUtil.getColor(false) : new Color(0x1F1F1F).getRGB();

        RenderUtils.drawRect(getX(), getY(), getWidth() + getX(), getHeight() + getY(), color);

        // If the color is -1, set it to black.
        if (color == -1) color = Color.BLACK.getRGB();

        // Draw the expand button.
        if (settings.stream().anyMatch(setting -> setting.getSetting().isVisible())) {
            if (expandAnimation.getState()) {
                font.getProductSans().drawString("-", getX() + getWidth() - 10, (getY() + getHeight() / 2f) - (font.getProductSans().getHeight() / 2f), color == Color.BLACK.getRGB() ? Color.BLACK.getRGB() : new Color(-1).getRGB(), false);
            } else {
                font.getProductSans().drawString("+", getX() + getWidth() - 10, (getY() + getHeight() / 2f) - (font.getProductSans().getHeight() / 2f), color == Color.BLACK.getRGB() ? Color.BLACK.getRGB() : new Color(-1).getRGB(), false);
            }
        }
        // Draw the module name.
        font.getProductSans().drawString(module.getName(), getX() + 4, (getY() + getHeight() / 2f) - (font.getProductSans().getHeight() / 2f), module.isEnabled() ? (color == Color.BLACK.getRGB() ? Color.BLACK.getRGB() : new Color(-1).getRGB()) : new Color(0xff5b5b5b).getRGB(), false);
    }

    /**
     * Checks if the mouse is hovered over the module element.
     *
     * @param mouseX The x position of the mouse.
     * @param mouseY The y position of the mouse.
     * @return If the mouse is hovered over the module element.
     */
    @Override
    public boolean mouseClicked(float mouseX, float mouseY, InteractionComponent click) {
        if (hovered(mouseX, mouseY)) {
            switch (click) {
                case LEFT: {
                    module.toggle();
                    break;
                }

                case RIGHT: {
                    expandAnimation.setState(!expandAnimation.getState());
                    break;
                }
            }
        }

        if (expandAnimation.getState()) {
            for (SettingElement<?> setting : settings) {
                if (setting.getSetting().isVisible()) {
                    setting.mouseClicked(mouseX, mouseY, click);
                }
            }
        }
        return false;
    }

    /**
     * Called when the mouse is released.
     *
     * @param mouseX The x position of the mouse.
     * @param mouseY The y position of the mouse.
     * @param click  The interaction component.
     */
    @Override
    public void mouseReleased(float mouseX, float mouseY, InteractionComponent click) {

    }

    /**
     * Called when a key is typed.
     *
     * @param typedChar The typed character.
     * @param keyCode   The key code.
     */
    @Override
    public void keyTyped(char typedChar, int keyCode) {
        for (SettingElement<?> setting : settings) {
            setting.keyTyped(typedChar, keyCode);
        }
    }

    /**
     * Gets the offset of the module element.
     *
     * @return The offset.
     */
    @Override
    public float getOffset() {
        float settingHeight = 0f;

        for (SettingElement<?> settingElement : settings) {
            if (settingElement.getSetting().isVisible()) {
                settingHeight += settingElement.getOffset();
            }
        }

        return (float) (getHeight() + (settingHeight * expandAnimation.getFactor()));
    }
}
