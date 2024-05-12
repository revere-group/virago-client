package dev.revere.virago.client.modules.render;

import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.player.ModalUpdateEvent;
import dev.revere.virago.client.events.render.Render2DEvent;
import dev.revere.virago.client.events.render.Render3DEvent;
import dev.revere.virago.client.events.render.RenderNametagEvent;
import dev.revere.virago.util.Logger;
import dev.revere.virago.util.render.ColorUtil;
import dev.revere.virago.util.render.RenderUtils;
import dev.revere.virago.util.render.RoundedUtils;
import lombok.AllArgsConstructor;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/2/2024
 */
@ModuleData(name = "ESP", displayName = "ESP", description = "Draws a box around entities", type = EnumModuleType.RENDER)
public class ESP extends AbstractModule {

    private final Setting<Boolean> nameTagsProperty = new Setting<>("NameTags", true);
    private final Setting<TagColorMode> tagColorModeProperty = new Setting<>("Tag Color", TagColorMode.CUSTOM).visibleWhen(nameTagsProperty::getValue);
    private final Setting<Boolean> healthProperty = new Setting<>("Health", true);
    private final Setting<HealthColorMode> healthColorModeProperty = new Setting<>("Health Color", HealthColorMode.CUSTOM).visibleWhen(healthProperty::getValue);
    private final Setting<Boolean> skeletonProperty = new Setting<>("Skeleton", true);
    private final Setting<Boolean> armorProperty = new Setting<>("Armor", true);
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

    private final Setting<Float> skeletonThickness = new Setting<>("Skeleton Width", 1.3f)
            .minimum(1.0f)
            .maximum(10.0f)
            .incrementation(0.1f)
            .visibleWhen(skeletonProperty::getValue);

    public final Setting<Boolean> personProperty = new Setting<>("Person", false);
    private final Setting<PersonMode> personModeProperty = new Setting<>("Person Mode", PersonMode.ZIUE).visibleWhen(personProperty::getValue);

    private final Map<EntityPlayer, float[][]> entities = new HashMap<>();

    @EventHandler
    private final Listener<RenderNametagEvent> renderNametagListener = event -> {
        if (event.getEntity() instanceof EntityPlayer && nameTagsProperty.getValue()) {
            event.setCancelled(true);
        }
    };

