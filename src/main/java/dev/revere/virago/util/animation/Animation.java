package dev.revere.virago.util.animation;

import java.util.function.Supplier;

/**
 * @author Remi
 * @project Virago
 * @date 3/21/2024
 */
public class Animation {
    private final Easing easing;
    private boolean state;
    private final Supplier<Float> animationTimeSupplier;
    private long time;

    public Animation(Supplier<Float> animationTimeSupplier, boolean initialState, Supplier<Easing> easingSupplier) {
        this.easing = easingSupplier.get();
        this.animationTimeSupplier = animationTimeSupplier;
        setState(initialState);
    }

    /**
     * Gets the factor of this animation
     *
     * @return the factor based on time and speed
     */
    public double getFactor() {
        float animationTime = animationTimeSupplier.get();
        double linear = (System.currentTimeMillis() - time) / animationTime;
        if (!state) {
            linear = 1.0 - linear;
        }

        return Math.min(Math.max(easing.ease(linear), 0.0), 1.0);
    }

    public Animation setState(boolean state) {
        this.state = state;
        time = System.currentTimeMillis();
        return this;
    }

    public boolean getState() {
        return state;
    }
}