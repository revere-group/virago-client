package dev.revere.virago.client.modules.render;

import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;

/**
 * @author Remi
 * @project Virago
 * @date 3/25/2024
 */
@ModuleData(name = "Animation", displayName = "Animation", description = "Allows you to animate blocks", type = EnumModuleType.RENDER)
public class BlockAnimations extends AbstractModule {

    public final Setting<Animation> animation = new Setting<>("Animation", Animation.EXHI_TAP)
            .describedBy("The block animation");

    public final Setting<SwingAnimation> swing = new Setting<>("Swing", SwingAnimation.NORMAL)
            .describedBy("The swing animation");

    public final Setting<Double> speed = new Setting<>("Slowdown", 1.2)
            .minimum(0.1)
            .maximum(3.5)
            .incrementation(0.01)
            .describedBy("Change the speed of the animation.");

    public enum Animation {
        OLD,
        ASTOLFO,
        CHILL,
        EXHI_TAP,
        SLIDE,
        SWING
    }

    public enum SwingAnimation {
        SMOOTH,
        NORMAL
    }
}
