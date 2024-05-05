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
import dev.revere.virago.client.events.player.UpdateEvent;
import dev.revere.virago.client.events.player.WorldChangeEvent;
import dev.revere.virago.client.notification.NotificationType;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.client.services.NotificationService;
import dev.revere.virago.util.player.PlayerUtil;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Zion
 * @project Virago
 * @date 05/05/2024
 */

@ModuleData(name = "AlphaNoFall", displayName = "Alpha No Fall", description = "Testing", type = EnumModuleType.PLAYER)
public class AlphaNoFall extends AbstractModule {
    private final List<Packet> regularPackets = Collections.synchronizedList(new ArrayList<>());
    private final List<Packet> fallPackets = Collections.synchronizedList(new ArrayList<>());

    private boolean blinking;
    private float distance;
    private float lastDistance;

    @EventHandler
    private final Listener<PreMotionEvent> onPreMotion = event -> {
        final float fallDistance = mc.thePlayer.fallDistance;

        if (fallDistance == 0) {
            distance = 0;
        }

        distance += fallDistance - lastDistance;
        lastDistance = fallDistance;

        if ((!canFall() && mc.isIntegratedServerRunning()) || mc.thePlayer.capabilities.isFlying) return;
        if (distance > 2.5) {
            blinking = true;
        } else {
            if (blinking) {
                this.fallPackets.forEach(packet -> {
                    this.fallPackets.remove(packet);
                    mc.thePlayer.sendQueue.getNetworkManager().sendPacketWithoutEvent(packet);
                });

                blinking = false;
                Virago.getInstance().getServiceManager().getService(NotificationService.class).notify(NotificationType.INFO, "No Fall", "Blinked whilst falling down " + Math.round(distance) + " blocks.");
            }
        }
    };


    @EventHandler
    private final Listener<PacketEvent> onPacket = event -> {
      if(event.getEventState() != PacketEvent.EventState.SENDING || !blinking)
          return;

      Packet packet = event.getPacket();

      if(event.getPacket() instanceof C03PacketPlayer) {
          regularPackets.add(packet);
          C03PacketPlayer fallPacket = event.getPacket();

          switch(packet.getClass().getName().substring(0, 3)) {
              case "C03":
                  fallPackets.add(new C03PacketPlayer(true));
                  break;
              case "C04":
                  fallPackets.add(new C03PacketPlayer.C04PacketPlayerPosition(fallPacket.getPositionX(), fallPacket.getPositionY(), fallPacket.getPositionZ(), true));
                  break;
              case "C05":
                  fallPackets.add(new C03PacketPlayer.C05PacketPlayerLook(fallPacket.getYaw(), fallPacket.getPitch(), true));
                  break;
              case "C06":
                  fallPackets.add(new C03PacketPlayer.C06PacketPlayerPosLook(fallPacket.getPositionX(), fallPacket.getPositionY(), fallPacket.getPositionZ(), fallPacket.getYaw(), fallPacket.getPitch(), true));
                  break;
          }

          event.setCancelled(true);
      }
    };

    /**
     * Checks if the player can fall
     *
     * @return if the player can fall
     */
    private boolean canFall() {
        return mc.thePlayer.isEntityAlive() && isBlockUnder() && mc.theWorld != null && !mc.thePlayer.isOnLadder() && !mc.thePlayer.isInWater() && !mc.thePlayer.isInLava();
    }

    /**
     * Checks if there is a block under the player
     *
     * @return If there is a block under the player
     */
    private boolean isBlockUnder() {
        for (int offset = 0; offset < mc.thePlayer.posY + mc.thePlayer.getEyeHeight(); offset += 2) {
            BlockPos blockPos = new BlockPos(mc.thePlayer.posX, offset, mc.thePlayer.posZ);

            if (mc.theWorld.getBlockState(blockPos).getBlock() != Blocks.air) {
                return true;
            }
        }
        return false;
    }
}
