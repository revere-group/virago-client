package dev.revere.virago.client.modules.player;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.player.PreMotionEvent;
import dev.revere.virago.client.events.player.UpdateEvent;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.util.misc.TimerUtil;
import dev.revere.virago.util.rotation.MathUtil;
import net.minecraft.item.Item;
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
    private long nextUse;
    private final ModuleService moduleService = Virago.getInstance().getServiceManager().getService(ModuleService.class);

    private final Setting<Integer> minHealth = new Setting<>("Minimum Health", 12)
            .maximum(20)
            .minimum(2)
            .incrementation(1);

    private final Setting<Integer> maximumDelay = new Setting<>("Maximum Delay", 30)
            .maximum(500)
            .minimum(0)
            .incrementation(5);

    private final Setting<Integer> minimumDelay = new Setting<>("Minimum Delay", 50)
            .maximum(500)
            .minimum(0)
            .incrementation(5);

    @EventHandler
    private final Listener<PreMotionEvent> preMotionEventListener = event -> {
        if(moduleService.getModule(Scaffold.class).isEnabled())
            return;

        for(int i = 0; i < 9; i++) {
            final ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);

            if(stack == null)
                continue;

            final Item item = stack.getItem();
            if(!(item instanceof ItemSkull)) continue;
            if(mc.thePlayer.getHealth() > minHealth.getValue()) continue;

            setSlot(i);
        }
    };


    private void setSlot(int slot) {
        if(slot < 0 || slot > 8)
            return;

        final int oldSlot = mc.thePlayer.inventory.currentItem;
        mc.thePlayer.inventory.currentItem = slot;

        if(timer.hasTimeElapsed(nextUse)) {
            mc.getNetHandler().getNetworkManager().sendPacketWithoutEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
            nextUse = Math.round(MathUtil.getRandom(minimumDelay.getValue(), maximumDelay.getValue()));
            timer.reset();
        }

        mc.thePlayer.inventory.currentItem = oldSlot;
    }
}
