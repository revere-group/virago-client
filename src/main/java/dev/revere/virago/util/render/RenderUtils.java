package dev.revere.virago.util.render;

import dev.revere.virago.Virago;
import dev.revere.virago.client.modules.render.HUD;
import dev.revere.virago.client.services.ModuleService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * @author Remi
 * @project Virago
 * @date 3/19/2024
 */
public class RenderUtils {
    public static Minecraft mc = Minecraft.getMinecraft();
    private static final Frustum frustum = new Frustum();

    /**
     * Gets the client color
     *
     * @param index index
     * @return color
     */
    public static int getColor(int index) {
        HUD hud = Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(HUD.class);
        switch (hud.colorMode.getValue()) {
            case CUSTOM:
                return ColorUtil.interpolateColorsBackAndForth(hud.fadeSpeed.getValue().intValue(), index, hud.customColor1.getValue(), hud.customColor2.getValue(), false).getRGB();
            case STATIC:
                return hud.customColor1.getValue().getRGB();
            case CLIENT:
                return ColorUtil.interpolateColorsBackAndForth(hud.fadeSpeed.getValue().intValue(), index, new Color(255, 11, 82), new Color(-1), false).getRGB();
            case RAINBOW:
                return ColorUtil.rainbow(hud.rainbowSpeed.getValue().intValue() * hud.y);
            case RAINBOW_PULSE:
                return ColorUtil.interpolateColorsBackAndForth(hud.fadeSpeed.getValue().intValue(), index, hud.customColor1.getValue(), new Color(ColorUtil.rainbow(1000)), false).getRGB();
        }
        return -1;
    }

    /**
     * Draws a gradient rect
     *
     * @param left       left
     * @param top        top
     * @param right      right
     * @param bottom     bottom
     * @param time       time
     * @param difference difference
     * @param delay      delay
     * @param direction  direction
     */
    public static void renderGradientRect(int left, int top, int right, int bottom, double time, long difference, long delay, Direction direction) {
        int i;
        if (direction == Direction.RIGHT) {
            for (i = 0; i < right - left; ++i) {
                Gui.drawRect(left + i, top, right, bottom, getColor(i));
            }
        }
        if (direction == Direction.LEFT) {
            for (i = 0; i < right - left; ++i) {
                Gui.drawRect(left + i, top, right, bottom, getColor(i));
            }
        }
        if (direction == null) {
            for (i = 0; i < bottom - top; ++i) {
                Gui.drawRect(left, top + i, right, bottom, getColor(i));
            }
        }
        if (direction == Direction.UP) {
            for (i = 0; i < bottom - top; ++i) {
                Gui.drawRect(left, top + i, right, bottom, getColor(i));
            }
        }
    }

    /**
     * Draws a hollow rectangle with a defined width
     *
     * @param x     x position
     * @param y     y position
     * @param w     width
     * @param h     height
     * @param width width of the rectangle
     * @param color color
     */
    public static void drawHollowRectDefineWidth(float x, float y, float w, float h, float width, int color) {
        Gui.drawHorizontalLineDefineWidth(x, w, y, width, color);
        Gui.drawHorizontalLineDefineWidth(x, w, h, width, color);

        Gui.drawVerticalLineDefineWidth(x, h, y, width, color);
        Gui.drawVerticalLineDefineWidth(w, h, y, width, color);
    }

    /**
     * Scales the screen
     *
     * @param x     x position
     * @param y     y position
     * @param scale scale
     */
    public static void scale(float x, float y, float[] scale) {
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(scale[0], scale[1], 1);
        GlStateManager.translate(-x, -y, 0);
    }

    /**
     * Checks if an entity is in the view frustrum
     *
     * @param entity the entity
     * @return if the entity is in the view frustrum
     */
    public static boolean isInViewFrustum(Entity entity) {
        return isInViewFrustum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck;
    }

    /**
     * Checks if a bounding box is in the view frustrum
     *
     * @param bb the bounding box
     * @return if the bounding box is in the view frustrum
     */
    private static boolean isInViewFrustum(AxisAlignedBB bb) {
        Entity current = Minecraft.getMinecraft().getRenderViewEntity();
        frustum.setPosition(current.posX, current.posY, current.posZ);
        return frustum.isBoundingBoxInFrustum(bb);
    }

    /**
     * Prepares the 3D rendering
     */
    public static void pre3D() {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
    }

    /**
     * Post 3D rendering
     */
    public static void post3D() {
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
        GL11.glColor4f(1, 1, 1, 1);
    }

