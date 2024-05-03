package dev.revere.virago.client.modules.player;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.player.PreMotionEvent;
import dev.revere.virago.client.modules.combat.KillAura;
import dev.revere.virago.client.services.ModuleService;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.BlockPos;

@ModuleData(name = "AutoTool", displayName = "Auto Tool", description = "Automatically switch to most useful tool", type = EnumModuleType.PLAYER)
public class AutoTool extends AbstractModule {

    @EventHandler
    private final Listener<PreMotionEvent> onPreMotion = event -> {
        KillAura aura = Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(KillAura.class);
        if (mc.gameSettings.keyBindAttack.isKeyDown() && mc.objectMouseOver != null) {
            BlockPos pos = mc.objectMouseOver.getBlockPos();
            updateTool(pos);
        }
    };

    /**
     * Updates the player's current item to the best tool for the block at the given position
     *
     * @param pos the position of the block
     */
    private void updateTool(BlockPos pos) {
        int itemToUse = this.getBestToolSlot(pos);
        if (itemToUse == -1) {
            return;
        }

        mc.thePlayer.inventory.currentItem = itemToUse;
    }

    /**
     * Gets the best tool slot for the block at the given position
     *
     * @param pos the position of the block
     * @return the best tool slot
     */
    private int getBestToolSlot(BlockPos pos) {
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        float bestStr = 1.0f;
        int itemToUse = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack == null || !(stack.getStrVsBlock(block) > bestStr)) {
                continue;
            }

            bestStr = stack.getStrVsBlock(block);
            itemToUse = i;
        }

        return itemToUse;
    }
}
