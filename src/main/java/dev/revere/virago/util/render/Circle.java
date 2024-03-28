package dev.revere.virago.util.render;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Vec3;

public class Circle {
    private final Vec3 vector;
    private int prevTick;
    private int tick;

    /**
     * Circle constructor to initialize the circle.
     *
     * @param vector the vector of the circle
     */
    public Circle(Vec3 vector) {
        this.vector = vector;
        this.prevTick = this.tick = 20;
    }

    /**
     * Get the animation of the circle.
     *
     * @param partialTicks the partial ticks
     * @return the animation
     */
    public double getAnimation(float partialTicks) {
        return (this.prevTick + (this.tick - this.prevTick) * partialTicks) / 20.0f;
    }

    /**
     * Update the circle.
     *
     * @return true if the circle is updated
     */
    public boolean update() {
        this.prevTick = this.tick;
        return this.tick-- <= 0;
    }

    /**
     * Get the position of the circle.
     *
     * @return the position
     */
    public Vec3 pos() {
        return new Vec3(this.vector.xCoord - Minecraft.getMinecraft().getRenderManager().renderPosX, this.vector.yCoord - Minecraft.getMinecraft().getRenderManager().renderPosY, this.vector.zCoord - Minecraft.getMinecraft().getRenderManager().renderPosZ);
    }
}
