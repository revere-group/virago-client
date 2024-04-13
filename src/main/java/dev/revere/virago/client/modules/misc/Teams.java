package dev.revere.virago.client.modules.misc;

import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/13/2024
 */
@ModuleData(name = "Teams", description = "Prevent yourself from attacking your teammates", type = EnumModuleType.MISC)
public class Teams extends AbstractModule {

    public final Setting<Boolean> scoreboard = new Setting<>("Scoreboard", false).describedBy("Check the scoreboard for team members");
    public final Setting<Boolean> armor = new Setting<>("Armor", false).describedBy("Check the armor of the player");
    public final Setting<Boolean> color = new Setting<>("Color", false).describedBy("Check the armor of the player");

    public boolean isTeammate(EntityLivingBase entity) {
        if (scoreboard.getValue() && mc.thePlayer.getTeam() != null && entity.getTeam() != null && mc.thePlayer.isOnSameTeam(entity)) {
            return true;
        }

        if (armor.getValue() && entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;

            if (mc.thePlayer.inventory.armorInventory[3] != null && player.inventory.armorInventory[3] != null) {
                ItemStack myHead = mc.thePlayer.inventory.armorInventory[3];
                ItemArmor myItemArmor = (ItemArmor) myHead.getItem();

                ItemStack entityHead = player.inventory.armorInventory[3];
                ItemArmor entityItemArmor = (ItemArmor) entityHead.getItem();

                if (myItemArmor.getColor(myHead) == entityItemArmor.getColor(entityHead)) {
                    return true;
                }
            }
        }

        if (color.getValue() && mc.thePlayer.getDisplayName() != null && entity.getDisplayName() != null) {
            String targetName = entity.getDisplayName().getFormattedText().replace("§r", "");
            String clientName = mc.thePlayer.getDisplayName().getFormattedText().replace("§r", "");

            return targetName.startsWith(String.format("§%s", clientName));
        }

        return false;
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
