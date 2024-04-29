package dev.revere.virago.client.modules.player;

import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.player.UpdateEvent;
import dev.revere.virago.util.misc.TimerUtil;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

@ModuleData(name = "AutoHead" ,displayName = "Auto Head", description = "Use golden heads and golden apples automatically", type = EnumModuleType.PLAYER)
public class AutoHead extends AbstractModule {

    private final TimerUtil timer = new TimerUtil();

    private final Setting<Integer> minHealth = new Setting<>("Minimum Health", 12)
            .maximum(20)
            .minimum(2)
            .incrementation(1);

    private final Setting<Integer> delay = new Setting<>("Delay", 50)
            .maximum(500)
            .minimum(0)
            .incrementation(5);

    @EventHandler
    private final Listener<UpdateEvent> onUpdate = event -> {
      if(mc.thePlayer.getHealth() < minHealth.getValue() && timer.hasTimeElapsed(delay.getValue())) {
          useHead();
      }

      timer.reset();
    };

    private void useHead() {
        int index;
        int item = -1;
        boolean found = false;

        ItemStack stack;

        for(index = 36; index < 45; index++) {
            stack = mc.thePlayer.inventoryContainer.getSlot(index).getStack();
            if(stack == null || !(stack.getItem() instanceof ItemSkull) && !(stack.getItem() instanceof ItemAppleGold))
                continue;

            item = index;
            found = true;
            break;
        }

        if(!found) return;

        final int slot = mc.thePlayer.inventory.currentItem;
        mc.thePlayer.inventory.currentItem = item - 36;
        mc.playerController.updateController();

        mc.getNetHandler().getNetworkManager().sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
        mc.getNetHandler().getNetworkManager().sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
        mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));

        mc.thePlayer.stopUsingItem();
        mc.thePlayer.inventory.currentItem = slot;
    }
}
