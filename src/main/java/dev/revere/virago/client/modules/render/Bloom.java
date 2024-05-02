package dev.revere.virago.client.modules.render;

import dev.revere.virago.Virago;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.render.ShaderEvent;
import dev.revere.virago.util.render.BloomUtil;
import dev.revere.virago.util.render.RenderUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.shader.Framebuffer;

import java.awt.*;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/6/2024
 */
@ModuleData(name = "Bloom", displayName = "Bloom", description = "Adds a bloom effect to the game", type = EnumModuleType.RENDER)
public class Bloom extends AbstractModule {

    private final Setting<Integer> bloomIterations = new Setting<>("Bloom Iterations", 1)
            .minimum(1)
            .maximum(5)
            .incrementation(1);

    private final Setting<Integer> bloomOffset = new Setting<>("Bloom Offset", 1)
            .minimum(1)
            .maximum(3)
            .incrementation(1);

    private Framebuffer blurFramebuffer = new Framebuffer(1, 1, false);

    public void applyBlurEffect() {
        ScaledResolution sr = new ScaledResolution(mc);
        if (mc.thePlayer == null)
            return;

        blurFramebuffer = RenderUtils.createFrameBuffer(blurFramebuffer);
        blurFramebuffer.framebufferClear();
        blurFramebuffer.bindFramebuffer(false);

        Virago.getInstance().getEventBus().call(new ShaderEvent());

        if (mc.currentScreen instanceof GuiChat) {
            Gui.drawRect2(2, sr.getScaledHeight() - 14, sr.getScaledWidth() - 4, 12, Color.BLACK.getRGB());
        }

        blurFramebuffer.unbindFramebuffer();

        BloomUtil.renderBlur(blurFramebuffer.framebufferTexture, bloomIterations.getValue(), bloomOffset.getValue());
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
