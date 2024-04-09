package dev.revere.virago.client.modules.render;

import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.input.KeyDownEvent;
import dev.revere.virago.client.events.render.Render2DEvent;
import dev.revere.virago.client.events.render.Render3DEvent;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.util.render.ColorUtil;
import dev.revere.virago.util.render.RenderUtils;
import lombok.Getter;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.util.Map;

/**
 * @author Remi
 * @project Virago
 * @date 3/23/2024
 */
@Getter
@ModuleData(name = "Indicators", description = "Displays various indicators", type = EnumModuleType.RENDER)
public class Indicators extends AbstractModule {

    private Setting<Float> scale = new Setting<>("Scale", 7.0F)
            .minimum(5.0F)
            .maximum(30.0F)
            .incrementation(1.0F)
            .describedBy("The scale of the indicators");

    private Setting<Float> radius = new Setting<>("Radius", 50.0F)
            .minimum(10.0F)
            .maximum(100.0F)
            .incrementation(1.0F)
            .describedBy("The radius of the indicators");

    private final Map<Entity, Vec3> entityUpperBounds = Maps.newHashMap();
    private final Map<Entity, Vec3> entityLowerBounds = Maps.newHashMap();

    @EventHandler
    private final Listener<Render3DEvent> render3DEventListener = event -> {
        if (!this.entityUpperBounds.isEmpty()) {
            this.entityUpperBounds.clear();
        }

        if (!this.entityLowerBounds.isEmpty()) {
            this.entityLowerBounds.clear();
        }

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityPlayer)) return;
            Vec3 bound = getEntityRenderPosition(entity);
            bound.add(new Vec3(0.0D, entity.height + 0.2D, 0.0D));
            Vec3 upperBounds = RenderUtils.to2D(bound.xCoord, bound.yCoord, bound.zCoord);
            Vec3 lowerBounds = RenderUtils.to2D(bound.xCoord, (bound.yCoord - 2.0), bound.zCoord);

            if (upperBounds == null || lowerBounds == null) {
                continue;
            }

            this.entityUpperBounds.put(entity, upperBounds);
            this.entityLowerBounds.put(entity, lowerBounds);
        }
    };

    private Vec3 getEntityRenderPosition(Entity entity) {
        double partial = mc.timer.renderPartialTicks;
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partial - RenderManager.viewerPosX;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partial - RenderManager.viewerPosY;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partial - RenderManager.viewerPosZ;
        return new Vec3(x, y, z);
    }

    @EventHandler
    private final Listener<Render2DEvent> render2DEventListener = event -> {
        mc.theWorld.loadedEntityList.forEach(entity -> {
            if (!(entity instanceof EntityPlayer)) return;

            EntityPlayer player = (EntityPlayer) entity;
            if (player == mc.thePlayer) return;

            Vec3 position = getEntityLowerBounds().get(player);
            if (position == null) return;

            if (this.isOnScreen(position)) return;

            int x = Display.getWidth() / 2 / (mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale);
            int y = Display.getHeight() / 2 / (mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale);

            float yaw = getRotations(entity) - mc.thePlayer.rotationYaw;

            GL11.glTranslatef((float) x, (float) y, 0.0f);
            GL11.glRotatef(yaw, 0.0f, 0.0f, 1.0f);
            GL11.glTranslatef((float) (-x), (float) (-y), 0.0f);
            RenderUtils.drawTracerPointer((float) x, (float) y - this.radius.getValue(), this.scale.getValue(), 2.0f, 1.0f, ColorUtil.getColor(true));
            GL11.glTranslatef((float) x, (float) y, 0.0f);
            GL11.glRotatef((-yaw), 0.0f, 0.0f, 1.0f);
            GL11.glTranslatef((float) (-x), (float) (-y), 0.0f);
        });
    };

    private boolean isOnScreen(Vec3 position) {
        int guiScaleX;
        int guiScaleY;
        int guiScale;
        if (!(position.xCoord > -1.0)) {
            return false;
        }
        if (!(position.zCoord < 1.0)) {
            return false;
        }
        double posX = position.xCoord;
        guiScale = Indicators.mc.gameSettings.guiScale == 0 ? 1 : Indicators.mc.gameSettings.guiScale;
        if (!(posX / guiScale >= 0.0)) {
            return false;
        }
        double posX2 = position.xCoord;
        guiScaleX = Indicators.mc.gameSettings.guiScale == 0 ? 1 : Indicators.mc.gameSettings.guiScale;
        if (!(posX2 / guiScaleX <= Display.getWidth())) {
            return false;
        }
        double posY = position.yCoord;
        guiScaleY = Indicators.mc.gameSettings.guiScale == 0 ? 1 : Indicators.mc.gameSettings.guiScale;
        if (!(posY / guiScaleY >= 0.0)) {
            return false;
        }
        double posY2 = position.yCoord;
        int screenHeight = Indicators.mc.gameSettings.guiScale == 0 ? 1 : Indicators.mc.gameSettings.guiScale;
        if (posY2 / screenHeight <= Display.getHeight()) return false;
        return false;
    }


    private static float getRotations(Entity entity) {
        double entityX = entity.posX - mc.thePlayer.posX;
        double entityZ = entity.posZ - mc.thePlayer.posZ;
        return (float) (-(Math.atan2(entityX, entityZ) * 57.29577951308232));
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
