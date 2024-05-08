package dev.revere.virago.client.gui.menu.components;

import dev.revere.virago.Virago;
import dev.revere.virago.client.gui.menu.EnumSelectDesign;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.util.animation.Animation;
import dev.revere.virago.util.animation.Easing;
import dev.revere.virago.util.render.ColorUtil;
import dev.revere.virago.util.render.RenderUtils;
import dev.revere.virago.util.render.RoundedUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

@Getter
public class GuiSelectDesignButton extends GuiButton {

    private final Animation animation = new Animation(() -> 2.0f, false, () -> Easing.CUBIC_IN_OUT);
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
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(new ResourceLocation(design.getShaderPreviewPath()));
            RoundedUtils.texture(this.xPosition, this.yPosition, this.width, this.height, 12, 255);
            RoundedUtils.outline(this.xPosition, this.yPosition, this.width, this.height, 10, 1f, new Color(ColorUtil.getColor(false)));
        }
    }
}
