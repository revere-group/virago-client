package dev.revere.virago.util;

import com.google.common.collect.Multimap;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.Iterator;
import java.util.List;

import static dev.revere.virago.util.ItemUtil.getItemDamage;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/2/2024
 */
public class InventoryUtil {

    public static final int INCLUDE_ARMOR_BEGIN = 5;
    public static final int EXCLUDE_ARMOR_BEGIN = 9;
    public static final int ONLY_HOT_BAR_BEGIN = 36;
    public static final int END = 45;

    public static void windowClick(Minecraft mc, int windowId, int slotId, int mouseButtonClicked, ClickType mode) {
        mc.playerController.windowClick(windowId, slotId,
                mouseButtonClicked, mode.ordinal(), mc.thePlayer);
    }

    public static void windowClick(Minecraft mc, int slotId, int mouseButtonClicked, ClickType mode) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slotId,
                mouseButtonClicked, mode.ordinal(), mc.thePlayer);
    }

    public enum ClickType {
        CLICK, SHIFT_CLICK, SWAP_WITH_HOT_BAR_SLOT, PLACEHOLDER, DROP_ITEM
    }

    public static boolean isGoodItem(final Item item) {
        return item instanceof ItemEnderPearl || item == Items.arrow;
    }

    public static boolean isBestSword(final EntityPlayerSP player,
                                      final ItemStack itemStack) {
        double damage = 0.0;
        ItemStack bestStack = null;

        for (int i = EXCLUDE_ARMOR_BEGIN; i < END; i++) {
            final ItemStack stack = player.inventoryContainer.getSlot(i).getStack();

            if (stack != null && stack.getItem() instanceof ItemSword) {
                double newDamage = getItemDamage(stack);

                if (newDamage > damage) {
                    damage = newDamage;
                    bestStack = stack;
                }
            }
        }

        return bestStack == itemStack || getItemDamage(itemStack) > damage;
    }

    public static boolean isBestArmor(final EntityPlayerSP player,
                                      final ItemStack itemStack) {
        final ItemArmor itemArmor = (ItemArmor) itemStack.getItem();

        double reduction = 0.0;
        ItemStack bestStack = null;

        for (int i = INCLUDE_ARMOR_BEGIN; i < END; i++) {
            final ItemStack stack = player.inventoryContainer.getSlot(i).getStack();

            if (stack != null && stack.getItem() instanceof ItemArmor) {
                final ItemArmor stackArmor = (ItemArmor) stack.getItem();
                if (stackArmor.armorType == itemArmor.armorType) {
                    final double newReduction = getDamageReduction(stack);

                    if (newReduction > reduction) {
                        reduction = newReduction;
                        bestStack = stack;
                    }
                }
            }
        }

        return bestStack == itemStack || getDamageReduction(itemStack) > reduction;
    }

    public static int getToolType(final ItemStack stack) {
        final ItemTool tool = (ItemTool) stack.getItem();

        if (tool instanceof ItemPickaxe) return 0;
        else if (tool instanceof ItemAxe) return 1;
        else if (tool instanceof ItemSpade) return 2;
        else return -1;
    }

    public static boolean isBestTool(final EntityPlayerSP player, final ItemStack itemStack) {
        final int type = getToolType(itemStack);

        Tool bestTool = new Tool(-1, -1, null);

        for (int i = EXCLUDE_ARMOR_BEGIN; i < END; i++) {
            final ItemStack stack = player.inventoryContainer.getSlot(i).getStack();

            if (stack != null && stack.getItem() instanceof ItemTool && type == getToolType(stack)) {
                final double efficiency = getToolEfficiency(stack);
                if (efficiency > bestTool.getEfficiency())
                    bestTool = new Tool(i, efficiency, stack);
            }
        }

        return bestTool.getStack() == itemStack ||
                getToolEfficiency(itemStack) > bestTool.getEfficiency();
    }

    public static boolean isBestBow(final EntityPlayerSP player,
                                    final ItemStack itemStack) {
        double bestBowDmg = -1.0;
        ItemStack bestBow = null;

        for (int i = EXCLUDE_ARMOR_BEGIN; i < END; i++) {
            final ItemStack stack = player.inventoryContainer.getSlot(i).getStack();
            if (stack != null && stack.getItem() instanceof ItemBow) {
                final double damage = getBowDamage(stack);

                if (damage > bestBowDmg) {
                    bestBow = stack;
                    bestBowDmg = damage;
                }
            }
        }

        return itemStack == bestBow || getBowDamage(itemStack) > bestBowDmg;
    }

    public static double getDamageReduction(final ItemStack stack) {
        double reduction = 0.0;

        final ItemArmor armor = (ItemArmor) stack.getItem();

        reduction += armor.damageReduceAmount;

        if (stack.isItemEnchanted())
            reduction += EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack) * 0.25;

        return reduction;
    }

    public static boolean isBuffPotion(final ItemStack stack) {
        final ItemPotion potion = (ItemPotion) stack.getItem();
        final List<PotionEffect> effects = potion.getEffects(stack);

        for (final PotionEffect effect : effects)
            if (Potion.potionTypes[effect.getPotionID()].isBadEffect())
                return false;

        return true;
    }

    public static double getBowDamage(ItemStack stack) {
        double damage = 0.0;

        if (stack.getItem() instanceof ItemBow && stack.isItemEnchanted())
            damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);

        return damage;
    }

    public static boolean isGoodFood(final ItemStack stack) {
        final ItemFood food = (ItemFood) stack.getItem();

        if (food instanceof ItemAppleGold)
            return true;

        return food.getHealAmount(stack) >= 4 && food.getSaturationModifier(stack) >= 0.3F;
    }

    public static float getToolEfficiency(final ItemStack itemStack) {
        final ItemTool tool = (ItemTool) itemStack.getItem();

        float efficiency = tool.getEfficiencyOnProperMaterial();

        final int lvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack);

        if (efficiency > 1.0F && lvl > 0)
            efficiency += lvl * lvl + 1;

        return efficiency;
    }

    public static double getItemDamage(final ItemStack stack) {
        double damage = 0.0;

        final Multimap<String, AttributeModifier> attributeModifierMap = stack.getAttributeModifiers();

        for (final String attributeName : attributeModifierMap.keySet()) {
            if (attributeName.equals("generic.attackDamage")) {
                final Iterator<AttributeModifier> attributeModifiers = attributeModifierMap.get(attributeName).iterator();
                if (attributeModifiers.hasNext())
                    damage += attributeModifiers.next().getAmount();
                break;
            }
        }

        if (stack.isItemEnchanted()) {
            damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);
            damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25;
        }

        return damage;
    }

    public static boolean isStackValidToPlace(final ItemStack stack) {
        return stack.stackSize >= 1 && validateBlock(Block.getBlockFromItem(stack.getItem()), BlockAction.PLACE);
    }

    public enum BlockAction {
        PLACE, REPLACE, PLACE_ON
    }

    public static boolean validateBlock(final Block block, final BlockAction action) {
        if (block instanceof BlockContainer) return false;
        final Material material = block.getMaterial();

        switch (action) {
            case PLACE:
                return !(block instanceof BlockFalling) && block.isFullBlock() && block.isFullCube();
            case REPLACE:
                return material.isReplaceable();
            case PLACE_ON:
                return !(block instanceof BlockAir);
        }

        return true;
    }

    private static class Tool {
        private final int slot;
        private final double efficiency;
        private final ItemStack stack;

        public Tool(int slot, double efficiency, ItemStack stack) {
            this.slot = slot;
            this.efficiency = efficiency;
            this.stack = stack;
        }

        public int getSlot() {
            return slot;
        }

        public double getEfficiency() {
            return efficiency;
        }

        public ItemStack getStack() {
            return stack;
        }
    }
}
