package dev.revere.virago.util.render;

import dev.revere.virago.util.shader.Shader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.awt.*;

/**
 * @author Remi
 * @project Virago
 * @date 3/19/2024
 */
public class RoundedUtils {
    private static final Shader rectShader = new Shader(new ResourceLocation("virago/shader/roundedRect.frag"));
    private static final Shader outlineShader = new Shader(new ResourceLocation("virago/shader/roundedOutline.frag"));
    private static final Shader outlineGradientShader = new Shader(new ResourceLocation("virago/shader/roundedGradientOutline.frag"));
    private static final Shader textureShader = new Shader(new ResourceLocation("virago/shader/texture.frag"));
    private static final Shader gradientShader = new Shader(new ResourceLocation("virago/shader/roundedGradientRect.frag"));
    private static final Shader shadowShader = new Shader(new ResourceLocation("virago/shader/shadowRect.frag"));
    private static final Shader shadowGradientShader = new Shader(new ResourceLocation("virago/shader/shadowGradient.frag"));

    /**
     * Draws a rounded rectangle
     *
     * @param x      x position
     * @param y      y position
     * @param width  width
     * @param height height
     * @param radius radius
     * @param color1  first color
     * @param color2  second color
     * @param color3  third color
     * @param color4  color
     */
    public static void glRound(float x, float y, float width, float height, float radius, int color1, int color2, int color3, int color4) {
        int rad;
        GL11.glShadeModel(7425);
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glHint(3155, 4354);
        GL11.glBegin(9);
        ColorUtil.glColor(color1);
        for (rad = 0; rad <= 90; ++rad) {
            GL11.glVertex2d((double) ((double) (x + radius) + Math.sin((double) rad * Math.PI / 180.0) * (double) radius * -1.0), (double) ((double) (y + radius) + Math.cos((double) rad * Math.PI / 180.0) * (double) radius * -1.0));
        }
        ColorUtil.glColor(color2);
        for (rad = 90; rad <= 180; ++rad) {
            GL11.glVertex2d((double) ((double) (x + radius) + Math.sin((double) rad * Math.PI / 180.0) * (double) radius * -1.0), (double) ((double) (y + height - radius) + Math.cos((double) rad * Math.PI / 180.0) * (double) radius * -1.0));
        }
        ColorUtil.glColor(color3);
        for (rad = 0; rad <= 90; ++rad) {
            GL11.glVertex2d((double) ((double) (x + width - radius) + Math.sin((double) rad * Math.PI / 180.0) * (double) radius), (double) ((double) (y + height - radius) + Math.cos((double) rad * Math.PI / 180.0) * (double) radius));
        }
        ColorUtil.glColor(color4);
        for (rad = 90; rad <= 180; ++rad) {
            GL11.glVertex2d((double) ((double) (x + width - radius) + Math.sin((double) rad * Math.PI / 180.0) * (double) radius), (double) ((double) (y + radius) + Math.cos((double) rad * Math.PI / 180.0) * (double) radius));
        }
        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glShadeModel(7424);
        GlStateManager.resetColor();
    }

    /**
     * Draws a rounded rectangle
     *
     * @param x      x position
     * @param y      y position
     * @param width  width
     * @param height height
     * @param radius radius
     * @param color  color
     */
    public static void glRound(float x, float y, float width, float height, float radius, int color) {
        RoundedUtils.glRound(x, y, width, height, radius, color, color, color, color);
    }

    /**
     * Draws a circle
     *
     * @param x     x position
     * @param y     y position
     * @param radius radius
     * @param color  color
     */
    public static void circle(float x, float y, float radius, Color color) {
        RoundedUtils.round(x - radius, y - radius, radius * 2.0f, radius * 2.0f, radius - 1.0f, color);
    }

    /**
     * Draws a gradient
     *
     * @param x       x position
     * @param y       y position
     * @param width   width
     * @param height  height
     * @param radius  radius
     * @param opacity opacity
     * @param colours colors
     */
    public static void gradient(float x, float y, float width, float height, float radius, float opacity, Color[] colours) {
        RoundedUtils.gradient(x, y, width, height, radius, opacity, colours[0], colours[1], colours[2], colours[3]);
    }

