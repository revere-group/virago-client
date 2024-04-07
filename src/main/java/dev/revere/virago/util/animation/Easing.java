package dev.revere.virago.util.animation;

/**
 * @author Remi
 * @project Virago
 * @date 3/21/2024
 */
public interface Easing {
    Easing EXPO_IN_OUT = (x) -> x == 0 ? 0 : x == 1 ? 1 : x < 0.5 ? Math.pow(2, 20 * x - 10) / 2 : (2 - Math.pow(2, -20 * x + 10)) / 2;
    Easing CUBIC_IN_OUT = (x) -> x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;
    Easing SINE_IN_OUT = x -> (Math.sin(Math.PI * (x - 0.5)) + 1) / 2;
    Easing CIRC_OUT = x -> Math.sqrt(1 - Math.pow(x - 1, 2));
    Easing LINEAR = x -> x;
    Easing BACK_IN_OUT = x -> {
        double s = 1.70158;
        if ((x /= 0.5) < 1) return 0.5 * (x * x * (((s *= (1.525)) + 1) * x - s));
        return 0.5 * ((x -= 2) * x * (((s *= (1.525)) + 1) * x + s) + 2);
    };


    double ease(double x);
}