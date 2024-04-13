package dev.revere.virago.client.modules.render;

import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.render.Render3DEvent;
import dev.revere.virago.util.render.ColorUtil;
import net.minecraft.client.renderer.entity.RenderManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@ModuleData(name = "China Hat", description = "Renders a hat above players heads", type = EnumModuleType.RENDER)
public class ChinaHat extends AbstractModule {
    @EventHandler
    private final Listener<Render3DEvent> render3DEvent = event -> {
        final double x = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * mc.timer.renderPartialTicks - RenderManager.viewerPosX;
        final double y = (mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.timer.renderPartialTicks - RenderManager.viewerPosY) + mc.thePlayer.getEyeHeight() + 0.6;
        final double z = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * mc.timer.renderPartialTicks - RenderManager.viewerPosZ;

        if (mc.gameSettings.thirdPersonView == 0)
            return;

        GL11.glDisable(3553);
        GL11.glLineWidth(1);
        GL11.glBegin(3);

        for (int i = 0; i <= 10800; ++i) {
            final Color color = new Color(ColorUtil.getColor(true));

            GL11.glColor4f(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, 1F);
            GL11.glVertex3d(x, y, z);
            GL11.glVertex3d(x + 0.65 * Math.cos(i * 6.283185307179586 / 5400), y - 0.3, z + 0.65 * Math.sin(i * 6.283185307179586 / 5400));
        }

        GL11.glEnd();
        GL11.glEnable(2929);
        GL11.glEnable(3553);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1F);
    };

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
