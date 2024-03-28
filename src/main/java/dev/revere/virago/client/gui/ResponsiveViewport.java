package dev.revere.virago.client.gui;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

/**
 * @author Remi
 * @project Virago
 * @date 3/21/2024
 */
@Getter
public abstract class ResponsiveViewport extends GuiScreen {

    public ScaledResolution scaledResolution;
    public float sWidth;
    public float sHeight;

    public abstract void initializeUI();
    public abstract void drawElements(float mouseX, float mouseY);
    public abstract void handleInteraction(float mouseX, float mouseY, int button);

    @Override
    public void initGui() {
        initializeUI();
        super.initGui();
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawElements(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int state) throws IOException {
        handleInteraction(mouseX, mouseY, state);
    }

    public float getScaleFactor() {
        return 1.0f / (scaledResolution.getScaleFactor() * .5f);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
