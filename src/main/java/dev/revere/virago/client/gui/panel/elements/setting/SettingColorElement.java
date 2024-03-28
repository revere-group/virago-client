package dev.revere.virago.client.gui.panel.elements.setting;

import dev.revere.virago.Virago;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.gui.components.InteractionComponent;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.util.render.RenderUtils;
import dev.revere.virago.util.render.RoundedUtils;
import org.lwjgl.input.Mouse;

import java.awt.*;

/**
 * @author Remi
 * @project Virago
 * @date 3/22/2024
 */
public class SettingColorElement extends SettingElement<Color> {
    private boolean dragging, sliding, opacitySliding;

    public SettingColorElement(Setting<Color> set, float x, float y, float width, float height) {
        super(set, x, y, width, height);
    }

    @Override
    public void draw(float mouseX, float mouseY, int mouseDelta) {
        if (!Mouse.isButtonDown(0)) {
            sliding = false;
            dragging = false;
            opacitySliding = false;
        }

        Color originalSetColor = getSetting().getValue();
        float[] hsb = Color.RGBtoHSB(
                originalSetColor.getRed(),
                originalSetColor.getGreen(),
                originalSetColor.getBlue(),
                null
        );

        float opacity = (float) originalSetColor.getAlpha() / 255.0f;

        float pickerX = getX() + 4;
        float pickerY = getY() + 18;
        float pickerWidth = getWidth() - 8;
        float pickerHeight = getHeight() - 50;

        float RAWpickerTargetX = Math.min(Math.max(0, pickerWidth + (pickerX - mouseX)), pickerWidth - 1);
        float RAWpickerTargetY = Math.min(Math.max(0, pickerHeight + (pickerY - mouseY)), pickerHeight);

        float pickerTargetX = RAWpickerTargetX / pickerWidth;
        float pickerTargetY = RAWpickerTargetY / pickerHeight;

        float hueX = pickerX + 0.5f;
        float hueY = pickerY + pickerHeight + 2;
        float hueWidth = pickerWidth - 1.5f;
        float hueHeight = 10;

        float opacityX = pickerX + 0.5f;;
        float opacityY = getY() + getHeight() - 18;
        float opacityWidth = pickerWidth - 1.5f;
        float opacityHeight = 10;

        float RAWhueTargetX = RAWpickerTargetX;
        float RAWopacityTargetX = RAWpickerTargetX;
        float hueTargetX = RAWhueTargetX / hueWidth;
        float opacityTargetX = 1 - (RAWopacityTargetX / opacityWidth);

        Color hueCol = new Color(Color.HSBtoRGB(hsb[0], 1.0f, 1.0f));
        float[] hsb2 = {hsb[0], hsb[1], hsb[2], opacity};

        RoundedUtils.gradient(pickerX, pickerY, pickerWidth, pickerHeight, 1, 1.0f, Color.WHITE, Color.BLACK, hueCol, Color.BLACK);

        // hue
        for (int i = 0; i < hueWidth; i++) {
            RenderUtils.rect(hueX + i, hueY, 1, hueHeight, new Color(Color.HSBtoRGB((i / (getWidth() - 8F)), 1.0F, 1.0F)));
        }
        RenderUtils.rect(hueX + hueWidth * ((hsb[0] - (1 / 360f))), hueY, 1, hueHeight, Color.WHITE);

        // opacity
        for (int i = 0; i < opacityWidth; i++) {
            float opacityValue = (opacityWidth - i) / opacityWidth;
            Color opacityColor = new Color(getSetting().getValue().getRed(), getSetting().getValue().getGreen(), getSetting().getValue().getBlue(), (int) (255 * opacityValue));
            RenderUtils.rect(opacityX + i, opacityY, 1, opacityHeight, opacityColor);
        }
        RenderUtils.rect(opacityX + opacityWidth * (opacitySliding ? opacityTargetX : 1 - opacity), opacityY, 1, opacityHeight, Color.WHITE);

        Color newColor;
        if (opacitySliding) {
            float opacityValue = 1 - opacityTargetX;
            opacityValue = Math.max(0, Math.min(opacityValue, 1));

            newColor = new Color(originalSetColor.getRed(), originalSetColor.getGreen(), originalSetColor.getBlue(), (int) (255 * opacityValue));
            getSetting().setValue(newColor);
        } else if (dragging) {
            float saturationValue = 1 - pickerTargetX;
            float brightnessValue = pickerTargetY;
            saturationValue = Math.max(0, Math.min(saturationValue, 1));
            brightnessValue = Math.max(0, Math.min(brightnessValue, 1));

            newColor = new Color(Color.HSBtoRGB(hsb[0], saturationValue, brightnessValue));
            getSetting().setValue(new Color(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), originalSetColor.getAlpha()));
        } else if (sliding) {
            float hueValue = 1 - hueTargetX;
            hueValue = Math.max(0, Math.min(hueValue, 1));

            newColor = new Color(Color.HSBtoRGB(hueValue, hsb[1], hsb[2]));
            getSetting().setValue(new Color(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), originalSetColor.getAlpha()));
        }

        // gradient picker
        float circleWidth = 3;
        RoundedUtils.circle(pickerX + (hsb2[1]) * pickerWidth, pickerY + (1 - hsb2[2]) * pickerHeight, circleWidth, Color.WHITE);

        Virago.getInstance().getServiceManager().getService(FontService.class).getProductSans().drawString(getSetting().getName(), getX() + 4f, getY() + 4, new Color(0xff8f8f8f).getRGB(), false);
    }

    @Override
    public boolean mouseClicked(float mouseX, float mouseY, InteractionComponent click) {
        float pickerX = getX() + 4;
        float pickerY = getY() + 18;
        float pickerWidth = getWidth() - 8;
        float pickerHeight = getHeight() - 50;

        float hueX = pickerX + 0.5f;
        float hueY = pickerY + pickerHeight + 2;
        float hueWidth = pickerWidth - 1.5f;
        float hueHeight = 10;

        float opacityX = pickerX + 0.5f;;
        float opacityY = getY() + getHeight() - 18;
        float opacityWidth = pickerWidth - 1.5f;
        float opacityHeight = 10;

        if (mouseX >= pickerX && mouseY >= pickerY && mouseX <= pickerX + pickerWidth && mouseY <= pickerY + pickerHeight) {
            if (click.equals(InteractionComponent.LEFT)) {
                dragging = true;
            }
        }

        if (mouseX >= hueX && mouseY >= hueY && mouseX <= hueX + hueWidth && mouseY <= hueY + hueHeight) {
            if (click.equals(InteractionComponent.LEFT)) {
                sliding = true;
            }
        }

        if (mouseX >= opacityX && mouseY >= opacityY && mouseX <= opacityX + opacityWidth && mouseY <= opacityY + opacityHeight) {
            if (click.equals(InteractionComponent.LEFT)) {
                opacitySliding = true;
            }
        }


        return super.mouseClicked(mouseX, mouseY, click);
    }
}
