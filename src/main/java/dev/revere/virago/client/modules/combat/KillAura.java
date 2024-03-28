package dev.revere.virago.client.modules.combat;

import com.google.common.eventbus.Subscribe;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.render.Render3DEvent;
import dev.revere.virago.client.events.update.PostMotionEvent;
import dev.revere.virago.client.events.update.PreMotionEvent;
import dev.revere.virago.client.events.update.StrafeEvent;
import dev.revere.virago.client.events.update.UpdateEvent;
import dev.revere.virago.util.TimerUtil;
import dev.revere.virago.util.render.ColorUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Remi
 * @project Virago
 * @date 3/25/2024
 */
@ModuleData(name = "KillAura", description = "Automatically attacks entities around you", type = EnumModuleType.COMBAT)
public class KillAura extends AbstractModule {
    private final Setting<AttackStage> attackStage = new Setting<>("Attack Stage", AttackStage.PRE).describedBy("The attack stage.");
    private final Setting<BlockMode> blockMode = new Setting<>("Block Mode", BlockMode.FAKE)
            .describedBy("The autoblock mode.");

    private final Setting<Double> aps = new Setting<>("APS", 10.0).minimum(1.0).maximum(20.0).incrementation(0.5).describedBy("The amount of times to attack per second");
    private final Setting<Double> range = new Setting<>("Range", 4.0).minimum(2.0).maximum(6.0).incrementation(0.1).describedBy("The range to attack");

    private final Setting<Boolean> smoothRotations = new Setting<>("Smooth Rotations", true).describedBy("Rotate smoothly.");
    private final Setting<Boolean> moveFix = new Setting<>("Move Fix", false).describedBy("Fix the move speed when attacking");
    private final Setting<Boolean> gcdFix = new Setting<>("GCD Fix", false).describedBy("Whether to enable a GCD fix.");

    private final Setting<Boolean> targetPlayers = new Setting<>("Players", true).describedBy("Target players.");
    private final Setting<Boolean> targetAnimals = new Setting<>("Animals", false).describedBy("Target animals.");
    private final Setting<Boolean> targetMonsters = new Setting<>("Monsters", false).describedBy("Target monsters.");
    private final Setting<Boolean> targetInvisibles = new Setting<>("Invisibles", false).describedBy("Target invisibles.");
    private final Setting<Boolean> targetThruWalls = new Setting<>("Through Walls", true).describedBy("Target entities through walls.");

    private final TimerUtil attackTimer = new TimerUtil();
    private EntityLivingBase target;

    private float finalPitch;
    private float finalYaw;

    public boolean blocking = false;
    private int blockingTicks;

    public KillAura() {
        setKey(Keyboard.KEY_R);
    }

    @EventHandler
    private final Listener<PreMotionEvent> preMotionEventListener = event -> {
        setMetaData("R: " + range.getValue().floatValue() + " APS: " + aps.getValue().floatValue());
        this.target = this.getSingleTarget();

        if (target == null) {
            this.releaseBlock();
            this.blockingTicks = 0;
            return;
        }

        blocking = true;

        if (!moveFix.getValue()) {
            float[] rots = this.getRotations(target);
            finalYaw = processRotation((rots[0]));
            finalPitch = processRotation((rots[1]));
        }

        if(smoothRotations.getValue()) {
            float sens = (float) ((Math.pow(mc.gameSettings.mouseSensitivity * 0.6F + 0.2F, 3) * 8.0F) * 0.15F);
            finalYaw = interpolateRotation(mc.thePlayer.rotationYaw, finalYaw, 360);
            finalPitch = interpolateRotation(mc.thePlayer.rotationPitch, finalPitch, 90);
            finalYaw = Math.round(finalYaw / sens) * sens;
            finalPitch = Math.round(finalPitch / sens) * sens;
        }

        mc.thePlayer.rotationYawHead = mc.thePlayer.renderYawOffset = finalYaw;
        mc.thePlayer.rotationPitchHead = finalPitch;
        event.setYaw(mc.thePlayer.renderYawOffset);
        event.setPitch(mc.thePlayer.rotationPitchHead);

        //this.preAutoblock();
        if (this.attackStage.getValue().equals(AttackStage.PRE) && this.hitTimerDone()) {
            this.attack(this.target);
        }
    };

    @EventHandler
    private final Listener<StrafeEvent> strafeEventListener = event -> {
        if (moveFix.getValue()) {
            float[] rots = this.getRotations(target);
            finalYaw = processRotation(rots[0]);
            finalPitch = processRotation(rots[1]);

            event.setYaw(finalYaw);
            event.setPitch(finalPitch);
        }
    };

    @EventHandler
    private final Listener<PostMotionEvent> postMotionEventListener = event -> {
        //this.postAutoblock();
        if (this.attackStage.getValue().equals(AttackStage.POST) && this.hitTimerDone()) {
            this.attack(this.target);
        }
    };

