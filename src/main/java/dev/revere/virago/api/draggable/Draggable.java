package dev.revere.virago.api.draggable;

import dev.revere.virago.api.module.AbstractModule;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Remi
 * @project Virago
 * @date 3/23/2024
 */
@Setter
@Getter
public class Draggable {

    private AbstractModule module;

    private float initialX;
    private float initialY;

    private float startX;
    private float startY;

    private float x;
    private float y;
    private float width;
    private float height;
    private boolean dragging;
    private String name;

    /**
     * Constructor for the Draggable.
     *
     * @param module    The module.
     * @param name      The name.
     * @param initialX  The initial x position.
     * @param initialY  The initial y position.
     */
    public Draggable(AbstractModule module,  String name, float initialX, float initialY) {
        this.module = module;
        this.name = name;
        this.x = initialX;
        this.y = initialY;
        this.initialX = initialX;
        this.initialY = initialY;
    }

    /**
     * Draws the draggable.
     *
     * @param mouseX The x position of the mouse.
     * @param mouseY The y position of the mouse.
     */
    public final void draw(float mouseX, float mouseY) {
        if(!this.module.isEnabled()) return;
        if (dragging) {
            x = (mouseX - startX);
            y = (mouseY - startY);
        }
    }

    /**
     * Handles the click event.
     *
     * @param mouseX The x position of the mouse.
     * @param mouseY The y position of the mouse.
     * @param button The button.
     */
    public final void onClick(float mouseX, float mouseY, int button) {
        if(!this.module.isEnabled()) return;
        if (hovered(mouseX, mouseY) && button == 0 && !dragging) {
            dragging = true;
            startX = (int) (mouseX - x);
            startY = (int) (mouseY - y);
        }
    }

    /**
     * Handles the release event.
     *
     * @param button The button.
     */
    public final void onRelease(int button) {
        if(!this.module.isEnabled()) return;
        if (button == 0) {
            dragging = false;
        }
    }

    public boolean hovered(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
