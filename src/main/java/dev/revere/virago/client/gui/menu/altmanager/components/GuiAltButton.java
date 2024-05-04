package dev.revere.virago.client.gui.menu.altmanager.components;

import dev.revere.virago.Virago;
import dev.revere.virago.api.alt.Alt;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.util.render.RoundedUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Remi
 * @project Virago
 * @date 5/4/2024
 */
@Getter
@Setter
public class GuiAltButton {
    protected int id;
    public float x;
    public float y;
    public float width;
    public float height;
    public boolean selected;
    public Alt alt;

    /**
     * Initializes the button
     *
     * @param id     the id of the button
     * @param x      the x position of the button
     * @param y      the y position of the button
     * @param width  the width of the button
     * @param height the height of the button
     * @param alt    the alt account
     */
    public GuiAltButton(int id, float x, float y, float width, float height, Alt alt) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.alt = alt;
        this.selected = false;
    }

    /**
     * Draws the button
     */
    public void drawScreen() {
        FontService fontService = Virago.getInstance().getServiceManager().getService(FontService.class);
        RoundedUtils.shadowGradient(getX(), getY(), getPanelWidth(), getPanelHeight(), 0, 5, 10, new Color(20, 20, 20, 255), new Color(20, 20, 20, 255), new Color(20, 20, 20, 255), new Color(20, 20, 20, 255), false);

        if (selected) {
            Gui.drawRect((int) getX(), (int) getY(), (int) (getX() + getPanelWidth()), (int) (getY() + getPanelHeight()), new Color(0, 0, 0, 100).getRGB());
        }

        Date date = new Date(alt.getCreationDate());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        fontService.getProductSans().drawString(alt.getUsername(), (int) (getX() + 32), (int) (getY() + 6), -1);
        fontService.getProductSans().drawString(dateFormat.format(date), (int) (getX() + 32), (int) (getY() + 16), -1);
        fontService.getProductSans().drawString(alt.getType().toUpperCase(), (int) (getX() + 35 + fontService.getProductSans().getStringWidth(dateFormat.format(date))), (int) (getY() + 16), getAltTypeColor().getRGB());
        drawPlayerHead(getX() + 3, getY() + 3, (int) ((getHeight() - 3) - (getY() + 3)));
    }

    private Color getAltTypeColor() {
        switch (alt.getType().toLowerCase()) {
            case "cracked":
                return new Color(55, 255, 0);
            case "cookie":
                return new Color(0, 196, 255);
            default:
                return new Color(255, 255, 255);
        }
    }

    /**
     * Draws the player head
     *
     * @param x    the x position of the head
     * @param y    the y position of the head
     * @param size the size of the head
     */
    private void drawPlayerHead(float x, float y, int size) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture((id % 3 == 0) ? new ResourceLocation("textures/entity/steve.png") : new ResourceLocation("textures/entity/alex.png"));
        Gui.drawScaledCustomSizeModalRect((int) x, (int) y, 8.0F, 8.0F, 8, 8, size, size, 64.0F, 64.0F);
    }

    /**
     * Checks if the mouse is hovering over the button
     *
     * @param mouseX the x position of the mouse
     * @param mouseY the y position of the mouse
     * @return if the mouse is hovering over the button
     */
    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= getX() && mouseX <= getX() + getPanelWidth() && mouseY >= getY() && mouseY <= getY() + getPanelHeight();
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
}