package dev.revere.virago.client.gui.newgui.clickgui.components.mods;

import dev.revere.virago.Virago;
import dev.revere.virago.client.gui.newgui.framework.components.MenuButton;
import dev.revere.virago.client.gui.newgui.framework.draw.ButtonState;
import dev.revere.virago.client.gui.newgui.framework.draw.DrawType;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.util.render.RoundedUtils;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public class ModsButton extends MenuButton {
    public ModsButton(String text, int x, int y) {
        super(text, x, y, 120, 20);
    }

    private final FontService fontService = Virago.getInstance().getServiceManager().getService(FontService.class);

    @Override
    public void onInitColors() {
        super.onInitColors();

        setColor(DrawType.BACKGROUND, ButtonState.NORMAL, new Color(43, 43, 43, 255));
        setColor(DrawType.BACKGROUND, ButtonState.ACTIVE, new Color(68, 68, 68, 255));
        setColor(DrawType.BACKGROUND, ButtonState.HOVER, new Color(58, 58, 58, 255));
        setColor(DrawType.BACKGROUND, ButtonState.HOVERACTIVE, new Color(82, 82, 82, 255));
        setColor(DrawType.BACKGROUND, ButtonState.DISABLED, new Color(150, 150, 150, 255));

        setColor(DrawType.LINE, ButtonState.NORMAL, new Color(43, 43, 43, 255));
        setColor(DrawType.LINE, ButtonState.ACTIVE, new Color(68, 68, 68, 255));
        setColor(DrawType.LINE, ButtonState.HOVER, new Color(58, 58, 58, 255));
        setColor(DrawType.LINE, ButtonState.HOVERACTIVE, new Color(82, 82, 82, 255));
        setColor(DrawType.LINE, ButtonState.DISABLED, new Color(150, 150, 150, 255));

        setColor(DrawType.TEXT, ButtonState.NORMAL, new Color(182, 182, 182, 255));
        setColor(DrawType.TEXT, ButtonState.ACTIVE, new Color(182, 182, 182, 255));
        setColor(DrawType.TEXT, ButtonState.HOVER, new Color(182, 182, 182, 255));
        setColor(DrawType.TEXT, ButtonState.HOVERACTIVE, new Color(182, 182, 182, 255));
        setColor(DrawType.TEXT, ButtonState.DISABLED, new Color(150, 150, 150, 255));
    }

    @Override
    public boolean passesThrough() {
        if (disabled) {
            return true;
        }

        int width = (this.width == -1 && this.height == -1) ? (getStringWidth(text) + minOffset * 2) : this.width;
        int height = (this.width == -1 && this.height == -1) ? (getStringHeight(text) + minOffset * 2) : this.height;

        int x = this.getRenderX();
        int y = this.getRenderY();
        int mouseX = parent.getMouseX();
        int mouseY = parent.getMouseY();

        if (mouseDown) {
            if (mouseX >= x - 20 && mouseX <= x + width) {
                if (mouseY >= y && mouseY <= y + height + 1) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void onPreSort() {
        int x = this.getRenderX();
        int y = this.getRenderY();
        int width = (this.width == -1 && this.height == -1) ? (getStringWidth(text) + minOffset * 2) : this.width;
        int height = (this.width == -1 && this.height == -1) ? (getStringHeight(text) + minOffset * 2) : this.height;
        int mouseX = parent.getMouseX();
        int mouseY = parent.getMouseY();

        ButtonState state = active ? ButtonState.ACTIVE : ButtonState.NORMAL;

        if (!disabled) {
            if (mouseX >= x - 20 && mouseX <= x + width) {
                if (mouseY >= y && mouseY <= y + height + 1) {
                    state = ButtonState.HOVER;

                    if (active) {
                        state = ButtonState.HOVERACTIVE;
                    }

                    if (mouseDown) {
                        active = !active;
                        onAction();
                    }
                }
            }
        } else {
            state = ButtonState.DISABLED;
        }

        lastState = state;
    }

    @Override
    public void onRender() {
        int x = this.getRenderX();
        int y = this.getRenderY();
        int width = (this.width == -1 && this.height == -1) ? (getStringWidth(text) + minOffset * 2) : this.width;
        int height = (this.width == -1 && this.height == -1) ? (getStringHeight(text) + minOffset * 2) : this.height;

        int backgroundColor = getColor(DrawType.BACKGROUND, lastState);
        int lineColor = getColor(DrawType.LINE, lastState);
        int textColor = getColor(DrawType.TEXT, lastState);

        GlStateManager.color(1, 1, 1);

        RoundedUtils.round(x, y, width, height, 12.0f, new Color(43, 44, 48, 255));
        //RoundedUtils.drawRoundedRect(x + 1, y + 1, x + width - 1, y + height - 1, 12.0f, new Color(35, 35, 35, 255).getRGB());

		/*drawHorizontalLine(x, y, width + 1, 1, lineColor);
		drawVerticalLine(x, y + 1, height - 1, 1, lineColor);
		drawHorizontalLine(x, y + height, width + 1, 1, lineColor);
		drawVerticalLine(x + width, y + 1, height - 1, 1, lineColor);*/

        fontService.getProductSans28().drawString(text, x + (width / 2 - getStringWidth(text) / 2), y + height / 2 - (getStringHeight(text) / 2), -1);
        mouseDown = false;
    }

    @Override
    public int getStringWidth(String string) {
        return fontService.getProductSans28().getStringWidth(string);
    }

    @Override
    public int getStringHeight(String string) {
        return fontService.getProductSans28().getHeight();
    }
}
