package dev.revere.virago.client.modules.render;

import dev.revere.virago.Virago;
import dev.revere.virago.api.draggable.Draggable;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.font.FontRenderer;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.render.Render2DEvent;
import dev.revere.virago.client.events.render.ShaderEvent;
import dev.revere.virago.client.services.DraggableService;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.util.render.ColorUtil;
import dev.revere.virago.util.render.RenderUtils;
import dev.revere.virago.util.render.RoundedUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Remi
 * @project Virago
 * @date 3/17/2024
 */

@ModuleData(name = "HUD", displayName = "HUD", type = EnumModuleType.RENDER, description = "Displays information on your screen", isHidden = true)
public class HUD extends AbstractModule {

    private final Draggable watermarkDraggable = Virago.getInstance().getServiceManager().getService(DraggableService.class).addDraggable(new Draggable(this, "Watermark", 3, 3));
    private final Draggable fpsDraggable = Virago.getInstance().getServiceManager().getService(DraggableService.class).addDraggable(new Draggable(this, "FPS", 6, 20));
    private final Draggable bpsDraggable = Virago.getInstance().getServiceManager().getService(DraggableService.class).addDraggable(new Draggable(this, "BPS", 20, 40));

    public Setting<Boolean> arraylist = new Setting<>("ArrayList", true).describedBy("Should the arraylist be rendered?");
    public Setting<Boolean> lowercase = new Setting<>("Lowercase", true)
            .visibleWhen(arraylist::getValue)
            .describedBy("Make the text forced lowercase");
    public Setting<Boolean> watermark = new Setting<>("Watermark", true).describedBy("Should the watermark be rendered?");
    public Setting<Boolean> background = new Setting<>("Background", true).describedBy("Should the background be rendered?");
    public Setting<Boolean> fps = new Setting<>("FPS", false).describedBy("Should the fps be rendered?");
    public Setting<Boolean> bps = new Setting<>("BPS", false).describedBy("Should the bps be rendered?");

    public Setting<WatermarkMode> watermarkMode = new Setting<>("Watermark Type", WatermarkMode.CSGO).describedBy("The watermark mode to use for the HUD");
    public Setting<ColorMode> colorMode = new Setting<>("Color", ColorMode.CLIENT).describedBy("The color mode to use for the HUD");
    public Setting<FontType> fontType = new Setting<>("Font", FontType.SF_PRO).describedBy("The font type to use for the HUD");
    public Setting<OutlineMode> arrayListOutline = new Setting<>("Outline", OutlineMode.TOP_RIGHT)
            .visibleWhen(arraylist::getValue)
            .describedBy("How to outline the arraylist");

    public static Setting<MetaData> arrayListMetaData = new Setting<>("Metadata", MetaData.SIMPLE)
            .describedBy("How to draw the module metadata");

    public Setting<Color> customColor1 = new Setting<>("First", new Color(0xE70BFF))
            .visibleWhen(() -> colorMode.getValue() == ColorMode.CUSTOM || colorMode.getValue() == ColorMode.STATIC)
            .describedBy("Custom color either for static or fade accent");

    public Setting<Color> customColor2 = new Setting<>("Second", new Color(0))
            .visibleWhen(() -> colorMode.getValue() == ColorMode.CUSTOM)
            .describedBy("Custom color either for static or fade accent");

    private final Setting<Integer> opacity = new Setting<>("Opacity", 100)
            .minimum(0)
            .maximum(255)
            .incrementation(1)
            .describedBy("The opacity for background")
            .visibleWhen(() -> background.getValue());

    public Setting<Float> rainbowSpeed = new Setting<>("Rainbow Speed", 12F)
            .minimum(1F)
            .maximum(20F)
            .incrementation(1F)
            .visibleWhen(() -> colorMode.getValue() == ColorMode.RAINBOW)
            .describedBy("The speed at which the rainbow occurs");

