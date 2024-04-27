package dev.revere.virago.util.render;

import dev.revere.virago.Virago;
import dev.revere.virago.client.modules.render.HUD;
import dev.revere.virago.client.services.ModuleService;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import net.optifine.util.MathUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * @author Remi
 * @project Virago
 * @date 3/19/2024
 */
public class ColorUtil {

    /**
     * Sets the color
     *
     * @param color the color
     */
    public static void glColor(int color) {
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        int a = color >> 24 & 0xFF;
        GL11.glColor4f((float)r / 255.0f, (float)g / 255.0f, (float)b / 255.0f, (float)a / 255.0f);
    }

    public static int reAlpha(int color, float alpha) {
        try {
            Color c = new Color(color);
            float r = 0.003921569f * (float)c.getRed();
            float g = 0.003921569f * (float)c.getGreen();
            float b = 0.003921569f * (float)c.getBlue();
            return new Color(r, g, b, alpha).getRGB();
        }
        catch (Throwable e) {
            return color;
        }
    }

    /**
     * Sets the color
     *
     * @param red   the red
     * @param green the green
     * @param blue  the blue
     * @param alpha the alpha
     */
    public static void color(final double red, final double green, final double blue, final double alpha) {
        GL11.glColor4d(red, green, blue, alpha);
    }

    /**
     * Sets the color
     *
     * @param color the color
     */
    public static void color(Color color) {
        if (color == null)
            color = Color.white;
        color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
    }


    public static void color(int color) {
        float[] rgba = convertRGB(color);
        GL11.glColor4f(rgba[0], rgba[1], rgba[2], rgba[3]);
    }


