package dev.revere.virago.client.modules.render;

import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.render.Render2DEvent;
import dev.revere.virago.client.events.render.Render3DEvent;
import dev.revere.virago.util.Logger;
import dev.revere.virago.util.render.ColorUtil;
import dev.revere.virago.util.render.RenderUtils;
import dev.revere.virago.util.render.RoundedUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/3/2024
 */
@ModuleData(name = "ChestESP", description = "Highlights chests", type = EnumModuleType.RENDER)
public class ChestESP extends AbstractModule {
    public Setting<Mode> mode = new Setting<>("Mode", Mode.HOLLOW)
            .describedBy("The mode of ESP");

    public Setting<Boolean> glow = new Setting<>("Glow", true)
            .describedBy("Whether to render the glow.");

    public Setting<Boolean> outline = new Setting<>("Outlines", true)
            .describedBy("Whether to draw the outlines");


    private final FloatBuffer windowPosition = BufferUtils.createFloatBuffer(4);
    private final IntBuffer viewport = GLAllocation.createDirectIntBuffer(16);
    private final FloatBuffer modelMatrix = GLAllocation.createDirectFloatBuffer(16);
    private final FloatBuffer projectionMatrix = GLAllocation.createDirectFloatBuffer(16);
    private final Map<TileEntity, float[]> entityPosMap = new HashMap<>();
    private static final Map<TileEntity, float[][]> entities = new HashMap<>();

    @EventHandler
    public final Listener<Render3DEvent> render3DEventListener = e -> {
        if (mode.getValue() == Mode.FILLED || mode.getValue() == Mode.HOLLOW || mode.getValue() == Mode.BOTH) {
            ScaledResolution sr = new ScaledResolution(mc);
            entities.keySet().removeIf(player -> !mc.theWorld.loadedTileEntityList.contains(player));
            if (!entityPosMap.isEmpty()) {
                entityPosMap.clear();
            }
            int scaleFactor = sr.getScaleFactor();
            for (Object tile : mc.theWorld.loadedTileEntityList) {
                if (tile instanceof TileEntityChest) {
                    TileEntityChest player = (TileEntityChest) tile;
                    GlStateManager.pushMatrix();
                    Vec3 vec3 = getVec3(player);

                    float posX = (float) (vec3.xCoord - mc.getRenderManager().viewerPosX);
                    float posY = (float) (vec3.yCoord - mc.getRenderManager().viewerPosY);
                    float posZ = (float) (vec3.zCoord - mc.getRenderManager().viewerPosZ);

                    AxisAlignedBB bb = new AxisAlignedBB(posX + 1, posY + 1, posZ + 1, posX, posY, posZ);
                    double[][] vectors = {{bb.minX, bb.minY, bb.minZ}, {bb.minX, bb.maxY, bb.minZ},
                            {bb.minX, bb.maxY, bb.maxZ}, {bb.minX, bb.minY, bb.maxZ}, {bb.maxX, bb.minY, bb.minZ},
                            {bb.maxX, bb.maxY, bb.minZ}, {bb.maxX, bb.maxY, bb.maxZ}, {bb.maxX, bb.minY, bb.maxZ}};

                    Vector3f projection;
                    Vector4f position = new Vector4f(Float.MAX_VALUE, Float.MAX_VALUE, -1.0F, -1.0F);

                    for (double[] vec : vectors) {
                        projection = project2D((float) vec[0], (float) vec[1], (float) vec[2], scaleFactor);
                        if (projection != null && projection.z >= 0.0F && projection.z < 1.0F) {
                            position.x = Math.min(position.x, projection.x);
                            position.y = Math.min(position.y, projection.y);
                            position.z = Math.max(position.z, projection.x);
                            position.w = Math.max(position.w, projection.y);
                        }
                    }
                    entityPosMap.put(player, new float[]{position.x, position.z, position.y, position.w});
                    GlStateManager.popMatrix();
                } else if (tile instanceof TileEntityEnderChest) {
                    TileEntityEnderChest player = (TileEntityEnderChest) tile;
                    GlStateManager.pushMatrix();
                    Vec3 vec3 = getVec3(player);

                    float posX = (float) (vec3.xCoord - mc.getRenderManager().viewerPosX);
                    float posY = (float) (vec3.yCoord - mc.getRenderManager().viewerPosY);
                    float posZ = (float) (vec3.zCoord - mc.getRenderManager().viewerPosZ);

                    AxisAlignedBB bb = new AxisAlignedBB(posX + 1, posY + 1, posZ + 1, posX, posY, posZ);
                    double[][] vectors = {{bb.minX, bb.minY, bb.minZ}, {bb.minX, bb.maxY, bb.minZ},
                            {bb.minX, bb.maxY, bb.maxZ}, {bb.minX, bb.minY, bb.maxZ}, {bb.maxX, bb.minY, bb.minZ},
                            {bb.maxX, bb.maxY, bb.minZ}, {bb.maxX, bb.maxY, bb.maxZ}, {bb.maxX, bb.minY, bb.maxZ}};

                    Vector3f projection;
                    Vector4f position = new Vector4f(Float.MAX_VALUE, Float.MAX_VALUE, -1.0F, -1.0F);
                    for (double[] vec : vectors) {
                        projection = project2D((float) vec[0], (float) vec[1], (float) vec[2], scaleFactor);
                        if (projection != null && projection.z >= 0.0F && projection.z < 1.0F) {
                            position.x = Math.min(position.x, projection.x);
                            position.y = Math.min(position.y, projection.y);
                            position.z = Math.max(position.z, projection.x);
                            position.w = Math.max(position.w, projection.y);
                        }
                    }
                    entityPosMap.put(player, new float[]{position.x, position.z, position.y, position.w});
                    GlStateManager.popMatrix();
                }
            }
        }
    };

