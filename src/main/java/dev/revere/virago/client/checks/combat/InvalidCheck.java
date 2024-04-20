package dev.revere.virago.client.checks.combat;

import dev.revere.virago.api.anticheat.AbstractCheck;
import dev.revere.virago.api.anticheat.CheckData;
import dev.revere.virago.api.anticheat.EnumCheckType;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author Zion
 * @project Virago-Client
 * @date 4/19/2024
 */
@CheckData(name = "Invalid", description = "Checks for invalid rotations", type = EnumCheckType.COMBAT)
public class InvalidCheck extends AbstractCheck {
    @Override
    public boolean runCheck(EntityPlayer player) {
        if(player.rotationPitch > 90.0F) {
            return true;
        } else {
            return Math.round(player.motionX) > 5.0F || Math.round(player.motionY) > 5.0F || Math.round(player.motionZ) > 5.0F;
        }
    }
}
