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

    private final boolean initialState;

    private long time;

    public Animation(Supplier<Float> animationTimeSupplier, boolean initialState, Supplier<Easing> easingSupplier) {
        this.easing = easingSupplier.get();
        this.animationTimeSupplier = animationTimeSupplier;
        this.initialState = initialState;
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

    public void resetToDefault() {
        state = initialState;

        time = (long) (initialState ? System.currentTimeMillis() - ((1 - getLinearFactor()) * animationTimeSupplier.get()) : System.currentTimeMillis() - (getLinearFactor() * animationTimeSupplier.get()));
    }

    public double getLinearFactor() {
        return state ? clamp(((System.currentTimeMillis() - time) / animationTimeSupplier.get())) : clamp((1 - (System.currentTimeMillis() - time) / animationTimeSupplier.get()));
    }

    public Animation setState(boolean state) {
        this.state = state;
        time = System.currentTimeMillis();
        return this;
    }
    /**
     * Internal use only! Clamps the given value in between 0 and 1
     * @param in The given value.
     * @return The clamped value.
     */
    private double clamp(double in) {
        return in < 0 ? 0 : Math.min(in, 1);
    }


    public boolean getState() {
        return state;
    }
}