package dev.revere.virago.client.gui.menu.components;

import dev.revere.virago.Virago;
import dev.revere.virago.client.gui.menu.EnumSelectDesign;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.util.render.ColorUtil;
import dev.revere.virago.util.render.RoundedUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

@Getter
public class GuiSelectDesignButton extends GuiButton {

    private final EnumSelectDesign design;

    public GuiSelectDesignButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, EnumSelectDesign design) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.design = design;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.visible)
        {
            FontService fontService = Virago.getInstance().getServiceManager().getService(FontService.class);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            RoundedUtils.round(this.xPosition, this.yPosition, this.width, this.height, 3, new Color(ColorUtil.getColor(false)));
            RoundedUtils.outline(this.xPosition, this.yPosition, this.width, this.height, 3, 1, new Color(ColorUtil.getColor(false)));
            fontService.getRalewayExtraBold14().drawCenteredStringWithShadow(design.toString(), this.xPosition + this.width / 2, this.yPosition + (this.height - 2) / 2, new Color(255, 255, 255, 255).getRGB());
            boolean hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
        }
    }
}
