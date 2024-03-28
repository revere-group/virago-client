package dev.revere.virago.client.gui.menu.components;

import dev.revere.virago.util.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

public class GuiImageButton extends GuiButton {
    private ResourceLocation location;

    public GuiImageButton(int buttonId, int x, int y, int widthIn, int heightIn, ResourceLocation location) {
        super(buttonId, x, y, widthIn, heightIn, "");
        this.location = location;
    }

    public void setLocation(ResourceLocation location) {
        this.location = location;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        super.drawButton(mc, mouseX, mouseY);

        if (this.visible) {
            RenderUtils.drawImage(location, xPosition + 5, yPosition + 5, 10, 10);
        }
    }
}
