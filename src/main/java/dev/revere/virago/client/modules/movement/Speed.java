package dev.revere.virago.client.modules.movement;

import com.google.common.eventbus.Subscribe;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.MoveEvent;
import dev.revere.virago.client.events.input.KeyDownEvent;
import dev.revere.virago.client.events.update.PreMotionEvent;
import dev.revere.virago.client.events.update.UpdateEvent;
import dev.revere.virago.util.Logger;
import net.minecraft.block.BlockSlab;
import net.minecraft.network.Packet;
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

    public final Setting<WatchdogMode> watchdogMode = new Setting<>("Watchdog Mode", WatchdogMode.HOP)
            .describedBy("How to control speed on Hypixel");

    private final Setting<Double> speedWatchdog = new Setting<>("Speed",0.8)
            .minimum(0.3D)
            .maximum(2D)
            .incrementation(0.05D)
            .describedBy("The speed you will go.")
            .visibleWhen(() -> watchdogMode.getValue() == WatchdogMode.HOP);

    private final Setting<Boolean> safeStrafe = new Setting<>("Safe Strafe", false)
            .describedBy("Whether to enable safe strafe.")
            .visibleWhen(() -> watchdogMode.getValue() == WatchdogMode.HOP);

    private final Setting<Double> strafeModifier = new Setting<>("Strafe Modifier", 0.5)
            .minimum(0.1D)
            .maximum(1.0D)
            .incrementation(0.05D)
            .describedBy("The amount to modify strafe.")
            .visibleWhen(() -> (watchdogMode.getValue() == WatchdogMode.HOP && safeStrafe.getValue()) || watchdogMode.getValue() == WatchdogMode.HOP_SMOOTH);

    private final Setting<Double> speedNoStrafe = new Setting<>("Speed (nostrafe)", 1.95)
            .minimum(1.5D)
            .maximum(2.25D)
            .incrementation(0.05D)
            .describedBy("The speed you will go.")
            .visibleWhen(() -> watchdogMode.getValue() == WatchdogMode.NO_STRAFE);

    private double speed, lastDist, baseSpeed, boostSpeed;
    private int strafeTicks, stage;

    private boolean prevOnGround, canStrafe;
    private double lastMotionX, lastMotionZ;

    private final LinkedBlockingQueue<Packet<?>> packets = new LinkedBlockingQueue<>();

    private boolean isGrounded = false;
    private int increment = 0;

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
        if(mc.thePlayer.ticksExisted <= 5)
            this.packets.clear();
    };

    @EventHandler
    private final Listener<MoveEvent> moveEventListener = event -> {
        switch (watchdogMode.getValue()) {
            case HOP:
                if (mc.thePlayer.isMoving()) {
                    speed = mc.thePlayer.getBaseMoveSpeed();
                    if (mc.thePlayer.onGround && !prevOnGround) {
                        prevOnGround = true;
                        event.setY(0.41999998688698);
                        mc.thePlayer.motionY = 0.42;
                        speed *= speedWatchdog.getValue();
                    } else if (prevOnGround) {
                        double difference = (0.76D * (lastDist - mc.thePlayer.getBaseMoveSpeed()));
                        speed = lastDist - difference;
                        prevOnGround = false;
                    } else {
                        speed = lastDist - lastDist / 159D;
                    }

                    /*if (mc.thePlayer.hurtTime > 0) {
                        speed = Math.hypot(mc.thePlayer.motionX, mc.thePlayer.motionZ) + 0.0245F;
                    }*/

                    if (safeStrafe.getValue() && !mc.thePlayer.onGround) {
                        mc.thePlayer.setSpeedWithCorrection(event, Math.max(mc.thePlayer.getSpeed(), speed), lastMotionX, lastMotionZ, strafeModifier.getValue());
                    } else {
                        mc.thePlayer.setSpeed(event, Math.max(mc.thePlayer.getSpeed(), speed));
                    }

                    lastMotionX = event.getX();
                    lastMotionZ = event.getZ();
                }
                break;

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

                    /*if (mc.thePlayer.hurtTime > 0) {
                        speed = Math.hypot(mc.thePlayer.motionX, mc.thePlayer.motionZ) + 0.0245F;
                    }*/
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
            case HOP_SMOOTH:
                if (mc.thePlayer.isMoving()) {
                    if  (mc.thePlayer.onGround) {
                        prevOnGround = true;
                        if  (mc.thePlayer.isMoving()) {
                            event.setY(0.41999998688698);
                            mc.thePlayer.motionY = 0.42;
                            speed *= 0.91;
                            speed += 0.2 + mc.thePlayer.getAIMoveSpeed();
                        }
                    } else if (prevOnGround) {
                        speed *= 0.54;
                        speed += 0.026;
                        prevOnGround = false;
                    } else {
                        speed *= 0.91;
                        speed += 0.025 +  (mc.thePlayer.getBaseMoveSpeed() - 0.2873) * 0.08;
                    }

                    //if (mc.thePlayer.hurtTime == 9) speed += 0.1;

                    if (mc.thePlayer.fallDistance < 1) {
                        mc.thePlayer.setSpeedWithCorrection(event, speed, lastMotionX, lastMotionZ, strafeModifier.getValue());
                    } else {
                        speed = mc.thePlayer.getSpeed();
                    }
                }

                lastMotionX = event.getX();
                lastMotionZ = event.getZ();
                break;
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

    public void setSpeed2(double moveSpeed) {
        setSpeed(moveSpeed, mc.thePlayer.rotationYaw, mc.thePlayer.movementInput.moveStrafe, mc.thePlayer.movementInput.moveForward);
    }

    public void setSpeed(double moveSpeed, float yaw, double strafe, double forward) {
        if (forward != 0.0D) {
            if (strafe > 0.0D) {
                yaw += ((forward > 0.0D) ? -45 : 45);
            } else if (strafe < 0.0D) {
                yaw += ((forward > 0.0D) ? 45 : -45);
            }
            strafe = 0.0D;
            if (forward > 0.0D) {
                forward = 1.0D;
            } else if (forward < 0.0D) {
                forward = -1.0D;
            }
        }
        if (strafe > 0.0D) {
            strafe = 1.0D;
        } else if (strafe < 0.0D) {
            strafe = -1.0D;
        }

        // Calculate the player's motion.
        double mx = Math.cos(Math.toRadians((yaw + 90.0F)));
        double mz = Math.sin(Math.toRadians((yaw + 90.0F)));

        // Set the player's motion.
        mc.thePlayer.motionX = forward * moveSpeed * mx + strafe * moveSpeed * mz;
        mc.thePlayer.motionZ = forward * moveSpeed * mz - strafe * moveSpeed * mx;
    }

    private enum WatchdogMode {
        HOP, HOP_SMOOTH, GROUND, NO_STRAFE
    }
}
