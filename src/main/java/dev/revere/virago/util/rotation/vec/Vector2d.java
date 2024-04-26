package dev.revere.virago.util.rotation.vec;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Remi
 * @project Virago-Client
 * @date 4/12/2024
 */
@Getter
@Setter
public final class Vector2d {

    public double x, y;

    public Vector2d(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2d offset(final double x, final double y) {
        return new Vector2d(this.x + x, this.y + y);
    }

    public Vector2d offset(Vector2d xy) {
        return offset(xy.x, xy.y);
    }
}