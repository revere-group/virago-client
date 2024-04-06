package dev.revere.virago.util.render;

import dev.revere.virago.util.shader.Shader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/6/2024
 */
public class BloomUtil {

    private static final Shader kawaseDownBloom = new Shader(new ResourceLocation("virago/shader/kawaseDownBloom.frag"));
    private static final Shader kawaseUpBloom = new Shader(new ResourceLocation("virago/shader/kawaseUpBloom.frag"));

    private static final List<Framebuffer> framebufferList = new ArrayList<>();
    private static Framebuffer framebuffer = new Framebuffer(1, 1, true);
    private static int currentIterations;

    private static void initFramebuffers(float iterations) {
        for (Framebuffer framebuffer : framebufferList) {
            framebuffer.deleteFramebuffer();
        }

        framebufferList.clear();
        framebufferList.add(framebuffer = RenderUtils.createFrameBuffer(null, true));

        for (int i = 1; i <= iterations; i++) {
            Framebuffer currentBuffer = new Framebuffer((int) (Minecraft.getMinecraft().displayWidth / Math.pow(2, i)), (int) (Minecraft.getMinecraft().displayHeight / Math.pow(2, i)), true);
            currentBuffer.setFramebufferFilter(GL11.GL_LINEAR);

            GlStateManager.bindTexture(currentBuffer.framebufferTexture);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL14.GL_MIRRORED_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL14.GL_MIRRORED_REPEAT);
            GlStateManager.bindTexture(0);

            framebufferList.add(currentBuffer);
        }
    }


    public static void renderBlur(int framebufferTexture, int iterations, int offset) {
        if (currentIterations != iterations || (framebuffer.framebufferWidth != Minecraft.getMinecraft().displayWidth || framebuffer.framebufferHeight != Minecraft.getMinecraft().displayHeight)) {
            initFramebuffers(iterations);
            currentIterations = iterations;
        }

        RenderUtils.setAlphaLimit(0);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_ONE);

        GL11.glClearColor(0, 0, 0, 0);
        renderFBO(framebufferList.get(1), framebufferTexture, kawaseDownBloom, offset);

        for (int i = 1; i < iterations; i++) {
            renderFBO(framebufferList.get(i + 1), framebufferList.get(i).framebufferTexture, kawaseDownBloom, offset);
        }

        for (int i = iterations; i > 1; i--) {
            renderFBO(framebufferList.get(i - 1), framebufferList.get(i).framebufferTexture, kawaseUpBloom, offset);
        }

        Framebuffer lastBuffer = framebufferList.get(0);
        lastBuffer.framebufferClear();
        lastBuffer.bindFramebuffer(false);

        kawaseUpBloom.init();
        int offsetLocation = GL20.glGetUniformLocation(kawaseUpBloom.getProgram(), "offset");
        int inTextureLocation = GL20.glGetUniformLocation(kawaseUpBloom.getProgram(), "inTexture");
        int checkLocation = GL20.glGetUniformLocation(kawaseUpBloom.getProgram(), "check");
        int textureToCheckLocation = GL20.glGetUniformLocation(kawaseUpBloom.getProgram(), "textureToCheck");
        int halfpixelLocation = GL20.glGetUniformLocation(kawaseUpBloom.getProgram(), "halfpixel");
        int iResolutionLocation = GL20.glGetUniformLocation(kawaseUpBloom.getProgram(), "iResolution");

        GL20.glUniform2f(offsetLocation, offset, offset);
        GL20.glUniform1i(inTextureLocation, 0);
        GL20.glUniform1i(checkLocation, 1);
        GL20.glUniform1i(textureToCheckLocation, 16);
        GL20.glUniform2f(halfpixelLocation, 1.0f / framebuffer.framebufferWidth, 1.0f / framebuffer.framebufferHeight);
        GL20.glUniform2f(iResolutionLocation, framebuffer.framebufferWidth, framebuffer.framebufferHeight);

        GlStateManager.setActiveTexture(GL13.GL_TEXTURE16);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, framebufferTexture);
        GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, framebufferList.get(1).framebufferTexture);

        drawQuads();
        kawaseUpBloom.finish();

        GlStateManager.clearColor(0, 0, 0, 0);
        Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(false);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, framebufferList.get(0).framebufferTexture);
        RenderUtils.setAlphaLimit(0);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        drawQuads();

        GlStateManager.bindTexture(0);
        RenderUtils.setAlphaLimit(0);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static void renderFBO(Framebuffer framebuffer, int framebufferTexture, Shader shader, float offset) {
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(false);
        shader.init();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, framebufferTexture);

        int offsetLocation = GL20.glGetUniformLocation(shader.getProgram(), "offset");
        int inTextureLocation = GL20.glGetUniformLocation(shader.getProgram(), "inTexture");
        int checkLocation = GL20.glGetUniformLocation(shader.getProgram(), "check");
        int halfpixelLocation = GL20.glGetUniformLocation(shader.getProgram(), "halfpixel");
        int iResolutionLocation = GL20.glGetUniformLocation(shader.getProgram(), "iResolution");

        GL20.glUniform2f(offsetLocation, offset, offset);
        GL20.glUniform1i(inTextureLocation, 0);
        GL20.glUniform1i(checkLocation, 0);
        GL20.glUniform2f(halfpixelLocation, 1.0f / framebuffer.framebufferWidth, 1.0f / framebuffer.framebufferHeight);
        GL20.glUniform2f(iResolutionLocation, framebuffer.framebufferWidth, framebuffer.framebufferHeight);
        drawQuads();
        shader.finish();
    }

    public static void drawQuads() {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        float width = (float) sr.getScaledWidth_double();
        float height = (float) sr.getScaledHeight_double();
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 1);
        GL11.glVertex2f(0, 0);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex2f(0, height);
        GL11.glTexCoord2f(1, 0);
        GL11.glVertex2f(width, height);
        GL11.glTexCoord2f(1, 1);
        GL11.glVertex2f(width, 0);
        GL11.glEnd();
    }

}
