package dev.revere.virago.client.gui.menu.altmanager.components;

import dev.revere.virago.Virago;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.util.render.RenderUtils;
import dev.revere.virago.util.render.RoundedUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

/**
 * @author Remi
 * @project Virago
 * @date 5/4/2024
 */
@Getter
@Setter
public class GuiMainButton extends Gui {
    public String name;
    public int id;
    public float x;
    public float y;
    public float width;
    public float height;
    public boolean hovered;

    /**
     * Initializes the button
     *
     * @param name   the name of the button
     * @param id     the id of the button
     * @param width  the width of the button
     * @param height the height of the button
     */
    public GuiMainButton(String name, int id, float width, float height) {
        this.name = name;
        this.id = id;
        this.width = width;
        this.height = height;
    }

    /**
     * Sets the x position of the button
     *
     * @param x the x position
     */
    public void setX(float x) {
        if (this.x != x) {
            this.x = x;
        }
    }

    /**
     * Sets the y position of the button
     *
     * @param y the y position
     */
    public void setY(float y) {
        if (this.y != y) {
            this.y = y;
        }
    }

    public float getWidth() {
        return getX() + getPanelWidth();
    }

    public float getHeight() {
        return getY() + getPanelHeight();
    }

    public float getPanelWidth() {
        return this.width;
    }

    public float getPanelHeight() {
        return this.height;
    }

    /**
     * Draws the button
     *
     * @param mc     the minecraft instance
     * @param mouseX the x position of the mouse
     * @param mouseY the y position of the mouse
     */
    public void drawButtonCenteredString(Minecraft mc, int mouseX, int mouseY) {
        FontService fontService = Virago.getInstance().getServiceManager().getService(FontService.class);
        setHovered(mouseX >= getX() && mouseY >= getY() && mouseX < getWidth() && mouseY < getHeight());
        RoundedUtils.glRound(getX(), getY(), getPanelWidth(), getPanelHeight(), 5, isHovered() ? new Color(50, 50, 50, 200).getRGB() : new Color(20, 20, 20, 200).getRGB());
        RoundedUtils.outline(getX(), getY(), getPanelWidth(), getPanelHeight(), 5, 1, new Color(50, 50, 50, 200));
        //RenderUtils.drawBorderedRect(getX(), getY(), getPanelWidth(), getPanelHeight(), 0xff303030, isHovered() ? new Color(50, 50, 50, 100).getRGB() : 0);

        fontService.getProductSans().drawCenteredString(getName(), getWidth() - 50, getHeight() - 14, -1);
        //this.drawCenteredString(mc.fontRendererObj, getName(), (int) getWidth() - 50, (int) getHeight() - 14, -1);
    }

    /**
     * Fired when a button is clicked.
     *
     * @param mouseX the x position of the mouse
     * @param mouseY the y position of the mouse
     */
    public boolean mousePressed(int mouseX, int mouseY) {
        if ((mouseX >= getX() && mouseY >= getY() && mouseX < getWidth() && mouseY < getHeight())) {
            playPressSound(Minecraft.getMinecraft().getSoundHandler());
        }
        return (mouseX >= getX() && mouseY >= getY() && mouseX < getWidth() && mouseY < getHeight());
    }

    /**
     * Plays the press sound
     *
     * @param soundHandlerIn the sound handler
     */
    public void playPressSound(SoundHandler soundHandlerIn) {
        soundHandlerIn.playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
    }
}
