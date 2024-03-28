package dev.revere.virago.client.gui.menu;


import dev.revere.virago.Virago;
import dev.revere.virago.client.gui.menu.alt.GuiAltManager;
import dev.revere.virago.client.gui.menu.components.GuiImageButton;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.util.render.RenderUtils;
import dev.revere.virago.util.render.RoundedUtils;
import net.minecraft.client.gui.*;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class CustomGuiMainMenu extends GuiScreen {

    private static final ResourceLocation LOGO_TEXTURE = new ResourceLocation("virago/textures/logo/logo.png");
    private static final ResourceLocation COSMETICS_TEXTURE = new ResourceLocation("virago/textures/icons/cosmetics.png");
    private static final ResourceLocation CLOSE_TEXTURE = new ResourceLocation("virago/textures/icons/close.png");
    private static final ResourceLocation ALTS_TEXTURE = new ResourceLocation("virago/textures/icons/alts.png");
    private static final ResourceLocation OPTIONS_TEXTURE = new ResourceLocation("virago/textures/icons/options.png");
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("virago/textures/bg.png");

    /**
     * Initializes the screen and all the components in it.
     */
    @Override
    public void initGui() {
        int centerX = width / 2;
        int centerY = height / 2;

        this.buttonList.clear();
        this.buttonList.add(new GuiButton(1, centerX - 85, centerY + 37, 170, 18, "S I N G L E P L A Y E R"));
        this.buttonList.add(new GuiButton(2, centerX - 85, centerY + 60, 170, 18, "M U L T I P L A Y E R"));

        int BUTTON_WIDTH = 20;
        int BUTTON_HEIGHT = 20;

        int buttonSpacing = 3;
        int totalButtonWidth = 3 * BUTTON_WIDTH + 2 * buttonSpacing;
        int startX = (width - totalButtonWidth) / 2;
        int startY = height - BUTTON_HEIGHT - 10;

        this.buttonList.add(new GuiImageButton(3, startX, startY, BUTTON_WIDTH, BUTTON_HEIGHT, COSMETICS_TEXTURE));
        this.buttonList.add(new GuiImageButton(4, startX + BUTTON_WIDTH + buttonSpacing, startY, BUTTON_WIDTH, BUTTON_HEIGHT, OPTIONS_TEXTURE));
        this.buttonList.add(new GuiImageButton(5, startX + 2 * (BUTTON_WIDTH + buttonSpacing), startY, BUTTON_WIDTH, BUTTON_HEIGHT, ALTS_TEXTURE));
        this.buttonList.add(new GuiImageButton(8, width - BUTTON_WIDTH - 10, 10, BUTTON_WIDTH, BUTTON_HEIGHT, CLOSE_TEXTURE));
    }

    /**
     * Draws the screen and all the components in it.
     *
     * @param mouseX the x position of the mouse
     * @param mouseY the y position of the mouse
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtils.drawImage(BACKGROUND_TEXTURE, 0, 0, width, height);
        FontService fonts = Virago.getInstance().getServiceManager().getService(FontService.class);

        int boxWidth = 240;
        int boxHeight = 200;
        int centerX = width / 2;
        int centerY = height / 2;

        // Draw background
        RoundedUtils.shadowGradient(centerX - (float) boxWidth / 2, centerY - (float) boxHeight / 2 + 5, boxWidth, boxHeight, 10, 10, 100, new Color(20, 20, 20, 200), new Color(20, 20, 20, 200), new Color(20, 20, 20, 200), new Color(20, 20, 20, 200), false);

        // Draw text
        fonts.getRalewayExtraBold().drawCenteredStringWithShadow("VIRAGO CLIENT", centerX, centerY + 15, 0xFFFFFF);

        // Draw logo
        RenderUtils.drawImage(LOGO_TEXTURE, centerX - 50, centerY - 90, 100, 100);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Fired when a button is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     *
     * @param button the button that was clicked
     */
    @Override
    protected void actionPerformed(GuiButton button) {
        if(button.enabled) {
            switch(button.id) {
                case 1:
                    this.mc.displayGuiScreen(new GuiSelectWorld(this));
                    break;
                case 2:
                    this.mc.displayGuiScreen(new GuiMultiplayer(this));
                    break;
                case 4:
                    this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
                    break;
                case 5:
                    this.mc.displayGuiScreen(new GuiAltManager(this));
                    break;
                case 8:
                    this.mc.shutdown();
                    break;
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
