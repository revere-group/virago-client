package dev.revere.virago.client.gui.menu;


import dev.revere.virago.Virago;
import dev.revere.virago.client.gui.menu.altmanager.GuiAltManager;
import dev.revere.virago.client.gui.menu.components.GuiImageButton;
import dev.revere.virago.client.gui.menu.components.GuiSelectDesignButton;
import dev.revere.virago.client.services.DesignService;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.util.Logger;
import dev.revere.virago.util.render.ColorUtil;
import dev.revere.virago.util.render.RenderUtils;
import dev.revere.virago.util.render.RoundedUtils;
import dev.revere.virago.util.shader.GLSLSandboxShader;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.awt.*;

public class GuiSelectDesign extends GuiScreen {

    private final ResourceLocation LOGO_TEXTURE = new ResourceLocation("virago/textures/logo/logo.png");
    private GLSLSandboxShader backgroundShader;
    private EnumSelectDesign selectedDesign;

    /**
     * Initializes the screen and all the components in it.
     */
    @Override
    public void initGui() {
        buttonList.clear();

        int buttonWidth = 130;
        int buttonHeight = 70;
        int buttonMargin = 5;

        int boxHeight = 250;
        int centerX = width / 2;
        int centerY = height / 2;

        float xOffset = (float) (-((buttonWidth + buttonMargin) * 3) / 2) + 2;
        float yOffset = centerY - (float) boxHeight / 2 + 100;

        for (int i = 0; i < 3; i++) {
            EnumSelectDesign enumSelectDesign = EnumSelectDesign.values()[i];
            buttonList.add(new GuiSelectDesignButton(i, (int) (centerX + xOffset), (int) yOffset, buttonWidth, buttonHeight, enumSelectDesign.toString(), enumSelectDesign));
            xOffset += buttonWidth + buttonMargin;
        }

        yOffset += buttonHeight + buttonMargin;
        xOffset = (float) (-((buttonWidth + buttonMargin) * 3) / 2) + 2;

        for (int i = 3; i < 6; i++) {
            EnumSelectDesign enumSelectDesign = EnumSelectDesign.values()[i];
            buttonList.add(new GuiSelectDesignButton(i, (int) (centerX + xOffset), (int) yOffset, buttonWidth, buttonHeight, enumSelectDesign.toString(), enumSelectDesign));
            xOffset += buttonWidth + buttonMargin;
        }

        DesignService service = Virago.getInstance().getServiceManager().getService(DesignService.class);
        if (Virago.getInstance().getServiceManager().getService(DesignService.class).getSelectedDesign() == null) {
            selectedDesign = EnumSelectDesign.values()[0];
            service.setSelectedDesign(selectedDesign);
        }

        updateBackgroundShader(service.getSelectedDesign());
    }

    private void updateBackgroundShader(EnumSelectDesign location) {
        try {
            DesignService service = Virago.getInstance().getServiceManager().getService(DesignService.class);
            service.setSelectedDesign(location);
            backgroundShader = new GLSLSandboxShader(location.getShaderPath());
        } catch (Exception e) {
            Logger.err("Failed to load background shader. " + e.getMessage(), getClass());
        }
    }

    /**
     * Draws the screen and all the components in it.
     *
     * @param mouseX the x position of the mouse
     * @param mouseY the y position of the mouse
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.disableCull();
        this.backgroundShader.useShader(this.width * 2, this.height * 2, mouseX, mouseY, (System.currentTimeMillis() - Virago.getInstance().getStarted()) / 1000f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(-1f, -1f);
        GL11.glVertex2f(-1f, 1f);
        GL11.glVertex2f(1f, 1f);
        GL11.glVertex2f(1f, -1f);
        GL11.glEnd();
        GL20.glUseProgram(0);

        FontService fonts = Virago.getInstance().getServiceManager().getService(FontService.class);

        int boxWidth = 440;
        int boxHeight = 270;
        int centerX = width / 2;
        int centerY = height / 2;

        RoundedUtils.round(centerX - (float) boxWidth / 2, centerY - (float) boxHeight / 2 + 5, boxWidth, boxHeight, 10, new Color(20, 20, 20, 200));
        RoundedUtils.round(centerX - (float) boxWidth / 2, centerY - (float) boxHeight / 2 + 90, boxWidth, 3, 2, new Color(ColorUtil.getColor(false)).darker());
        RoundedUtils.outline(centerX - (float) boxWidth / 2, centerY - (float) boxHeight / 2 + 5, boxWidth, boxHeight, 10, 1, new Color(ColorUtil.getColor(false)));

        fonts.getRalewayExtraBold().drawCenteredStringWithShadow("Choose your design", centerX, centerY - 60, 0xFFFFFF);

        RenderUtils.drawImage(LOGO_TEXTURE, centerX - 33, centerY - 125, 64, 64);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Fired when a button is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     *
     * @param button the button that was clicked
     */
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button instanceof GuiSelectDesignButton) {
            GuiSelectDesignButton designButton = (GuiSelectDesignButton) button;
            selectedDesign = designButton.getDesign();
            updateBackgroundShader(selectedDesign);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
