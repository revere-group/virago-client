package dev.revere.virago.client.modules.movement;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.packet.PacketEvent;
import dev.revere.virago.client.events.player.MoveEvent;
import dev.revere.virago.client.events.player.PreMotionEvent;
import dev.revere.virago.client.notification.NotificationType;
import dev.revere.virago.client.services.NotificationService;
import dev.revere.virago.util.Logger;
import net.minecraft.block.BlockSlab;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import org.lwjgl.input.Keyboard;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Remi
 * @project Virago
 * @date 3/17/2024
 */

@ModuleData(name = "Speed", description = "Increases your movement speed", type = EnumModuleType.MOVEMENT)
public class Speed extends AbstractModule {

    private final Setting<WatchdogMode> watchdogMode = new Setting<>("Watchdog Mode", WatchdogMode.NO_STRAFE)
            .describedBy("How to control speed on Hypixel");

    private final Setting<Double> speedNoStrafe = new Setting<>("Speed (nostrafe)", 2.1)
            .minimum(1.5D)
            .maximum(2.25D)
            .incrementation(0.05D)
            .describedBy("The speed you will go.")
            .visibleWhen(() -> watchdogMode.getValue() == WatchdogMode.NO_STRAFE);

    private double speed;
    private double lastDist;

    private boolean prevOnGround;
    private double lastMotionX, lastMotionZ;

    public Speed() {
        setKey(Keyboard.KEY_V);
    }

    @EventHandler
    private final Listener<PreMotionEvent> preMotionEventListener = event -> {
        switch (watchdogMode.getValue()) {
            case GROUND:
                if(mc.thePlayer.onGround && !(mc.thePlayer.ticksExisted % 4 == 0) &&  !(mc.theWorld.getBlockState(mc.thePlayer.getPosition().add(0, -1, 0)).getBlock() instanceof BlockSlab)) mc.timer.timerSpeed = 4.0f;
                else mc.timer.timerSpeed = 1.0f;
                break;
        }
    };

    @EventHandler
    private final Listener<MoveEvent> moveEventListener = event -> {
        switch (watchdogMode.getValue()) {
            case NO_STRAFE:
                if (mc.thePlayer.isMoving()) {
                    if (mc.thePlayer.onGround && !prevOnGround) {
                        speed = mc.thePlayer.getBaseMoveSpeed();
                        prevOnGround = true;
                        event.setY(0.41999998688698);
                        mc.thePlayer.motionY = 0.42;
                        if(mc.thePlayer.isPotionActive(Potion.moveSpeed)) speed *= (speedNoStrafe.getValue() * 0.85);
                        else speed *= speedNoStrafe.getValue();
                    } else if (prevOnGround) {
                        double difference = (0.76D * (lastDist - mc.thePlayer.getBaseMoveSpeed()));
                        speed = lastDist - difference;
                    } else {
                        speed = lastDist - lastDist / 159D;
                    }

                    if(mc.thePlayer.onGround || prevOnGround) {
                        mc.thePlayer.setSpeed(event, Math.max(mc.thePlayer.getSpeed(), speed));
                    } else if (mc.thePlayer.hurtTime == 9) {
                        mc.thePlayer.setSpeed(event, Math.max(mc.thePlayer.getSpeed(), speed));
                    } else {
                        mc.thePlayer.setSpeedWithCorrection(event, Math.max(mc.thePlayer.getSpeed(), speed), lastMotionX, lastMotionZ, 0.1);
                    }

                    lastMotionX = event.getX();
                    lastMotionZ = event.getZ();
                    if(this.prevOnGround && !mc.thePlayer.onGround)
                        prevOnGround = false;
                } else {
                    mc.thePlayer.setSpeed(event, 0);
                }
                break;
            case TEST:
                if (mc.thePlayer.isMoving()) {
                    if (mc.thePlayer.onGround && !prevOnGround) {
                        speed = mc.thePlayer.getBaseMoveSpeed();
                        prevOnGround = true;
                        event.setY(0.41999998688698);
                        mc.thePlayer.motionY = 0.42;
                        if(mc.thePlayer.isPotionActive(Potion.moveSpeed)) speed *= (speedNoStrafe.getValue() * 0.85);
                        else speed *= speedNoStrafe.getValue();
                    } else if (prevOnGround) {
                        double lowerBound = 1.22;
                        double upperBound = 1.28;

                        double randomFactor = lowerBound + Math.random() * (upperBound - lowerBound);
                        double difference = (randomFactor * (lastDist - mc.thePlayer.getBaseMoveSpeed()));
                        speed = lastDist - difference;
                    } else {
                        speed = lastDist - lastDist / 159D;
                    }

                    if(mc.thePlayer.onGround || prevOnGround)
                        mc.thePlayer.setSpeed(event, Math.max(mc.thePlayer.getSpeed(), speed));
                    else
                        mc.thePlayer.setSpeedWithCorrection(event, Math.max(mc.thePlayer.getSpeed(), speed), lastMotionX, lastMotionZ, 0.1);

                    lastMotionX = event.getX();
                    lastMotionZ = event.getZ();
                    if(this.prevOnGround && !mc.thePlayer.onGround)
                        prevOnGround = false;
                } else {
                    mc.thePlayer.setSpeed(event, 0);
                }
                break;
        }
    };

    @EventHandler
    private final Listener<PacketEvent> packetEventListener = event -> {
        Packet packet = event.getPacket();
        if (event.getEventState() == PacketEvent.EventState.RECEIVING) {
            if (packet instanceof S08PacketPlayerPosLook) {
                Virago.getInstance().getServiceManager().getService(NotificationService.class).notify(NotificationType.ERROR, "Lagback Check", "Speed has been disabled due to lagback.");
                mc.thePlayer.onGround = false;
                mc.thePlayer.motionX *= 0;
                mc.thePlayer.motionZ *= 0;
                mc.thePlayer.jumpMovementFactor = 0;
                toggleSilent();
            }
        }
    };

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1.0F;
        super.onDisable();
    }

    private enum WatchdogMode {
        TEST, GROUND, NO_STRAFE
    }
}
