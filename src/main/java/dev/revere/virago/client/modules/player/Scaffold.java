package dev.revere.virago.client.modules.player;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.packet.PacketEvent;
import dev.revere.virago.client.events.player.*;
import dev.revere.virago.client.events.render.Render2DEvent;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.util.Logger;
import dev.revere.virago.util.misc.TimerUtil;
import dev.revere.virago.util.rotation.RayCastUtil;
import dev.revere.virago.util.rotation.vec.Vector2f;
import lombok.Getter;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.util.*;
import org.apache.commons.lang3.RandomUtils;

import java.awt.*;

/**
 * @author Remi
 * @project Virago
 * @date 3/28/2024
 */
@ModuleData(name = "Scaffold", displayName = "Scaffold", description = "Automatically places blocks under you", type = EnumModuleType.PLAYER)
public class Scaffold extends AbstractModule {

    private final Setting<Mode> mode = new Setting<>("Mode", Mode.NORMAL)
            .describedBy("The mode of the scaffold.");

    private final Setting<Rotations> rotations = new Setting<>("Rotations", Rotations.NORMAL)
            .describedBy("The rotations of the scaffold.");

    private final Setting<PlaceMode> placeMode = new Setting<>("PlaceMode", PlaceMode.PRE)
            .describedBy("The place mode of the scaffold.");

    private final Setting<VecMode> vecMode = new Setting<>("Vec3 Mode", VecMode.LEGIT)
            .describedBy("The mode of the Vec3s.");

    private final Setting<ItemMode> itemMode = new Setting<>("ItemMode", ItemMode.SWITCH)
            .describedBy("The item mode of the scaffold.");

    private final Setting<Long> placeDelay = new Setting<>("Place Delay", 0L)
            .minimum(0L)
            .maximum(1000L)
            .incrementation(1L)
            .describedBy("The amount of times to attack per second");

    private final Setting<Boolean> sprint = new Setting<>("Allow Sprinting", false)
            .describedBy("Whether or not to allow sprinting.");

    private final Setting<Double> timer = new Setting<>("TimerBoost", 1.0)
            .minimum(1.0)
            .maximum(2.0)
            .incrementation(0.1)
            .describedBy("The timer boost of the scaffold.");

    private final Setting<Boolean> keepY = new Setting<>("KeepY", true)
            .describedBy("The blocks will stay at the same Y coordinate. Useful for using speed while scaffolding.");

    private final Setting<Boolean> autoJump = new Setting<>("AutoJump", true)
            .describedBy("Automatically jumps while Keep Y is enabled.")
            .visibleWhen(keepY::getValue);

    private final Setting<Boolean> swing = new Setting<>("Swing", false)
            .describedBy("Swings the item while placing.");

    private final Setting<TowerMode> towerMode = new Setting<>("Tower Mode", TowerMode.NONE)
            .describedBy("The mode of the tower.");

    private final Setting<Boolean> towerMove = new Setting<>("Tower Move", false)
            .describedBy("Allows you to go up faster and easier.");

    private final Setting<Boolean> sprintFix = new Setting<>("Sprint Fix", false)
            .describedBy("Helps with bypassing some anticheats.");

    private final Setting<Boolean> gcdFix = new Setting<>("GCD Fix", false)
            .describedBy("Whether to enable a GCD fix.");

    private final Setting<Boolean> safewalk = new Setting<>("Safewalk", true);

    private BlockInfo info;
    private int lastSlot, oldSlot, sprintTicks, towerTicks;
    private ItemStack stackToPlace;

    private float finalRotationYaw, finalRotationPitch, blockYaw;

    private boolean sneaking = false;
    private boolean isPlacing = false;
    public double yCoordinate;

    private final TimerUtil placeTimer = new TimerUtil();
    private final TimerUtil firstJumpDelay = new TimerUtil();

    private Vector2f rotationsVec;

    private int offGroundTicks = 0;
    private int blockCount = 0;
    private int airTicks;

    @EventHandler
    private final Listener<Render2DEvent> render2DEventListener = event -> {
        FontService font = Virago.getInstance().getServiceManager().getService(FontService.class);
        blockCount = getBlockCount();
        ScaledResolution sr = new ScaledResolution(mc);
        String s = String.valueOf(blockCount);

        float percentage = (Math.min(blockCount, 256) / 256f) / 3f;

        int l1 = sr.getScaledWidth() / 2 - (font.getProductSans().getStringWidth(s) / 2);
        int i1 = sr.getScaledHeight() / 2 - font.getProductSans().getHeight() - 10;
        font.getProductSans().drawString(s, l1 + 1, i1, 0);
        font.getProductSans().drawString(s, l1 - 1, i1, 0);
        font.getProductSans().drawString(s, l1, i1 + 1, 0);
        font.getProductSans().drawString(s, l1, i1 - 1, 0);
        font.getProductSans().drawString(s, l1, i1, new Color(Color.HSBtoRGB(percentage, 1.0F, 1.0F)).getRGB());
    };

