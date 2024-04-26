package dev.revere.virago.util.sound;

import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;

@UtilityClass
public class SoundUtil {
    /**
     * Plays a sound.
     *
     * @param sound the sound
     */
    public void playSound(final String sound) {
        playSound(sound, 1, 1);
    }

    /**
     * Plays a sound.
     *
     * @param sound  the sound
     * @param volume the volume
     * @param pitch  the pitch
     */
    public void playSound(final String sound, final float volume, final float pitch) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.theWorld.playSound(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, sound, volume, pitch, false);
    }
}