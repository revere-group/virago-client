package dev.revere.virago.client.modules.player;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.client.events.game.TickEvent;
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
import java.util.List;

@ModuleData(name = "Anti Void", description = "Prevents you from falling into the void.", type = EnumModuleType.PLAYER)
public class AntiVoid extends AbstractModule {

    private List<Packet> packets = new ArrayList<>();
    private Vec3 position;
    private boolean overVoid;
    private boolean blinking;
    private boolean worldLoaded;


    @EventHandler
    private final Listener<PacketEvent> packetEvent = event -> {
        if(!(event.getEventState() == PacketEvent.EventState.SENDING))
            return;

        C03PacketPlayer wrapper = event.getPacket();

        if(!isBlockUnder()) {
            Logger.addChatMessage("we just ran this code and set it to true.");
            blinking = true;
            overVoid = true;

            Logger.addChatMessage(String.valueOf(mc.thePlayer.fallDistance));

            if(position != null && mc.thePlayer.fallDistance > 10) {
                mc.thePlayer.setPosition(position.xCoord, position.yCoord, position.zCoord);
                Logger.addChatMessage("sent position packet.");

                mc.thePlayer.motionY = (0 - 0.08) * 0.98F;
                mc.thePlayer.motionX = 0;
                mc.thePlayer.motionZ = 0;

                this.packets.forEach(packet -> {
                    Logger.addChatMessage("Released packet.");
                    this.packets.remove(packet);
                    mc.thePlayer.sendQueue.getNetworkManager().sendPacketWithoutEvent(packet);
                });

                blinking = false;
                packets.clear();
            }
        } else {
            if(overVoid) {
                overVoid = false;
                blinking = false;
            }

            position = new Vec3(wrapper.getPositionX(), wrapper.getPositionY(), wrapper.getPositionZ());
        }
    };

    @EventHandler
    private final Listener<PacketEvent> onPacket = event -> {
        if(mc.thePlayer == null) return;
        if(!blinking) return;

        if (event.getEventState() == PacketEvent.EventState.SENDING && event.getPacket() instanceof C03PacketPlayer) {
            Logger.addChatMessage(String.valueOf(blinking));
            Logger.addChatMessage("packet was sent and we blinked it.");
            event.setCancelled(true);
            this.packets.add(event.getPacket());
        }
    };

    @EventHandler
    private final Listener<WorldChangeEvent> onWorldChange = event -> {
        Logger.addChatMessage("Called world change event.");
        worldLoaded = false; // Reset the worldLoaded flag on world change
        blinking = false;
        packets.clear();
    };

    @EventHandler
    private final Listener<TickEvent> onClientTick = event -> {
        if (mc.theWorld != null && mc.thePlayer != null && mc.thePlayer.getEntityBoundingBox() != null) {
            worldLoaded = true; // Set worldLoaded to true once the world has loaded
        }
    };

    @Override
    public void onEnable() {
        super.onEnable();
        blinking = false;
        packets.clear();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        blinking = false;
        packets.clear();
    }



    private boolean isBlockUnder() {
        // Check if the world is still loading
        if (!worldLoaded) {
            return true; // Assume there's a block under when world is still loading
        }

        for (int offset = (int) mc.thePlayer.posY; offset > 0; offset -= 1) {
            AxisAlignedBB boundingBox = mc.thePlayer.getEntityBoundingBox().offset(0, -(mc.thePlayer.posY - offset), 0);

            if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, boundingBox).isEmpty())
                return true;
        }
        return false;
    }


}