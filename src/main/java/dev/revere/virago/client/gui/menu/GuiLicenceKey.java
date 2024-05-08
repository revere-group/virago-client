package dev.revere.virago.client.gui.menu;


import dev.revere.virago.Virago;
import dev.revere.virago.api.protection.auth.Safelock;
import dev.revere.virago.client.services.ConfigService;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.util.Logger;
import dev.revere.virago.util.render.RenderUtils;
import dev.revere.virago.util.render.RoundedUtils;
import dev.revere.virago.client.gui.menu.components.CustomGuiTextField;
import dev.revere.virago.util.shader.GLSLSandboxShader;
import lombok.Setter;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.awt.*;
import java.io.IOException;

@Setter
public class GuiLicenceKey extends GuiScreen {
    private static final ResourceLocation LOGO_TEXTURE = new ResourceLocation("virago/textures/logo/logo.png");
    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation("virago/textures/bg.png");
    private CustomGuiTextField licenceKey;
    private GuiButton button;

    public static boolean isAuthorized = false;
    public static String status = "";

    private GLSLSandboxShader backgroundShader;

    /**
     * Initializes the screen and all the components in it.
     */
    @Override
    public void initGui() {
        int centerX = width / 2;
        int centerY = height / 2;

        // Create text field
        this.licenceKey = new CustomGuiTextField(0, centerX - 85, centerY + 37, 170, 18, "enter your license key...");
        String keyFromConfig = Virago.getInstance().getServiceManager().getService(ConfigService.class).loadLicenseKey();
        this.licenceKey.setText(keyFromConfig != null ? keyFromConfig : "");
        this.licenceKey.setCanLoseFocus(true);
        this.licenceKey.setMaxStringLength(29);

        // Create button
        this.button = new GuiButton(1, centerX - 85, centerY + 60, 170, 18, "A U T H O R I Z E");
        this.button.enabled = true;

        // Add components to the screen
        this.buttonList.add(this.button);
        try {
            this.backgroundShader = new GLSLSandboxShader("/assets/minecraft/virago/shader/background/noise.fsh");
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
        //RenderUtils.drawImage(BACKGROUND_TEXTURE, 0, 0, width, height);
        GlStateManager.disableCull();
        this.backgroundShader.useShader(this.width, this.height + 600, mouseX, mouseY, (System.currentTimeMillis() - Virago.getInstance().getDiscordRPC().getCreated()) / 1000f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(-1f, -1f);
        GL11.glVertex2f(-1f, 1f);
        GL11.glVertex2f(1f, 1f);
        GL11.glVertex2f(1f, -1f);
        GL11.glEnd();
        GL20.glUseProgram(0);
        FontService fonts = Virago.getInstance().getServiceManager().getService(FontService.class);

        int boxWidth = 240;
        int boxHeight = 200;
        int centerX = width / 2;
        int centerY = height / 2;

        // Draw background
        RoundedUtils.shadowGradient(centerX - (float) boxWidth / 2, centerY - (float) boxHeight / 2 + 5, boxWidth, boxHeight, 10, 10, 100, new Color(20, 20, 20, 200), new Color(20, 20, 20, 200), new Color(20, 20, 20, 200), new Color(20, 20, 20, 200), false);

        // Draw text field
        this.licenceKey.drawTextBox();

        // Draw text
        fonts.getRalewayExtraBold().drawCenteredStringWithShadow("VIRAGO CLIENT", centerX, centerY + 15, 0xFFFFFF);
        fonts.getRalewayExtraBold14().drawCenteredStringWithShadow(status, centerX, centerY + 88, 0xFFFFFF);

        // Draw logo
        RenderUtils.drawImage(LOGO_TEXTURE, centerX - 50, centerY - 90, 100, 100);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     *
     * @param  mouseX the x position of the mouse
     * @param  mouseY the y position of the mouse
     * @param  mouseButton the mouse button clicked
     * @throws IOException if an I/O exception occurs
     */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.licenceKey.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     *
     * @param  typedChar the character typed
     * @param  keyCode the key code
     * @throws IOException if an I/O exception occurs
     */
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        this.licenceKey.textboxKeyTyped(typedChar, keyCode);
    }

    /**
     * Fired when a button is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     *
     * @param button the button that was clicked
     */
    @Override
    protected void actionPerformed(GuiButton button) {
        if (button == this.button) {
            if(new Safelock(this.licenceKey.getText(), "https://virago.revere.dev/api/client", "ba05042d0880ef940054eec4ad14177e3561414a").nigger()) {
                Virago.getInstance().getServiceManager().getService(ConfigService.class).saveLicenseKey(this.licenceKey.getText());
            }
        }
    }

    @Override
    public void updateScreen() {
        // Update text field
        this.licenceKey.updateCursorCounter();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
