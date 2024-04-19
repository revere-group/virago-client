package dev.revere.virago.api.anticheat;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/19/2024
 */
@Getter
public abstract class AbstractCheck {

    private final String checkName;
    private final EnumCheckType checkType;
    private final String checkDescription;

    public AbstractCheck() {
        CheckData checkData = getClass().getAnnotation(CheckData.class);
        this.checkName = checkData.name();
        this.checkType = checkData.type();
        this.checkDescription = checkData.description();
    }

    /**
     * Run check for a player.
     *
     * @param player the player
     * @return the return value
     */
    public abstract boolean runCheck(EntityPlayer player);
}
