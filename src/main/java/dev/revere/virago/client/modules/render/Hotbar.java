package dev.revere.virago.client.modules.render;

import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.client.events.render.Render2DEvent;
import dev.revere.virago.client.events.render.ShaderEvent;
import dev.revere.virago.util.render.RoundedUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.*;

/**
 * @author Remi
 * @project Virago
 * @date 5/2/2024
 */
@ModuleData(name = "Hotbar", displayName = "Hotbar", description = "Customizes the hotbar", type = EnumModuleType.RENDER)
public class Hotbar extends AbstractModule {

    @EventHandler
    private final Listener<Render2DEvent> render2DEventListener = event -> {
        ScaledResolution sr = new ScaledResolution(mc);

        if (mc.getRenderViewEntity() instanceof EntityPlayer) {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

            Gui.drawRect(
                    sr.getScaledWidth() / 2.0F - 90.5f,
                    sr.getScaledHeight() - 21,
                    sr.getScaledWidth() / 2.0F + 90.5f,
                    sr.getScaledHeight(),
                    0x77000000
            );

            Gui.drawRect(
                    sr.getScaledWidth() / 2.0F - 91 + mc.thePlayer.inventory.currentItem * 20,
                    sr.getScaledHeight() - 21,
                    sr.getScaledWidth() / 2.0F - 91 + mc.thePlayer.inventory.currentItem * 20 + 22,
                    sr.getScaledHeight(),
                    0x88000000
            );

            RoundedUtils.shadowGradient(
                    sr.getScaledWidth() / 2.0F - 91,
                    sr.getScaledHeight() - 22,
                    91 * 2,
                    sr.getScaledHeight(),
                    0,
                    10,
                    5,
                    new Color(0x77000000),
                    new Color(0x77000000),
                    new Color(0x77000000),
                    new Color(0x77000000),
                    false
            );

            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.enableGUIStandardItemLighting();

            for (int index = 0; index < 9; ++index) {
                int x = sr.getScaledWidth() / 2 - 90 + index * 20 + 2;
                int y = sr.getScaledHeight() - 19;

                mc.ingameGUI.renderHotbarItem(index, x, y, mc.timer.renderPartialTicks, mc.thePlayer);
            }

            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
        }
    };
}
