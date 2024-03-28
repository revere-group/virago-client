package dev.revere.virago.client.gui.components;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;

/**
 * @author Remi
 * @project Virago
 * @date 3/21/2024
 */
@Getter
@Setter
public abstract class RenderableComponent {

    private float x;
    private float y;
    private float width;
    private float height;

    protected Minecraft mc = Minecraft.getMinecraft();

    public RenderableComponent(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void draw(float mouseX, float mouseY, int mouseDelta);

    public abstract boolean mouseClicked(float mouseX, float mouseY, InteractionComponent click);

    public abstract void mouseReleased(float mouseX, float mouseY, InteractionComponent click);

    public abstract void keyTyped(char typedChar, int keyCode);

    public float getOffset() {
        return 0f;
    }

    public boolean hovered(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