    /**
     * Sets the alpha limit
     *
     * @param limit the limit
     */
    public static void setAlphaLimit(float limit) {
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, (float) (limit * .01));
    }

    /**
     * Projects a 3D vector to a 2D vector
     *
     * @param x x position
     * @param y y position
     * @param z z position
     * @return 2D vector
     */
    public static double[] project2D(final double x, final double y, final double z) {
        FloatBuffer objectPosition = ActiveRenderInfo.objectCoords();
        ScaledResolution sc = new ScaledResolution(mc);
        if (GLU.gluProject((float)x, (float)y, (float)z, ActiveRenderInfo.modelview(), ActiveRenderInfo.projection(), ActiveRenderInfo.viewport(), objectPosition))
            return new double[]{ objectPosition.get(0) / sc.getScaleFactor(), objectPosition.get(1) / sc.getScaleFactor(),
                    objectPosition.get(2) };
        return null;
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
    public static void drawRoundedRect(double x, double y, final double width, final double height, final double radius, final int color) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        double x2 = x + width;
        double y2 = y + height;
        final float f = (color >> 24 & 0xFF) / 255.0f;
        final float f2 = (color >> 16 & 0xFF) / 255.0f;
        final float f3 = (color >> 8 & 0xFF) / 255.0f;
        final float f4 = (color & 0xFF) / 255.0f;
        GL11.glPushAttrib(0);
        GL11.glScaled(0.5, 0.5, 0.5);
        x *= 2.0;
        y *= 2.0;
        x2 *= 2.0;
        y2 *= 2.0;
        GL11.glDisable(3553);
        GL11.glColor4f(f2, f3, f4, f);
        GL11.glEnable(2848);
        GL11.glBegin(9);
        for (int i = 0; i <= 90; i += 3) {
            GL11.glVertex2d(x + radius + Math.sin(i * 3.141592653589793 / 180.0) * (radius * -1.0), y + radius + Math.cos(i * 3.141592653589793 / 180.0) * (radius * -1.0));
        }
        for (int i = 90; i <= 180; i += 3) {
            GL11.glVertex2d(x + radius + Math.sin(i * 3.141592653589793 / 180.0) * (radius * -1.0), y2 - radius + Math.cos(i * 3.141592653589793 / 180.0) * (radius * -1.0));
        }
        for (int i = 0; i <= 90; i += 3) {
            GL11.glVertex2d(x2 - radius + Math.sin(i * 3.141592653589793 / 180.0) * radius, y2 - radius + Math.cos(i * 3.141592653589793 / 180.0) * radius);
        }
        for (int i = 90; i <= 180; i += 3) {
            GL11.glVertex2d(x2 - radius + Math.sin(i * 3.141592653589793 / 180.0) * radius, y + radius + Math.cos(i * 3.141592653589793 / 180.0) * radius);
        }
        GL11.glEnd();
        GL11.glEnable(3553);
        GL11.glDisable(2848);
        GL11.glEnable(3553);
        GL11.glScaled(2.0, 2.0, 2.0);
        GL11.glPopAttrib();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    /**
     * Draws a rectangle
     *
     * @param x      x position
     * @param y      y position
     * @param width  width
     * @param height height
     * @param color  color
     */
    public static void rect(float x, float y, float width, float height, Color color) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        ColorUtil.glColor(color.getRGB());
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x, y + height);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x + width, y);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static void drawRect(float left, float top, float right, float bottom, int color) {
        float e;
        if (left < right) {
            e = left;
            left = right;
            right = e;
        }
        if (top < bottom) {
            e = top;
            top = bottom;
            bottom = e;
        }
        float a = (float)(color >> 24 & 0xFF) / 255.0f;
        float b = (float)(color >> 16 & 0xFF) / 255.0f;
        float c = (float)(color >> 8 & 0xFF) / 255.0f;
        float d = (float)(color & 0xFF) / 255.0f;
        WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color((float)b, (float)c, (float)d, (float)a);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(left, bottom, 0.0).endVertex();
        worldRenderer.pos(right, bottom, 0.0).endVertex();
        worldRenderer.pos(right, top, 0.0).endVertex();
        worldRenderer.pos(left, top, 0.0).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }

    /**
     * Draws an image
     *
     * @param image  image
     * @param x      x position
     * @param y      y position
     * @param width  width
     * @param height height
     */
    public static void drawImage(ResourceLocation image, float x, float y, float width, float height) {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        mc.getTextureManager().bindTexture(image);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        Gui.drawModalRectWithCustomSizedTexture((int) x, (int) y, 0.0f, 0.0f, (int) width, (int) height, width, height);
        GlStateManager.color(1, 1, 1, 1);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glPopMatrix();
    }

    /**
     * Checks if the mouse is hovered over a rectangle
     *
     * @param x      x position
     * @param y      y position
     * @param width  width
     * @param height height
     * @param mouseX mouse x position
     * @param mouseY mouse y position
     */
    public static boolean isHovered(float x, float y, float width, float height, int mouseX, int mouseY) {
        return (mouseX >= x && mouseX <= (x + width) && mouseY >= y && mouseY <= (y + height));
    }

    /**
     * Starts scissoring a rect
     *
     * @param x      X coord
     * @param y      Y coord
     * @param width  Width of scissor
     * @param height Height of scissor
     */
    public static void pushScissor(double x, double y, double width, double height) {
        width = MathHelper.clamp_double(width, 0, width);
        height = MathHelper.clamp_double(height, 0, height);

        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT);
        {
            scissorRect(x, y, width, height);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
        }
    }

    /**
     * Scissors a rect
     *
     * @param x      X coord
     * @param y      Y coord
     * @param width  Width of scissor
     * @param height Height of scissor
     */
    public static void scissorRect(double x, double y, double width, double height) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        final double scale = sr.getScaleFactor();

        y = sr.getScaledHeight() - y;

        x *= scale;
        y *= scale;
        width *= scale;
        height *= scale;

        GL11.glScissor((int) x, (int) (y - height), (int) width, (int) height);
    }

    /**
     * Disables scissor
     */
    public static void popScissor() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopAttrib();
    }

    /**
     * Draws a vertical gradient
     *
     * @param x      x position
     * @param y      y position
     * @param width  width
     * @param height height
     * @param top    top color
     * @param bottom bottom color
     */
    public static void drawVerticalGradient(float x, float y, float width, float height, Color top, Color bottom) {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBegin(GL11.GL_QUADS);
        ColorUtil.glColor(top.getRGB());
        GL11.glVertex2f(x, y);
        ColorUtil.glColor(bottom.getRGB());
        GL11.glVertex2f(x, y + height);
        GL11.glVertex2f(x + width, y + height);
        ColorUtil.glColor(top.getRGB());
        GL11.glVertex2f(x + width, y);
        GL11.glEnd();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    /**
     * Converts a 3D vector to a 2D vector
     *
     * @param x x position
     * @param y y position
     * @param z z position
     * @return 2D vector
     */
    public static Vec3 to2D(double x, double y, double z) {
        FloatBuffer screenCoords = BufferUtils.createFloatBuffer(3);
        IntBuffer viewport = BufferUtils.createIntBuffer(16);
        FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
        FloatBuffer projection = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelView);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection);
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);
        boolean result = GLU.gluProject((float) x, (float) y, (float) z, modelView, projection, viewport, screenCoords);
        if (!result) {
            return null;
        }
        return new Vec3(screenCoords.get(0), screenCoords.get(1), screenCoords.get(2));
    }

    /**
     * Draws a vertical gradient rect
     *
     * @param x      x position
     * @param y      y position
     * @param width  width
     * @param height height
     * @param top    top color
     * @param bottom bottom color
     */
    public static void verticalGradient(float x, float y, float width, float height, Color top, Color bottom) {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBegin(GL11.GL_QUADS);
        ColorUtil.glColor(top.getRGB());
        GL11.glVertex2f(x, y);
        ColorUtil.glColor(bottom.getRGB());
        GL11.glVertex2f(x, y + height);
        GL11.glVertex2f(x + width, y + height);
        ColorUtil.glColor(top.getRGB());
        GL11.glVertex2f(x + width, y);
        GL11.glEnd();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    /**
     * Draws a tracer pointer
     *
     * @param x         x position
     * @param y         y position
     * @param size      size
     * @param widthDiv  width division
     * @param heightDiv height division
     * @param color     color
     */
    public static void drawTracerPointer(float x, float y, float size, float widthDiv, float heightDiv, int color) {
        boolean blend = GL11.glIsEnabled(3042);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glPushMatrix();
        ColorUtil.glColor(color);
        GL11.glBegin(7);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d((x - size / widthDiv), (y + size));
        GL11.glVertex2d(x, (y + size / heightDiv));
        GL11.glVertex2d((x + size / widthDiv), (y + size));
        GL11.glVertex2d(x, y);
        GL11.glEnd();
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.8f);
        GL11.glBegin(2);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d((x - size / widthDiv), (y + size));
        GL11.glVertex2d(x, (y + size / heightDiv));
        GL11.glVertex2d((x + size / widthDiv), (y + size));
        GL11.glVertex2d(x, y);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        if (!blend) {
            GL11.glDisable(3042);
        }
        GL11.glDisable(2848);
    }

    /**
     * Creates a frame buffer
     *
     * @param framebuffer frame buffer
     * @return frame buffer
     */
    public static Framebuffer createFrameBuffer(Framebuffer framebuffer) {
        return createFrameBuffer(framebuffer, false);
    }

    /**
     * Creates a frame buffer
     *
     * @param framebuffer frame buffer
     * @param depth       depth
     * @return frame buffer
     */
    public static Framebuffer createFrameBuffer(Framebuffer framebuffer, boolean depth) {
        if (needsNewFramebuffer(framebuffer)) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new Framebuffer(mc.displayWidth, mc.displayHeight, depth);
        }
        return framebuffer;
    }

    /**
     * Checks if a new frame buffer is needed
     *
     * @param framebuffer frame buffer
     * @return if a new frame buffer is needed
     */
    public static boolean needsNewFramebuffer(Framebuffer framebuffer) {
        return framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight;
    }

    public enum Direction {
        LEFT,
        UP,
        RIGHT,
        DOWN;
    }
}
