package dev.revere.virago.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

public class EntityUtil {
    /**
     * Gets the interpolated position of an entity.
     *
     * @param entityIn the entity
     * @return the interpolated position
     */
    public static Vec3 getInterpolatedPosition(Entity entityIn) {
        return new Vec3(entityIn.lastTickPosX, entityIn.lastTickPosY, entityIn.lastTickPosZ).add(EntityUtil.getInterpolatedAmount(entityIn, Minecraft.getMinecraft().timer.renderPartialTicks));
    }

    /**
     * Gets the interpolated amount of an entity.
     *
     * @param entity the entity
     * @param partialTicks the partial ticks
     * @return the interpolated amount
     */
    private static Vec3 getInterpolatedAmount(Entity entity, float partialTicks) {
        return new Vec3((entity.posX - entity.lastTickPosX) * (double)partialTicks, (entity.posY - entity.lastTickPosY) * (double)partialTicks, (entity.posZ - entity.lastTickPosZ) * (double)partialTicks);
    }
}
