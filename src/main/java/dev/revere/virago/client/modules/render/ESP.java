package dev.revere.virago.client.modules.render;

import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.render.Render2DEvent;
import dev.revere.virago.client.events.render.RenderNametagEvent;
import dev.revere.virago.util.render.ColorUtil;
import dev.revere.virago.util.render.RenderUtils;
import dev.revere.virago.util.render.RoundedUtils;
import lombok.AllArgsConstructor;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/2/2024
 */
@ModuleData(name = "ESP", description = "Draws a box around entities", type = EnumModuleType.RENDER)
public class ESP extends AbstractModule {

    private final Setting<Boolean> nameTagsProperty = new Setting<>("NameTags", true);
    private final Setting<Boolean> healthProperty = new Setting<>("Health", true);
    private final Setting<Boolean> handProperty = new Setting<>("Hand", false);
    private final Setting<Boolean> glow = new Setting<>("Glow", true);
    private final Setting<Boolean> innerGlow = new Setting<>("Inner Glow", false).visibleWhen(glow::getValue);
    private final Setting<Float> glowIntensity = new Setting<>("Intensity", 10f)
            .minimum(1f)
            .maximum(100f)
            .incrementation(1f)
            .visibleWhen(() -> glow.getValue() && !innerGlow.getValue());

    private final Setting<Float> innerGlowIntensity = new Setting<>("Inner Intensity", 1f)
            .minimum(1f)
            .maximum(10f)
            .incrementation(1f)
            .visibleWhen(innerGlow::getValue);

    private final Setting<BoxMode> boxModeProperty = new Setting<>("Box Mode", BoxMode.NONE);
    private final Setting<Boolean> oppositeCornersProperty = new Setting<>("Opposite Corners", false).visibleWhen(() -> boxModeProperty.getValue() == BoxMode.HALF_CORNERS);
    private final Setting<Integer> boxThicknessProperty = new Setting<>("Box Thickness", 1)
            .minimum(1)
            .maximum(10)
            .incrementation(1);

    public int getBoxColor() {
        return ColorUtil.getColor(true);
    }

    @EventHandler
    private final Listener<RenderNametagEvent> renderNametagListener = event -> {
        if (event.getEntity() instanceof EntityPlayer && nameTagsProperty.getValue()) {
            event.setCancelled(true);
        }
    };

