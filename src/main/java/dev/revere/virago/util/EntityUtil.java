package dev.revere.virago.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

public class EntityUtil {
    public static Vec3 getInterpolatedPosition(Entity entityIn) {
        return new Vec3(entityIn.lastTickPosX, entityIn.lastTickPosY, entityIn.lastTickPosZ).add(EntityUtil.getInterpolatedAmount(entityIn, Minecraft.getMinecraft().timer.renderPartialTicks));
    }

    private static Vec3 getInterpolatedAmount(Entity entity, float partialTicks) {
        return new Vec3((entity.posX - entity.lastTickPosX) * (double)partialTicks, (entity.posY - entity.lastTickPosY) * (double)partialTicks, (entity.posZ - entity.lastTickPosZ) * (double)partialTicks);
    }
}
