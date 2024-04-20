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
@CheckData(name = "Speed", description = "Checks for speed modifications.", type = EnumCheckType.MOVEMENT)
public class SpeedCheck extends AbstractCheck {
    @Override
    public boolean runCheck(EntityPlayer player) {
        if(!player.capabilities.isFlying) {
            String motionX = String.valueOf(player.motionX);
            if(player.isBlocking() && player.onGround && (player.motionX > 0.03F || motionX.startsWith("0.03"))) return true;
            if(player.isSneaking() && player.onGround && player.motionZ > 0.05F) return true;
            if(player.onGround && Math.round(player.motionY) != 0L) return true;
        }

        if(player.isSneaking() && player.onGround && !player.velocityChanged && player.hurtTime != 0) return true;
        return false;
    }
}
