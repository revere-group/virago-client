package dev.revere.virago.client.gui.menu;

import dev.revere.virago.Virago;
import dev.revere.virago.util.shader.GLSLSandboxShader;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class GuiShaderPreview extends GuiScreen {
    private GLSLSandboxShader shader;

    @Override
    public void initGui() {
        try {
            shader = new GLSLSandboxShader("/assets/minecraft/virago/shader/background/noise.fsh");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.disableCull();
        this.shader.useShader(this.width * 2, this.height * 2, mouseX, mouseY, (System.currentTimeMillis() - Virago.getInstance().getStarted()) / 1000f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(-1f, -1f);
        GL11.glVertex2f(-1f, 1f);
        GL11.glVertex2f(1f, 1f);
        GL11.glVertex2f(1f, -1f);
        GL11.glEnd();
        GL20.glUseProgram(0);
    }
}
