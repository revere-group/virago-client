package dev.revere.virago.client.modules.player;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.client.events.packet.PacketEvent;
import dev.revere.virago.client.events.packet.TeleportEvent;
import dev.revere.virago.client.events.player.PostMotionEvent;
import dev.revere.virago.client.events.player.PreMotionEvent;
import dev.revere.virago.client.modules.combat.KillAura;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.util.Logger;
import net.minecraft.block.BlockChest;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/7/2024
 */
@ModuleData(name = "No Slow", description = "Prevents the player from slowing down when using items", type = EnumModuleType.PLAYER)
public class NoSlow extends AbstractModule {

    private boolean isUsingItem;

    @EventHandler
    private final Listener<PreMotionEvent> preMotionEventListener = event -> {
        if (mc.thePlayer.isUsingItem() && !(mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) && !(mc.thePlayer.getHeldItem().getItem() instanceof ItemBow)) {
            event.setPitch(90);
            mc.thePlayer.rotationPitchHead = 90;
        }
    };

    @EventHandler
    private final Listener<PostMotionEvent> postMotionEventListener = event -> {
        KillAura killAura = Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(KillAura.class);
        if (mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword && killAura.getSingleTarget() == null) {
            isUsingItem = true;
        }
        if (isUsingItem) {
            mc.getNetHandler().addToSendQueueNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
            mc.getNetHandler().addToSendQueueNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            isUsingItem = false;
        }
    };

    @EventHandler
    private final Listener<TeleportEvent> teleportEventListener = event -> {
        isUsingItem = false;
    };

    @EventHandler
    private final Listener<PacketEvent> packetEventListener = event -> {
        if (event.getEventState() == PacketEvent.EventState.SENDING && mc.thePlayer != null) {
            if (event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
                KillAura killAura = Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(KillAura.class);
                if (event.getPacket() instanceof C08PacketPlayerBlockPlacement && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword && (mc.objectMouseOver.getBlockPos() != null && mc.theWorld.getBlockState(mc.objectMouseOver.getBlockPos()) instanceof BlockChest)) {
                    event.setCancelled(true);
                } else if (event.getPacket() instanceof C07PacketPlayerDigging && killAura.getSingleTarget() != null) {
                    C07PacketPlayerDigging c07 = event.getPacket();
                    if (c07.getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM)
                        isUsingItem = true;
                }
            }
        }
    };

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
