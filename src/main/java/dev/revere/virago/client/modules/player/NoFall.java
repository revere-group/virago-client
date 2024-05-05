package dev.revere.virago.client.modules.player;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.packet.PacketEvent;
import dev.revere.virago.client.events.player.PreMotionEvent;
import dev.revere.virago.client.events.render.Render2DEvent;
import dev.revere.virago.client.notification.NotificationType;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.client.services.NotificationService;
import dev.revere.virago.util.Logger;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/7/2024
 */
@ModuleData(name = "NoFall", displayName = "No Fall", description = "Prevents fall damage", type = EnumModuleType.PLAYER)
public class NoFall extends AbstractModule {

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.BLINK);

    private final List<Packet<?>> packets = new CopyOnWriteArrayList<>();
    private final List<Vec3> vectors = new ArrayList<>();
    private boolean isBlinking;

    private float distance;
    private float lastDistance;

    @EventHandler
    private final Listener<PreMotionEvent> preMotionEventListener = event -> {
        final float fallDistance = mc.thePlayer.fallDistance;

        if (fallDistance == 0) {
            distance = 0;
        }

        distance += fallDistance - lastDistance;
        lastDistance = fallDistance;

        switch (mode.getValue()) {
            case BLINK: {
                if ((!canFall() && mc.isIntegratedServerRunning()) || mc.thePlayer.capabilities.isFlying) return;
                if (distance > 2.5 && distance < 30) {
                    this.isBlinking = true;

                    if (!this.packets.isEmpty()) {
                        event.setGround(true);

                        double posX = mc.thePlayer.posX;
                        double posY = mc.thePlayer.posY;
                        double posZ = mc.thePlayer.posZ;

                        if (posX != mc.thePlayer.lastTickPosX
                                || posY != mc.thePlayer.lastTickPosY
                                || posZ != mc.thePlayer.lastTickPosZ) {
                            this.vectors.add(new Vec3(posX, posY, posZ));
                        }
                    }
                } else {
                    if (this.isBlinking) {
                        this.packets.forEach(packet -> {
                            this.packets.remove(packet);
                            mc.thePlayer.sendQueue.getNetworkManager().sendPacketWithoutEvent(packet);
                        });

                        this.vectors.clear();
                        this.isBlinking = false;
                        Virago.getInstance().getServiceManager().getService(NotificationService.class).notify(NotificationType.INFO, "No Fall", "Blinked whilst falling down " + Math.round(distance) + " blocks.");
                    }
                }
                break;
            }
            case POSITION: {
                if (distance > 3.5 && !(relativeBlock(0, predictedMotion(mc.thePlayer.motionY), 0) instanceof BlockAir) && mc.thePlayer.ticksSinceTeleport > 50) {
                    mc.thePlayer.sendQueue.getNetworkManager().sendPacketWithoutEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 50 - Math.random(), mc.thePlayer.posZ, false));
                    distance = 0;
                }
                break;
            }
        }
    };

    @EventHandler
    private final Listener<PacketEvent> packetEventListener = event -> {
        switch (mode.getValue()) {
            case BLINK: {
                if (mc.thePlayer == null) return;
                if (!this.isBlinking) return;
                if (event.getEventState() == PacketEvent.EventState.SENDING && event.getPacket() instanceof C03PacketPlayer) {
                    C03PacketPlayer packet = event.getPacket();
                    switch (event.getPacket().getClass().getSimpleName().substring(0, 3)) {
                        case "C03":
                            packets.add(new C03PacketPlayer(true));
                            break;
                        case "C04":
                            packets.add(new C03PacketPlayer.C04PacketPlayerPosition(packet.getPositionX(), packet.getPositionY(), packet.getPositionZ(), true));
                            break;
                        case "C05":
                            packets.add(new C03PacketPlayer.C05PacketPlayerLook(packet.getYaw(), packet.getPitch(), true));
                            break;
                        case "C06":
                            packets.add(new C03PacketPlayer.C06PacketPlayerPosLook(packet.getPositionX(), packet.getPositionY(), packet.getPositionZ(), packet.getYaw(), packet.getPitch(), true));
                            break;
                    }
                    event.setCancelled(true);
                }
                break;
            }
            case POSITION: {
                if (event.getEventState() == PacketEvent.EventState.RECEIVING) {
                    if (event.getPacket() instanceof S08PacketPlayerPosLook) {
                        mc.thePlayer.ticksSinceTeleport = 0;
                    }
                }
                break;
            }
        }
    };

    @EventHandler
    private final Listener<Render2DEvent> render2DEventListener = event -> {
        FontService font = Virago.getInstance().getServiceManager().getService(FontService.class);
        ScaledResolution sr = new ScaledResolution(mc);
        switch (mode.getValue()) {
            case BLINK:
                if (this.isBlinking) {
                    font.getSfProTextRegular().drawString("Blinking for " + Math.round(distance) + " blocks.", (float) sr.getScaledWidth() / 2, (float) sr.getScaledHeight() / 2, -1);
                }
                break;
        }
    };

    /**
     * Checks if there is a block under the player
     *
     * @return if there is a block under the player
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

    /**
     * Gets the block at the relative position
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return the block at the relative position
     */
    private Block relativeBlock(double x, double y, double z) {
        return mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).add(x, y, z)).getBlock();
    }

    /**
     * Checks if the player can fall
     *
     * @return if the player can fall
     */
    private boolean canFall() {
        return mc.thePlayer.isEntityAlive() && isBlockUnder() && mc.theWorld != null && !mc.thePlayer.isOnLadder() && !mc.thePlayer.isInWater() && !mc.thePlayer.isInLava();
    }

    /**
     * Predicts the player's motion
     *
     * @param motion the motion
     * @return the predicted motion
     */
    public double predictedMotion(final double motion) {
        return (motion - 0.08) * 0.98F;
    }

    @Override
    public void onEnable() {
        this.vectors.clear();
        this.packets.clear();
        this.isBlinking = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    enum Mode {
        BLINK,
        POSITION
    }
}
