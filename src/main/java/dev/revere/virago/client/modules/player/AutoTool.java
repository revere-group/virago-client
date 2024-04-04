package dev.revere.virago.client.modules.player;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.client.events.update.PreMotionEvent;
import dev.revere.virago.client.modules.combat.KillAura;
import dev.revere.virago.client.services.ModuleService;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.BlockPos;

@ModuleData(name = "AutoTool", description = "Automatically switch to most useful tool", type = EnumModuleType.PLAYER)
public class AutoTool extends AbstractModule {

    float bestStr = 0.0f;
    int slot = -1;

    @EventHandler
    private final Listener<PreMotionEvent> onPreMotion = event -> {
        KillAura aura = Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(KillAura.class);

        if(aura.isEnabled() && aura.getSingleTarget() != null) {
            System.out.println("Step 1");

            for(int i = 0; i < 9; i++) {
                ItemStack stack = mc.thePlayer.inventory.mainInventory[i];

                if(stack == null && !(stack.getItem() instanceof ItemSword)) {
                    System.out.println("Step 2");
                    continue;
                }

                System.out.println("nigger1");

                ItemSword sword = (ItemSword) stack.getItem();

                if(sword.attackDamage < bestStr) {
                    System.out.println("Step 3");
                    continue;
                }

                bestStr = sword.attackDamage;
                slot = i;

                System.out.println(slot);
                mc.thePlayer.inventory.currentItem = slot;
            }
            return;
        }

        if(!mc.gameSettings.keyBindAttack.pressed || mc.objectMouseOver == null) {
            return;
        }

        BlockPos pos = mc.objectMouseOver.getBlockPos();
        if(pos == null) {
            return;
        }

        int itemToUse = this.getBestToolSlot(pos);
        if(itemToUse == -1) {
            return;
        }

        mc.thePlayer.inventory.currentItem = itemToUse;
    };

    private int getBestToolSlot(BlockPos pos) {
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        float bestStr = 1.0f;
        int itemToUse = -1;

        for(int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if(stack == null || !(stack.getStrVsBlock(block) > bestStr)) {
                continue;
            }

            bestStr = stack.getStrVsBlock(block);
            itemToUse = i;
        }

        return itemToUse;
    }



}
