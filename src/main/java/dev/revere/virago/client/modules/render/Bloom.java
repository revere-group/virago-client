package dev.revere.virago.client.modules.render;

import dev.revere.virago.Virago;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.render.ShaderEvent;
import dev.revere.virago.util.render.BloomUtil;
import dev.revere.virago.util.render.RenderUtils;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.shader.Framebuffer;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/6/2024
 */
@ModuleData(name = "Bloom", description = "Adds a bloom effect to the game", type = EnumModuleType.RENDER)
public class Bloom extends AbstractModule {

    private final Setting<Boolean> bloom = new Setting<>("Bloom", true);

    private final Setting<Integer> bloomIterations = new Setting<>("Bloom Iterations", 1)
            .minimum(1)
            .maximum(5)
            .incrementation(1)
            .visibleWhen(bloom::getValue);

    private final Setting<Integer> bloomOffset = new Setting<>("Bloom Offset", 1)
            .minimum(1)
            .maximum(3)
            .incrementation(1)
            .visibleWhen(bloom::getValue);

    private Framebuffer blurFramebuffer = new Framebuffer(1, 1, false);

    public void applyBlurEffect() {
        if (bloom.getValue() && mc.thePlayer != null) {
            blurFramebuffer = RenderUtils.createFrameBuffer(blurFramebuffer);
            blurFramebuffer.framebufferClear();
            blurFramebuffer.bindFramebuffer(false);

            Virago.getInstance().getEventBus().call(new ShaderEvent());

            blurFramebuffer.unbindFramebuffer();

            BloomUtil.renderBlur(blurFramebuffer.framebufferTexture, bloomIterations.getValue(), bloomOffset.getValue());
        }
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