    @EventHandler
    private final Listener<Render3DEvent> render3DEventListener = event -> {
        if(target != null) this.targetAnimation(this.target);
    };

    private void attack(EntityLivingBase e) {
        if (e == null) {
            return;
        }

        mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(e, C02PacketUseEntity.Action.ATTACK));
        mc.thePlayer.swingItem();
    }

    private boolean hitTimerDone() {
        return this.attackTimer.hasTimeElapsed((long) (1000.0 / this.aps.getValue()), true);
    }

    private void preAutoblock() {
        if (mc.thePlayer.getHeldItem() == null || !(mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) || target == null) {
            return;
        }

        switch (blockMode.getValue()) {
            case VANILLA:
            case H_V_H:
                mc.getNetHandler().addToSendQueueNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                blocking = true;
                break;
            case N_C_P:
                break;
            case WATCHDOG:
                if (this.blockingTicks == 0) {
                    sendBlocking(false, true);
                    this.blockingTicks = 6;
                } else if (this.blockingTicks == 4) {
                    releaseUseItem(false);
                }
                this.blockingTicks--;
                break;
            case VERUS:
                mc.getNetHandler().addToSendQueueNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem(), new BlockPos(-1, -1, -1)));
                blocking = true;
                break;
            case CONTROL:
                this.releaseBlock();
                break;
        }
    }

    private void postAutoblock() {
        if (mc.thePlayer.getHeldItem() == null || !(mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) || target == null) {
            return;
        }

        switch (blockMode.getValue()) {
            case N_C_P:
                if (!blocking) {
                    mc.getNetHandler().addToSendQueueNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                    blocking = true;
                }
                break;
            case CONTROL:
                mc.gameSettings.keyBindUseItem.pressed = true;
                blocking = true;
                break;
        }
    }

    public static void sendBlocking(boolean callEvent, boolean placement) {
        if (mc.thePlayer == null)
            return;

        C08PacketPlayerBlockPlacement packet;
        if (placement) {
            packet = new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, mc.thePlayer.getHeldItem(), 0, 0, 0);
        } else {
            packet = new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem());
        }

        if (callEvent) {
            mc.getNetHandler().addToSendQueue(packet);
        } else {
            mc.getNetHandler().addToSendQueueNoEvent(packet);
        }
    }

    public static void releaseUseItem(boolean callEvent) {
        if (mc.thePlayer == null)
            return;

        C07PacketPlayerDigging packet = new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN);
        if (callEvent) {
            mc.getNetHandler().addToSendQueue(packet);
        } else {
            mc.getNetHandler().addToSendQueueNoEvent(packet);
        }
    }

    private void releaseBlock() {
        if (blocking) {
            switch (blockMode.getValue()) {
                case N_C_P:
                case VANILLA:
                case VERUS:
                case H_V_H:
                    mc.getNetHandler().addToSendQueueNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    break;
                case CONTROL:
                    mc.gameSettings.keyBindUseItem.pressed = Mouse.isButtonDown(1);
                    break;
                case WATCHDOG:
                    this.blockingTicks = 0;
                    break;
            }
        }
        blocking = false;
    }

    private void targetAnimation(EntityLivingBase target) {
        final float partialTicks = mc.timer.renderPartialTicks;
        if(target == null) return;

        Color color = new Color(ColorUtil.getColor(true));

        if (mc.getRenderManager() == null) return;

        final double x = target.prevPosX + (target.posX - target.prevPosX) * partialTicks - (mc.getRenderManager()).renderPosX;
        final double y = target.prevPosY + (target.posY - target.prevPosY) * partialTicks + Math.sin(System.currentTimeMillis() / 2E+2) + 1 - (mc.getRenderManager()).renderPosY;
        final double z = target.prevPosZ + (target.posZ - target.prevPosZ) * partialTicks - (mc.getRenderManager()).renderPosZ;

        GL11.glPushMatrix();
        GL11.glDisable(3553);
        GL11.glEnable(2848);
        GL11.glEnable(2832);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
        GL11.glHint(3153, 4354);
        GL11.glDepthMask(false);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableCull();
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);

        for (float i = 0; i <= Math.PI * 2 + ((Math.PI * 2) / 32.F); i += (float) ((Math.PI * 2) / 32.F)) {
            double vecX = x + 0.67 * Math.cos(i);
            double vecZ = z + 0.67 * Math.sin(i);

            ColorUtil.color(ColorUtil.withAlpha(color, (int) (255 * 0.25)));
            GL11.glVertex3d(vecX, y, vecZ);
        }

        for (float i = 0; i <= Math.PI * 2 + (Math.PI * 2) / 32.F; i += (float) ((Math.PI * 2) / 32.F)) {
            double vecX = x + 0.67 * Math.cos(i);
            double vecZ = z + 0.67 * Math.sin(i);

            ColorUtil.color(ColorUtil.withAlpha(color, (int) (255 * 0.25)));
            GL11.glVertex3d(vecX, y, vecZ);

            ColorUtil.color(ColorUtil.withAlpha(color, 0));
            GL11.glVertex3d(vecX, y - Math.cos(System.currentTimeMillis() / 2E+2) / 2.0F, vecZ);
        }

        GL11.glEnd();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDepthMask(true);
        GL11.glEnable(2929);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.enableCull();
        GL11.glDisable(2848);
        GL11.glDisable(2848);
        GL11.glEnable(2832);
        GL11.glEnable(3553);
        GL11.glPopMatrix();
        ColorUtil.glColor(Color.WHITE.getRGB());
    }

    public float[] getRotations(EntityLivingBase target) {
        if (target == null) {
            return new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
        }
        double xDist = target.posX - mc.thePlayer.posX;
        double zDist = target.posZ - mc.thePlayer.posZ;
        AxisAlignedBB entityBB = target.getEntityBoundingBox().expand(0.1f, 0.1f, 0.1f);
        double playerEyePos = mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight();
        double yDist = playerEyePos > entityBB.maxY ? entityBB.maxY - playerEyePos : (playerEyePos < entityBB.minY ? entityBB.minY - playerEyePos : 0.0);
        double fDist = MathHelper.sqrt_double(xDist * xDist + zDist * zDist);
        float yaw = interpolateRotation(finalYaw, (float) (StrictMath.atan2(zDist, xDist) * 57.29577951308232) - 90.0f, 45.0f);
        float pitch = interpolateRotation(finalPitch, (float) (-(StrictMath.atan2(yDist, fDist) * 57.29577951308232)), 45.0f);
        yaw = (float) ((double) yaw + (Math.random() - 0.5));
        pitch = (float) ((double) pitch + (Math.random() - 0.5));
        pitch = Math.min(pitch, 90.0f);
        pitch = Math.max(pitch, -90.0f);
        return new float[]{yaw, pitch};
    }

    public EntityLivingBase getSingleTarget() {
        List<EntityLivingBase> targets = mc.theWorld.getLoadedEntityLivingBases().stream().filter(entity -> entity != mc.thePlayer).filter(entity -> entity.ticksExisted > 0).filter(entity -> (double) mc.thePlayer.getDistanceToEntity((Entity) entity) <= this.range.getValue()).filter(entity -> mc.theWorld.loadedEntityList.contains(entity)).filter(this::validTarget).sorted(Comparator.comparingDouble(entity -> mc.thePlayer.getDistanceSqToEntity(entity))).collect(Collectors.toList());
        if (targets.isEmpty()) {
            return null;
        }
        return targets.get(0);
    }

    private boolean validTarget(EntityLivingBase entity) {
        if (entity.isInvisible()) {
            return this.validTargetLayer2(entity) && this.targetInvisibles.getValue();
        }
        return this.validTargetLayer2(entity);
    }

    private boolean validTargetLayer2(EntityLivingBase entity) {
        if (!entity.canEntityBeSeen(mc.thePlayer)) {
            return this.validTargetLayer3(entity) && this.targetThruWalls.getValue();
        }
        return this.validTargetLayer3(entity);
    }

    private boolean validTargetLayer3(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            return this.targetPlayers.getValue();
        }
        if (entity instanceof EntityAnimal) {
            return this.targetAnimals.getValue();
        }
        if (entity instanceof EntityMob) {
            return this.targetMonsters.getValue();
        }
        if (entity instanceof EntityVillager || entity instanceof EntityArmorStand) {
            return false;
        }
        return false;
    }

    private static float interpolateRotation(float prev, float now, float maxTurn) {
        float var4 = MathHelper.wrapAngleTo180_float(now - prev);
        if (var4 > maxTurn) {
            var4 = maxTurn;
        }
        if (var4 < -maxTurn) {
            var4 = -maxTurn;
        }
        return prev + var4;
    }

    private float processRotation(float value) {
        float toReturn = value;
        if (gcdFix.getValue()) {
            double m = 0.005 * (double) mc.gameSettings.mouseSensitivity;
            double gcd = m * m * m * 1.2;
            toReturn = (float) ((double) toReturn - (double) toReturn % gcd);
            return toReturn;
        }
        return toReturn;
    }


    @Override
    public void onEnable() {
        super.onEnable();
        attackTimer.reset();
        finalYaw = mc.thePlayer.rotationYaw;
        finalPitch = mc.thePlayer.rotationPitch;
        mc.gameSettings.keyBindUseItem.pressed = false;
        blocking = true;
        blockingTicks = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.releaseBlock();
    }

    enum BlockMode {
        FAKE, H_V_H, VANILLA, CONTROL, VERUS, WATCHDOG, N_C_P
    }

    public enum AttackStage {
        PRE,
        POST;
    }
}