    /**
     * Draws a gradient
     *
     * @param x     x position
     * @param y     y position
     * @param width width
     * @param height height
     * @param radius radius
     * @param opacity opacity
     * @param c1    color
     * @param c2    color
     * @param c3    color
     * @param c4    color
     */
    public static void gradient(float x, float y, float width, float height, float radius, float opacity, Color c1, Color c2, Color c3, Color c4) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL20.glUseProgram(gradientShader.getProgram());
        GL20.glUniform2f(GL20.glGetUniformLocation(gradientShader.getProgram(), "size"), width, height);
        GL20.glUniform1f(GL20.glGetUniformLocation(gradientShader.getProgram(), "radius"), radius);
        GL20.glUniform1f(GL20.glGetUniformLocation(gradientShader.getProgram(), "alpha"), opacity);
        GL20.glUniform4f(GL20.glGetUniformLocation(gradientShader.getProgram(), "color1"), (c1.getRed() / 255.0f), (c1.getGreen() / 255.0f), (c1.getBlue() / 255.0f), 1.0f);
        GL20.glUniform4f(GL20.glGetUniformLocation(gradientShader.getProgram(), "color2"), (c2.getRed() / 255.0f), (c2.getGreen() / 255.0f), (c2.getBlue() / 255.0f), 1.0f);
        GL20.glUniform4f(GL20.glGetUniformLocation(gradientShader.getProgram(), "color3"), (c3.getRed() / 255.0f), (c3.getGreen() / 255.0f), (c3.getBlue() / 255.0f), 1.0f);
        GL20.glUniform4f(GL20.glGetUniformLocation(gradientShader.getProgram(), "color4"), (c4.getRed() / 255.0f), (c4.getGreen() / 255.0f), (c4.getBlue() / 255.0f), 1.0f);
        RoundedUtils.rect(x, y, width, height);
        GL20.glUseProgram(0);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    /**
     * Draws a texture
     *
     * @param x       x position
     * @param y       y position
     * @param width   width
     * @param height  height
     * @param radius  radius
     * @param opacity opacity
     */
    public static void texture(float x, float y, float width, float height, float radius, float opacity) {
        GlStateManager.resetColor();
        GL20.glUseProgram(textureShader.getProgram());
        GL20.glUniform2f(GL20.glGetUniformLocation(textureShader.getProgram(), "size"), width, height);
        GL20.glUniform1f(GL20.glGetUniformLocation(textureShader.getProgram(), "radius"), radius);
        GL20.glUniform1f(GL20.glGetUniformLocation(textureShader.getProgram(), "alpha"), opacity);
        GL20.glUniform1i(GL20.glGetUniformLocation(textureShader.getProgram(), "texture"), 0);
        RoundedUtils.rect(x, y, width, height);
        GL20.glUseProgram(0);
        GlStateManager.disableBlend();
    }

    /**
     * Draws a rounded rectangle
     *
     * @param x      x position
     * @param y      y position
     * @param width  width
     * @param height height
     * @param radius radius
     * @param color  color
     */
    public static void round(float x, float y, float width, float height, float radius, Color color) {
        GlStateManager.resetColor();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL20.glUseProgram(rectShader.getProgram());
        GL20.glUniform2f(GL20.glGetUniformLocation(rectShader.getProgram(), "size"), width, height);
        GL20.glUniform1f(GL20.glGetUniformLocation(rectShader.getProgram(), "radius"), radius);
        GL20.glUniform4f(GL20.glGetUniformLocation(rectShader.getProgram(), "color"), (color.getRed() / 255.0f), (color.getGreen() / 255.0f), (color.getBlue() / 255.0f), (color.getAlpha() / 255.0f));
        RoundedUtils.rect(x, y, width, height);
        GL20.glUseProgram(0);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.resetColor();
    }

    /**
     * Draws a rounded outline
     *
     * @param x         x position
     * @param y         y position
     * @param width     width
     * @param height    height
     * @param radius    radius
     * @param thickness thickness
     * @param color     color
     */
    public static void outline(float x, float y, float width, float height, float radius, float thickness, Color color) {
        GlStateManager.resetColor();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL20.glUseProgram(outlineShader.getProgram());
        GL20.glUniform2f(GL20.glGetUniformLocation(outlineShader.getProgram(), "size"), width, height);
        GL20.glUniform1f(GL20.glGetUniformLocation(outlineShader.getProgram(), "radius"), radius);
        GL20.glUniform1f(GL20.glGetUniformLocation(outlineShader.getProgram(), "thickness"), thickness);
        GL20.glUniform4f(GL20.glGetUniformLocation(outlineShader.getProgram(), "color"), (color.getRed() / 255.0f), (color.getGreen() / 255.0f), (color.getBlue() / 255.0f), (color.getAlpha() / 255.0f));
        RoundedUtils.rect(x, y, width, height);
        GL20.glUseProgram(0);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.resetColor();
    }

    /**
     * Draws a rounded outline
     *
     * @param x         x position
     * @param y         y position
     * @param width     width
     * @param height    height
     * @param radius    radius
     * @param thickness thickness
     * @param opacity   opacity
     * @param colours   colors
     */
    public static void outline(float x, float y, float width, float height, float radius, float thickness, float opacity, Color[] colours) {
        RoundedUtils.outline(x, y, width, height, radius, thickness, opacity, colours[0], colours[1], colours[2], colours[3]);
    }

