package dev.revere.virago.client.modules.render;

import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import org.lwjgl.opengl.GL11;

/**
 * @author Remi
 * @project Virago
 * @date 3/26/2024
 */
@ModuleData(name = "MotionBlur", description = "Adds motion blur to the game", type = EnumModuleType.RENDER)
public class MotionBlur extends AbstractModule {

    public static Setting<Integer> amount = new Setting<>("Amount", 4)
            .minimum(1)
            .maximum(10)
            .incrementation(1)
            .describedBy("The amount of motion blur to apply");

    public static float value;

    public static float getMultiplier() {
        return amount.getValue() * 10;
    }

    public static float getAccumulationValue() {
        value = getMultiplier() * 10.0F;
        long lastTimestampInGame = System.currentTimeMillis();

        if (value > 996.0F) {
            value = 996.0F;
        }

        if (value > 990.0F) {
            value = 990.0F;
        }

        long i = System.currentTimeMillis() - lastTimestampInGame;

        if (i > 10000L) {
            return 0.0F;
        } else {
            if (value < 0.0F) {
                value = 0.0F;
            }

            return value / 1000.0F;
        }
    }


    public void createAccumulation() {
        if (!isEnabled()) return;
        float value = getAccumulationValue();
        GL11.glAccum(GL11.GL_MULT, value);
        GL11.glAccum(GL11.GL_ACCUM, 1.0F - value);
        GL11.glAccum(GL11.GL_RETURN, 1.0F);
    }
}
