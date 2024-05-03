package dev.revere.virago.client.modules.combat;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.font.FontRenderer;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.network.socket.SocketClient;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.packet.PacketEvent;
import dev.revere.virago.client.events.render.Render2DEvent;
import dev.revere.virago.client.events.render.Render3DEvent;
import dev.revere.virago.client.events.player.PostMotionEvent;
import dev.revere.virago.client.events.player.PreMotionEvent;
import dev.revere.virago.client.events.player.StrafeEvent;
import dev.revere.virago.client.gui.menu.GuiLicenceKey;
import dev.revere.virago.client.modules.misc.Teams;
import dev.revere.virago.client.modules.player.Scaffold;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.client.services.FriendService;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.util.misc.TimerUtil;
import dev.revere.virago.util.render.ColorUtil;
import dev.revere.virago.util.render.RenderUtils;
import dev.revere.virago.util.render.RoundedUtils;
import dev.revere.virago.util.rotation.RotationUtil;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.util.*;
import net.optifine.util.MathUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @author Remi
 * @project Virago
 * @date 3/25/2024
 */
@ModuleData(name = "KillAura", displayName = "Kill Aura", description = "Automatically attacks entities around you", type = EnumModuleType.COMBAT)
public class KillAura extends AbstractModule {

    private final Setting<AttackStage> attackStage = new Setting<>("Attack Stage", AttackStage.PRE).describedBy("The attack stage.");
    public final Setting<BlockMode> blockMode = new Setting<>("Block Mode", BlockMode.FAKE)
            .describedBy("The autoblock mode.");

    private final Setting<SortMode> sortMode = new Setting<>("Sort Mode", SortMode.RANGE).describedBy("The sort mode.");
    private final Setting<RotationMode> rotationMode = new Setting<>("Rotation Mode", RotationMode.NORMAL).describedBy("The rotation mode.");
    private final Setting<RandomMode> randomMode = new Setting<>("Random Mode", RandomMode.NORMAL).describedBy("The random mode.");

    private final Setting<Double> aps = new Setting<>("APS", 10.5).minimum(1.0).maximum(20.0).incrementation(0.5).describedBy("The amount of times to attack per second");
    public final Setting<Double> range = new Setting<>("Range", 3.0).minimum(2.0).maximum(6.0).incrementation(0.1).describedBy("The range to attack");
    private final Setting<Float> randomization = new Setting<>("Randomization", 0.1f)
            .minimum(0.1f)
            .maximum(5.0f)
            .incrementation(0.1f);
    private final Setting<Integer> maxTurnSpeed = new Setting<>("Max Turn Speed", 120)
            .minimum(10)
            .maximum(180)
            .incrementation(10);

    private final Setting<Integer> minTurnSpeed = new Setting<>("Min Turn Speed", 120)
            .minimum(10)
            .maximum(180)
            .incrementation(10);

    private final Setting<Boolean> smoothRotations = new Setting<>("Smooth Rotations", true).describedBy("Rotate smoothly.");
    private final Setting<Boolean> moveFix = new Setting<>("Move Fix", true).describedBy("Fix the move speed when attacking");
    private final Setting<Boolean> gcdFix = new Setting<>("GCD Fix", true).describedBy("Whether to enable a GCD fix.");

    private final Setting<Boolean> targetHud = new Setting<>("TargetHud", true).describedBy("Display a target hud.");
    private final Setting<Boolean> targetPlayers = new Setting<>("Players", true).describedBy("Target players.");
    private final Setting<Boolean> targetVillager = new Setting<>("Villager", false).describedBy("Target villagers.");
    private final Setting<Boolean> targetAnimals = new Setting<>("Animals", false).describedBy("Target animals.");
    private final Setting<Boolean> targetMonsters = new Setting<>("Monsters", false).describedBy("Target monsters.");
    private final Setting<Boolean> targetInvisibles = new Setting<>("Invisibles", false).describedBy("Target invisibles.");
    private final Setting<Boolean> targetThruWalls = new Setting<>("Through Walls", true).describedBy("Target entities through walls.");

    private final TimerUtil attackTimer = new TimerUtil();
    private EntityLivingBase target;

    private float yaw, pitch, lastYaw, lastPitch;

