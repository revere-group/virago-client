package dev.revere.virago.client.modules.player;

import dev.revere.virago.Virago;
import dev.revere.virago.api.event.handler.EventHandler;
import dev.revere.virago.api.event.handler.Listener;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.api.module.ModuleData;
import dev.revere.virago.api.setting.Setting;
import dev.revere.virago.client.events.packet.TeleportEvent;
import dev.revere.virago.client.events.player.PreMotionEvent;
import dev.revere.virago.client.modules.combat.KillAura;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.util.player.PlayerUtil;
import dev.revere.virago.util.rotation.RayCastUtil;
import dev.revere.virago.util.rotation.RotationUtil;
import dev.revere.virago.util.rotation.vec.Vector3d;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockBed;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/19/2024
 */
@ModuleData(name = "Breaker", description = "Automatically break blocks for you.", type = EnumModuleType.PLAYER)
public class Breaker extends AbstractModule {

    private final Setting<Boolean> instantBreak = new Setting<>("Instant Break", false);
    private final Setting<Boolean> throughWalls = new Setting<>("Through Walls", true);
    private final Setting<Boolean> emptySurroundings = new Setting<>("Empty Surroundings", true).visibleWhen(throughWalls::getValue);
    private final Setting<Boolean> rotations = new Setting<>("Rotations", true);
    private final Setting<Boolean> preventOwnBed = new Setting<>("Prevent own bed", true);

    private Vector3d block;
    private Vector3d lastBlock;
    private Vector3d ownLocation;
    private double damage;

    @EventHandler
    private final Listener<PreMotionEvent> preMotionEventListener = event -> {
        KillAura killAura = Virago.getInstance().getServiceManager().getService(ModuleService.class).getModule(KillAura.class);
        if (mc.gameSettings.keyBindAttack.isKeyDown() || killAura.isEnabled()) return;

        lastBlock = block;
        block = this.getBlock();
        if (block == null) return;

        if (this.rotations.getValue()) {
            float[] rotations = RotationUtil.getRotationFromPosition(block.getX(), block.getY(), block.getZ());
            event.setYaw(rotations[0]);
            event.setPitch(rotations[1]);

            mc.thePlayer.renderYawOffset = rotations[0];
            mc.thePlayer.rotationYawHead = rotations[0];
            mc.thePlayer.rotationPitchHead = rotations[1];
        }

        if (lastBlock == null || !lastBlock.equals(block)) {
            damage = 0;
        }

        this.destroy();
    };

    @EventHandler
    private final Listener<TeleportEvent> teleportEventListener = event -> {
        final double distance = mc.thePlayer.getDistance(event.getPosX(), event.getPosY(), event.getPosZ());

        if (distance > 40) {
            ownLocation = new Vector3d(event.getPosX(), event.getPosY(), event.getPosZ());
        }
    };

