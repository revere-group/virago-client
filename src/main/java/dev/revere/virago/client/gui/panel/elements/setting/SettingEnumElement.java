package dev.revere.virago.client.gui.panel.elements.setting;

import dev.revere.virago.Virago;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.gui.components.InteractionComponent;
import dev.revere.virago.client.gui.components.RenderableComponent;
import dev.revere.virago.client.modules.render.HUD;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.util.animation.Animation;
import dev.revere.virago.util.animation.Easing;
import dev.revere.virago.util.render.ColorUtil;
import dev.revere.virago.util.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Remi
 * @project Virago
 * @date 3/23/2024
 */
public class SettingEnumElement extends SettingElement<Enum<?>> {

    private final ArrayList<Button> buttons = new ArrayList<>();

    private final Animation expandAnimation = new Animation(() -> 200F, false, () -> Easing.CUBIC_IN_OUT);
    private final Animation rotate = new Animation(() -> 200F, false, () -> Easing.CUBIC_IN_OUT);

    public SettingEnumElement(Setting<Enum<?>> set, float x, float y, float width, float height) {
        super(set, x, y, width, height);

        Enum<?> enumeration = set.getValue();
        String[] values = Arrays.stream(enumeration.getClass().getEnumConstants()).map(Enum::name).toArray(String[]::new);

        for (int i = 0; i < values.length; i++) {
            buttons.add(new Button(i, getX(), getY(), getWidth(), getHeight()));
        }
    }

    @Override
    public void draw(float mouseX, float mouseY, int mouseDelta) {
        super.draw(mouseX, mouseY, mouseDelta);
        rotate.setState(expandAnimation.getState());

        FontService font = Virago.getInstance().getServiceManager().getService(FontService.class);
        if (expandAnimation.getState()) {
            font.getProductSans().drawString("-", getX() + getWidth() - 10, (getY() + getHeight() / 2f) - (font.getProductSans().getHeight() / 2f), new Color(0xff8f8f8f).getRGB());
        } else {
            font.getProductSans().drawString("+", getX() + getWidth() - 10, (getY() + getHeight() / 2f) - (font.getProductSans().getHeight() / 2f), new Color(0xff8f8f8f).getRGB());
        }
        if (expandAnimation.getFactor() > 0) {
            int i = 0;

            for (Button button : buttons) {
                button.setX(getX());
                button.setY(getY() + getHeight() + getHeight() * i);
                button.draw(mouseX, mouseY, mouseDelta);
                i++;
            }
        }
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, InteractionComponent click) {
        if (hovered(mouseX, mouseY) && click.equals(InteractionComponent.RIGHT)) {
            expandAnimation.setState(!expandAnimation.getState());
        }

        if (expandAnimation.getState()) {
            for (Button button : buttons) {
                button.mouseClicked(mouseX, mouseY, click);
            }
        }

        return super.mouseClicked(mouseX, mouseY, click);
    }

    @Override
    public float getOffset() {
        return super.getOffset() + (buttons.size() * getHeight()) * (float) expandAnimation.getFactor();
    }

    private class Button extends RenderableComponent {

        private final Animation hover = new Animation(() -> 200F, false, () -> Easing.LINEAR);
        private final int ordinal;

        public Button(int ordinal, float x, float y, float width, float height) {
            super(x, y, width, height);
            this.ordinal = ordinal;
        }

        @Override
        public void draw(float mouseX, float mouseY, int mouseDelta) {
            hover.setState(ordinal == getSetting().getValue().ordinal());

            Enum<?> enumeration = getSetting().getValue();

            RenderUtils.drawRect(getX() + 1.5f, getY(), getWidth() + getX() - 3, getHeight() + getY(), ColorUtil.interpolate(new Color(ColorUtil.getColor(false)), new Color(0x1F1F1F), hover.getFactor()).getRGB());
            FontService font = Virago.getInstance().getServiceManager().getService(FontService.class);
            font.getProductSans().drawString(formatEnum(Arrays.stream(enumeration.getClass().getEnumConstants()).filter(e -> e.ordinal() == ordinal).collect(Collectors.toList()).get(0)), getX() + 4f, getY() + getHeight() / 2f - font.getProductSans().getHeight() / 2f, ColorUtil.interpolate(new Color(-1), new Color(0xff5b5b5b), hover.getFactor()).getRGB(), false);
        }

        public String formatEnum(Enum<?> enumIn) {
            String text = enumIn.toString();
            StringBuilder formatted = new StringBuilder();

            boolean isNewWord = false;
            for (char c : text.toCharArray()) {
                if (c == '_') {
                    isNewWord = true;
                    continue;
                }

                if (isNewWord || formatted.length() == 0) {
                    formatted.append(Character.toUpperCase(c));
                    isNewWord = false;
                } else {
                    formatted.append(Character.toLowerCase(c));
                }
            }

            return formatted.toString();
        }

        @Override
        public boolean mouseClicked(float mouseX, float mouseY, InteractionComponent click) {
            if (hovered(mouseX, mouseY)) {
                Enum<?> enumeration = getSetting().getValue();
                getSetting().setValue(Enum.valueOf(enumeration.getClass(), Arrays.stream(enumeration.getClass().getEnumConstants()).map(Enum::name).toArray(String[]::new)[ordinal]));
            }

            return false;
        }

        @Override
        public void mouseReleased(float mouseX, float mouseY, InteractionComponent click) {

        }

        @Override
        public void keyTyped(char typedChar, int keyCode) {

        }
    }
}