    public Setting<Float> fadeSpeed = new Setting<>("Fade Speed", 10.0F)
            .minimum(1F)
            .maximum(20F)
            .incrementation(0.1F)
            .visibleWhen(() -> colorMode.getValue() == ColorMode.CUSTOM)
            .describedBy("The speed at which the fade occurs");

    public Setting<Float> colorSpacing = new Setting<>("Color Spacing", 4.0F)
            .minimum(1F)
            .maximum(10F)
            .incrementation(0.1F)
            .visibleWhen(() -> colorMode.getValue() == ColorMode.CUSTOM)
            .describedBy("The spacing between the colors");

    public Setting<Float> elementHeight = new Setting<>("Height", 13f)
            .minimum(11f)
            .maximum(15f)
            .incrementation(1f)
            .visibleWhen(arraylist::getValue)
            .describedBy("The height of each element");


    public FontRenderer fontRenderer;

    public int y;

    public HUD() {
        setEnabled(true);
    }

    /**
     * Renders the 2D screen
     *
     * @param event The event to render the 2D screen for
     */
    @EventHandler
    private final Listener<Render2DEvent> render2DEventListener = event -> {
        FontService font = Virago.getInstance().getServiceManager().getService(FontService.class);
        ScaledResolution sr = new ScaledResolution(mc);
        getFont(font);

        if (watermark.getValue())
            renderWatermark(false);

        if (fps.getValue())
            renderFPS(false);

        if (bps.getValue())
            renderBPS(false);

        List<AbstractModule> modules = getSortedModules();
        if (arraylist.getValue()) renderModules(sr, modules, false);
    };

    @EventHandler
    private final Listener<ShaderEvent> shaderEventListener = event -> {
        FontService font = Virago.getInstance().getServiceManager().getService(FontService.class);
        ScaledResolution sr = new ScaledResolution(mc);
        getFont(font);

        if (watermark.getValue())
            renderWatermark(true);

        if (fps.getValue())
            renderFPS(true);

        if (bps.getValue())
            renderBPS(true);

        List<AbstractModule> modules = getSortedModules();
        if (arraylist.getValue()) renderModules(sr, modules, true);
    };

    private void renderFPS(boolean shader) {
        String fps = Minecraft.getDebugFPS() + " FPS";
        int width = fontRenderer.getStringWidth(fps) + 4;
        int height = fontRenderer.getHeight() + 4;

        if (!shader) {
            RenderUtils.rect(fpsDraggable.getX(), fpsDraggable.getY() - 1, width, height, new Color(0, 0, 0, 120));
            RoundedUtils.shadowGradient(fpsDraggable.getX(), fpsDraggable.getY() - 1, width, height, 1, 5, 5, new Color(0, 0, 0, 100), new Color(0, 0, 0, 100), new Color(0, 0, 0, 100), new Color(0, 0, 0, 100), false);
            RenderUtils.renderGradientRect((int) fpsDraggable.getX(), (int) fpsDraggable.getY() - 1, (int) (width + fpsDraggable.getX()), (int) (fpsDraggable.getY()), 5.0, 2000L, 2L, RenderUtils.Direction.RIGHT);
        }

        fontRenderer.drawString(fps, fpsDraggable.getX() + 2, fpsDraggable.getY() + 2, 0xFFFFFFFF);
        fpsDraggable.setWidth(width);
        fpsDraggable.setHeight(height);
    }

    private void renderBPS(boolean shader) {
        String bps = getSpeed() + " BPS";
        int width = fontRenderer.getStringWidth(bps) + 4;
        int height = fontRenderer.getHeight() + 4;

        if (!shader) {
            RenderUtils.rect(bpsDraggable.getX(), bpsDraggable.getY() - 1, width, height, new Color(0, 0, 0, 120));
            RoundedUtils.shadowGradient(bpsDraggable.getX(), bpsDraggable.getY() - 1, width, height, 1, 5, 5, new Color(0, 0, 0, 100), new Color(0, 0, 0, 100), new Color(0, 0, 0, 100), new Color(0, 0, 0, 100), false);
            RenderUtils.renderGradientRect((int) bpsDraggable.getX(), (int) bpsDraggable.getY() - 1, (int) (width + bpsDraggable.getX()), (int) (bpsDraggable.getY()), 5.0, 2000L, 2L, RenderUtils.Direction.RIGHT);
        }

        fontRenderer.drawString(bps, bpsDraggable.getX() + 2, bpsDraggable.getY() + 2, 0xFFFFFFFF);
        bpsDraggable.setWidth(width);
        bpsDraggable.setHeight(height);
    }