    boolean firstJump = true;
    boolean yCoordinateUpdated = false;
    boolean fuckedUp = false;
    boolean fuckedUpAndJumped = false;

    @EventHandler
    private final Listener<UpdateEvent> updateEventListener = e -> {
        if (mc.thePlayer == null || mc.theWorld == null) this.toggle();

        if (mode.getValue().equals(Mode.WATCHDOG_JUMP)) {
            if (firstJump) {
                if (!yCoordinateUpdated) {
                    yCoordinate = mc.thePlayer.posY;
                    yCoordinateUpdated = true;
                }
                if (firstJumpDelay.hasTimeElapsed(500L) && firstJump && !(mc.thePlayer.posY > yCoordinate)) {
                    mc.thePlayer.jump();
                    yCoordinate = mc.thePlayer.posY;
                    yCoordinateUpdated = true;
                } else {
                    mc.thePlayer.setSprinting(false);
                }
                if (mc.thePlayer.posY > yCoordinate + 1 && mc.thePlayer.motionY < 0) {
                    info = this.getDiagonalBlockInfo(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ));
                    if (info.pos != null) this.placeBlock();
                    firstJump = false;
                    yCoordinateUpdated = false;
                }

                if (!mc.thePlayer.movementInput.jump && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, yCoordinate - 1, mc.thePlayer.posZ)).getBlock() == Blocks.air) {
                    info = this.getDiagonalBlockInfo(new BlockPos(mc.thePlayer.posX, yCoordinate - 1, mc.thePlayer.posZ));
                    if (info.pos != null) this.placeBlock();
                }
            } else {
                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    yCoordinate = mc.thePlayer.posY;
                    info = this.getDiagonalBlockInfo(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ));
                    yCoordinateUpdated = false;
                    if (info.pos != null) this.placeBlock();
                } else {
                    if (!yCoordinateUpdated)
                        yCoordinate = mc.thePlayer.posY - 1;

                    if (mc.thePlayer.fallDistance > 0.8 && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, yCoordinate, mc.thePlayer.posZ)).getBlock() == Blocks.air) {
                        info = this.getDiagonalBlockInfo(new BlockPos(mc.thePlayer.posX, yCoordinate, mc.thePlayer.posZ));
                        if (info.pos != null) this.placeBlock();
                        placeTimer.reset();
                    }
                    yCoordinateUpdated = true;
                }

                if (!mc.thePlayer.movementInput.jump && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, yCoordinate - 1, mc.thePlayer.posZ)).getBlock() == Blocks.air) {
                    info = this.getDiagonalBlockInfo(new BlockPos(mc.thePlayer.posX, yCoordinate - 1, mc.thePlayer.posZ));
                    if (info.pos != null) this.placeBlock();
                }
            }

            if (mc.thePlayer.posY <= yCoordinate && !firstJump && !mc.gameSettings.keyBindJump.isKeyDown()) {
                Logger.addChatMessage("fucked up");
                fuckedUp = true;
            }

            if (fuckedUp && !firstJump) {
                if (!fuckedUpAndJumped && !mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.thePlayer.jump();
                    mc.gameSettings.keyBindSprint.pressed = false;
                    mc.thePlayer.setSprinting(false);
                    fuckedUpAndJumped = true;
                }
                if (mc.thePlayer.posY > yCoordinate + 1 && mc.thePlayer.motionY < 0) {
                    info = this.getDiagonalBlockInfo(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ));
                    if (info.pos != null) this.placeBlock();
                    fuckedUp = false;
                    fuckedUpAndJumped = false;
                }
            }
        }

        if ((keepY.getValue() || mode.getValue() == Mode.WATCHDOG_JUMP) && !mc.thePlayer.movementInput.jump) {
            info = getDiagonalBlockInfo(new BlockPos(mc.thePlayer.posX, yCoordinate - 1, mc.thePlayer.posZ));
        } else {
            info = getDiagonalBlockInfo(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ));
        }

        if (mode.getValue() == Mode.WATCHDOG) {
            if (mc.thePlayer.onGround && !(mc.thePlayer.ticksExisted % 3 == 0)) {
                mc.timer.timerSpeed = timer.getValue().floatValue();
            } else mc.timer.timerSpeed = 1.0f;
        } else mc.timer.timerSpeed = timer.getValue().floatValue();

        if (info == null || info.pos == null) return;

        sprintTicks++;
    };

    @EventHandler
    private final Listener<PreMotionEvent> preMotionEventListener = e -> {
        if (info == null || info.pos == null)
            return;

        if (!isReplaceable(info)) return;
        this.setRotations(e);

        if (sneaking)
            mc.getNetHandler().addToSendQueueNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));

        stackToPlace = setStackToPlace();

        if (mc.thePlayer.onGround) {
            if (mode.getValue() != Mode.WATCHDOG_JUMP) {
                yCoordinate = mc.thePlayer.posY;
            }
            if (mode.getValue() == Mode.WATCHDOG_JUMP && mc.thePlayer.isMoving() && !firstJump) {
                mc.thePlayer.jump();
            } else if (keepY.getValue() && autoJump.getValue() && mc.thePlayer.isMoving()) mc.thePlayer.jump();
        }

        if (mode.getValue() == Mode.WATCHDOG_JUMP && mc.thePlayer.isMoving()) {
            if (firstJump) {
                mc.gameSettings.keyBindSprint.pressed = false;
            } else {
                mc.thePlayer.setSprinting(true);
            }
        } else if (mode.getValue() != Mode.WATCHDOG) {
            mc.thePlayer.setSprinting(sprint.getValue() && mc.thePlayer.isMoving());
        }

        if (mode.getValue() == Mode.WATCHDOG_SPRINT && mc.thePlayer.ticksExisted % 2 == 0 && !mc.gameSettings.keyBindJump.isKeyDown()) {
            e.setY(mc.thePlayer.posY + .00001 + Math.random() / 100000);
        } else if (mc.gameSettings.keyBindJump.isKeyDown()) {
            e.setY(mc.thePlayer.posY);
        }

        if (placeMode.getValue() == PlaceMode.PRE && !mode.getValue().equals(Mode.WATCHDOG) && !mode.getValue().equals(Mode.WATCHDOG_JUMP)) {
            if (info.pos != null && !keepY.getValue()) {
                this.placeBlock();
            }
        } else if (mode.getValue().equals(Mode.WATCHDOG)) {
            if (!mc.gameSettings.keyBindJump.isKeyDown()) {
                if (info.pos != null) {
                    this.placeBlock();
                }
            }
        }

        this.preTowerMotion(e);
    };

    @EventHandler
    private final Listener<PostMotionEvent> postMotionEventListener = e -> {
        if (placeMode.getValue() == PlaceMode.POST && !mode.getValue().equals(Mode.WATCHDOG)) {
            if (keepY.getValue() && !mc.thePlayer.movementInput.jump)
                info = this.getDiagonalBlockInfo(new BlockPos(mc.thePlayer.posX, yCoordinate - 1, mc.thePlayer.posZ));
            else
                info = this.getDiagonalBlockInfo(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ));
            if (info.pos != null) this.placeBlock();
        } else if (mode.getValue().equals(Mode.WATCHDOG)) {
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                if (keepY.getValue() && !mc.thePlayer.movementInput.jump) {
                    info = this.getDiagonalBlockInfo(new BlockPos(mc.thePlayer.posX, yCoordinate - 1, mc.thePlayer.posZ));
                } else {
                    if (mc.thePlayer.isMoving()) {
                        info = this.getDiagonalBlockInfo(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ));
                    } else {
                        info = this.getDiagonalBlockInfo(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ));
                        /*if (towerMode.getValue() == TowerMode.WATCHDOG) {
                            if (mc.thePlayer.ticksExisted % 6 == 0) {
                                info = this.getDiagonalBlockInfo(new BlockPos(mc.thePlayer.posX - 1, mc.thePlayer.posY - 1, mc.thePlayer.posZ));
                            }
                        }*/
                    }
                }
                if (info.pos != null) this.placeBlock();
            }
        } else if (mode.getValue() != Mode.WATCHDOG_JUMP) {
            //if (info.pos != null) this.placeBlock();
        }
    };

    @EventHandler
    private final Listener<MoveEvent> moveEventListener = e -> {
        this.moveTowerMotion(e);
        if (mc.thePlayer.isMoving() && mode.getValue() == Mode.WATCHDOG && getBlockCount() != 0) {
            if (towerMode.getValue() == TowerMode.WATCHDOG) {
                mc.thePlayer.setSpeed(e, mc.gameSettings.keyBindJump.isKeyDown() ? 0.2625 : 0.2085);
            } else {
                if (!mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.thePlayer.setSpeed(e, 0.2085);
                } else {
                    if (isGoingDiagonally()) {
                        mc.thePlayer.setSpeed(e, 0.24);
                    } else {
                        mc.thePlayer.setSpeed(e, 3);
                    }
                }
            }
        }

        if (mode.getValue() == Mode.WATCHDOG_JUMP) {
            if (firstJump) {
                //mc.thePlayer.setSpeed(e, 0.02);
            } else {
                if (mc.gameSettings.keyBindJump.isKeyDown()) {
                    mc.thePlayer.setSpeed(e, 0.3);
                }
            }
        }

        if (mc.thePlayer.isMoving() && mode.getValue() == Mode.WATCHDOG_SPRINT && mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown()) {
            if (isGoingDiagonally()) {
                mc.thePlayer.setSpeed(e, 0.25);
            } else {
                mc.thePlayer.setSpeed(e, 0.27);
            }
        }
    };

    @EventHandler
    private final Listener<SafeWalkEvent> safeWalkEventListener = e -> {
        e.setCancelled(safewalk.getValue());
    };

    @EventHandler
    public final Listener<PacketEvent> packetEventListener = e -> {
        if (e.getEventState() == PacketEvent.EventState.RECEIVING) {
            if (itemMode.getValue() == ItemMode.SPOOF) {
                if (e.getPacket() instanceof S2FPacketSetSlot) {
                    e.setCancelled(true);
                }
            }
        } else {
            if (itemMode.getValue() == ItemMode.SPOOF) {
                if (e.getPacket() instanceof C09PacketHeldItemChange) {
                    C09PacketHeldItemChange packet = e.getPacket();
                    packet.setSlotId(lastSlot);
                }
            }

            if (mode.getValue() == Mode.VERUS) {
                if (e.getPacket() instanceof C08PacketPlayerBlockPlacement) {
                    C08PacketPlayerBlockPlacement packet = e.getPacket();
                    //packet.setFacingX(0);
                    //packet.setFacingY(0);
                    //packet.setFacingZ(0);
                }
            }
        }
    };

    private void placeBlock() {
        boolean placed = false;
        if (sprintFix.getValue() && mc.thePlayer.isSprinting())
            mc.getNetHandler().addToSendQueueNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
        if (canPlace()) placed = sendPlace();

        if (placed) {
            if (swing.getValue()) {
                mc.thePlayer.swingItem();
            } else {
                mc.getNetHandler().addToSendQueueNoEvent(new C0APacketAnimation());
            }
            new Thread(this::stopPlacing).start();
        }
    }

    private boolean sendPlace() {
        isPlacing = true;
        return mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, getPlacingItem(), info.getPos(), info.getFacing(), getHitVec(info));
    }

    private boolean canPlace() {
        boolean correctMotion =
                (mc.thePlayer.isMoving() || mc.thePlayer.motionY != 0);
        boolean correctBlockStuff =
                isReplaceable(info);
        boolean timerReady =
                placeTimer.hasTimeElapsed(placeDelay.getValue(), true);

        return correctMotion && correctBlockStuff && timerReady;
    }

    private ItemStack getPlacingItem() {
        return (itemMode.getValue() == ItemMode.SPOOF ? stackToPlace : (mc.thePlayer.getHeldItem() != null ? mc.thePlayer.getHeldItem() : null));
    }

    private Vec3 getHitVec(BlockInfo info) {
        switch (vecMode.getValue()) {
            case DIR:
                final Vec3 eyesPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
                Vec3 rotationVector = getVectorForRotation(finalRotationYaw, finalRotationPitch);
                return eyesPos.addVector(rotationVector.xCoord * 4, rotationVector.yCoord * 4, rotationVector.zCoord * 4);
            case POS:
                return new Vec3(info.getPos().getX(), info.getPos().getY(), info.getPos().getZ());
            case STRICT:
                BlockPos pos = info.getPos();
                EnumFacing face = info.getFacing();
                double x = (double) pos.getX() + 0.5, y = (double) pos.getY() + 0.5, z = (double) pos.getZ() + 0.5;
                if (face != EnumFacing.UP && face != EnumFacing.DOWN) {
                    y += 0.5;
                } else {
                    x += 0.3;
                    z += 0.3;
                }
                if (face == EnumFacing.WEST || face == EnumFacing.EAST) {
                    z += 0.15;
                }
                if (face == EnumFacing.SOUTH || face == EnumFacing.NORTH) {
                    x += 0.15;
                }
                return new Vec3(x, y, z);
            case LEGIT:
                double x1 = info.getPos().getX() + 0.5f + (0.25f * info.getFacing().getDirectionVec().getX()),
                        y1 = info.getPos().getY() + 0.5f + (0.25f * info.getFacing().getDirectionVec().getY()),
                        z1 = info.getPos().getZ() + 0.5f + (0.25f * info.getFacing().getDirectionVec().getZ());
                return new Vec3(x1, y1, z1);
            case FACING:
                Vec3 hitVec = new Vec3(info.getPos().getX() + Math.random(), info.getPos().getY() + Math.random(), info.getPos().getZ() + Math.random());

                final MovingObjectPosition movingObjectPosition = RayCastUtil.rayCast(rotationsVec, mc.playerController.getBlockReachDistance());

                EnumFacing enumFacing = info.getFacing();

                switch (enumFacing) {
                    case DOWN:
                        hitVec.yCoord = info.getPos().getY();
                        break;

                    case UP:
                        hitVec.yCoord = info.getPos().getY() + 1;
                        break;

                    case NORTH:
                        hitVec.zCoord = info.getPos().getZ();
                        break;

                    case EAST:
                        hitVec.xCoord = info.getPos().getX() + 1;
                        break;

                    case SOUTH:
                        hitVec.zCoord = info.getPos().getZ() + 1;
                        break;

                    case WEST:
                        hitVec.xCoord = info.getPos().getX();
                        break;
                }

                if (movingObjectPosition != null && movingObjectPosition.getBlockPos().equals(info.getPos()) &&
                        movingObjectPosition.sideHit == enumFacing) {
                    hitVec = movingObjectPosition.hitVec;
                }

                return hitVec;
            case ZERO:
            default:
                return new Vec3(0, 0, 0);
        }
    }

    private Vec3 getVectorForRotation(final float yaw, final float pitch) {
        float yawCos = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float yawSin = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float pitchCos = -MathHelper.cos(-pitch * 0.017453292F);
        float pitchSin = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
    }

    private void preTowerMotion(PreMotionEvent e) {
        switch (towerMode.getValue()) {
            case POSITION:
                if (mc.thePlayer.ticksExisted % 10 == 0)
                    for (int i = 0; i < 5; i++)
                        mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.2, mc.thePlayer.posZ);
                break;
            case SMALLHOP:
                if (mc.thePlayer.ticksExisted % 10 == 0)
                    mc.thePlayer.motionY = 0.2f;
                break;
            case VERUS:
                if (mc.thePlayer.ticksExisted % 2 == 0)
                    mc.thePlayer.jump();
                break;
            case WATCHDOG:
                if (mc.thePlayer.movementInput.jump && mc.thePlayer.isMoving() && getBlockCount() != 0) {
                    airTicks++;
                    int position = (int) mc.thePlayer.posY;

                    if (mc.thePlayer.onGround) {
                        airTicks = 0;
                    }

                    if (airTicks == 0 && mc.thePlayer.posY - position < 0.05) {
                        mc.thePlayer.posY = position;
                        mc.thePlayer.motionY = 0.42f;
                    }
                    if (airTicks == 1) {
                        mc.thePlayer.motionY = 0.33f;
                    }
                    if (airTicks == 2) {
                        mc.thePlayer.motionY = 1 - mc.thePlayer.posY % 1;
                        airTicks = -1;
                    }
                } else {
                    airTicks = 0;
                }
                break;
            case WATCHDOG_LOWHOP:
                if(!GameSettings.isKeyDown(mc.gameSettings.keyBindJump)) {
                    offGroundTicks = 0;
                    return;
                }
                if (!mc.thePlayer.isMoving()) {
                    offGroundTicks = 0;
                    return;
                }

                offGroundTicks = mc.thePlayer.onGround ? 0 : offGroundTicks + 1;

                if(mc.thePlayer.onGround) {
                    Logger.addChatMessage("motion y = 0.4191");
                    mc.thePlayer.motionY = 0.42;
                }

                if(offGroundTicks == 1) {
                    Logger.addChatMessage("motion y = 0.327318");
                    mc.thePlayer.motionY = 0.33;
                }

                if(offGroundTicks == 6) {
                    Logger.addChatMessage("motion y = -1.0");
                    offGroundTicks = 0;
                    mc.thePlayer.motionY = -1;
                }
                break;
            case NONE:
                break;
        }
    }

    private void moveTowerMotion(MoveEvent e) {
        switch (towerMode.getValue()) {
            case MOTION:
                if (canTower(1.0))
                    e.setY(mc.thePlayer.motionY = 0.42f);
                break;
            case NCP:
                if (mc.thePlayer.onGround)
                    towerTicks = 0;

                if (canTower(1.1)) {
                    int position = (int) mc.thePlayer.posY;
                    if (mc.thePlayer.posY - position < 0.05) {
                        mc.thePlayer.posY = position;
                        e.setY(mc.thePlayer.motionY = 0.42f);
                        towerTicks = 1;
                    } else if (towerTicks == 1) {
                        e.setY(mc.thePlayer.motionY = 0.34f);
                        towerTicks++;
                    } else if (towerTicks == 2) {
                        e.setY(mc.thePlayer.motionY = 0.25f);
                        towerTicks++;
                    }
                }
                break;
            case WATCHDOG:
                break;
            case NONE:
                break;
        }
    }

    private boolean canTower(final double down) {
        return towerMode.getValue() != TowerMode.NONE &&
                mc.gameSettings.keyBindJump.isKeyDown() &&
                (!mc.thePlayer.isMoving() || towerMove.getValue()) &&
                !(mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - down, mc.thePlayer.posZ)).getBlock() instanceof BlockAir);
    }

    private void setRotations(PreMotionEvent e) {
        float[] rots;
        float yaw, pitch;
        switch (rotations.getValue()) {
            case NORMAL:
                rots = getRotations(info);
                yaw = processRotation(rots[0]);
                pitch = processRotation(rots[1]);
                e.setYaw(mc.thePlayer.rotationYawHead = mc.thePlayer.renderYawOffset = finalRotationYaw = yaw);
                e.setPitch(mc.thePlayer.rotationPitchHead = finalRotationPitch = pitch);
                rotationsVec = new Vector2f(yaw, pitch);
                break;
            case AAC:
                rots = getAACRotations();
                yaw = rots[0];
                pitch = processRotation(rots[1]);
                e.setYaw(mc.thePlayer.rotationYawHead = mc.thePlayer.renderYawOffset = finalRotationYaw = yaw);
                e.setPitch(mc.thePlayer.rotationPitchHead = finalRotationPitch = pitch);
                rotationsVec = new Vector2f(yaw, pitch);
                break;
            case BRUTE_FORCE:
                rots = getBruteForceRotations(info);
                yaw = processRotation(rots[0]);
                pitch = processRotation(rots[1]);
                e.setYaw(mc.thePlayer.rotationYawHead = mc.thePlayer.renderYawOffset = finalRotationYaw = yaw);
                e.setPitch(mc.thePlayer.rotationPitchHead = finalRotationPitch = pitch);
                rotationsVec = new Vector2f(yaw, pitch);
                break;
            case SNAP:
                if (isPlacing) {
                    rots = getRotations(info);
                    yaw = processRotation(rots[0]);
                    pitch = processRotation(rots[1]);
                    e.setYaw(mc.thePlayer.rotationYawHead = finalRotationYaw = yaw);
                    e.setPitch(mc.thePlayer.rotationPitchHead = finalRotationPitch = pitch);
                    rotationsVec = new Vector2f(yaw, pitch);
                } else {
                    e.setYaw(mc.thePlayer.rotationYawHead = finalRotationYaw = mc.thePlayer.rotationYaw);
                    e.setPitch(mc.thePlayer.rotationPitchHead = finalRotationPitch = mc.thePlayer.rotationPitch);
                }
                break;
            case WATCHDOG:
                rots = getRotations(info);
                /*pitch = processRotation(rots[1]);
                double xDiff = info.getPos().getX() - mc.thePlayer.posX;
                double yDiff = info.getPos().getY() - mc.thePlayer.posY - 1.7;
                double zDiff = info.getPos().getZ() - mc.thePlayer.posZ;
                double dist = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff);
                yaw = (float) (Math.atan2(zDiff, xDiff) * 180.0D / Math.PI) - 90.0F;
                pitch = (float) -(Math.atan2(yDiff, dist) * 180.0D / Math.PI);
                pitch = 82;
                yaw = processRotation(mc.thePlayer.getDirection() - 150);
                yaw = processRotation(mc.thePlayer.rotationYaw + 180.0F);
                */

                yaw = processRotation(mc.thePlayer.getDirection() + 180.0F);
                if ((towerMode.getValue() == TowerMode.NONE || towerMode.getValue() == TowerMode.WATCHDOG_LOWHOP) && mc.thePlayer.movementInput.jump) {
                    pitch = processRotation(rots[1]);
                } else {
                    pitch = (float) (81.0 + Math.random() / 100.0);
                    pitch = processRotation((mc.thePlayer.movementInput.jump ? 90 : pitch));
                }
                mc.thePlayer.renderYawOffset = mc.thePlayer.getDirection() + 180.0F;
                e.setYaw(mc.thePlayer.rotationYawHead = finalRotationYaw = yaw);
                e.setPitch(mc.thePlayer.rotationPitchHead = finalRotationPitch = pitch);
                rotationsVec = new Vector2f(yaw, pitch);
                break;
        }
    }

    private float[] getRotations(BlockInfo info) {
        if (mc.thePlayer == null || mc.theWorld == null) this.toggle();
        float yaw = 0, pitch = 90;

        final Vec3 eyes = mc.thePlayer.getPositionEyes(RandomUtils.nextFloat(2.997f, 3.997f));
        final Vec3 position = new Vec3(info.getPos().getX() + 0.49, info.getPos().getY() + 0.49, info.getPos().getZ() + 0.49).add(new Vec3(info.getFacing().getDirectionVec()));
        final Vec3 resultPosition = position.subtract(eyes);
        yaw = (float) Math.toDegrees(Math.atan2(resultPosition.zCoord, resultPosition.xCoord)) - 90.0F;
        pitch = (float) -Math.toDegrees(Math.atan2(resultPosition.yCoord, Math.hypot(resultPosition.xCoord, resultPosition.zCoord)));

        //pitch = 83;
        return new float[]{yaw, pitch};
    }

    private float[] getAACRotations() {
        if (mc.thePlayer == null || mc.theWorld == null) this.toggle();
        float clientYaw = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw);
        float diff = (int) ((blockYaw - clientYaw) / 45.0F);

        float yaw = clientYaw + diff * 45;

        float pitch;

        if (!mc.thePlayer.onGround || mc.gameSettings.keyBindJump.isKeyDown()) {
            pitch = 90;
        } else if (isGoingDiagonally()) {
            pitch = 83;
        } else {
            pitch = 81.5F;
        }
        return new float[]{yaw, pitch};
    }

    private boolean isGoingDiagonally() {
        return Math.abs(mc.thePlayer.motionX) > 0.08 && Math.abs(mc.thePlayer.motionZ) > 0.08;
    }

    private float[] getBruteForceRotations(BlockInfo info) {
        if (mc.thePlayer == null || mc.theWorld == null) this.toggle();

        float yaw = mc.thePlayer.rotationYaw - 180, pitch = getRotations(info)[1];

        float ogyaw = yaw;

        for (int i = (int) ogyaw; i < ogyaw - 180; i++) {
            yaw = MathHelper.wrapAngleTo180_float(i);
            Vec3 src = mc.thePlayer.getPositionEyes(1.0F);
            Vec3 rotationVec = Entity.getVectorForRotation(pitch, i);
            Vec3 dest = src.addVector(rotationVec.xCoord * 3, rotationVec.yCoord * 3, rotationVec.zCoord * 3);
            IBlockState blockState = mc.theWorld.getBlockState(info.pos);
            AxisAlignedBB bb = blockState.getBlock().getCollisionBoundingBox(mc.theWorld, info.pos, blockState);

            if (bb.calculateIntercept(src, dest) != null) {
                yaw = i;
                break;
            }
        }

        return new float[]{yaw, pitch};
    }

    private BlockInfo getDiagonalBlockInfo(BlockPos pos) {
        BlockPos up = new BlockPos(0, -1, 0),
                east = new BlockPos(-1, 0, 0),
                west = new BlockPos(1, 0, 0),
                north = new BlockPos(0, 0, 1),
                south = new BlockPos(0, 0, -1);

        if (canPlaceAt(pos.add(up))) {
            return new BlockInfo(pos.add(up), EnumFacing.UP);
        }

        if (canPlaceAt(pos.add(east))) {
            blockYaw = 90;
            return new BlockInfo(pos.add(east), EnumFacing.EAST);
        }

        if (canPlaceAt(pos.add(west))) {
            blockYaw = -90;
            return new BlockInfo(pos.add(west), EnumFacing.WEST);
        }

        if (canPlaceAt(pos.add(south))) {
            blockYaw = 180;
            return new BlockInfo(pos.add(south), EnumFacing.SOUTH);
        }

        if (canPlaceAt(pos.add(north))) {
            blockYaw = 0;
            return new BlockInfo(pos.add(north), EnumFacing.NORTH);
        }

        BlockPos[] positions = {east, west, south, north};
        BlockInfo data;

        for (BlockPos offset : positions) {
            if ((data = getBlockInfo(pos.add(offset))) != null) {
                return data;
            }
        }

        for (BlockPos offset1 : positions)
            for (BlockPos offset2 : positions)
                if ((data = getBlockInfo(pos.add(offset1).add(offset2))) != null) {
                    return data;
                }

        for (BlockPos offset1 : positions)
            for (BlockPos offset2 : positions)
                for (BlockPos offset3 : positions)
                    if ((data = getBlockInfo(pos.add(offset1).add(offset2).add(offset3))) != null) {
                        return data;
                    }


        return new BlockInfo(pos, EnumFacing.DOWN);
    }

    private BlockInfo getBlockInfo(BlockPos pos) {
        if (mc.theWorld.getBlockState(pos.add(0, -1, 0)).getBlock() != Blocks.air) {
            return new BlockInfo(pos.add(0, -1, 0), EnumFacing.UP);
        } else if (mc.theWorld.getBlockState(pos.add(-1, 0, 0)).getBlock() != Blocks.air) {
            blockYaw = 90;
            return new BlockInfo(pos.add(-1, 0, 0), EnumFacing.EAST);
        } else if (mc.theWorld.getBlockState(pos.add(1, 0, 0)).getBlock() != Blocks.air) {
            blockYaw = -90;
            new BlockInfo(pos.add(1, 0, 0), EnumFacing.WEST);
        } else if (mc.theWorld.getBlockState(pos.add(0, 0, -1)).getBlock() != Blocks.air) {
            blockYaw = 180;
            return new BlockInfo(pos.add(0, 0, -1), EnumFacing.SOUTH);
        } else if (mc.theWorld.getBlockState(pos.add(0, 0, 1)).getBlock() != Blocks.air) {
            blockYaw = 0;
            return new BlockInfo(pos.add(0, 0, 1), EnumFacing.NORTH);
        }
        return null;
    }

    public boolean canPlaceAt(BlockPos pos) {
        return mc.theWorld.getBlockState(pos).getBlock() != Blocks.air;
    }

    private ItemStack setStackToPlace() {
        ItemStack block = mc.thePlayer.getCurrentEquippedItem();
        if (block != null && block.getItem() != null && !(block.getItem() instanceof ItemBlock)) {
            block = null;
        }

        int slot = lastSlot;
        for (short g = 0; g < 9; g++) {
            if (mc.thePlayer.inventoryContainer.getSlot(g + 36).getHasStack() &&
                    isValidBlock(mc.thePlayer.inventoryContainer.getSlot(g + 36).getStack())
                    && (block == null)) {

                if (mc.thePlayer.inventoryContainer.getSlot(g + 36).getStack().stackSize <= 0) continue;
                slot = g;
                block = mc.thePlayer.inventoryContainer.getSlot(g + 36).getStack();
            }
        }

        if (lastSlot != slot) {
            if (itemMode.getValue() == ItemMode.SWITCH) mc.thePlayer.inventory.currentItem = slot;
            else mc.getNetHandler().addToSendQueueNoEvent(new C09PacketHeldItemChange(slot));

            lastSlot = slot;
        }
        return block;

        /*int mostBlocksSlot = findMostBlocksHotbarSlot();
        if (mostBlocksSlot != -1 && mostBlocksSlot != lastSlot) {
            if (itemMode.getValue() == ItemMode.SWITCH) {
                mc.thePlayer.inventory.currentItem = mostBlocksSlot;
            } else {
                mc.getNetHandler().addToSendQueueNoEvent(new C09PacketHeldItemChange(mostBlocksSlot));
            }
            lastSlot = mostBlocksSlot;
        }

        return block;*/
    }

    private int findMostBlocksHotbarSlot() {
        int mostBlocksSlot = -1;
        int mostBlocksCount = 0;

        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(hotbarSlot + 36).getStack();
            if (stack != null && stack.getItem() instanceof ItemBlock) {
                int blockCount = stack.stackSize;
                if (blockCount > mostBlocksCount) {
                    mostBlocksCount = blockCount;
                    mostBlocksSlot = hotbarSlot;
                }
            }
        }

        return mostBlocksSlot;
    }

    private boolean isValidBlock(ItemStack stack) {
        return (stack.getItem() instanceof ItemBlock) &&
                !((ItemBlock) stack.getItem()).getBlock().getLocalizedName().toLowerCase().contains("chest") &&
                !((ItemBlock) stack.getItem()).getBlock().getLocalizedName().toLowerCase().contains("table") &&
                !((ItemBlock) stack.getItem()).getBlock().getLocalizedName().toLowerCase().contains("tnt") &&
                !((ItemBlock) stack.getItem()).getBlock().getLocalizedName().toLowerCase().contains("slab");
    }

    private int getBlockCount() {
        int blockCount = 0;
        for (int i = 0; i < 45; i++) {
            Slot slot;
            try {
                slot = mc.thePlayer.inventoryContainer.getSlot(i);
            } catch (Exception ex) {
                continue;
            }

            if (!slot.getHasStack()) continue;

            ItemStack stack = slot.getStack();

            if (!isValidBlock(stack)) continue;

            blockCount += stack.stackSize;
        }
        return blockCount;
    }

    private void stopPlacing() {
        try {
            Thread.sleep(100);
            isPlacing = false;
        } catch (Exception ignored) {
        }
    }

    private float processRotation(float value) {
        float toReturn = value;
        if (gcdFix.getValue()) {
            double m = 0.005 * mc.gameSettings.mouseSensitivity;
            double gcd = m * m * m * 1.2;
            toReturn -= toReturn % gcd;
            return MathHelper.wrapAngleTo180_float(toReturn);
        } else return MathHelper.wrapAngleTo180_float(toReturn);
    }

    private boolean isReplaceable(BlockInfo info) {
        return mc.theWorld.getBlockState(info.pos).getBlock().canCollideCheck(mc.theWorld.getBlockState(info.pos), false);
    }

    public void onEnable() {
        super.onEnable();
        lastSlot = mc.thePlayer.inventory.currentItem;
        oldSlot = mc.thePlayer.inventory.currentItem;
        blockYaw = mc.thePlayer.rotationYaw - 180;
        sprintTicks = 0;
        towerTicks = 0;
        sneaking = false;
        isPlacing = false;
        firstJump = true;
        yCoordinate = mc.thePlayer.posY - 1;
        yCoordinateUpdated = false;
        fuckedUpAndJumped = false;
        fuckedUp = false;
        placeTimer.reset();
        firstJumpDelay.reset();
        blockCount = getBlockCount();

        if (mode.getValue() == Mode.WATCHDOG_SPRINT) {
            mc.thePlayer.motionX = 0.0f;
            mc.thePlayer.motionY = 0.42f;
            mc.thePlayer.motionZ = 0.0f;
        }
    }


    public void onDisable() {
        super.onDisable();
        lastSlot = mc.thePlayer.inventory.currentItem;
        mc.gameSettings.keyBindSneak.pressed = false;
        mc.timer.timerSpeed = 1F;

        if (itemMode.getValue() == ItemMode.SPOOF) {
            mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
        } else {
            mc.thePlayer.inventory.currentItem = oldSlot;
        }
    }

    enum Mode {
        NORMAL, WATCHDOG_JUMP, WATCHDOG, VERUS, VULCAN, WATCHDOG_SPRINT
    }

    enum Rotations {
        NORMAL, AAC, BRUTE_FORCE, SNAP, NONE, WATCHDOG
    }

    enum PlaceMode {
        PRE, POST
    }

    enum ItemMode {
        SWITCH, SPOOF
    }

    enum TowerMode {
        NONE, MOTION, POSITION, NCP, WATCHDOG, SMALLHOP, VERUS, WATCHDOG_LOWHOP
    }

    enum VecMode {
        FACING, ZERO, POS, LEGIT, DIR, STRICT
    }

    @Getter
    private static class BlockInfo {
        private final BlockPos pos;
        private final EnumFacing facing;

        public BlockInfo(BlockPos position, EnumFacing face) {
            this.pos = position;
            this.facing = face;
        }

        public BlockPos getPos() {
            if (pos == null) {
                return new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ);
            } else return pos;
        }
    }
}
