package dev.revere.virago.client.modules.combat;

import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.player.PreMotionEvent;
import dev.revere.virago.util.misc.TimerUtil;
import dev.revere.virago.util.rotation.RotationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;

/**
 * @author Remi
 * @project Virago
 * @date 5/1/2024
 */
@ModuleData(name = "AimAssist", displayName = "Aim Assist", description = "Assists in aiming at players.", type = EnumModuleType.COMBAT)
public class AimAssist extends AbstractModule {

    private final Setting<Integer> range = new Setting<>("Range", 4)
            .minimum(1)
            .maximum(6)
            .incrementation(1)
            .describedBy("The range of the aim assist.");

    private final Setting<Integer> speed = new Setting<>("Speed", 1)
            .minimum(1)
            .maximum(20)
            .incrementation(1)
            .describedBy("The speed of the aim assist.");


    private final Setting<Double> jitterRange = new Setting<>("Jitter Range", 0.1)
            .minimum(0.05)
            .maximum(1.0)
            .incrementation(0.05)
            .describedBy("The range for randomization of aim jitter.");

    private final Setting<Boolean> vertical = new Setting<>("Vertical", false);

    private final TimerUtil timer = new TimerUtil();
    private Entity target;

    @EventHandler
    private final Listener<PreMotionEvent> preMotionEventListener = event -> {
        updateMetaData();
        handleAimAssist();
    };

    private void handleAimAssist() {
        if (!canAttack(target)) {
            target = getClosestTarget(range.getValue());
        }

        if (target == null) {
            timer.reset();
            return;
        }

        if (Mouse.isButtonDown(0)) {
            lookAtTarget(target, speed.getValue(), jitterRange.getValue());
            if (vertical.getValue()) {
                applyVerticalJitter();
            }
        }

        timer.reset();
    }

    /**
     * Updates the metadata
     */
    private void updateMetaData() {
        setMetaData("S: " + speed.getValue());
    }

    /**
     * Looks at the target entity with a given speed and jitter range
     *
     * @param entity     the entity to look at
     * @param speed      the speed to look at the entity
     * @param jitterRange the range of jitter
     */
    private void lookAtTarget(Entity entity, int speed, double jitterRange) {
        float[] rotations = RotationUtil.getRotations(entity);
        float deltaYaw = limitAngleChange(mc.thePlayer.rotationYaw, rotations[0], speed);
        float deltaPitch = limitAngleChange(mc.thePlayer.rotationPitch, rotations[1], speed);

        deltaYaw += (float) getRandomInRange(-jitterRange, jitterRange);
        deltaPitch += (float) getRandomInRange(-jitterRange, jitterRange);

        mc.thePlayer.rotationYaw = deltaYaw;
        if (vertical.getValue()) {
            mc.thePlayer.rotationPitch = deltaPitch;
        }
    }

    /**
     * Applies vertical jitter to the player
     */
    private void applyVerticalJitter() {
        mc.thePlayer.rotationPitch += (float) getRandomInRange(-jitterRange.getValue(), jitterRange.getValue());
        mc.thePlayer.rotationPitch = MathHelper.clamp_float(mc.thePlayer.rotationPitch, -90.0F, 90.0F);
    }

    /**
     * Gets a random number in a range
     *
     * @param min the minimum number
     * @param max the maximum number
     * @return a random number in a range
     */
    private double getRandomInRange(double min, double max) {
        return Math.random() * (max - min) + min;
    }

    /**
     * Limits the angle change
     *
     * @param current  the current angle
     * @param intended the intended angle
     * @param speed    the speed of the angle change
     * @return the limited angle change
     */
    private float limitAngleChange(float current, float intended, int speed) {
        float change = intended - current;
        if (change > speed) {
            change = speed;
        } else if (change < -speed) {
            change = -speed;
        }
        return current + change;
    }

    /**
     * Gets the closest target
     *
     * @param range the range to get the target
     * @return the closest target
     */
    private EntityLivingBase getClosestTarget(double range) {
        EntityLivingBase target = null;
        double minDistanceSquared = Double.MAX_VALUE;

        for (Entity object : mc.theWorld.loadedEntityList) {
            if (object instanceof EntityLivingBase) {
                EntityLivingBase player = (EntityLivingBase) object;
                if (canAttack(player)) {
                    double distanceSquared = mc.thePlayer.getDistanceSqToEntity(player);
                    if (distanceSquared <= range * range && distanceSquared < minDistanceSquared) {
                        minDistanceSquared = distanceSquared;
                        target = player;
                    }
                }
            }
        }

        return target;
    }

    /**
     * Checks if the player can attack the target
     *
     * @param target the target to attack
     * @return if the player can attack the target
     */
    private boolean canAttack(Entity target) {
        return target instanceof EntityLivingBase && mc.thePlayer.canEntityBeSeen(target) && target != mc.thePlayer && mc.thePlayer.isEntityAlive() && mc.thePlayer.getDistanceSqToEntity(target) <= range.getValue() * range.getValue() && mc.thePlayer.ticksExisted > 100;
    }
}