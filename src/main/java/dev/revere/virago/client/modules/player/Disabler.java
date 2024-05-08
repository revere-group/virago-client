package dev.revere.virago.client.modules.player;

import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.packet.PacketEvent;
import dev.revere.virago.util.misc.TimerUtil;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Zion
 * @project Virago
 * @date 08/05/2024
 */
@ModuleData(name = "Disabler", displayName = "Disabler", description = "Disables anticheats", type = EnumModuleType.PLAYER)
public class Disabler extends AbstractModule {

    private final Setting<Boolean> inventoryMove = new Setting<>("Inventory Move", true);
    private boolean isCrafting = false;
    private TimerUtil timedOut = new TimerUtil();
    private CopyOnWriteArrayList<C0EPacketClickWindow> clickPackets = new CopyOnWriteArrayList<>();

    @EventHandler
    private final Listener<PacketEvent> packetEventListener = event -> {
        if(!inventoryMove.getValue())
            return;

        C0EPacketClickWindow clickPacket;
        C16PacketClientStatus statusPacket;

        if (event.getPacket() instanceof C16PacketClientStatus && (statusPacket = event.getPacket()).getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT) {
            event.setCancelled(true);
        }

        if(event.getPacket() instanceof C0DPacketCloseWindow) {
            C0DPacketCloseWindow closePacket = event.getPacket();
            if(closePacket.getWindowId() == 0) {
                if(isCrafting) {
                    isCrafting = false;
                }
            }
        }

        if(event.getPacket() instanceof C0EPacketClickWindow && (clickPacket = event.getPacket()).getWindowId() == 0) {
            if(isCrafting && clickPacket.getSlotId() >= 1 && clickPacket.getSlotId() <= 4) {
                this.isCrafting = true;
            }

            if(!isCrafting && clickPacket.getSlotId() == 0 && clickPacket.getClickedItem() != null) {
                this.isCrafting = false;
            }

            timedOut.reset();
            event.setCancelled(true);
            clickPackets.add(clickPacket);
        }

        boolean isDragging = false;
        if(mc.currentScreen instanceof GuiInventory && mc.thePlayer.inventory.getItemStack() != null) {
            isDragging = true;
        }

        if(mc.thePlayer.ticksExisted % 5 != 0) return;
        if(clickPackets.isEmpty()) return;
        if(isDragging) return;
        if(isCrafting) return;

        mc.getNetHandler().getNetworkManager().sendPacketWithoutEvent(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));

        Iterator<C0EPacketClickWindow> iterator = this.clickPackets.iterator();
        while (true) {
            if (!iterator.hasNext()) {
                mc.getNetHandler().getNetworkManager().sendPacketWithoutEvent(new C0DPacketCloseWindow(0));
                clickPackets.clear();
                timedOut.reset();
                return;
            }
            C0EPacketClickWindow clickWindowPacket = iterator.next();
            mc.getNetHandler().getNetworkManager().sendPacketWithoutEvent(clickWindowPacket);
        }
    };
}