    private float finalPitch;
    private float finalYaw;

    public boolean blocking = false;
    private int blockingTicks;

    private boolean blinking;
    private int stage;
    private int delay;
    private boolean unb2;

    private final List<Packet<?>> packets = new CopyOnWriteArrayList<>();

    public KillAura() {
        setKey(Keyboard.KEY_R);
    }

    @EventHandler
    private final Listener<PreMotionEvent> preMotionEventListener = event -> {
        setMetaData("R: " + range.getValue().floatValue() + " APS: " + aps.getValue().floatValue());
        if (Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(Scaffold.class).isEnabled())
            return;

        if (SocketClient.jwt == null && (!(mc.currentScreen instanceof GuiLicenceKey))) {
            System.exit(0);
        }

        this.target = this.getSingleTarget();

        if (target == null) {
            this.releaseBlock();
            this.blockingTicks = 0;
            return;
        }

        if (blockMode.getValue() == BlockMode.FAKE || blockMode.getValue() == BlockMode.WATCHDOG)
            blocking = true;

        if (!moveFix.getValue()) {
            float[] rots = this.getRotations(target);
            finalYaw = processRotation((rots[0]));
            finalPitch = processRotation((rots[1]));
        }

        if (smoothRotations.getValue()) {
            float sens = (float) ((Math.pow(mc.gameSettings.mouseSensitivity * 0.6F + 0.2F, 3) * 8.0F) * 0.15F);
            finalYaw = interpolateRotation(mc.thePlayer.rotationYaw, finalYaw, 360);
            finalPitch = interpolateRotation(mc.thePlayer.rotationPitch, finalPitch, 90);
            finalYaw = Math.round(finalYaw / sens) * sens;
            finalPitch = Math.round(finalPitch / sens) * sens;
        }

        calculateRotations(target);
        mc.thePlayer.rotationYawHead = mc.thePlayer.renderYawOffset = yaw;
        mc.thePlayer.rotationPitchHead = pitch;
        event.setYaw(mc.thePlayer.renderYawOffset);
        event.setPitch(mc.thePlayer.rotationPitchHead);

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
    private final Listener<Render2DEvent> render2DEventListener = event -> {
        if (!targetHud.getValue()) return;
        ScaledResolution sr = new ScaledResolution(mc);
        Iterator<Entity> iterator = mc.theWorld.loadedEntityList.iterator();
        int renderIndex = 0;
        while (iterator.hasNext()) {
            Entity entity = iterator.next();
            if (!(entity instanceof EntityPlayer)) continue;
            EntityPlayer player = (EntityPlayer) entity;
            if (target == entity) {
                if (player.targetHUD == null) {
                    player.targetHUD = new TargetHUD(player);
                }
                int size = 33;
                player.targetHUD.render((float) sr.getScaledWidth() / 2.0f + 14.0f, (float) sr.getScaledHeight() / 2.0f - 14.0f + (float) (renderIndex * size));
                ++renderIndex;
            }
        }
    };

    @EventHandler
    private final Listener<PostMotionEvent> postMotionEventListener = event -> {
        if (Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(Scaffold.class).isEnabled())
            return;

        this.postAutoblock();
        if (this.attackStage.getValue().equals(AttackStage.POST) && this.hitTimerDone()) {
            this.attack(this.target);
        }
    };

    @EventHandler
    private final Listener<Render3DEvent> render3DEventListener = event -> {
        //this.preAutoblock();
        if (Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(Scaffold.class).isEnabled())
            return;

        if (target != null)  {
            this.targetAnimation(this.target);
        }
    };

    private void calculateRotations(EntityLivingBase target) {
        lastYaw = yaw;
        lastPitch = pitch;
        float[] prevRots = new float[]{lastYaw, lastPitch};
        float[] rotations = new float[]{0, 0};

        switch (rotationMode.getValue()) {
            case NONE: {
                rotations = new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
                break;
            }
            case SMOOTH: {
                rotations = RotationUtil.getRotations(target);
                break;
            }
            case NORMAL: {
                rotations = RotationUtil.getGCDRotations(RotationUtil.getRotationsNormal(mc.thePlayer.getPositionVector(), target.getPositionVector()), prevRots);
                break;
            }
            default: {
                break;
            }
        }

        yaw = rotations[0];
        pitch = rotations[1];

        switch (randomMode.getValue()) {
            case NORMAL: {
                yaw += (float) (Math.random() * randomization.getValue());
                pitch += (float) (Math.random() * randomization.getValue());
                break;
            }
            case DOUBLED: {
                yaw += (float) (Math.random() * randomization.getValue());
                pitch += (float) (Math.random() * randomization.getValue());

                if (mc.thePlayer.ticksExisted % 3 == 0) {
                    yaw += (float) (Math.random() * randomization.getValue());
                    pitch += (float) (Math.random() * randomization.getValue());
                }

                break;
            }
            case GAUSSIAN: {
                yaw += (float) (ThreadLocalRandom.current().nextGaussian() * randomization.getValue());
                pitch += (float) (ThreadLocalRandom.current().nextGaussian() * randomization.getValue());
                break;
            }
            case AUGUSTUS: {
                final float random1 = RotationUtil.nextSecureFloat(-randomization.getValue(), randomization.getValue());
                final float random2 = RotationUtil.nextSecureFloat(-randomization.getValue(), randomization.getValue());
                final float random3 = RotationUtil.nextSecureFloat(-randomization.getValue(), randomization.getValue());
                final float random4 = RotationUtil.nextSecureFloat(-randomization.getValue(), randomization.getValue());
                yaw += RotationUtil.nextSecureFloat(Math.min(random1, random2), Math.max(random1, random2));
                pitch += RotationUtil.nextSecureFloat(Math.min(random3, random4), Math.max(random3, random4));
                break;
            }
            case MULTIPOINTS: {
                pitch += (float) MathUtils.randomNumber(randomization.getValue() * 4, 0);
                yaw += (float) (Math.random() * randomization.getValue());
                break;
            }
            default: {
                break;
            }
        }

        float speed = (float) MathUtils.randomNumber(
                maxTurnSpeed.getValue().floatValue(),
                minTurnSpeed.getValue().floatValue()
        );

        yaw = RotationUtil.smoothRotation(lastYaw, yaw, speed);
        pitch = RotationUtil.smoothRotation(lastPitch, pitch, speed);

        float[] fixedRotations = RotationUtil.getFixedRotations(
                new float[]{yaw, pitch},
                new float[]{lastYaw, lastPitch}
        );

        yaw = fixedRotations[0];
        pitch = fixedRotations[1];
    }

    private void attack(EntityLivingBase e) {
        if (e == null) {
            return;
        }

        if (blockMode.getValue() == BlockMode.WATCHDOG) {
            unb2 = false;
            delay = 0;
            stage += 1;

            if(stage == 1) {
                blinking = true;
                releaseBlock();
            } else if(stage == 2) {
                mc.thePlayer.swingItem();
                mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(e, C02PacketUseEntity.Action.ATTACK));
                mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(e, C02PacketUseEntity.Action.INTERACT));
                blinking = false;

                this.packets.forEach(packet -> {
                    this.packets.remove(packet);
                    mc.thePlayer.sendQueue.getNetworkManager().sendPacketWithoutEvent(packet);
                });

                preAutoblock();
                stage = 0;
            }

            if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                unb2 = true;
            } else {
                return;
            }

