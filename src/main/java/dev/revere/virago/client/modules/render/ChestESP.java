package dev.revere.virago.client.modules.render;

import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.render.Render2DEvent;
import dev.revere.virago.client.events.render.Render3DEvent;
import dev.revere.virago.util.render.ColorUtil;
import dev.revere.virago.util.render.RenderUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
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

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

@ModuleData(name = "ChestESP", description = "Highlight all chests around you", type = EnumModuleType.RENDER)
public class ChestESP extends AbstractModule {

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.HOLLOW);
    public Setting<Boolean> glow = new Setting<>("Glow", true).describedBy("Whether to render the glow.");
    public Setting<Boolean> outline = new Setting<>("Outlines", true).describedBy("Whether to draw the outlines");

    private final Map<TileEntity, float[][]> entities = new HashMap<>();
    private final Map<TileEntity, float[]> entityPosMap = new HashMap<>();

    private final FloatBuffer windowPosition = BufferUtils.createFloatBuffer(4);
    private final IntBuffer viewport = GLAllocation.createDirectIntBuffer(16);
    private final FloatBuffer modelMatrix = GLAllocation.createDirectFloatBuffer(16);
    private final FloatBuffer projectionMatrix = GLAllocation.createDirectFloatBuffer(16);


    @EventHandler
    private final Listener<Render3DEvent> onRender3D = event -> {
        ScaledResolution resolution = new ScaledResolution(mc);
        entities.keySet().removeIf(player -> !mc.theWorld.loadedTileEntityList.contains(player));

        if(!entityPosMap.isEmpty())
            this.entityPosMap.clear();

        int scaleFactor = resolution.getScaleFactor();

        for(Object tile : mc.theWorld.loadedTileEntityList) {
            Vector3f projection;
            Vector4f position;
            double[][] vectors;
            AxisAlignedBB bb;

            float x;
            float y;
            float z;

            Vec3 vec3;
            TileEntity chest;

            if(tile instanceof TileEntityChest) {
                chest = (TileEntityChest) tile;
                GlStateManager.pushMatrix();
                vec3 = getVec3(chest);

                x = (float) (vec3.xCoord - RenderManager.viewerPosX);
                y = (float) (vec3.yCoord - RenderManager.viewerPosY);
                z = (float) (vec3.zCoord - RenderManager.viewerPosZ);

                bb = new AxisAlignedBB(x + 1.0f, y + 1.0f, z + 1.0f, x, y, z);
                vectors = new double[][]{{bb.minX, bb.minY, bb.minZ}, {bb.minX, bb.maxY, bb.minZ}, {bb.minX, bb.maxY, bb.maxZ}, {bb.minX, bb.minY, bb.maxZ}, {bb.maxX, bb.minY, bb.minZ}, {bb.maxX, bb.maxY, bb.minZ}, {bb.maxX, bb.maxY, bb.maxZ}, {bb.maxX, bb.minY, bb.maxZ}};
                position = new Vector4f(Float.MAX_VALUE, Float.MAX_VALUE, -1.0f, -1.0f);

                for (double[] vec : vectors) {
                    projection = project2D((float) vec[0], (float) vec[1], (float) vec[2], scaleFactor);

                    if (projection == null || projection.z >= 0.0f || !(projection.z < 1.0f))
                        continue;

                    position.x = Math.min(position.x, projection.x);
                    position.y = Math.min(position.y, projection.y);
                    position.z = Math.max(position.z, projection.x);
                    position.w = Math.max(position.w, position.y);
                }

                entityPosMap.put(chest, new float[]{position.x, position.z, position.y, position.w});
                GlStateManager.popMatrix();
            }

            if(!(tile instanceof TileEntityEnderChest))
                continue;

            chest = (TileEntityEnderChest) tile;
            GlStateManager.pushMatrix();
            vec3 = getVec3(chest);

            x = (float)(vec3.xCoord - RenderManager.viewerPosX);
            y = (float)(vec3.yCoord - RenderManager.viewerPosY);
            z = (float)(vec3.zCoord - RenderManager.viewerPosZ);

            bb = new AxisAlignedBB(x + 1.0f, y + 1.0f, z + 1.0f, x, y, z);
            vectors = new double[][]{{bb.minX, bb.minY, bb.minZ}, {bb.minX, bb.maxY, bb.minZ}, {bb.minX, bb.maxY, bb.maxZ}, {bb.minX, bb.minY, bb.maxZ}, {bb.maxX, bb.minY, bb.minZ}, {bb.maxX, bb.maxY, bb.minZ}, {bb.maxX, bb.maxY, bb.maxZ}, {bb.maxX, bb.minY, bb.maxZ}};
            position = new Vector4f(Float.MAX_VALUE, Float.MAX_VALUE, -1.0f, -1.0f);

            for(double[] vec : vectors) {
                projection = this.project2D((float)vec[0], (float)vec[1], (float)vec[2], scaleFactor);

                if (projection == null || !(projection.z >= 0.0f) || !(projection.z < 1.0f))
                    continue;

                position.x = Math.min(position.x, projection.x);
                position.y = Math.min(position.y, projection.y);
                position.z = Math.max(position.z, projection.x);
                position.w = Math.max(position.w, projection.y);
            }

            this.entityPosMap.put(chest, new float[]{position.x, position.z, position.y, position.w});
            GlStateManager.popMatrix();
        }
    };


    @EventHandler
    private final Listener<Render2DEvent> onRender2D = event -> {
        int color = ColorUtil.getColor(true);

        for(TileEntity tile : entityPosMap.keySet()) {
            GL11.glPushMatrix();

            float[] positions = entityPosMap.get(tile);

            float x = positions[0];
            float x2 = positions[1];
            float y = positions[2];
            float y2 = positions[3];

            switch(mode.getValue()) {
                case FILLED: {
                    RenderUtils.drawRect(x, y, x2, y2, color);
                    break;
                }

                case HOLLOW: {
                    GL11.glDisable(3553);
                    ChestESP.enableAlpha();
                    ChestESP.disableAlpha();

                    if (this.outline.getValue()) {
                        RenderUtils.drawHollowRectDefineWidth(x - 0.5f, y - 0.5f, x2 - 0.5f, y2 - 0.5f, 0.5f, -1778384896);
                        RenderUtils.drawHollowRectDefineWidth(x + 0.5f, y + 0.5f, x2 + 0.5f, y2 + 0.5f, 0.5f, -1778384896);
                    }

                    RenderUtils.drawHollowRectDefineWidth(x, y, x2, y2, 0.5f, color);
                    GL11.glEnable(3553);
                    break;
                }

                case BOTH: {
                    RenderUtils.drawRect(x, y, x2, y2, color);

                    GL11.glDisable(3553);
                    ChestESP.enableAlpha();
                    ChestESP.disableAlpha();

                    if (this.outline.getValue()) {
                        RenderUtils.drawHollowRectDefineWidth(x - 0.5f, y - 0.5f, x2 - 0.5f, y2 - 0.5f, 0.5f, -1778384896);
                        RenderUtils.drawHollowRectDefineWidth(x + 0.5f, y + 0.5f, x2 + 0.5f, y2 + 0.5f, 0.5f, -1778384896);
                    }

                    RenderUtils.drawHollowRectDefineWidth(x, y, x2, y2, 0.5f, color);
                    GL11.glEnable(3553);
                    break;
                }
            }
        }
    };



    private Vector3f project2D(float x, float y, float z, int scaleFactor) {
        GL11.glGetFloat(2982, this.modelMatrix);
        GL11.glGetFloat(2983, this.projectionMatrix);
        GL11.glGetInteger(2978, this.viewport);

        if (GLU.gluProject(x, y, z, this.modelMatrix, this.projectionMatrix, this.viewport, this.windowPosition))
            return new Vector3f(this.windowPosition.get(0) / (float) scaleFactor, ((float) mc.displayHeight - this.windowPosition.get(1)) / (float) scaleFactor, this.windowPosition.get(2));

        return null;
    }

    private Vec3 getVec3(TileEntity var0) {
        float timer = mc.timer.renderPartialTicks;

        double x = (float)var0.getPos().getX() + 0.0f * timer;
        double y = (float)var0.getPos().getY() + 0.0f * timer;
        double z = (float)var0.getPos().getZ() + 0.0f * timer;

        return new Vec3(x, y, z);
    }

    public static void enableAlpha() {
        GL11.glEnable(3042);
        GL14.glBlendFuncSeparate(770, 771, 1, 0);
    }

    public static void disableAlpha() {
        GL11.glDisable(3042);
    }


    private enum Mode {
        HOLLOW,
        FILLED,
        BOTH
    }
}
