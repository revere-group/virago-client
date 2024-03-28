package dev.revere.virago.util.animation.simple;

import dev.revere.virago.util.animation.util.AnimationUtils;
import lombok.Getter;

/**
 * @author Athena Development
 * @project Virago
 * @date 6/10/2023
 */
@Getter
public class SimpleAnimation {

    private float value;
    private long lastMS;

    /**
     * SimpleAnimation constructor
     *
     * @param value the value
     */
    public SimpleAnimation(final float value){
        this.value = value;
        this.lastMS = System.currentTimeMillis();
    }

    /**
     * Set the animation
     *
     * @param value the value
     * @param speed the speed
     */
    public void setAnimation(final float value, double speed){

        final long currentMS = System.currentTimeMillis();
        final long delta = currentMS - this.lastMS;
        this.lastMS = currentMS;

        double deltaValue = 0.0;

        if(speed > 28) {
            speed = 28;
        }

        if (speed != 0.0)
        {
            deltaValue = Math.abs(value - this.value) * 0.35f / (10.0 / speed);
        }

        this.value = AnimationUtils.calculateCompensation(value, this.value, deltaValue, delta);
    }
}