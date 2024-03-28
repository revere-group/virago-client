package dev.revere.virago.util.animation.util;

/**
 * @author Athena Development
 * @project Virago
 * @date 6/10/2023
 */
public class AnimationUtils {

    /**
     * Calculate the compensation
     *
     * @param target the target
     * @param current the current
     * @param speed the speed
     * @param delta the delta
     * @return the compensation
     */
    public static float calculateCompensation(final float target, float current, final double speed, long delta) {

        final float diff = current - target;

        double add =  (delta * (speed / 50));

        if (diff > speed){
            if(current - add > target) {
                current -= add;
            }else {
                current = target;
            }
        }
        else if (diff < -speed) {
            if(current + add < target) {
                current += add;
            }else {
                current = target;
            }
        }
        else{
            current = target;
        }

        return current;
    }
}