    /**
     * Draws a rounded outline
     *
     * @param x         x position
     * @param y         y position
     * @param width     width
     * @param height    height
     * @param radius    radius
     * @param thickness thickness
     * @param opacity   opacity
     * @param c1        color
     * @param c2        color
     * @param c3        color
     * @param c4        color
     */
    public static void outline(float x, float y, float width, float height, float radius, float thickness, float opacity, Color c1, Color c2, Color c3, Color c4) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL20.glUseProgram(outlineGradientShader.getProgram());
        GL20.glUniform2f(GL20.glGetUniformLocation(outlineGradientShader.getProgram(), "size"), width, height);
        GL20.glUniform1f(GL20.glGetUniformLocation(outlineGradientShader.getProgram(), "radius"), (radius - 1.5f));
        GL20.glUniform1f(GL20.glGetUniformLocation(outlineGradientShader.getProgram(), "thickness"), thickness);
        GL20.glUniform1f(GL20.glGetUniformLocation(outlineGradientShader.getProgram(), "alpha"), opacity);
        GL20.glUniform4f(GL20.glGetUniformLocation(outlineGradientShader.getProgram(), "color1"), (c1.getRed() / 255.0f), (c1.getGreen() / 255.0f), (c1.getBlue() / 255.0f), 1.0f);
        GL20.glUniform4f(GL20.glGetUniformLocation(outlineGradientShader.getProgram(), "color2"), (c2.getRed() / 255.0f), (c2.getGreen() / 255.0f), (c2.getBlue() / 255.0f), 1.0f);
        GL20.glUniform4f(GL20.glGetUniformLocation(outlineGradientShader.getProgram(), "color3"), (c3.getRed() / 255.0f), (c3.getGreen() / 255.0f), (c3.getBlue() / 255.0f), 1.0f);
        GL20.glUniform4f(GL20.glGetUniformLocation(outlineGradientShader.getProgram(), "color4"), (c4.getRed() / 255.0f), (c4.getGreen() / 255.0f), (c4.getBlue() / 255.0f), 1.0f);
        RoundedUtils.rect(x, y, width, height);
        GL20.glUseProgram(0);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    /**
     * Draws a shadow
     *
     * @param x        x position
     * @param y        y position
     * @param width    width
     * @param height   height
     * @param radius   radius
     * @param softness softness
     * @param color    color
     */
    public static void shadow(float x, float y, float width, float height, float radius, float softness, Color color) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        int sF = sr.getScaleFactor();
        x *= 2.0f;
        y *= 2.0f;
        width *= 2.0f;
        height *= 2.0f;
        radius *= 2.0f;
        GlStateManager.resetColor();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GL11.glDisable(3008);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL20.glUseProgram(shadowShader.getProgram());
        GL20.glUniform2f(GL20.glGetUniformLocation(shadowShader.getProgram(), "location"), x, (float) (sr.getScaledHeight_double() * sF - height - y));
        GL20.glUniform2f(GL20.glGetUniformLocation(shadowShader.getProgram(), "size"), width, height);
        GL20.glUniform1f(GL20.glGetUniformLocation(shadowShader.getProgram(), "radius"), radius);
        GL20.glUniform1f(GL20.glGetUniformLocation(shadowShader.getProgram(), "shadowSoftness"), softness);
        GL20.glUniform1f(GL20.glGetUniformLocation(shadowShader.getProgram(), "edgeSoftness"), 1.0f);
        GL20.glUniform4f(GL20.glGetUniformLocation(shadowShader.getProgram(), "color"), (color.getRed() / 255.0f), (color.getGreen() / 255.0f), (color.getBlue() / 255.0f), (color.getAlpha() / 255.0f));
        float g = width / 5.05f;
        float h = height / 5.05f;
        shadowShader.bind(0.0f, 0.0f, (float) (sr.getScaledWidth_double() * sF), (float) (sr.getScaledHeight_double() * sF));
        GL20.glUseProgram(0);
        GL11.glEnable(3008);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.resetColor();
    }

    /**
     * Draws a gradient shadow
     *
     * @param x        x position
     * @param y        y position
     * @param width    width
     * @param height   height
     * @param radius   radius
     * @param softness softness
     * @param opacity  opacity
     * @param c1        color
     * @param c2        color
     * @param c3        color
     * @param c4        color
     */
    public static void shadowGradient(float x, float y, float width, float height, float radius, float softness, float opacity, Color c1, Color c2, Color c3, Color c4, boolean inner) {
        GlStateManager.resetColor();
        GL11.glDisable(3008);
        GL11.glShadeModel(7425);
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL20.glUseProgram(shadowGradientShader.getProgram());
        float g = width / 5.05f;
        float h = height / 5.05f;
        GL20.glUniform2f(GL20.glGetUniformLocation(shadowGradientShader.getProgram(), "size"), width, height);
        GL20.glUniform1f(GL20.glGetUniformLocation(shadowGradientShader.getProgram(), "radius"), radius);
        GL20.glUniform1f(GL20.glGetUniformLocation(shadowGradientShader.getProgram(), "shadowSoftness"), softness);
        GL20.glUniform1f(GL20.glGetUniformLocation(shadowGradientShader.getProgram(), "edgeSoftness"), 1.0f);
        GL20.glUniform1f(GL20.glGetUniformLocation(shadowGradientShader.getProgram(), "alpha"), opacity);
        GL20.glUniform4f(GL20.glGetUniformLocation(shadowGradientShader.getProgram(), "color1"), (c1.getRed() / 255.0f), (c1.getGreen() / 255.0f), (c1.getBlue() / 255.0f), 1.0f);
        GL20.glUniform4f(GL20.glGetUniformLocation(shadowGradientShader.getProgram(), "color2"), (c2.getRed() / 255.0f), (c2.getGreen() / 255.0f), (c2.getBlue() / 255.0f), 1.0f);
        GL20.glUniform4f(GL20.glGetUniformLocation(shadowGradientShader.getProgram(), "color3"), (c3.getRed() / 255.0f), (c3.getGreen() / 255.0f), (c3.getBlue() / 255.0f), 1.0f);
        GL20.glUniform4f(GL20.glGetUniformLocation(shadowGradientShader.getProgram(), "color4"), (c4.getRed() / 255.0f), (c4.getGreen() / 255.0f), (c4.getBlue() / 255.0f), 1.0f);
        GL20.glUniform1i(GL20.glGetUniformLocation(shadowGradientShader.getProgram(), "inner"), (inner ? 1 : 0));
        if (inner) {
            RoundedUtils.rect(x, y, width, height);
        } else {
            GL11.glBegin(7);
            GL11.glTexCoord2f(-0.2f, -0.2f);
            GL11.glVertex2f((x - g), (y - h));
            GL11.glTexCoord2f(-0.2f, 1.2f);
            GL11.glVertex2f((x - g), (y + height + h));
            GL11.glTexCoord2f(1.2f, 1.2f);
            GL11.glVertex2f((x + width + g), (y + height + h));
            GL11.glTexCoord2f(1.2f, -0.2f);
            GL11.glVertex2f((x + width + g), (y - h));
            GL11.glEnd();
        }
        GL20.glUseProgram(0);
        GL11.glEnable(3553);
        GL11.glEnable(3008);
        GL11.glShadeModel(7424);
        GlStateManager.resetColor();
    }

    /**
     * Draws a rectangle
     *
     * @param x      x position
     * @param y      y position
     * @param width  width
     * @param height height
     */
    public static void rect(float x, float y, float width, float height) {
        GL11.glBegin(7);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex2f(x, (y + height));
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex2f((x + width), (y + height));
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex2f((x + width), y);
        GL11.glEnd();
    }

    /**
     * Draws a rounded outline
     *
     * @param x         x position
     * @param y         y position
     * @param x1        x1 position
     * @param y1        y1 position
     * @param radius    radius
     * @param lineWidth line width
     * @param color     color
     */
    public static void drawRoundedOutline(float x, float y, float x1, float y1, float radius, float lineWidth, int color) {
        GL11.glPushAttrib(0);
        GL11.glScaled(0.5D, 0.5D, 0.5D);
        x *= 2.0F;
        y *= 2.0F;
        x1 *= 2.0F;
        y1 *= 2.0F;
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glLineWidth(lineWidth);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        ColorUtil.glColor(color);
        for (int i = 0; i <= 90; i += 3) {
            GL11.glVertex2d(x + radius + Math.sin(i * 3.141592653589793 / 180.0) * (radius * -1.0), y + radius + Math.cos(i * 3.141592653589793 / 180.0) * (radius * -1.0));
        }
        for (int i = 90; i <= 180; i += 3) {
            GL11.glVertex2d(x + radius + Math.sin(i * 3.141592653589793 / 180.0) * (radius * -1.0), y1 - radius + Math.cos(i * 3.141592653589793 / 180.0) * (radius * -1.0));
        }
        for (int i = 0; i <= 90; i += 3) {
            GL11.glVertex2d(x1 - radius + Math.sin(i * 3.141592653589793 / 180.0) * radius, y1 - radius + Math.cos(i * 3.141592653589793 / 180.0) * radius);
        }
        for (int i = 90; i <= 180; i += 3) {
            GL11.glVertex2d(x1 - radius + Math.sin(i * 3.141592653589793 / 180.0) * radius, y + radius + Math.cos(i * 3.141592653589793 / 180.0) * radius);
        }
        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glScaled(2.0D, 2.0D, 2.0D);
        GL11.glPopAttrib();
    }

}
