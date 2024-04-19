package dev.revere.virago.util.player;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/19/2024
 */
public class PlayerUtil {

    /**
     * Gets a block at the specified coordinates.
     *
     * @param x the x
     * @param y the y
     * @param z the z
     * @return the block
     */
    public static Block block(final double x, final double y, final double z) {
        return Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    /**
     * Gets a block relative to the player.
     *
     * @param offsetX the offset x
     * @param offsetY the offset y
     * @param offsetZ the offset z
     * @return the block relative to player
     */
    public static Block blockRelativeToPlayer(final double offsetX, final double offsetY, final double offsetZ) {
        return Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(Minecraft.getMinecraft().thePlayer).add(offsetX, offsetY, offsetZ)).getBlock();
    }
}
