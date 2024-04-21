package dev.revere.virago.client.modules.player;

import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.client.events.game.TickEvent;
import dev.revere.virago.client.events.packet.PacketEvent;
import dev.revere.virago.client.events.packet.TeleportEvent;
import dev.revere.virago.client.events.player.WorldChangeEvent;
import dev.revere.virago.util.Logger;
import dev.revere.virago.util.player.PlayerUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;

@ModuleData(name = "Anti Void", description = "Prevents you from falling into the void.", type = EnumModuleType.PLAYER)
public class AntiVoid extends AbstractModule {

    private List<Packet> packets = new ArrayList<>();
    private BlockPos lastGroundPosition;
    private boolean worldLoaded;
    private Vec3 position;

    @EventHandler
    private final Listener<PacketEvent> packetEvent = event -> {
        if (event.getEventState() != PacketEvent.EventState.SENDING || !(event.getPacket() instanceof C03PacketPlayer))
            return;

        C03PacketPlayer wrapper = event.getPacket();

        if (mc.thePlayer.capabilities.allowFlying || mc.thePlayer.capabilities.isCreativeMode) return;

        if (!isBlockUnder(30)) {
            if (mc.thePlayer.fallDistance < 8) {
                Logger.addChatMessage("Falling into the void, cancelling packet.");
                event.setCancelled(true);
                packets.add(event.getPacket());
            } else {
                if (!packets.isEmpty()) {
                    packets.forEach(packet -> {
                        if (position != null) {
                            mc.thePlayer.setPosition(position.xCoord, position.yCoord, position.zCoord);
                        }
                    });
                    packets.clear();
                }
            }
        } else {
            position = new Vec3(wrapper.getPositionX(), wrapper.getPositionY(), wrapper.getPositionZ());
            if (!packets.isEmpty()) {
                Logger.addChatMessage("Sending " + packets.size() + " packets.");
                packets.forEach(packet -> {
                    mc.getNetHandler().getNetworkManager().sendPacketWithoutEvent(packet);
                });
                packets.clear();
            }
        }
    };

    @EventHandler
    private final Listener<TeleportEvent> teleportEventListener = event -> {
        Logger.addChatMessage("Called teleport event.");
        worldLoaded = false;
        packets.clear();
    };

    @EventHandler
    private final Listener<WorldChangeEvent> onWorldChange = event -> {
        Logger.addChatMessage("Called world change event.");
        worldLoaded = false;
        packets.clear();
    };

    @EventHandler
    private final Listener<TickEvent> onClientTick = event -> {
        if (mc.theWorld != null && mc.thePlayer != null && mc.thePlayer.getEntityBoundingBox() != null) {
            worldLoaded = true;
        }
    };

    @Override
    public void onEnable() {
        super.onEnable();
        packets.clear();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        packets.clear();
    }

    public boolean isBlockUnder(final double height) {
        return isBlockUnder(height, true);
    }

    public boolean isBlockUnder(final double height, final boolean boundingBox) {
        if (!worldLoaded) {
            return true;
        }

        if (boundingBox) {
            for (int offset = 0; offset < height; offset += 2) {
                final AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().offset(0, -offset, 0);

                if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                    return true;
                }
            }
        } else {
            for (int offset = 0; offset < height; offset++) {
                if (PlayerUtil.blockRelativeToPlayer(0, -offset, 0).isFullBlock()) {
                    return true;
                }
            }
        }
        return false;
    }
}