    @EventHandler
    private final Listener<ModalUpdateEvent> modalUpdateEventListener = event -> {
        if (entities.containsKey(event.getPlayer())) {
            ModelPlayer model = event.getModel();
            entities.put(event.getPlayer(), new float[][]{{model.bipedHead.rotateAngleX, model.bipedHead.rotateAngleY, model.bipedHead.rotateAngleZ}, {model.bipedRightArm.rotateAngleX, model.bipedRightArm.rotateAngleY, model.bipedRightArm.rotateAngleZ}, {model.bipedLeftArm.rotateAngleX, model.bipedLeftArm.rotateAngleY, model.bipedLeftArm.rotateAngleZ}, {model.bipedRightLeg.rotateAngleX, model.bipedRightLeg.rotateAngleY, model.bipedRightLeg.rotateAngleZ}, {model.bipedLeftLeg.rotateAngleX, model.bipedLeftLeg.rotateAngleY, model.bipedLeftLeg.rotateAngleZ}});
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
            if (!RenderUtils.isInViewFrustum(entity)) continue;
            addEntity((EntityPlayer) entity, new ModelPlayer(0, false));
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
            Color color = new Color(ColorUtil.getColor(false));

            float width = maxX - minX;
            float height = maxY - minY;

            if (glow.getValue() && !innerGlow.getValue()) {
                RoundedUtils.shadow(minX, minY, width, height, 0, glowIntensity.getValue(), new Color(ColorUtil.getColor(true)));
            } else if (glow.getValue()) {
                RoundedUtils.shadowGradient(minX, minY, width, height, 1, innerGlowIntensity.getValue(), 0.5f, new Color(ColorUtil.getColor(true)), new Color(ColorUtil.getColor(true)), new Color(ColorUtil.getColor(true)), new Color(ColorUtil.getColor(true)), true);
            }

            if (personProperty.getValue()) {
                switch (personModeProperty.getValue()) {
                    case ZION:
                        mc.getTextureManager().bindTexture(new ResourceLocation("virago/textures/esp/zion.jpg"));
                        break;
                    case ZIUE:
                        mc.getTextureManager().bindTexture(new ResourceLocation("virago/textures/esp/ziue.png"));
                        break;
                    case NTDI:
                        mc.getTextureManager().bindTexture(new ResourceLocation("virago/textures/esp/ntdi.png"));
                        break;
                    case BRANDON:
                        mc.getTextureManager().bindTexture(new ResourceLocation("virago/textures/esp/brandon.jpg"));
                        break;
                }

                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                Gui.drawModalRectWithCustomSizedTexture((int) minX, (int) minY, 0, 0, (int) width, (int) height, width, height);
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

                Color tagColor;
                String text;
                if (Objects.requireNonNull(tagColorModeProperty.getValue()) == TagColorMode.ENTITY) {
                    tagColor = new Color(255, 255, 255, 255);
                    text = entity.getDisplayName().getFormattedText();
                } else {
                    tagColor = new Color(ColorUtil.getColor(false));
                    text = entity.getDisplayName().getFormattedText().replaceAll("§[0-9a-f]", "");
                }
                ColorUtil.color(tagColor, MathHelper.floor_float(opacity));
                if (entity != mc.thePlayer) {
                    mc.fontRendererObj.drawStringWithShadow(text, minX + (maxX - minX) / 2 - mc.fontRendererObj.getStringWidth(entity.getDisplayName().getFormattedText()) / 2f, boxModeProperty.getValue() == BoxMode.BOX || boxModeProperty.getValue() == BoxMode.FILL ? minY - mc.fontRendererObj.FONT_HEIGHT - 3 : minY - mc.fontRendererObj.FONT_HEIGHT / 2f, tagColor.getRGB());
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
                Color healthColor;

                if (Objects.requireNonNull(healthColorModeProperty.getValue()) == HealthColorMode.ENTITY) {
                    healthColor = Color.GREEN;
                    if (entity.getHealth() < entity.getMaxHealth() / 2) healthColor = Color.YELLOW;
                    if (entity.getHealth() < entity.getMaxHealth() / 3) healthColor = Color.ORANGE;
                    if (entity.getHealth() < entity.getMaxHealth() / 4) healthColor = Color.RED;
                } else {
                    healthColor = new Color(ColorUtil.getColor(false));
                }

                ColorUtil.color(healthColor, MathHelper.floor_float(opacity));
                GL11.glVertex2f(minX, minY + (maxY - minY));
                GL11.glVertex2f(minX, maxY - (maxY - minY) * (entity.getHealth() / entity.getMaxHealth()));
                GL11.glEnd();
                RenderUtils.post3D();
            }
            if (armorProperty.getValue()) {
                drawArmor(entity, minX, minY, maxX, maxY, opacity);
            }
        }
    };

    @EventHandler
    private final Listener<Render3DEvent> render3DEventListener = event -> {
        entities.keySet().removeIf(player -> !mc.theWorld.playerEntities.contains(player));
        if (skeletonProperty.getValue()) {
            this.startEnd(true);
            GL11.glEnable(2903);
            GL11.glDisable(2848);
            for (EntityPlayer entity : mc.theWorld.playerEntities) {
                drawSkeleton(event, entity);
            }
            this.startEnd(false);
        }
    };


    private void drawArmor(EntityLivingBase entity, float minX, float minY, float maxX, float maxY, float opacity) {
        // Determine the width and height of the armor display
        float armorWidth = 3;
        float armorHeight = maxY - minY;

        // Calculate the position to draw the armor display
        float armorX = maxX + 2; // Place the armor to the right of the entity box
        float armorY = (minY + maxY) / 2f; // Initial center position

        // Calculate the distance between the player and the entity
        float distance = entity.getDistanceToEntity(mc.thePlayer);

        // Calculate the scale factor based on the distance
        float scaleFactor = MathHelper.clamp_float(1.0f - (distance / 200.0f), 0.5f, 0.7f);

        // Adjust the armor Y-coordinate based on the scale factor
        armorY -= (armorHeight * scaleFactor) / 2;

        // Apply the scale factor to the OpenGL matrix
        GL11.glPushMatrix();
        GL11.glTranslatef(armorX, armorY, 0);
        GL11.glScalef(scaleFactor, scaleFactor, 1.0f);

        // Draw the armor display background
        RenderUtils.drawRect(0, 0, armorWidth, armorHeight, 0x00ffffff);

        // Calculate the armor slot height based on the number of armor pieces
        float armorSlotHeight = armorHeight / 4;

        // Iterate through each armor slot and draw the armor piece
        for (int i = 0; i < 4; i++) {
            ItemStack armorPiece = entity.getEquipmentInSlot(i + 1);
            if (armorPiece != null && armorPiece.getItem() != null) {
                float armorPieceY = i * armorSlotHeight;
                if (entity != mc.thePlayer) {
                    drawArmorPiece(armorPiece, 7, armorPieceY, armorWidth, armorSlotHeight, opacity);
                }
            }
        }

        // Restore the OpenGL matrix
        GL11.glPopMatrix();

    }

    private void drawArmorPiece(ItemStack armorPiece, float x, float y, float width, float height, float opacity) {
        GL11.glPushMatrix();

        // Translate to the position to draw the armor piece
        GL11.glTranslatef(x, y, 0);

        // Use RenderItem class to render the armor piece
        mc.getRenderItem().renderItemAndEffectIntoGUI(armorPiece, 0, 0);

        // Draw the armor piece durability
        double damage = ((armorPiece.getMaxDamage() - armorPiece.getItemDamage()) / (double) armorPiece.getMaxDamage()) * 100;
        mc.fontRendererObj.drawStringWithShadow(String.valueOf(Math.round(damage)), 2, 15, new Color(255, 255, 255, MathHelper.floor_float(opacity)).getRGB());

        // Pop the OpenGL matrix to restore the previous state
        GL11.glPopMatrix();
    }

    /**
     * Converts the given axis aligned bounding box to 2D
     *
     * @param interpolatedBB the bounding box to convert
     * @param vectors        the vectors to store the converted bounding box in
     * @param coords         the coordinates to store the converted bounding box in
     */
    private void convertTo2D(AxisAlignedBB interpolatedBB, double[][] vectors, float[] coords) {
        if (coords == null || vectors == null || interpolatedBB == null) return;
        double x = RenderManager.viewerPosX;
        double y = RenderManager.viewerPosY;
        double z = RenderManager.viewerPosZ;

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

    /**
     * Adds an entity to the map
     *
     * @param e     the entity to add
     * @param model the model of the entity
     */
    private void addEntity(final EntityPlayer e, final ModelPlayer model) {
        entities.put(e, new float[][]{
                {model.bipedHead.rotateAngleX, model.bipedHead.rotateAngleY, model.bipedHead.rotateAngleZ},
                {model.bipedRightArm.rotateAngleX, model.bipedRightArm.rotateAngleY,
                        model.bipedRightArm.rotateAngleZ},
                {model.bipedLeftArm.rotateAngleX, model.bipedLeftArm.rotateAngleY, model.bipedLeftArm.rotateAngleZ},
                {model.bipedRightLeg.rotateAngleX, model.bipedRightLeg.rotateAngleY,
                        model.bipedRightLeg.rotateAngleZ},
                {model.bipedLeftLeg.rotateAngleX, model.bipedLeftLeg.rotateAngleY,
                        model.bipedLeftLeg.rotateAngleZ}});
    }

    /**
     * Draws the skeleton of the player entity
     *
     * @param event  the render event to draw the skeleton in
     * @param entity the player entity to draw the skeleton for
     */
    private void drawSkeleton(Render3DEvent event, EntityPlayer entity) {
        final Color color = new Color(ColorUtil.getColor(false));
        final float[][] entityPosition = entities.get(entity);

        // Check if the entity is visible and alive, and if it's not the local player or dead
        if (entityPosition == null || entity.isInvisible() || !entity.isEntityAlive() || entity == mc.thePlayer || entity.isDead || entity.isPlayerSleeping()) {
            return;
        }

        // Save the current matrix
        GL11.glPushMatrix();

        // Set the line width to the skeleton thickness
        GL11.glLineWidth(skeletonThickness.getValue());

        // Translate to the position of the player entity
        final Vec3 vec = this.getVec3(entity);
        final double x = vec.xCoord - RenderManager.renderPosX;
        final double y = vec.yCoord - RenderManager.renderPosY;
        final double z = vec.zCoord - RenderManager.renderPosZ;
        GL11.glTranslated(x, y, z);

        // Rotate the skeleton according to the player's yaw offset
        final float xOff = entity.prevRenderYawOffset + (entity.renderYawOffset - entity.prevRenderYawOffset) * event.getPartialTicks();
        GL11.glRotatef(-xOff, 0.0f, 1.0f, 0.0f);
        GL11.glTranslated(0.0, 0.0, entity.isSneaking() ? -0.235 : 0.0);

        // Adjust Y offset based on sneaking status
        final float yOff = entity.isSneaking() ? 0.6f : 0.75f;

        // Render the right leg
        GL11.glPushMatrix();
        GlStateManager.color((float) color.getRed() / 255, (float) color.getGreen() / 255, (float) color.getBlue() / 255, 1.0f);
        GL11.glTranslated(-0.125, yOff, 0.0);

        // Apply rotation to the right leg
        if (entityPosition[3][0] != 0.0f) {
            GL11.glRotatef(entityPosition[3][0] * 57.295776f, 1.0f, 0.0f, 0.0f);
        }
        if (entityPosition[3][1] != 0.0f) {
            GL11.glRotatef(entityPosition[3][1] * 57.295776f, 0.0f, 1.0f, 0.0f);
        }
        if (entityPosition[3][2] != 0.0f) {
            GL11.glRotatef(entityPosition[3][2] * 57.295776f, 0.0f, 0.0f, 1.0f);
        }

        // Render the right leg line
        GL11.glBegin(3);
        GL11.glVertex3d(0.0, 0.0, 0.0);
        GL11.glVertex3d(0.0, -yOff, 0.0);
        GL11.glEnd();
        GL11.glPopMatrix();

        // Render the left leg
        GL11.glPushMatrix();
        GlStateManager.color((float) color.getRed() / 255, (float) color.getGreen() / 255, (float) color.getBlue() / 255, 1.0f);
        GL11.glTranslated(0.125, yOff, 0.0);

        // Apply rotation to the left leg
        if (entityPosition[4][0] != 0.0f) {
            GL11.glRotatef(entityPosition[4][0] * 57.295776f, 1.0f, 0.0f, 0.0f);
        }
        if (entityPosition[4][1] != 0.0f) {
            GL11.glRotatef(entityPosition[4][1] * 57.295776f, 0.0f, 1.0f, 0.0f);
        }
        if (entityPosition[4][2] != 0.0f) {
            GL11.glRotatef(entityPosition[4][2] * 57.295776f, 0.0f, 0.0f, 1.0f);
        }

        // Render the left leg line
        GL11.glBegin(3);
        GL11.glVertex3d(0.0, 0.0, 0.0);
        GL11.glVertex3d(0.0, -yOff, 0.0);
        GL11.glEnd();
        GL11.glPopMatrix();

        // Adjust position for sneaking animation
        GL11.glTranslated(0.0, 0.0, entity.isSneaking() ? 0.25 : 0.0);

        // Render the right arm
        GL11.glPushMatrix();
        GlStateManager.color((float) color.getRed() / 255, (float) color.getGreen() / 255, (float) color.getBlue() / 255, 1.0f);
        GL11.glTranslated(0.0, entity.isSneaking() ? -0.05 : 0.0, entity.isSneaking() ? -0.01725 : 0.0);
        GL11.glPushMatrix();
        GlStateManager.color((float) color.getRed() / 255, (float) color.getGreen() / 255, (float) color.getBlue() / 255, 1.0f);
        GL11.glTranslated(-0.375, yOff + 0.55, 0.0);

        // Apply rotation to the right arm
        if (entityPosition[1][0] != 0.0f) {
            GL11.glRotatef(entityPosition[1][0] * 57.295776f, 1.0f, 0.0f, 0.0f);
        }
        if (entityPosition[1][1] != 0.0f) {
            GL11.glRotatef(entityPosition[1][1] * 57.295776f, 0.0f, 1.0f, 0.0f);
        }
        if (entityPosition[1][2] != 0.0f) {
            GL11.glRotatef(-entityPosition[1][2] * 57.295776f, 0.0f, 0.0f, 1.0f);
        }

        // Render the right arm line
        GL11.glBegin(3);
        GL11.glVertex3d(0.0, 0.0, 0.0);
        GL11.glVertex3d(0.0, -0.5, 0.0);
        GL11.glEnd();
        GL11.glPopMatrix();

        // Render the left arm
        GL11.glPushMatrix();
        GL11.glTranslated(0.375, yOff + 0.55, 0.0);

        // Apply rotation to the left arm
        if (entityPosition[2][0] != 0.0f) {
            GL11.glRotatef(entityPosition[2][0] * 57.295776f, 1.0f, 0.0f, 0.0f);
        }
        if (entityPosition[2][1] != 0.0f) {
            GL11.glRotatef(entityPosition[2][1] * 57.295776f, 0.0f, 1.0f, 0.0f);
        }
        if (entityPosition[2][2] != 0.0f) {
            GL11.glRotatef(-entityPosition[2][2] * 57.295776f, 0.0f, 0.0f, 1.0f);
        }

        // Render the left arm line
        GL11.glBegin(3);
        GL11.glVertex3d(0.0, 0.0, 0.0);
        GL11.glVertex3d(0.0, -0.5, 0.0);
        GL11.glEnd();
        GL11.glPopMatrix();

        // Rotate the head
        GL11.glRotatef(xOff - entity.rotationYawHead, 0.0f, 1.0f, 0.0f);
        GL11.glPushMatrix();
        GlStateManager.color((float) color.getRed() / 255, (float) color.getGreen() / 255, (float) color.getBlue() / 255, 1.0f);
        GL11.glTranslated(0.0, yOff + 0.55, 0.0);

        // Apply rotation to the head
        if (entityPosition[0][0] != 0.0f) {
            GL11.glRotatef(entityPosition[0][0] * 57.295776f, 1.0f, 0.0f, 0.0f);
        }

        // Render the head line
        GL11.glBegin(3);
        GL11.glVertex3d(0.0, 0.0, 0.0);
        GL11.glVertex3d(0.0, 0.3, 0.0);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glPopMatrix();

        // Adjust position for sneaking animation
        GL11.glRotatef(entity.isSneaking() ? 25.0f : 0.0f, 1.0f, 0.0f, 0.0f);
        GL11.glTranslated(0.0, entity.isSneaking() ? -0.16175 : 0.0, entity.isSneaking() ? -0.48025 : 0.0);

        // Render the body
        GL11.glPushMatrix();
        GL11.glTranslated(0.0, yOff, 0.0);
        GL11.glBegin(3);
        GL11.glVertex3d(-0.125, 0.0, 0.0);
        GL11.glVertex3d(0.125, 0.0, 0.0);
        GL11.glEnd();
        GL11.glPopMatrix();

        // Render the upper body
        GL11.glPushMatrix();
        GlStateManager.color((float) color.getRed() / 255, (float) color.getGreen() / 255, (float) color.getBlue() / 255, 1.0f);
        GL11.glTranslated(0.0, yOff, 0.0);
        GL11.glBegin(3);
        GL11.glVertex3d(0.0, 0.0, 0.0);
        GL11.glVertex3d(0.0, 0.55, 0.0);
        GL11.glEnd();
        GL11.glPopMatrix();

        // Render the upper body sides
        GL11.glPushMatrix();
        GL11.glTranslated(0.0, yOff + 0.55, 0.0);
        GL11.glBegin(3);
        GL11.glVertex3d(-0.375, 0.0, 0.0);
        GL11.glVertex3d(0.375, 0.0, 0.0);
        GL11.glEnd();
        GL11.glPopMatrix();

        // Restore the matrix
        GL11.glPopMatrix();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    /**
     * Gets the interpolated position of the entity
     *
     * @param  entity the entity to get the position for
     * @return the interpolated position of the entity
     */
    private Vec3 getVec3(final EntityPlayer entity) {
        final float timer = mc.timer.renderPartialTicks;
        final double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer;
        final double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer;
        final double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer;
        return new Vec3(x, y, z);
    }

    /**
     * Starts or ends the rendering of the entity
     *
     * @param revert if the rendering should be reverted
     */
    private void startEnd(final boolean revert) {
        if (revert) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GL11.glEnable(2848);
            GlStateManager.disableDepth();
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GlStateManager.blendFunc(770, 771);
            GL11.glHint(3154, 4354);
        } else {
            GlStateManager.disableBlend();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(2848);
            GlStateManager.enableDepth();
            GlStateManager.popMatrix();
        }
        GlStateManager.depthMask(!revert);
    }

    @AllArgsConstructor
    public enum PersonMode {
        ZIUE("ziue"),
        ZION("Zion"),
        NTDI("NTDI"),
        BRANDON("Brandon");

        private final String personName;

        @Override
        public String toString() {
            return personName;
        }
    }

    @AllArgsConstructor
    public enum HealthColorMode {
        CUSTOM("Custom"),
        ENTITY("Entity");

        private final String addonName;

        @Override
        public String toString() {
            return addonName;
        }
    }

    @AllArgsConstructor
    public enum TagColorMode {
        CUSTOM("Custom"),
        ENTITY("Entity");

        private final String addonName;

        @Override
        public String toString() {
            return addonName;
        }
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