    @EventHandler
    private final Listener<Render2DEvent> render2DListener = event -> {
        Gui.drawRect(0, 0, 0, 0, 0);
        final List<EntityLivingBase> livingEntities = mc.theWorld.loadedEntityList.stream()
                .filter(entity -> entity instanceof EntityPlayer)
                .map(entity -> (EntityLivingBase) entity)
                .filter(entity -> entity.getDistanceToEntity(mc.thePlayer) < 200).collect(Collectors.toList());

        for (EntityLivingBase entity : livingEntities) {
            if (!RenderUtils.isInViewFrustrum(entity)) continue;
            final double diffX = entity.posX - entity.lastTickPosX;
            final double diffY = entity.posY - entity.lastTickPosY;
            final double diffZ = entity.posZ - entity.lastTickPosZ;
            final double deltaX = mc.thePlayer.posX - entity.posX;
            final double deltaY = mc.thePlayer.posY - entity.posY;
            final double deltaZ = mc.thePlayer.posZ - entity.posZ;
            final float partialTicks = event.getPartialTicks();
            final AxisAlignedBB interpolatedBB = new AxisAlignedBB(
                    entity.lastTickPosX - entity.width / 2 + diffX * partialTicks,
                    entity.lastTickPosY + diffY * partialTicks,
                    entity.lastTickPosZ - entity.width / 2 + diffZ * partialTicks,
                    entity.lastTickPosX + entity.width / 2 + diffX * partialTicks,
                    entity.lastTickPosY + entity.height + diffY * partialTicks,
                    entity.lastTickPosZ + entity.width / 2 + diffZ * partialTicks);
            final double[][] vectors = new double[8][2];
            final float[] coords = new float[4];
            convertTo2D(interpolatedBB, vectors, coords);
            float minX = coords[0], minY = coords[1], maxX = coords[2], maxY = coords[3];
            float opacity = 255 - MathHelper.clamp_float(MathHelper.sqrt_double(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ) * 4, 0, 255);
            Color color = new Color(getBoxColor());

            float width = maxX - minX;
            float height = maxY - minY;
            if (glow.getValue() && !innerGlow.getValue()) {
                RoundedUtils.shadow(minX, minY, width, height, 0, glowIntensity.getValue(), new Color(ColorUtil.getColor(true)));
            } else if (glow.getValue()) {
                RoundedUtils.shadowGradient(minX, minY, width, height, 1, innerGlowIntensity.getValue(), 0.5f, new Color(ColorUtil.getColor(true)), new Color(ColorUtil.getColor(true)), new Color(ColorUtil.getColor(true)), new Color(ColorUtil.getColor(true)), true);
            }
            switch (boxModeProperty.getValue()) {
                case BOX: {
                    RenderUtils.pre3D();
                    GL11.glLineWidth(boxThicknessProperty.getValue().floatValue() * 4f);
                    GL11.glBegin(GL11.GL_LINE_LOOP);
                    GL11.glColor4f(0, 0, 0, opacity / 255f);
                    GL11.glVertex2f(minX, minY);
                    GL11.glVertex2f(maxX, minY);
                    GL11.glVertex2f(maxX, maxY);
                    GL11.glVertex2f(minX, maxY);
                    GL11.glEnd();

                    GL11.glLineWidth(boxThicknessProperty.getValue().floatValue());
                    ColorUtil.color(color.getRGB());
                    GL11.glBegin(GL11.GL_LINE_LOOP);
                    GL11.glVertex2f(minX, minY);
                    GL11.glVertex2f(maxX, minY);
                    GL11.glVertex2f(maxX, maxY);
                    GL11.glVertex2f(minX, maxY);
                    GL11.glEnd();
                    RenderUtils.post3D();
                    break;
                }
                case FILL: {
                    RenderUtils.drawRect(minX, minY, maxX, maxY, color.getRGB());
                    break;
                }
                case HORIZ_SIDES: {
                    RenderUtils.pre3D();
                    float lineLength = (maxX - minX) / 3;
                    GL11.glLineWidth(boxThicknessProperty.getValue().floatValue() * 4f);
                    GL11.glColor4f(0, 0, 0, opacity / 255f);
                    GL11.glBegin(GL11.GL_LINES);
                    GL11.glVertex2f(minX, minY);
                    GL11.glVertex2f(minX + lineLength, minY);
                    GL11.glVertex2f(minX, minY);
                    GL11.glVertex2f(minX, maxY);
                    GL11.glVertex2f(minX, maxY);
                    GL11.glVertex2f(minX + lineLength, maxY);
                    GL11.glVertex2f(maxX, minY);
                    GL11.glVertex2f(maxX - lineLength, minY);
                    GL11.glVertex2f(maxX, minY);
                    GL11.glVertex2f(maxX, maxY);
                    GL11.glVertex2f(maxX, maxY);
                    GL11.glVertex2f(maxX - lineLength, maxY);
                    GL11.glEnd();

                    GL11.glLineWidth(boxThicknessProperty.getValue().floatValue());
                    GL11.glBegin(GL11.GL_LINES);
                    ColorUtil.color(color.getRGB());
                    GL11.glVertex2f(minX, minY);
                    GL11.glVertex2f(minX + lineLength, minY);
                    GL11.glVertex2f(minX, minY);
                    GL11.glVertex2f(minX, maxY);
                    GL11.glVertex2f(minX, maxY);
                    GL11.glVertex2f(minX + lineLength, maxY);
                    GL11.glVertex2f(maxX, minY);
                    GL11.glVertex2f(maxX - lineLength, minY);
                    GL11.glVertex2f(maxX, minY);
                    GL11.glVertex2f(maxX, maxY);
                    GL11.glVertex2f(maxX, maxY);
                    GL11.glVertex2f(maxX - lineLength, maxY);
                    GL11.glEnd();
                    RenderUtils.post3D();
                    break;
                }
                case VERT_SIDES: {
                    RenderUtils.pre3D();
                    float lineLength = (maxX - minX) / 3;
                    GL11.glLineWidth(boxThicknessProperty.getValue().floatValue() * 4f);
                    GL11.glColor4f(0, 0, 0, opacity / 255f);
                    GL11.glBegin(GL11.GL_LINES);
                    GL11.glVertex2f(minX, minY);
                    GL11.glVertex2f(minX, minY + lineLength);
                    GL11.glVertex2f(minX, minY);
                    GL11.glVertex2f(maxX, minY);
                    GL11.glVertex2f(maxX, minY);
                    GL11.glVertex2f(maxX, minY + lineLength);
                    GL11.glVertex2f(minX, maxY);
                    GL11.glVertex2f(minX, maxY - lineLength);
                    GL11.glVertex2f(minX, maxY);
                    GL11.glVertex2f(maxX, maxY);
                    GL11.glVertex2f(maxX, maxY);
                    GL11.glVertex2f(maxX, maxY - lineLength);
                    GL11.glEnd();
                    GL11.glLineWidth(boxThicknessProperty.getValue().floatValue());
                    ColorUtil.color(color.getRGB());
                    GL11.glBegin(GL11.GL_LINES);
                    GL11.glVertex2f(minX, minY);
                    GL11.glVertex2f(minX, minY + lineLength);
                    GL11.glVertex2f(minX, minY);
                    GL11.glVertex2f(maxX, minY);
                    GL11.glVertex2f(maxX, minY);
                    GL11.glVertex2f(maxX, minY + lineLength);
                    GL11.glVertex2f(minX, maxY);
                    GL11.glVertex2f(minX, maxY - lineLength);
                    GL11.glVertex2f(minX, maxY);
                    GL11.glVertex2f(maxX, maxY);
                    GL11.glVertex2f(maxX, maxY);
                    GL11.glVertex2f(maxX, maxY - lineLength);
                    GL11.glEnd();
                    RenderUtils.post3D();
                    break;
                }
                case CORNERS: {
                    RenderUtils.pre3D();
                    GL11.glLineWidth(boxThicknessProperty.getValue().floatValue() * 4f);
                    GL11.glBegin(GL11.GL_LINES);
                    GL11.glColor4f(0, 0, 0, opacity / 255f);
                    float lineLength = (maxX - minX) / 3;
                    GL11.glVertex2f(minX, minY);
                    GL11.glVertex2f(minX + lineLength, minY);
                    GL11.glVertex2f(minX, minY);
                    GL11.glVertex2f(minX, minY + lineLength);

                    GL11.glVertex2f(maxX, minY);
                    GL11.glVertex2f(maxX - lineLength, minY);
                    GL11.glVertex2f(maxX, minY);
                    GL11.glVertex2f(maxX, minY + lineLength);

                    GL11.glVertex2f(minX, maxY);
                    GL11.glVertex2f(minX + lineLength, maxY);
                    GL11.glVertex2f(minX, maxY);
                    GL11.glVertex2f(minX, maxY - lineLength);

                    GL11.glVertex2f(maxX, maxY);
                    GL11.glVertex2f(maxX - lineLength, maxY);
                    GL11.glVertex2f(maxX, maxY);
                    GL11.glVertex2f(maxX, maxY - lineLength);
                    GL11.glEnd();

                    GL11.glLineWidth(boxThicknessProperty.getValue().floatValue());
                    GL11.glBegin(GL11.GL_LINES);
                    ColorUtil.color(color.getRGB());
                    GL11.glVertex2f(minX, minY);
                    GL11.glVertex2f(minX + lineLength, minY);
                    GL11.glVertex2f(minX, minY);
                    GL11.glVertex2f(minX, minY + lineLength);
                    GL11.glVertex2f(maxX, minY);
                    GL11.glVertex2f(maxX - lineLength, minY);
                    GL11.glVertex2f(maxX, minY);
                    GL11.glVertex2f(maxX, minY + lineLength);
                    GL11.glVertex2f(minX, maxY);
                    GL11.glVertex2f(minX + lineLength, maxY);
                    GL11.glVertex2f(minX, maxY);
                    GL11.glVertex2f(minX, maxY - lineLength);
                    GL11.glVertex2f(maxX, maxY);
                    GL11.glVertex2f(maxX - lineLength, maxY);
                    GL11.glVertex2f(maxX, maxY);
                    GL11.glVertex2f(maxX, maxY - lineLength);
                    GL11.glEnd();
                    RenderUtils.post3D();
                    break;
                }
                case HALF_CORNERS: {
                    RenderUtils.pre3D();
                    GL11.glLineWidth(boxThicknessProperty.getValue().floatValue() * 4f);
                    GL11.glBegin(GL11.GL_LINES);
                    GL11.glColor4f(0, 0, 0, opacity / 255f);
                    float lineLength = (maxX - minX) / 3;
                    if (oppositeCornersProperty.getValue()) {
                        GL11.glVertex2f(maxX, minY);
                        GL11.glVertex2f(maxX - lineLength, minY);
                        GL11.glVertex2f(maxX, minY);
                        GL11.glVertex2f(maxX, minY + lineLength);

                        GL11.glVertex2f(minX, maxY);
                        GL11.glVertex2f(minX + lineLength, maxY);
                        GL11.glVertex2f(minX, maxY);
                        GL11.glVertex2f(minX, maxY - lineLength);
                    } else {
                        GL11.glVertex2f(minX, minY);
                        GL11.glVertex2f(minX + lineLength, minY);
                        GL11.glVertex2f(minX, minY);
                        GL11.glVertex2f(minX, minY + lineLength);

                        GL11.glVertex2f(maxX, maxY);
                        GL11.glVertex2f(maxX - lineLength, maxY);
                        GL11.glVertex2f(maxX, maxY);
                        GL11.glVertex2f(maxX, maxY - lineLength);
                    }
                    GL11.glEnd();
                    GL11.glLineWidth(boxThicknessProperty.getValue().floatValue());
                    GL11.glBegin(GL11.GL_LINES);
                    ColorUtil.color(color.getRGB());
                    if (oppositeCornersProperty.getValue()) {
                        GL11.glVertex2f(maxX, minY);
                        GL11.glVertex2f(maxX - lineLength, minY);
                        GL11.glVertex2f(maxX, minY);
                        GL11.glVertex2f(maxX, minY + lineLength);

                        GL11.glVertex2f(minX, maxY);
                        GL11.glVertex2f(minX + lineLength, maxY);
                        GL11.glVertex2f(minX, maxY);
                        GL11.glVertex2f(minX, maxY - lineLength);
                    } else {
                        GL11.glVertex2f(minX, minY);
                        GL11.glVertex2f(minX + lineLength, minY);
                        GL11.glVertex2f(minX, minY);
                        GL11.glVertex2f(minX, minY + lineLength);

                        GL11.glVertex2f(maxX, maxY);
                        GL11.glVertex2f(maxX - lineLength, maxY);
                        GL11.glVertex2f(maxX, maxY);
                        GL11.glVertex2f(maxX, maxY - lineLength);
                    }
                    GL11.glEnd();
                    RenderUtils.post3D();
                    break;
                }
            }
            if (nameTagsProperty.getValue()) {
                float scale = 0.55f;
                float leftoverScale = 1 / scale;
                minX *= leftoverScale;
                minY *= leftoverScale;
                maxX *= leftoverScale;
                maxY *= leftoverScale;
                GL11.glScalef(scale, scale, 1);
                if (entity != mc.thePlayer) {
                    mc.fontRendererObj.drawStringWithShadow(entity.getDisplayName().getFormattedText(), minX + (maxX - minX) / 2 - mc.fontRendererObj.getStringWidth(entity.getDisplayName().getFormattedText()) / 2f, boxModeProperty.getValue() == BoxMode.BOX || boxModeProperty.getValue() == BoxMode.FILL ? minY - mc.fontRendererObj.FONT_HEIGHT - 3 : minY - mc.fontRendererObj.FONT_HEIGHT / 2f, new Color(255, 255, 255, MathHelper.floor_float(opacity)).getRGB());
                }
                GL11.glScalef(leftoverScale, leftoverScale, 1);
                minX *= scale;
                minY *= scale;
                maxX *= scale;
                maxY *= scale;
            }
            if (handProperty.getValue()) {
                if (entity.getHeldItem() != null) {
                    float scale = 0.5f;
                    float leftoverScale = 1 / scale;
                    minX *= leftoverScale;
                    minY *= leftoverScale;
                    maxX *= leftoverScale;
                    maxY *= leftoverScale;
                    GL11.glScalef(scale, scale, 1);
                    String text = entity.getHeldItem().getDisplayName();
                    if (entity != mc.thePlayer) {
                        mc.fontRendererObj.drawStringWithShadow(text, minX + (maxX - minX) / 2 - mc.fontRendererObj.getStringWidth(text) / 2f, boxModeProperty.getValue() == BoxMode.BOX || boxModeProperty.getValue() == BoxMode.FILL ? maxY + mc.fontRendererObj.FONT_HEIGHT - 3 : maxY - mc.fontRendererObj.FONT_HEIGHT / 2f, new Color(255, 255, 255, MathHelper.floor_float(opacity)).getRGB());
                    }
                    GL11.glScalef(leftoverScale, leftoverScale, 1);
                    minX *= scale;
                    minY *= scale;
                    maxX *= scale;
                    maxY *= scale;
                }
            }
            if (healthProperty.getValue()) {
                minX -= 3;
                maxX -= 3;
                RenderUtils.pre3D();
                GL11.glLineWidth(boxThicknessProperty.getValue().floatValue() * 4f);
                GL11.glBegin(GL11.GL_LINES);
                GL11.glColor4f(0, 0, 0, opacity / 255f);
                GL11.glVertex2f(minX, minY);
                GL11.glVertex2f(minX, maxY);
                GL11.glEnd();
                GL11.glLineWidth(boxThicknessProperty.getValue().floatValue());
                GL11.glBegin(GL11.GL_LINES);
                Color healthColor = Color.GREEN;
                if (entity.getHealth() < entity.getMaxHealth() / 2) healthColor = Color.YELLOW;
                if (entity.getHealth() < entity.getMaxHealth() / 3) healthColor = Color.ORANGE;
                if (entity.getHealth() < entity.getMaxHealth() / 4) healthColor = Color.RED;
                ColorUtil.color(healthColor, MathHelper.floor_float(opacity));
                GL11.glVertex2f(minX, minY + (maxY - minY));
                GL11.glVertex2f(minX, maxY - (maxY - minY) * (entity.getHealth() / entity.getMaxHealth()));
                GL11.glEnd();
                RenderUtils.post3D();
                minX += 3;
                maxX += 3;
            }
        }
    };


