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
public final class Vector2i {

    public int x, y;

    public Vector2i() {
    }

    public Vector2i(final int x, final int y) {
        this.x = x;
        this.y = y;
    }
}