    /**
     * Used to get the players speed
     */
    public double getSpeed() {
        double bps = (Math.hypot(mc.thePlayer.posX - mc.thePlayer.prevPosX, mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * mc.timer.timerSpeed) * 20;
        return Math.round(bps * 100.0) / 100.0;
    }

    private void renderWatermark(boolean shader) {
        String clientText = Virago.getInstance().getName().toLowerCase() + "\u00A7fclient v";
        String versionText = Virago.getInstance().getVersion() + " | ";
        String usernameText = Virago.getInstance().getViragoUser().getUsername() + "\u00A7f | ";
        String serverText = mc.getCurrentServerData() != null ? mc.getCurrentServerData().serverIP : "singleplayer";

        String finalText = clientText + versionText + usernameText + serverText;
        switch (watermarkMode.getValue()) {
            case TEXT:
                FontService font = Virago.getInstance().getServiceManager().getService(FontService.class);
                if (!shader) RoundedUtils.round(watermarkDraggable.getX(), watermarkDraggable.getY(), font.getRalewayExtraBold().getStringWidth(finalText.toUpperCase()) + 5, watermarkDraggable.getHeight(), 4, new Color(10, 10, 10));
                font.getRalewayExtraBold().drawString(finalText.toUpperCase(), watermarkDraggable.getX() + 2, watermarkDraggable.getY() + 5, ColorUtil.getColor(false), false);
                watermarkDraggable.setWidth(font.getRalewayExtraBold().getStringWidth(finalText.toUpperCase()));
                watermarkDraggable.setHeight(font.getRalewayExtraBold().getHeight() + 7);
                break;
            case CSGO:
                if (!shader) {
                    RenderUtils.rect(watermarkDraggable.getX(), watermarkDraggable.getY(), fontRenderer.getStringWidth(finalText) + 2, fontRenderer.getHeight() + 6, new Color(0, 0, 0, 150));
                    RoundedUtils.shadowGradient(watermarkDraggable.getX(), watermarkDraggable.getY(), fontRenderer.getStringWidth(finalText) + 2, fontRenderer.getHeight() + 6, 1, 5, 5, new Color(0, 0, 0, 150), new Color(0, 0, 0, 150), new Color(0, 0, 0, 150), new Color(0, 0, 0, 150), false);
                    RenderUtils.renderGradientRect((int) watermarkDraggable.getX(), (int) watermarkDraggable.getY(), (int) (fontRenderer.getStringWidth(finalText) + 2 + watermarkDraggable.getX()), (int) (watermarkDraggable.getY() + 1), 5.0, 2000L, 2L, RenderUtils.Direction.RIGHT);
                }
                fontRenderer.drawStringWithShadow(finalText, watermarkDraggable.getX() + 1, watermarkDraggable.getY() + 4, ColorUtil.getColor(false));
                watermarkDraggable.setWidth(fontRenderer.getStringWidth(finalText));
                watermarkDraggable.setHeight(fontRenderer.getHeight() + 6);
                break;
            case LOGO:
                if (!shader) RenderUtils.drawImage(new ResourceLocation("virago/textures/logo/logo.png"), watermarkDraggable.getX(), watermarkDraggable.getY(), 50, 50);
                fontRenderer.drawString(Virago.getInstance().getName().toLowerCase(), watermarkDraggable.getX() + 11, watermarkDraggable.getY() + 50, -1);
                watermarkDraggable.setWidth(fontRenderer.getStringWidth(Virago.getInstance().getName().toLowerCase()));
                watermarkDraggable.setHeight(fontRenderer.getHeight() + 50);
                break;
        }
    }

    /**
     * Gets the font
     *
     * @param font The font service
     */
    private void getFont(FontService font) {
        switch (fontType.getValue()) {
            case PRODUCT_SANS:
                fontRenderer = font.getProductSans();
                break;
            case POPPINS:
                fontRenderer = font.getPoppinsMedium();
                break;
            case SF_PRO:
                fontRenderer = font.getSfProTextRegular();
                break;
            case JETBRAINS:
                fontRenderer = font.getJetbrainsMonoBold();
                break;
        }
    }


    /**
     * Gets the sorted modules
     *
     * @return The sorted modules
     */
    private List<AbstractModule> getSortedModules() {
        List<AbstractModule> modules = Virago.getInstance().getServiceManager().getService(ModuleService.class).getModuleList().stream().filter(mod -> !mod.isHidden()).collect(Collectors.toList());
        modules.sort((module1, module2) -> {
            String moduleData1 = generateModuleData(module1);
            String moduleData2 = generateModuleData(module2);
            int width1 = fontRenderer.getStringWidth(moduleData1);
            int width2 = fontRenderer.getStringWidth(moduleData2);
            return Float.compare(width1, width2);
        });
        Collections.reverse(modules);
        return modules;
    }

    /**
     * Renders the modules
     *
     * @param sr      The scaled resolution
     * @param modules The modules to render
     */
    private void renderModules(ScaledResolution sr, List<AbstractModule> modules, boolean shader) {
        y = 6;
        int index = 0;
        for (AbstractModule module : modules) {
            if (!module.isEnabled()) continue;
            renderModule(module, sr, y, index, shader);
            y += elementHeight.getValue();
            index++;
        }
    }

    /**
     * Renders the module
     *
     * @param module The module to render
     * @param sr     The scaled resolution
     * @param y      The y position
     */
    private void renderModule(AbstractModule module, ScaledResolution sr, int y, int index, boolean shader) {
        String moduleData = generateModuleData(module);
        int moduleWidth = fontRenderer.getStringWidth(moduleData);

        if (!shader) {
            if (background.getValue())
                Gui.drawRect(sr.getScaledWidth() - moduleWidth - 8, y, sr.getScaledWidth() - 4, y + elementHeight.getValue().intValue(), new Color(0, 0, 0, opacity.getValue()).getRGB());
            renderBar(module, sr, y, index);
        }
        int padding = 3;
        if (fontType.getValue() == FontType.POPPINS) padding -= 1;
        fontRenderer.drawString(moduleData, (sr.getScaledWidth() - 6) - moduleWidth, y + padding, ColorUtil.getColor(true));
    }

    /**
     * Renders the bar
     *
     * @param module The module to render the bar for
     * @param sr     The scaled resolution
     * @param y      The y position
     */
    private void renderBar(AbstractModule module, ScaledResolution sr, int y, int index) {
        String moduleData = generateModuleData(module);
        int moduleWidth = fontRenderer.getStringWidth(moduleData);
        int height = elementHeight.getValue().intValue();
        float x = sr.getScaledWidth() - moduleWidth - 8;
        int color = ColorUtil.getColor(true);

        switch (arrayListOutline.getValue()) {
            case TOP: {
                if (index == 0) {
                    RenderUtils.drawRect(sr.getScaledWidth() - 4f, y - 1, x, y, color);
                }
                break;
            }
            case RIGHT:
                RenderUtils.drawRect(x + moduleWidth + 4f, y, x + moduleWidth + 5f, y + height, color);
                break;
            case TOP_RIGHT:
                if (index == 0) {
                    RenderUtils.drawRect(sr.getScaledWidth() - 4f, y - 1, x, y, color);
                }
                RenderUtils.drawRect(x + moduleWidth + 4f, y - 1f, x + moduleWidth + 5f, y + height, color);
                break;
            case LEFT:
                RenderUtils.drawRect(x - 1f, y, x, y + height, color);
                break;
            case FULL:
                RenderUtils.drawRect(x + moduleWidth + 4f, y - 1f, x + moduleWidth + 5f, y + height - 1f, color);
                RenderUtils.drawRect(x - 1f, y, x, y + height, color);

                if (index == 0) {
                    RenderUtils.drawRect(sr.getScaledWidth() - 4f, y - 1, x - 1, y, color);
                } else if (index == getSortedModules().size() - 1f) {
                    RenderUtils.drawRect(sr.getScaledWidth() - 1f, y + height - 1f, x, y + height, color);
                }

                /*if (index < getSortedModules().size() - 1f) {
                    int nextModuleWidth = calculateNextModuleWidth(getSortedModules(), index);
                    int nextModuleX = sr.getScaledWidth() - nextModuleWidth - 1;

                    int startX = (int) Math.min(x, nextModuleX);

                    RenderUtils.drawRect(startX, y + height, nextModuleWidth + startX, y + height + 1, color);
                }*/
                break;
        }
    }

    private int calculateModuleWidth(String text) {
        return fontRenderer.getStringWidth(text);
    }

    private String generateModuleData(AbstractModule module) {
        StringBuilder text = new StringBuilder().append(module.getDisplayName());

        if (!(Objects.equals(module.getMetaData(), ""))) {
            switch (arrayListMetaData.getValue()) {
                case SIMPLE: {
                    text.append(" ")
                            .append(EnumChatFormatting.GRAY)
                            .append(module.getMetaData());

                    break;
                }
                case SQUARE: {
                    text.append(EnumChatFormatting.GRAY)
                            .append(" ")
                            .append(EnumChatFormatting.GRAY)
                            .append("[")
                            .append(EnumChatFormatting.WHITE)
                            .append(module.getMetaData())
                            .append(EnumChatFormatting.GRAY)
                            .append("]");

                    break;
                }

                case DASH: {
                    text.append(" ")
                            .append(EnumChatFormatting.GRAY)
                            .append("- ")
                            .append(module.getMetaData());

                    break;
                }
            }
        }

        String finalText = text.toString();

        if (lowercase.getValue()) {
            finalText = finalText.toLowerCase(Locale.getDefault());
        }

        return finalText;
    }

    /**
     * Draws a title string
     *
     * @param text  The text to draw
     * @param x     The x position
     * @param y     The y position
     * @param color The color
     */
    private void drawTitleString(String text, int x, int y, int color) {
        Virago.getInstance().getServiceManager().getService(FontService.class).getRalewayExtraBold26().drawStringWithShadow(text, x, y, color);
    }

    /**
     * Draws a string with shadow
     *
     * @param text  The text to draw
     * @param x     The x position
     * @param y     The y position
     * @param color The color
     */
    private void drawStringWithShadow(String text, int x, int y, int color) {
        fontRenderer.drawStringWithShadow(text, x, y, color);
    }

    /**
     * Draws a string
     *
     * @param text  The text to draw
     * @param x     The x position
     * @param y     The y position
     * @param color The color
     */
    private void drawString(String text, int x, int y, int color) {
        fontRenderer.drawString(text, x, y, color);
    }


    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public enum ColorMode {
        CLIENT,
        CUSTOM,
        RAINBOW,
        RAINBOW_PULSE,
        STATIC,
    }

    public enum FontType {
        PRODUCT_SANS,
        JETBRAINS,
        POPPINS,
        SF_PRO
    }

    public enum WatermarkMode {
        TEXT,
        CSGO,
        LOGO,
    }

    public enum OutlineMode {
        TOP, RIGHT, TOP_RIGHT, LEFT, FULL, NONE
    }

    public enum MetaData {
        SIMPLE,
        SQUARE,
        DASH,
        NONE
    }
}