            delay += 1;
            if(delay == 2) {
                mc.getNetHandler().getNetworkManager().sendPacketWithoutEvent(new C07PacketPlayerDigging());
                unb2 = false;
                delay = 0;
            }
        } else {
            mc.thePlayer.swingItem();
            mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(e, C02PacketUseEntity.Action.ATTACK));
        }
    }

    private boolean hitTimerDone() {
        return this.attackTimer.hasTimeElapsed((long) (1000.0 / this.aps.getValue()), true);
    }

    @EventHandler
    private Listener<PacketEvent> packetEvent = event -> {
        if (mc.thePlayer == null || !blinking)
            return;

        if (event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
            event.setCancelled(true);
            packets.add(event.getPacket());
        }
    };

    private void preAutoblock() {
        if (mc.thePlayer.getHeldItem() == null || !(mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) || target == null) {
            return;
        }
        if (Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(Scaffold.class).isEnabled())
            return;

        switch (blockMode.getValue()) {
            case VANILLA:
            case H_V_H:
                mc.getNetHandler().addToSendQueueNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                blocking = true;
                break;
            case N_C_P:
                break;
            case WATCHDOG:
                mc.getNetHandler().getNetworkManager().sendPacketWithoutEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                blocking = true;
                break;
            case VERUS:
                mc.getNetHandler().addToSendQueueNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem(), new BlockPos(-1, -1, -1)));
                blocking = true;
                break;
            case CONTROL:
                mc.gameSettings.keyBindUseItem.pressed = true;
                blocking = true;
                //this.releaseBlock();
                break;
        }
    }

    private void postAutoblock() {
        if (mc.thePlayer.getHeldItem() == null || !(mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) || target == null) {
            return;
        }
        if (Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(Scaffold.class).isEnabled())
            return;

        switch (blockMode.getValue()) {
            case N_C_P:
                if (!blocking) {
                    mc.getNetHandler().addToSendQueueNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                    blocking = true;
                }
                break;
            case CONTROL:
                break;
        }
    }

    private void releaseBlock() {
        if (blocking) {
            switch (blockMode.getValue()) {
                case N_C_P:
                case VANILLA:
                case VERUS:
                case H_V_H:
                case WATCHDOG:
                    mc.getNetHandler().addToSendQueueNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    break;
                case CONTROL:
                    mc.gameSettings.keyBindUseItem.pressed = Mouse.isButtonDown(1);
                    break;
            }
        }
        blocking = false;
    }

    private void targetAnimation(EntityLivingBase target) {
        final float partialTicks = mc.timer.renderPartialTicks;
        if (target == null) return;

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
        GlStateManager.resetColor();
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
        List<EntityLivingBase> targets = mc.theWorld.getLoadedEntityLivingBases().stream()
                .filter(entity -> entity != mc.thePlayer)
                .filter(entity -> entity.ticksExisted > 0)
                .filter(entity -> (double) mc.thePlayer.getDistanceToEntity(entity) <= this.range.getValue())
                .filter(entity -> mc.theWorld.loadedEntityList.contains(entity))
                .filter(this::validTarget)
                //.sorted(Comparator.comparingDouble(entity -> mc.thePlayer.getDistanceSqToEntity(entity)))
                .collect(Collectors.toList());

        if (targets.isEmpty()) {
            return null;
        }

        switch (sortMode.getValue()) {
            case RANGE:
                return targets.stream()
                        .min(Comparator.comparingDouble(entity -> mc.thePlayer.getDistanceSqToEntity(entity)))
                        .orElse(null);
            case HEALTH:
                return targets.stream()
                        .min(Comparator.comparingDouble(EntityLivingBase::getHealth))
                        .orElse(null);
            case ARMOR:
                return targets.stream()
                        .max(Comparator.comparingDouble(this::gearLevelCalculation))
                        .orElse(null);
            default:
                return targets.get(0);
        }
    }

    /**
     * Calculates the gear level of the player
     *
     * @param entity the entity to calculate the gear level of
     * @return the gear level of the player
     */
    private double gearLevelCalculation(EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) {
            return 0;
        }

        EntityPlayer player = (EntityPlayer) entity;
        double totalArmorPoints = 0;

        for (ItemStack itemStack : player.inventory.armorInventory) {
            if (itemStack != null && itemStack.getItem() instanceof ItemArmor) {
                totalArmorPoints += ((ItemArmor) itemStack.getItem()).damageReduceAmount;
            }
        }

        return totalArmorPoints;
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
        FriendService friendService = Virago.getInstance().getServiceManager().getService(FriendService.class);
        Teams teams = Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(Teams.class);

        if (entity instanceof EntityPlayer) {
            if(friendService.isFriend(entity.getName())) {
                return false;
            }

            if (teams.isTeammate(entity) && teams.isEnabled()) {
                return false;
            }

            return this.targetPlayers.getValue();
        }

        if (entity instanceof EntityVillager) {
            return this.targetVillager.getValue();
        }

        if (entity instanceof EntityAnimal) {
            return this.targetAnimals.getValue();
        }

        if (entity instanceof EntityMob) {
            return this.targetMonsters.getValue();
        }

        if (entity instanceof EntityArmorStand) {
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
        yaw = mc.thePlayer.rotationYaw;
        pitch = mc.thePlayer.rotationPitch;
        lastYaw = mc.thePlayer.rotationYaw;
        lastPitch = mc.thePlayer.rotationPitch;

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

    public enum SortMode {
        RANGE, HEALTH, ARMOR
    }

    public enum BlockMode {
        FAKE, H_V_H, VANILLA, CONTROL, VERUS, WATCHDOG, N_C_P
    }

    public enum AttackStage {
        PRE,
        POST;
    }

    public enum RotationMode {
        NONE, SMOOTH, NORMAL
    }

    public enum RandomMode {
        NORMAL, DOUBLED, GAUSSIAN, AUGUSTUS, MULTIPOINTS
    }

    public class TargetHUD {
        public final EntityPlayer ent;
        public float animation = 0.0f;

        public FontRenderer fontRenderer;

        public TargetHUD(EntityPlayer player) {
            this.ent = player;
        }

        private void renderArmor(EntityPlayer player) {
            ItemStack stack;
            int index;
            int xOffset = 60;
            for (index = 3; index >= 0; --index) {
                stack = player.inventory.armorInventory[index];
                if (stack == null) continue;
                xOffset -= 8;
            }
            for (index = 3; index >= 0; --index) {
                stack = player.inventory.armorInventory[index];
                if (stack == null) continue;
                ItemStack armourStack = stack.copy();
                if (armourStack.hasEffect() && (armourStack.getItem() instanceof ItemTool || armourStack.getItem() instanceof ItemArmor)) {
                    armourStack.stackSize = 1;
                }
                this.renderItemStack(armourStack, xOffset, 12);
                xOffset += 16;
            }
        }

        private void renderItemStack(ItemStack stack, int x, int y) {
            GlStateManager.pushMatrix();
            GlStateManager.disableAlpha();
            mc.getRenderItem().zLevel = -150.0f;
            GlStateManager.disableCull();
            mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
            mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack, x, y);
            GlStateManager.enableCull();
            mc.getRenderItem().zLevel = 0.0f;
            GlStateManager.disableBlend();
            GlStateManager.scale(0.5f, 0.5f, 0.5f);
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            GlStateManager.scale(2.0f, 2.0f, 2.0f);
            GlStateManager.enableAlpha();
            GlStateManager.popMatrix();
        }

        public void render(float x, float y) {
            GL11.glPushMatrix();
            FontService font = Virago.getInstance().getServiceManager().getService(FontService.class);
            fontRenderer = font.getProductSans();

            String playerName = this.ent.getName();
            String healthStr = (double) Math.round(this.ent.getHealth() * 10.0f) / 10.0 + " hp";

            float width = Math.max(75.0f, fontRenderer.getStringWidth(playerName) + 45.0f);

            GL11.glTranslatef(x, y, 0.0f);
            RoundedUtils.glRound(0.0f, 0.0f, 28f + width, 36.0f, 6.0f, new Color(0, 0, 0, 150).getRGB());
            RoundedUtils.shadowGradient(0.0f, 0.0f, 28.0f + width, 36.0f, 5f, 10f, 10f, new Color(ColorUtil.reAlpha(-16777216, 0.5f)), new Color(ColorUtil.reAlpha(-16777216, 0.5f)), new Color(ColorUtil.reAlpha(-16777216, 0.5f)), new Color(ColorUtil.reAlpha(-16777216, 0.5f)), false);

            fontRenderer.drawString(playerName, 32.0f, 7.0f, -1);
            fontRenderer.drawString(healthStr, 28.0f + width - fontRenderer.getStringWidth(healthStr) - 2.0f, 8.0f, -3355444);

            float health = target.getHealth();
            double hpPercentage = health / target.getMaxHealth();
            hpPercentage = MathHelper.clamp_double(hpPercentage, 0.0, 1.0);
            float barWidth = 26.0f + width - 2.0f - 37.0f;
            float drawPercent = (float) (37.0 + (double) (barWidth / 100.0f) * (hpPercentage * 100.0));

            RenderUtils.drawRect(38.0f, 17.5f, 26.0f + width - 2.0f, 22.5f, ColorUtil.reAlpha(new Color(0).getRGB(), 0.35f));
            RenderUtils.renderGradientRect(38, 17, (int) drawPercent, 22, 5.0, 2000L, 2L, RenderUtils.Direction.RIGHT);

            font.getIcon10().drawString("s", 32.0f, 19f, -1);
            font.getIcon10().drawString("r", 32.0f, 27.5f, -1);

            float f3 = 37.0f + barWidth / 100.0f * (float) (this.ent.getTotalArmorValue() * 5);
            RenderUtils.drawRect(38.0f, 24.5f, 26.0f + width - 2.0f, 29.5f, ColorUtil.reAlpha(new Color(0).getRGB(), 0.35f));
            RenderUtils.drawRect(38.0f, 24.5f, f3, 29.5f, -12417291);
            for (NetworkPlayerInfo info : GuiPlayerTabOverlay.field_175252_a.sortedCopy(mc.getNetHandler().getPlayerInfoMap())) {
                if (mc.theWorld.getPlayerEntityByUUID(info.getGameProfile().getId()) != this.ent) continue;
                mc.getTextureManager().bindTexture(info.getLocationSkin());
                this.drawScaledCustomSizeModalRect(5.0f, 6.0f, 8.0f, 8.0f, 8.0f, 8.0f, 24.0f, 24.0f, 64.0f, 64.0f);
                if (this.ent.isWearing(EnumPlayerModelParts.HAT)) {
                    this.drawScaledCustomSizeModalRect(5.0f, 6.0f, 40.0f, 8.0f, 8.0f, 8.0f, 24.0f, 24.0f, 64.0f, 64.0f);
                }
                GlStateManager.bindTexture(0);
                break;
            }
            GL11.glPopMatrix();
            GlStateManager.resetColor();
        }

        public void rectangleBordered(double x, double y, double x1, double y1, double width, int internalColor, int borderColor) {
            this.rectangle(x + width, y + width, x1 - width, y1 - width, internalColor);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            this.rectangle(x + width, y, x1 - width, y + width, borderColor);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            this.rectangle(x, y, x + width, y1, borderColor);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            this.rectangle(x1 - width, y, x1, y1, borderColor);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            this.rectangle(x + width, y1 - width, x1 - width, y1, borderColor);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        }

        public void rectangle(double left, double top, double right, double bottom, int color) {
            double var5;
            if (left < right) {
                var5 = left;
                left = right;
                right = var5;
            }
            if (top < bottom) {
                var5 = top;
                top = bottom;
                bottom = var5;
            }
            float var11 = (float) (color >> 24 & 0xFF) / 255.0f;
            float var6 = (float) (color >> 16 & 0xFF) / 255.0f;
            float var7 = (float) (color >> 8 & 0xFF) / 255.0f;
            float var8 = (float) (color & 0xFF) / 255.0f;
            WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.color(var6, var7, var8, var11);
            worldRenderer.begin(7, DefaultVertexFormats.POSITION);
            worldRenderer.pos(left, bottom, 0.0).endVertex();
            worldRenderer.pos(right, bottom, 0.0).endVertex();
            worldRenderer.pos(right, top, 0.0).endVertex();
            worldRenderer.pos(left, top, 0.0).endVertex();
            Tessellator.getInstance().draw();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        }

        public void drawScaledCustomSizeModalRect(float x, float y, float u, float v, float uWidth, float vHeight, float width, float height, float tileWidth, float tileHeight) {
            float f = 1.0f / tileWidth;
            float f1 = 1.0f / tileHeight;
            GL11.glColor4f((float) 1.0f, (float) 1.0f, (float) 1.0f, (float) 1.0f);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer bufferbuilder = tessellator.getWorldRenderer();
            bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
            bufferbuilder.pos(x, y + height, 0.0).tex(u * f, (v + vHeight) * f1).endVertex();
            bufferbuilder.pos(x + width, y + height, 0.0).tex((u + uWidth) * f, (v + vHeight) * f1).endVertex();
            bufferbuilder.pos(x + width, y, 0.0).tex((u + uWidth) * f, v * f1).endVertex();
            bufferbuilder.pos(x, y, 0.0).tex(u * f, v * f1).endVertex();
            tessellator.draw();
        }
    }
}