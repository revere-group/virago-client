package dev.revere.virago.util.sound;

import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;

@UtilityClass
public class SoundUtil {
    public void playSound(final String sound) {
        playSound(sound, 1, 1);
    }

    public void playSound(final String sound, final float volume, final float pitch) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.theWorld.playSound(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, sound, volume, pitch, false);
    }
}