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
import dev.revere.virago.client.events.player.UpdateEvent;
import dev.revere.virago.client.events.render.Render2DEvent;
import dev.revere.virago.client.events.render.Render3DEvent;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.util.Logger;
import dev.revere.virago.util.misc.TimerUtil;
import dev.revere.virago.util.player.InventoryUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.MathHelper;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/7/2024
 */
@ModuleData(name = "Long Jump", description = "Jump further", type = EnumModuleType.MOVEMENT)
public class LongJump extends AbstractModule {

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.WATCHDOG);

    private boolean spartanCanLongJump = false;
    private boolean spartanCheck = false;
    private int spartanJumps;
    private int tick;

    private final TimerUtil timer = new TimerUtil();

    @EventHandler
    private final Listener<PreMotionEvent> preMotionEventListener = event -> {
        switch (mode.getValue()) {
            case WATCHDOG:
                if (mc.thePlayer.onGround) {
                    event.setPitch(90);
                    event.setYaw(180);
                    mc.thePlayer.rotationPitchHead = 90;
                    mc.thePlayer.rotationYawHead = 180;
                    mc.thePlayer.renderYawOffset = 180;
                }

                int item = InventoryUtil.findItem(Items.fire_charge);

                if (item == -1) return;

                tick++;

                mc.thePlayer.inventory.currentItem = item;

                if (tick == 5) {
                    mc.thePlayer.jump();
                    mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getCurrentEquippedItem());
                }

                if (mc.thePlayer.fallDistance > 0) {
                    mc.thePlayer.motionY += 0.02;
                }

                if (mc.thePlayer.onGround && timer.hasTimeElapsed(400L)) {
                    toggleSilent();
                }
                break;
            case VERUS:
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.rotationPitch = 90.0f;
                }
                if (timer.hasTimeElapsed(10, true)) {
                    int fireballSlot = -1;
                    for (int i = 0; i < 9; i++) {
                        ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                        if (stack != null && (stack.getItem().getUnlocalizedName().equalsIgnoreCase("item.fireball") || stack.getItem().getUnlocalizedName().equalsIgnoreCase("item.firecharge"))) {
                            fireballSlot = i;
                            break;
                        }
                    }
                    if (fireballSlot != -1) {
                        mc.thePlayer.inventory.currentItem = fireballSlot;
                        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getCurrentEquippedItem());
                        if (timer.hasTimeElapsed(100L)) {
                            mc.thePlayer.rotationPitch = 0f;
                        }
                    }
                }
                break;
            case SPARTAN:
                if (!this.spartanCheck && spartanCanLongJump) {
                    boolean doubleJump = timer.hasTimeElapsed(1000L) && !this.timer.hasTimeElapsed(2000L);
                    if (doubleJump) {
                        spartanJumps++;

                        if (mc.thePlayer.onGround) {
                            spartanJumps = 0;
                        }

                        if (spartanJumps == 0) {
                            mc.thePlayer.motionY = 1.02f;
                        }
                        if (spartanJumps == 1) {
                            mc.thePlayer.motionY = 0.43;
                        }
                        if (spartanJumps == 2) {
                            mc.thePlayer.motionY = 1 - mc.thePlayer.posY % 1;
                            spartanJumps = -1;
                        }
                    }
                    if (this.timer.hasTimeElapsed(2000L)) {
                        timer.reset();
                        toggleSilent();
                    }
                }

                if (mc.thePlayer.onGround && (mc.thePlayer.prevPosY - mc.thePlayer.posY) > 0) {
                    this.spartanJumps++;
                }

                if (spartanCheck && mc.thePlayer.onGround) {
                    mc.thePlayer.jump();
                }
                break;
            case FIREBALL:
                int fireballSlot = -1;
                for (int i = 0; i < 9; i++) {
                    ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                    if (stack != null && (stack.getItem().getUnlocalizedName().equalsIgnoreCase("item.fireball") || stack.getItem().getUnlocalizedName().equalsIgnoreCase("item.firecharge"))) {
                        fireballSlot = i;
                        break;
                    }
                }
                if (fireballSlot != -1) {
                    mc.thePlayer.inventory.currentItem = fireballSlot;
                    mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getCurrentEquippedItem());
                }
                break;
        }
    };

    @EventHandler
    private final Listener<PacketEvent> packetEventListener = event -> {
        switch (mode.getValue()) {
            case WATCHDOG:
                break;
            case SPARTAN:
                if (event.getEventState() == PacketEvent.EventState.SENDING) {
                    if (event.getPacket() instanceof C03PacketPlayer) {
                        C03PacketPlayer packet = event.getPacket();
                        if (spartanJumps >= 3) {
                            packet.setOnGround(true);
                            this.spartanJumps = 0;
                            this.spartanCheck = false;
                            this.spartanCanLongJump = true;
                        } else if (spartanJumps < 3) {
                            packet.setOnGround(false);
                        }
                    }
                }

                break;
            case FIREBALL:
                break;
        }
    };

    @EventHandler
    private final Listener<UpdateEvent> updateEventListener = event -> {
        switch (mode.getValue()) {
            case WATCHDOG:
                break;
            case SPARTAN:
                break;
            case FIREBALL:
                break;
        }
    };

    @EventHandler
    private final Listener<Render2DEvent> render2DEventListener = event -> {
        FontService font = Virago.getInstance().getServiceManager().getService(FontService.class);
        ScaledResolution sr = new ScaledResolution(mc);
    };

    @EventHandler
    private final Listener<Render3DEvent> render3DEventListener = event -> {
        switch (mode.getValue()) {
            case WATCHDOG:
                break;
            case SPARTAN:
                break;
            case FIREBALL:
                break;
        }
    };

    @EventHandler
    private final Listener<MoveEvent> moveEventListener = event -> {
        switch (mode.getValue()) {
            case WATCHDOG:
                if (mc.thePlayer.hurtTime > 7) {
                    mc.thePlayer.setSpeed(event, 1);
                }
                if (mc.thePlayer.fallDistance == 1) {
                    mc.thePlayer.setSpeed(event, 0.7);
                }
                break;
            case SPARTAN:
                if (!spartanCheck && spartanCanLongJump) {
                    mc.thePlayer.setSpeed(event, 1.2);
                } else if (spartanCheck) {
                    event.setX(0);
                    event.setZ(0);
                }
                break;
            case VERUS:
                if (mc.thePlayer.hurtTime > 0) {
                    mc.thePlayer.rotationPitch = 0f;
                    mc.thePlayer.setSpeed(event, 1.2);
                    toggleSilent();
                }
                break;
            case FIREBALL:
                if (mc.thePlayer.hurtTime >= 7) {
                    mc.thePlayer.setSpeed(event, 1.2);
                    toggleSilent();
                }
                break;
        }
    };

    @Override
    public void onEnable() {
        this.spartanJumps = 0;
        this.spartanCheck = true;
        this.tick = 0;
        this.timer.reset();
        super.onEnable();
    }

    public int getItemIndex() {
        return mc.thePlayer.inventory.currentItem;
    }

    public ItemStack getItemStack() {
        return (mc.thePlayer == null || mc.thePlayer.inventoryContainer == null ? null : mc.thePlayer.inventoryContainer.getSlot(getItemIndex() + 36).getStack());
    }

    /**
     * Strafes the player
     *
     * @param speed the speed to strafe
     * @param yaw   the yaw to strafe
     */
    public void strafe(final double speed, float yaw) {
        if (!mc.thePlayer.isMoving()) {
            return;
        }

        yaw = (float) Math.toRadians(yaw);
        mc.thePlayer.motionX = -MathHelper.sin(yaw) * speed;
        mc.thePlayer.motionZ = MathHelper.cos(yaw) * speed;
    }


    @Override
    public void onDisable() {
        this.timer.reset();
        super.onDisable();
    }

    enum Mode {
        WATCHDOG,
        VERUS,
        SPARTAN,
        FIREBALL
    }
}
