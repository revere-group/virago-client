package dev.revere.virago.client.gui.panel.elements.setting;

import dev.revere.virago.Virago;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.gui.components.InteractionComponent;
import dev.revere.virago.client.modules.render.HUD;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.util.animation.Animation;
import dev.revere.virago.util.animation.Easing;
import dev.revere.virago.util.render.ColorUtil;
import dev.revere.virago.util.render.RoundedUtils;

import java.awt.*;

/**
 * @author Remi
 * @project Virago
 * @date 3/22/2024
 */
public class SettingBooleanElement extends SettingElement<Boolean> {
    private final Animation toggle = new Animation(() -> 150f, getSetting().getValue(), () -> Easing.LINEAR);

    public SettingBooleanElement(Setting<Boolean> set, float x, float y, float width, float height) {
        super(set, x, y, width, height);
    }

    @Override
    public void draw(float mouseX, float mouseY, int mouseDelta) {
        super.draw(mouseX, mouseY, mouseDelta);
        FontService font = Virago.getInstance().getServiceManager().getService(FontService.class);
        RoundedUtils.round(getX() + getWidth() - 14, getY() + 4 , 10, 10, 2, new Color(0x1E1E1E));

        if (toggle.getState()) {
            font.getProductSans().drawCenteredString("+", getX() + getWidth() - 9.5f, getY() + getHeight() / 2f - font.getProductSans().getHeight() / 2f - 1f, ColorUtil.getColor(false));
        }
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, InteractionComponent click) {
        if (hovered(mouseX, mouseY) && click.equals(InteractionComponent.LEFT)) {
            toggle.setState(!getSetting().getValue());
            getSetting().setValue(!getSetting().getValue());
        }

        return super.mouseClicked(mouseX, mouseY, click);
    }
}