    public static void color(Color color, float alpha) {
        GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha / 255f);
    }

    public static Color withAlpha(final Color color, final int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) MathUtils.clamp(0, 255, alpha));
    }

    public static Color toColorRGB(int rgb, float alpha) {
        float[] rgba = convertRGB(rgb);
        return new Color(rgba[0], rgba[1], rgba[2], alpha / 255f);
    }

    public static float[] convertRGB(int rgb) {
        float a = (rgb >> 24 & 0xFF) / 255.0f;
        float r = (rgb >> 16 & 0xFF) / 255.0f;
        float g = (rgb >> 8 & 0xFF) / 255.0f;
        float b = (rgb & 0xFF) / 255.0f;
        return new float[]{r, g, b, a};
    }

    /**
     * Fade color
     *
     * @param color the color
     * @param index the index
     * @param count the count
     * @return the color
     */
    public static Color fade(Color color, int index, int count) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float speed = (float) index;
        float value = ((System.nanoTime() / 1000000 - (count) * 12) % speed) / speed;
        value *= 2;
        value -= 1;
        float function = -1 * (value * value) + 1;
        if (function < 0.4)
            function = 0.4F;
        int c = Color.HSBtoRGB(hsb[0], hsb[1], function);
        color = new Color(c);
        return new Color(c);
    }

    /**
     * Fade between colors
     *
     * @param color1 the color 1
     * @param color2 the color 2
     * @param offset the offset
     * @return the color
     */
    public static int fadeBetween(int color1, int color2, float offset) {
        if (offset > 1) {
            offset = 1 - offset % 1;
        }

        double invert = 1 - offset;
        int r = (int) ((color1 >> 16 & 0xFF) * invert + (color2 >> 16 & 0xFF) * offset);
        int g = (int) ((color1 >> 8 & 0xFF) * invert + (color2 >> 8 & 0xFF) * offset);
        int b = (int) ((color1 & 0xFF) * invert + (color2 & 0xFF) * offset);
        int a = (int) ((color1 >> 24 & 0xFF) * invert + (color2 >> 24 & 0xFF) * offset);
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    /**
     * Integrates alpha
     *
     * @param colour the colour
     * @param alpha  the alpha
     * @return the color
     */
    public static Color integrateAlpha(Color colour, float alpha) {
        return new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), MathHelper.clamp_int((int) alpha, 0, 255));
    }

    /**
     * Fade between colors
     *
     * @param speed the speed
     * @param index the index
     * @param start the start
     * @param end   the end
     * @return the color
     */
    public static Color fadeBetween(int speed, int index, Color start, Color end) {
        int tick = (int) (((System.currentTimeMillis()) / speed + index) % 360);
        tick = (tick >= 180 ? 360 - tick : tick) * 2;
        return ColorUtil.interpolate(start, end, tick / 360f);
    }

    /**
     * Fade between colors
     *
     * @param speed the speed
     * @param index the index
     * @param start the start
     * @param end   the end
     * @return the color
     */
    public static Color fadeBetween(int speed, int index, int totalModules, Color start, Color end) {
        int horizontalIndex = index * (360 / totalModules);
        int tick = (int) (((System.currentTimeMillis()) / speed + horizontalIndex) % 360);
        tick = (tick >= 180 ? 360 - tick : tick);
        return ColorUtil.interpolate(start, end, tick / 360f);
    }

    /**
     * Interpolates between two colors
     *
     * @param color1 First color
     * @param color2 Second color
     * @param ratio  Interpolation ratio (0 to 1)
     * @return Interpolated color
     */
    public static int interpolateColor(int color1, int color2, float ratio) {
        int red = (int) ((ColorUtil.red(color2) - ColorUtil.red(color1)) * ratio) + ColorUtil.red(color1);
        int green = (int) ((ColorUtil.green(color2) - ColorUtil.green(color1)) * ratio) + ColorUtil.green(color1);
        int blue = (int) ((ColorUtil.blue(color2) - ColorUtil.blue(color1)) * ratio) + ColorUtil.blue(color1);
        return new Color(red, green, blue).getRGB();
    }

    /**
     * Interpolates between two colors
     *
     * @param from  The first color
     * @param to    The second color
     * @param value The value
     * @return The interpolated color
     */
    public static Color interpolate(Color from, Color to, double value) {
        double progress = value > 1 ? 1 : (value < 0 ? 0 : value);
        int redDiff = to.getRed() - from.getRed();
        int greenDiff = to.getGreen() - from.getGreen();
        int blueDiff = to.getBlue() - from.getBlue();
        int alphaDiff = to.getAlpha() - from.getAlpha();
        int newRed = (int) (from.getRed() + (redDiff * progress));
        int newGreen = (int) (from.getGreen() + (greenDiff * progress));
        int newBlue = (int) (from.getBlue() + (blueDiff * progress));
        int newAlpha = (int) (from.getAlpha() + (alphaDiff * progress));
        return new Color(newRed, newGreen, newBlue, newAlpha);
    }

    /**
     * Gets the red component of a color
     *
     * @param color The color
     * @return The red component
     */
    private static int red(int color) {
        return (color >> 16) & 0xFF;
    }

    /**
     * Gets the green component of a color
     *
     * @param color The color
     * @return The green component
     */
    private static int green(int color) {
        return (color >> 8) & 0xFF;
    }

    /**
     * Gets the blue component of a color
     *
     * @param color The color
     * @return The blue component
     */
    private static int blue(int color) {
        return color & 0xFF;
    }

    public static int getColor(boolean animate) {
        HUD hud = Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(HUD.class);
        switch (hud.colorMode.getValue()) {
            case CUSTOM:
                if (animate) {
                    return ColorUtil.fadeBetween(hud.fadeSpeed.getValue().intValue(), hud.y * hud.colorSpacing.getValue().intValue(), hud.customColor1.getValue(), hud.customColor2.getValue()).getRGB();
                } else {
                    return hud.customColor1.getValue().getRGB();
                }
            case STATIC:
                return hud.customColor1.getValue().getRGB();
            case CLIENT:
                if (animate) {
                    return ColorUtil.fadeBetween(hud.fadeSpeed.getValue().intValue(), hud.y * hud.colorSpacing.getValue().intValue(), new Color(255, 11, 82), new Color(-1)).getRGB();
                } else {
                    return new Color(255, 11, 82).getRGB();
                }
            case RAINBOW_PULSE:
                if (animate) {
                    return ColorUtil.fadeBetween(hud.fadeSpeed.getValue().intValue(), hud.y * hud.colorSpacing.getValue().intValue(), new Color(rainbow(1000)), Color.WHITE).getRGB();
                } else {
                    return hud.customColor1.getValue().getRGB();
                }
            case RAINBOW:
                return animate ? ColorUtil.rainbow(hud.rainbowSpeed.getValue().intValue() * hud.y) : ColorUtil.rainbow(hud.rainbowSpeed.getValue().intValue());
        }
        return -1;
    }

    public static int rainbow(int delay) {
        double rainbowState = Math.ceil((System.currentTimeMillis() + delay / 2) / 10.0);
        rainbowState %= 360;
        return Color.getHSBColor((float) (rainbowState / 360.0f), 0.5f, 1f).getRGB();
    }

    // thanks tena <3
    public static Color interpolateColorsBackAndForth(int speed, int index, Color start, Color end, boolean trueColor) {
        int angle = (int)((System.currentTimeMillis() / (long)speed + (long)index) % 360L);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        return trueColor ? interpolateColorHue(start, end, (float)angle / 360.0f) : interpolateColorC(start, end, (float)angle / 360.0f);
    }

    public static Color interpolateColorC(Color color1, Color color2, float amount) {
        amount = Math.min(1.0f, Math.max(0.0f, amount));
        return new Color(interpolateInt(color1.getRed(), color2.getRed(), amount), interpolateInt(color1.getGreen(), color2.getGreen(), amount), interpolateInt(color1.getBlue(), color2.getBlue(), amount), interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static Color interpolateColorHue(Color color1, Color color2, float amount) {
        amount = Math.min(1.0f, Math.max(0.0f, amount));
        float[] color1HSB = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
        float[] color2HSB = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);
        Color resultColor = Color.getHSBColor(interpolateFloat(color1HSB[0], color2HSB[0], amount), interpolateFloat(color1HSB[1], color2HSB[1], amount), interpolateFloat(color1HSB[2], color2HSB[2], amount));
        return new Color(resultColor.getRed(), resultColor.getGreen(), resultColor.getBlue(), interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static Double interpolate(double oldValue, double newValue, double interpolationValue) {
        return oldValue + (newValue - oldValue) * interpolationValue;
    }

    public static float interpolateFloat(float oldValue, float newValue, double interpolationValue) {
        return interpolate(oldValue, newValue, (float)interpolationValue).floatValue();
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return interpolate(oldValue, newValue, (float)interpolationValue).intValue();
    }
}