    @EventHandler
    public final Listener<Render2DEvent> render2DEventListener = e -> {
        if(mode.getValue() == Mode.FILLED || mode.getValue() == Mode.HOLLOW || mode.getValue() == Mode.BOTH) {
            for (TileEntity player : entityPosMap.keySet()) {
                GL11.glPushMatrix();

                float[] positions = entityPosMap.get(player);
                float x = positions[0];
                float x2 = positions[1];
                float y = positions[2];
                float y2 = positions[3];

                switch (mode.getValue()) {
                    case FILLED:
                        Gui.drawRect(x, y, x2, y2, ColorUtil.getColor(true));
                        break;
                    case HOLLOW:
                        GL11.glDisable(GL11.GL_TEXTURE_2D);
                        enableAlpha();
                        disableAlpha();

                        if(outline.getValue()) {
                            RenderUtils.drawHollowRectDefineWidth(x - 0.5f, y - 0.5f, x2 - 0.5f, y2 - 0.5f, 0.5f, 0x96000000);
                            RenderUtils.drawHollowRectDefineWidth(x + 0.5f, y + 0.5f, x2 + 0.5f, y2 + 0.5f, 0.5f, 0x96000000);
                        }

                        RenderUtils.drawHollowRectDefineWidth(x, y, x2, y2, 0.5f, ColorUtil.getColor(true));

                        GL11.glEnable(GL11.GL_TEXTURE_2D);
                        break;
                    case BOTH:
                        Gui.drawRect(x, y, x2, y2, ColorUtil.getColor(true));
                        GL11.glDisable(GL11.GL_TEXTURE_2D);
                        enableAlpha();
                        disableAlpha();

                        if(outline.getValue()) {
                            RenderUtils.drawHollowRectDefineWidth(x - 0.5f, y - 0.5f, x2 - 0.5f, y2 - 0.5f, 0.5f, 0x96000000);
                            RenderUtils.drawHollowRectDefineWidth(x + 0.5f, y + 0.5f, x2 + 0.5f, y2 + 0.5f, 0.5f, 0x96000000);
                        }

                        RenderUtils.drawHollowRectDefineWidth(x, y, x2, y2, 0.5f, ColorUtil.getColor(true));

                        GL11.glEnable(GL11.GL_TEXTURE_2D);
                        break;
                }
                float width = x2-x;
                float height = y2-y;

                if(glow.getValue()) RoundedUtils.shadow(x, y, width, height, 0,10, new Color(ColorUtil.getColor(true)));
                GL11.glPopMatrix();
            }
        }
    };

    private Vector3f project2D(float x, float y, float z, int scaleFactor) {
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelMatrix);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionMatrix);
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);
        if (GLU.gluProject(x, y, z, modelMatrix, projectionMatrix, viewport, windowPosition)) {
            return new Vector3f(windowPosition.get(0) / scaleFactor,
                    (mc.displayHeight - windowPosition.get(1)) / scaleFactor, windowPosition.get(2));
        }

        return null;
    }

    public static void enableAlpha() {
        GL11.glEnable(GL11.GL_BLEND);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
    }

    public static void disableAlpha() {
        GL11.glDisable(GL11.GL_BLEND);
    }

    private Vec3 getVec3(final TileEntity var0) {
        final float timer = mc.timer.renderPartialTicks;
        final double x = var0.getPos().getX() + (0) * timer;
        final double y = var0.getPos().getY() + (0) * timer;
        final double z = var0.getPos().getZ() + (0) * timer;
        return new Vec3(x, y, z);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public enum Mode {
        HOLLOW,
        FILLED,
        BOTH
    }
}