    private void convertTo2D(AxisAlignedBB interpolatedBB, double[][] vectors, float[] coords) {
        if (coords == null || vectors == null || interpolatedBB == null) return;
        double x = mc.getRenderManager().viewerPosX;
        double y = mc.getRenderManager().viewerPosY;
        double z = mc.getRenderManager().viewerPosZ;

        vectors[0] = RenderUtils.project2D(interpolatedBB.minX - x, interpolatedBB.minY - y,
                interpolatedBB.minZ - z);
        vectors[1] = RenderUtils.project2D(interpolatedBB.minX - x, interpolatedBB.minY - y,
                interpolatedBB.maxZ - z);
        vectors[2] = RenderUtils.project2D(interpolatedBB.minX - x, interpolatedBB.maxY - y,
                interpolatedBB.minZ - z);
        vectors[3] = RenderUtils.project2D(interpolatedBB.maxX - x, interpolatedBB.minY - y,
                interpolatedBB.minZ - z);
        vectors[4] = RenderUtils.project2D(interpolatedBB.maxX - x, interpolatedBB.maxY - y,
                interpolatedBB.minZ - z);
        vectors[5] = RenderUtils.project2D(interpolatedBB.maxX - x, interpolatedBB.minY - y,
                interpolatedBB.maxZ - z);
        vectors[6] = RenderUtils.project2D(interpolatedBB.minX - x, interpolatedBB.maxY - y,
                interpolatedBB.maxZ - z);
        vectors[7] = RenderUtils.project2D(interpolatedBB.maxX - x, interpolatedBB.maxY - y,
                interpolatedBB.maxZ - z);

        float minW = (float) Arrays.stream(vectors).min(Comparator.comparingDouble(pos -> pos[2])).orElse(new double[]{0.5})[2];
        float maxW = (float) Arrays.stream(vectors).max(Comparator.comparingDouble(pos -> pos[2])).orElse(new double[]{0.5})[2];
        if (maxW > 1 || minW < 0) return;
        float minX = (float) Arrays.stream(vectors).min(Comparator.comparingDouble(pos -> pos[0])).orElse(new double[]{0})[0];
        float maxX = (float) Arrays.stream(vectors).max(Comparator.comparingDouble(pos -> pos[0])).orElse(new double[]{0})[0];
        final float top = (mc.displayHeight / (float) new ScaledResolution(mc).getScaleFactor());
        float minY = (float) (top - Arrays.stream(vectors).min(Comparator.comparingDouble(pos -> top - pos[1])).orElse(new double[]{0})[1]);
        float maxY = (float) (top - Arrays.stream(vectors).max(Comparator.comparingDouble(pos -> top - pos[1])).orElse(new double[]{0})[1]);
        coords[0] = minX;
        coords[1] = minY;
        coords[2] = maxX;
        coords[3] = maxY;
    }

    @AllArgsConstructor
    public enum BoxMode {
        NONE("None"),
        BOX("Box"),
        FILL("Fill"),
        HORIZ_SIDES("Horizontal Sides"),
        VERT_SIDES("Vertical Sides"),
        CORNERS("Corners"),
        HALF_CORNERS("Half Corners");

        private final String addonName;

        @Override
        public String toString() {
            return addonName;
        }
    }
}
