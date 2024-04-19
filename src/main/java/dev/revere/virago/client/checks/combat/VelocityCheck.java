package dev.revere.virago.client.checks.combat;

import dev.revere.virago.api.anticheat.AbstractCheck;
import dev.revere.virago.api.anticheat.CheckData;
import dev.revere.virago.api.anticheat.EnumCheckType;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/19/2024
 */
@CheckData(name = "Velocity", description = "Checks for velocity modifications.", type = EnumCheckType.COMBAT)
public class VelocityCheck extends AbstractCheck {
    @Override
    public boolean runCheck(EntityPlayer player) {
        return false;
    }
}
