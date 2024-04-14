package dev.revere.virago.client.modules.player;

import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.client.events.packet.PacketEvent;
import dev.revere.virago.client.events.player.PreMotionEvent;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C18PacketSpectate;
import net.minecraft.util.AxisAlignedBB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ModuleData(name = "Anti Void", description = "Prevents you from falling into the void.", type = EnumModuleType.PLAYER)
public class AntiVoid extends AbstractModule {

    private List<Packet> packets = new ArrayList<>();
    private double flyHeight;
    private boolean flagged;

    @EventHandler
    private final Listener<PreMotionEvent> onPreMotion = event -> {
        updateFlyHeight();
        if(mc.thePlayer.isCollidedHorizontally) {
            flagged = false;
        }

        if(flyHeight > 40 && mc.thePlayer.fallDistance > 0 && !packets.isEmpty() && !flagged && mc.thePlayer.motionY < 0) {
            Collections.reverse(packets);
            mc.getNetHandler().getNetworkManager().sendPacketWithoutEvent(new C18PacketSpectate(mc.thePlayer.getUniqueID()));

            for(Packet packet : packets) {
                mc.getNetHandler().getNetworkManager().sendPacketWithoutEvent(packet);
            }

            packets.clear();
        }
    };

    @EventHandler
    private final Listener<PacketEvent> onPacket = event -> {
      if(event.getEventState() == PacketEvent.EventState.SENDING) {
          Packet packet = event.getPacket();

          if(packet instanceof C03PacketPlayer) {
              if(!(flyHeight > 40) || !(mc.thePlayer.fallDistance > 0)) {
                  packets.add(packet);

                  if(packets.size() > 5)
                      packets.remove(0);
              } else if(!isBlockUnder() && mc.thePlayer.motionY < 0) {
                  event.setCancelled(true);
              }
          }
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

    public void updateFlyHeight() {
        double h = 1.0D;
        AxisAlignedBB box = mc.thePlayer.getEntityBoundingBox().expand(0.0625D, 0.0625D, 0.0625D);

        for (this.flyHeight = 0.0D; this.flyHeight < mc.thePlayer.posY; this.flyHeight += h) {
            AxisAlignedBB nextBox = box.offset(0.0D, -this.flyHeight, 0.0D);
            if (mc.theWorld.checkBlockCollision(nextBox)) {
                if (h < 0.0625D) {
                    break;
                }

                this.flyHeight -= h;
                h /= 2.0D;
            }
        }
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
