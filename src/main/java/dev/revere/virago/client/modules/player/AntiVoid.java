package dev.revere.virago.client.modules.player;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.client.events.packet.PacketEvent;
import dev.revere.virago.client.events.packet.TeleportEvent;
import dev.revere.virago.client.events.player.PreMotionEvent;
import dev.revere.virago.client.events.player.WorldChangeEvent;
import dev.revere.virago.util.Logger;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C18PacketSpectate;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@ModuleData(name = "Anti Void", description = "Prevents you from falling into the void.", type = EnumModuleType.PLAYER)
public class AntiVoid extends AbstractModule {

    private List<Packet> packets = new ArrayList<>();
    private Vec3 position;
    private boolean overVoid;
    private boolean blinking;

    @EventHandler
    private final Listener<PacketEvent> packetEvent = event -> {
        C03PacketPlayer wrapper = event.getPacket();

        if (!isBlockUnder()) {
            Logger.addChatMessage("We are over the void.");
            blinking = true;
            overVoid = true;

            if (position != null && mc.thePlayer.fallDistance > 10) {
                Logger.addChatMessage("Teleporting to position.");
                mc.thePlayer.setPosition(position.xCoord, position.yCoord, position.zCoord);

                mc.thePlayer.motionY = (0 - 0.08) * 0.98F;
                mc.thePlayer.motionX = 0;
                mc.thePlayer.motionZ = 0;

                Iterator<Packet> iterator = packets.iterator();
                while (iterator.hasNext()) {
                    Logger.addChatMessage("Sending packets.");
                    Packet packet = iterator.next();
                    mc.thePlayer.sendQueue.getNetworkManager().sendPacketWithoutEvent(packet);
                    iterator.remove();
                }

                blinking = false;
                packets.clear();
            }
        } else {
            if (blinking) {
                Iterator<Packet> iterator = packets.iterator();
                while (iterator.hasNext()) {
                    Logger.addChatMessage("Sending packets.");
                    Packet packet = iterator.next();
                    mc.thePlayer.sendQueue.getNetworkManager().sendPacketWithoutEvent(packet);
                    iterator.remove();
                }
                event.setCancelled(false);
                blinking = false;
            }
            if (overVoid) {
                Logger.addChatMessage("set over void and blinking to false.");
                overVoid = false;
                blinking = false;
            }


            position = new Vec3(wrapper.getPositionX(), wrapper.getPositionY(), wrapper.getPositionZ());
            Logger.addChatMessage("we dont cancel here");
        }
    };

    @EventHandler
    private final Listener<PacketEvent> onPacket = event -> {
        if (mc.thePlayer == null) return;

        if (event.getEventState() == PacketEvent.EventState.SENDING && event.getPacket() instanceof C03PacketPlayer && blinking) {
            Logger.addChatMessage("Cancelling packet.");
            event.setCancelled(true);
            this.packets.add(event.getPacket());
        }
    };

    @EventHandler
    private final Listener<WorldChangeEvent> onWorldChange = event -> {
        blinking = false;
    };

    @EventHandler
    private final Listener<TeleportEvent> onTeleport = event -> {

    };

    @Override
    public void onEnable() {
        super.onEnable();
        blinking = false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        blinking = false;
    }

    private boolean isBlockUnder() {
        for (int offset = (int) mc.thePlayer.posY; offset > 0; offset -= 1) {
            AxisAlignedBB boundingBox = mc.thePlayer.getEntityBoundingBox().offset(0, -(mc.thePlayer.posY - offset), 0);

            if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, boundingBox).isEmpty())
                return true;
        }
        return false;
    }
}