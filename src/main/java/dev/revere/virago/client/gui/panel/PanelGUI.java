package dev.revere.virago.client.gui.panel;

import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.client.gui.ResponsiveViewport;
import dev.revere.virago.client.gui.components.InteractionComponent;
import dev.revere.virago.client.gui.components.RenderableComponent;
import dev.revere.virago.client.gui.panel.elements.Panel;
import dev.revere.virago.util.animation.Animation;
import dev.revere.virago.util.animation.Easing;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Remi
 * @project Virago
 * @date 3/21/2024
 */
public class PanelGUI extends ResponsiveViewport {

    private final ArrayList<RenderableComponent> components = new ArrayList<>();

    private RenderableComponent draggingPanel;
    private boolean drag;
    private float dragX;
    private float dragY;

    /**
     * Constructor for the PanelGUI.
     */
    public PanelGUI() {
        int x = 0;
        for (EnumModuleType type : EnumModuleType.values()) {
            components.add(new Panel(type, 20 + x, 10, 105, 20));
            x += 110;
        }
    }

    /**
     * Initializes the UI.
     */
    @Override
    public void initializeUI() {
    }

    /**
     * Draws the elements of the panel.
     *
     * @param mouseX The x position of the mouse.
     * @param mouseY The y position of the mouse.
     */
    @Override
    public void drawElements(float mouseX, float mouseY) {
        if (draggingPanel != null) {
            drag(draggingPanel, mouseX, mouseY);
        }

        int mouseDelta = Mouse.getDWheel();

        components.forEach(component -> component.draw(mouseX, mouseY, mouseDelta));
    }

    /**
     * Handles interaction with the panel.
     *
     * @param mouseX The x position of the mouse.
     * @param mouseY The y position of the mouse.
     * @param button The button pressed.
     */
    @Override
    public void handleInteraction(float mouseX, float mouseY, int button) {
        for (RenderableComponent component : components) {
            if (button == 0 && component.hovered(mouseX, mouseY)) {
                this.draggingPanel = component;
            }
            if (components.get(components.size() - 2).hovered(mouseX, mouseY)) {
                continue;
            }

            component.mouseClicked(mouseX, mouseY, InteractionComponent.getInteraction(button));
        }
    }

    /**
     * Handles mouse releasing
     *
     * @param mouseX The x position of the mouse.
     * @param mouseY The y position of the mouse.
     * @param state  The state of the mouse.
     */
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        drag = false;
        draggingPanel = null;
        components.forEach(component -> component.mouseReleased(mouseX, mouseY, InteractionComponent.getInteraction(state)));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    /**
     * Handles key typing.
     *
     * @param typedChar The typed character.
     * @param keyCode   The key code.
     * @throws IOException
     */
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        components.forEach(component -> component.keyTyped(typedChar, keyCode));
    }

    /**
     * Handles dragging of the panel.
     *
     * @param component The component to drag.
     * @param mouseX    The x position of the mouse.
     * @param mouseY    The y position of the mouse.
     */
    private void drag(RenderableComponent component, float mouseX, float mouseY) {
        if (!this.drag && Mouse.isButtonDown(0)) {
            drag = false;
        }

        if (this.drag) {
            component.setX(mouseX + this.dragX);
            component.setY(mouseY + this.dragY);

            if (!Mouse.isButtonDown(0)) {
                this.drag = false;
            }
        }

        if (component.hovered(mouseX, mouseY) && Mouse.isButtonDown(0)) {
            if (!this.drag) {
                this.dragX = (component.getX() - mouseX);
                this.dragY = (component.getY() - mouseY);
                this.drag = true;
            }
        }
    }
}