    public Vector3d getBlock() {
        if (ownLocation != null && mc.thePlayer.getDistanceSq(ownLocation.getX(), ownLocation.getY(), ownLocation.getZ()) < 35 * 35 && preventOwnBed.getValue()) {
            return null;
        }

        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {

                    final Block block = PlayerUtil.blockRelativeToPlayer(x, y, z);
                    final Vector3d position = new Vector3d(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z);

                    if (!(block instanceof BlockBed)) {
                        continue;
                    }

                    final MovingObjectPosition movingObjectPosition = RayCastUtil.rayCast(RotationUtil.calculate(position), 3.5f);
                    if (movingObjectPosition == null || movingObjectPosition.hitVec.distanceTo(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)) > 3.5) {
                        continue;
                    }

                    if (!throughWalls.getValue()) {
                        final BlockPos blockPos = movingObjectPosition.getBlockPos();
                        if (!blockPos.equalsVector(position)) {
                            continue;
                        }
                    } else if (emptySurroundings.getValue()) {
                        Vector3d addVec = position;
                        double hardness = Double.MAX_VALUE;
                        boolean empty = false;

                        for (int addX = -1; addX <= 1; addX++) {
                            for (int addY = 0; addY <= 1; addY++) {
                                for (int addZ = -1; addZ <= 1; addZ++) {
                                    if (empty || (mc.thePlayer.getDistanceSq(position.getX() + addX, position.getY() + addY, position.getZ() + addZ) + 3 > 3.5 * 3.5))
                                        continue;

                                    if (Math.abs(addX) + Math.abs(addY) + Math.abs(addZ) != 1) {
                                        continue;
                                    }

                                    Block possibleBlock = PlayerUtil.block(position.getX() + addX, position.getY() + addY, position.getZ() + addZ);

                                    if (possibleBlock instanceof BlockBed) {
                                        continue;
                                    } else if (possibleBlock instanceof BlockAir) {
                                        empty = true;
                                        continue;
                                    }

                                    double possibleHardness = possibleBlock.getBlockHardness();

                                    if (possibleHardness < hardness) {
                                        hardness = possibleHardness;
                                        addVec = position.add(addX, addY, addZ);
                                    }
                                }
                            }
                        }

                        if (!empty) {
                            if (addVec.equals(position)) {
                                return null;
                            } else {
                                return addVec;
                            }
                        }
                    }

                    return position;
                }
            }
        }

        return null;
    }

    public void updateDamage(final BlockPos blockPos, final double hardness) {
        damage += hardness;
        mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), blockPos, (int) (damage * 10 - 1));
    }

    public void destroy() {
        final BlockPos blockPos = new BlockPos(block.getX(), block.getY(), block.getZ());
        final int originalSlot = getItemIndex();

        int bestSlot = findBestToolSlot(blockPos);
        boolean isWool = mc.theWorld.getBlockState(blockPos).getBlock().getMaterial() == Material.cloth;

        if (isWool) {
            bestSlot = findShearsSlot();
        }
        if (bestSlot != originalSlot) {
            mc.thePlayer.inventory.currentItem = bestSlot;
            mc.playerController.updateController();
        }

        final double hardness = getPlayerRelativeBlockHardness(mc.theWorld, blockPos, getItemIndex());
        if (instantBreak.getValue()) {
            mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos, EnumFacing.UP));
            mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos, EnumFacing.UP));
            mc.playerController.onPlayerDestroyBlock(blockPos, EnumFacing.DOWN);
        } else {
            if (damage <= 0) {
                mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos, EnumFacing.UP));

                if (hardness >= 1) {
                    mc.playerController.onPlayerDestroyBlock(blockPos, EnumFacing.DOWN);
                    damage = 0;
                }

                this.updateDamage(blockPos, hardness);
            } else if (damage > 1) {
                mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos, EnumFacing.UP));
                mc.playerController.onPlayerDestroyBlock(blockPos, EnumFacing.DOWN);
                damage = 0;
                this.updateDamage(blockPos, hardness);
            } else {
                this.updateDamage(blockPos, hardness);
            }

            mc.thePlayer.swingItem();
        }
        if (bestSlot != originalSlot) {
            mc.thePlayer.inventory.currentItem = originalSlot;
            mc.playerController.updateController();
        }
    }

    private int findShearsSlot() {
        for (int slot = 0; slot < mc.thePlayer.inventory.mainInventory.length; slot++) {
            ItemStack itemStack = mc.thePlayer.inventory.mainInventory[slot];
            if (itemStack != null && itemStack.getItem() instanceof ItemShears) {
                return slot;
            }
        }
        return getItemIndex();
    }

    private int findBestToolSlot(BlockPos blockPos) {
        float bestEfficiency = 0;
        int bestSlot = getItemIndex();

        for (int slot = 0; slot < mc.thePlayer.inventory.mainInventory.length; slot++) {
            ItemStack itemStack = mc.thePlayer.inventory.mainInventory[slot];
            if (itemStack != null && itemStack.getItem() instanceof ItemTool) {
                float efficiency = getToolDigEfficiency(mc.theWorld.getBlockState(blockPos).getBlock(), slot);
                if (efficiency > bestEfficiency) {
                    bestEfficiency = efficiency;
                    bestSlot = slot;
                }
            }
        }

        return bestSlot;
    }

    private int getItemIndex() {
        return mc.thePlayer.inventory.currentItem;
    }

    private float getPlayerRelativeBlockHardness(final World worldIn, final BlockPos pos, final int slot) {
        final Block block = mc.theWorld.getBlockState(pos).getBlock();
        final float f = block.getBlockHardness(worldIn, pos);
        return f < 0.0F ? 0.0F : (!canHeldItemHarvest(block, slot) ? getToolDigEfficiency(block, slot) / f / 100.0F : getToolDigEfficiency(block, slot) / f / 30.0F);
    }

    private boolean canHeldItemHarvest(final Block blockIn, final int slot) {
        if (blockIn.getMaterial().isToolNotRequired()) {
            return true;
        } else {
            final ItemStack itemstack = mc.thePlayer.inventory.getStackInSlot(slot);
            return itemstack != null && itemstack.canHarvestBlock(blockIn);
        }
    }

    private ItemStack getCurrentItemInSlot(final int slot) {
        return slot < 9 && slot >= 0 ? mc.thePlayer.inventory.mainInventory[slot] : null;
    }

    private float getStrVsBlock(final Block blockIn, final int slot) {
        float f = 1.0F;

        if (mc.thePlayer.inventory.mainInventory[slot] != null) {
            f *= mc.thePlayer.inventory.mainInventory[slot].getStrVsBlock(blockIn);
        }
        return f;
    }

    private float getToolDigEfficiency(final Block blockIn, final int slot) {
        float f = getStrVsBlock(blockIn, slot);

        if (f > 1.0F) {
            final int i = EnchantmentHelper.getEfficiencyModifier(mc.thePlayer);
            final ItemStack itemstack = getCurrentItemInSlot(slot);

            if (i > 0 && itemstack != null) {
                f += (float) (i * i + 1);
            }
        }

        if (mc.thePlayer.isPotionActive(Potion.digSpeed)) {
            f *= 1.0F + (float) (mc.thePlayer.getActivePotionEffect(Potion.digSpeed).getAmplifier() + 1) * 0.2F;
        }

        if (mc.thePlayer.isPotionActive(Potion.digSlowdown)) {
            final float f1;

            switch (mc.thePlayer.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) {
                case 0:
                    f1 = 0.3F;
                    break;

                case 1:
                    f1 = 0.09F;
                    break;

                case 2:
                    f1 = 0.0027F;
                    break;

                case 3:
                default:
                    f1 = 8.1E-4F;
            }

            f *= f1;
        }

        if (mc.thePlayer.isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier(mc.thePlayer)) {
            f /= 5.0F;
        }

        if (!mc.thePlayer.onGround) {
            f /= 5.0F;
        }
        return f;
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
