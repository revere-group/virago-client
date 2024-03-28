package dev.revere.virago.client.gui.menu.alt.components;

import dev.revere.virago.Virago;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.util.render.RoundedUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

/**
 * @author Remi
 * @project Virago
 * @date 3/27/2024
 */
public class CustomRectButton extends GuiButton {
    private final Color backgroundColor;
    private final Color outlineColor;
    private final Color textColor;

    public CustomRectButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, Color backgroundColor, Color outlineColor, Color textColor) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.backgroundColor = backgroundColor;
        this.outlineColor = outlineColor;
        this.textColor = textColor;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.blendFunc(770, 771);

            // Draw rounded rectangle background
            RoundedUtils.shadowGradient(this.xPosition, this.yPosition, this.width, this.height, 5, 5, 5, backgroundColor, backgroundColor, backgroundColor, backgroundColor, false);
            RoundedUtils.round(this.xPosition, this.yPosition, this.width, this.height, 5, backgroundColor);

            // Draw outline
            RoundedUtils.outline(this.xPosition, this.yPosition, this.width, this.height, 5, 1, outlineColor);

            // Draw button text
            FontService fontService = Virago.getInstance().getServiceManager().getService(FontService.class);
            String buttonText = this.displayString;
            int textWidth = fontService.getRalewayExtraBold().getStringWidth(buttonText);
            int textX = this.xPosition + (this.width - textWidth) / 2;
            int textY = this.yPosition + (this.height - 8) / 2;
            fontService.getRalewayExtraBold().drawStringWithShadow(buttonText, textX, textY, textColor.getRGB());
        }
    }
}