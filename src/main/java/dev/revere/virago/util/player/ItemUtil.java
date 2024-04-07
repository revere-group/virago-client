package dev.revere.virago.util.player;

import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Remi
 * @project Virago-Client
 * @date 3/28/2024
 */
public class ItemUtil
{

    public static ItemStack findBestSword() {
        ItemStack best = null;
        float swordDamage = 0.0f;
        for (int i = 9; i < 45; ++i) {
            if (Minecraft.getMinecraft().thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                final ItemStack is = Minecraft.getMinecraft().thePlayer.inventoryContainer.getSlot(i).getStack();
                if (is.getItem() instanceof ItemSword) {
                    final float swordD = (float) InventoryUtil.getItemDamage(is);
                    if (swordD > swordDamage) {
                        swordDamage = swordD;
                        best = is;
                    }
                }
            }
        }
        return best;
    }

    /**
     * Compare the damage between two items.
     *
     * @param item1 the first item
     * @param item2 the second item
     * @return the item with the most damage
     */
    public static ItemStack compareDamage(final ItemStack item1, final ItemStack item2) {
        try {
            if (item1 == null || item2 == null) {
                return null;
            }
            if (!(item1.getItem() instanceof ItemSword) && item2.getItem() instanceof ItemSword) {
                return null;
            }
            if (InventoryUtil.getItemDamage(item1) > InventoryUtil.getItemDamage(item2)) {
                return item1;
            }
            if (InventoryUtil.getItemDamage(item2) > InventoryUtil.getItemDamage(item1)) {
                return item2;
            }
            return item1;
        }
        catch (NullPointerException e) {
            return item1;
        }
    }

    /**
     * Check if an item is valid.
     *
     * @param stack the item stack
     * @return if the item is valid
     */
    public static boolean isValidItem(final ItemStack stack) {
        final Item item = stack.getItem();
        if (item instanceof ItemBlock) {
            final Block block = ((ItemBlock) item).getBlock();
            if (block instanceof BlockGlass || block instanceof BlockStainedGlass || (block.isFullBlock() && !(block instanceof BlockTNT || block instanceof BlockSlime || block instanceof BlockFalling))) {
                return true;
            }
        }

        return ItemUtil.compareDamage(stack, ItemUtil.findBestSword()) != null && ItemUtil.compareDamage(stack, ItemUtil.findBestSword()) == stack || stack.getItem() instanceof ItemBlock || stack.getItem() instanceof ItemPotion && InventoryUtil.isBuffPotion(stack) || stack.getItem() instanceof ItemArmor || stack.getItem() instanceof ItemAppleGold || stack.getItem() instanceof ItemFood || stack.getItem() instanceof ItemSword || stack.getItem() instanceof ItemBow || stack.getItem().getUnlocalizedName().contains("arrow");
    }
}
