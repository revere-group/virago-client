package dev.revere.virago.client.gui.newgui.clickgui.components.mods;

import dev.revere.virago.client.gui.newgui.framework.components.MenuNewColorPicker;
import dev.revere.virago.client.gui.newgui.framework.draw.ButtonState;
import dev.revere.virago.client.gui.newgui.framework.draw.DrawImpl;
import dev.revere.virago.client.gui.newgui.framework.draw.DrawType;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

import java.awt.*;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public class MenuModNewColorPicker extends MenuNewColorPicker {
    public MenuModNewColorPicker(int x, int y, int width, int height, int defaultColor) {
        super(x, y, width, height, defaultColor);
    }

    @Override
    public void onInitColors() {
        super.onInitColors();

        setColor(DrawType.LINE, ButtonState.NORMAL, new Color(43, 43, 43, 255));
        setColor(DrawType.LINE, ButtonState.ACTIVE, new Color(53, 53, 53, 255));
        setColor(DrawType.LINE, ButtonState.HOVER, new Color(48, 48, 48, 255));
        setColor(DrawType.LINE, ButtonState.HOVERACTIVE, new Color(59, 59, 59, 255));
        setColor(DrawType.LINE, ButtonState.DISABLED, new Color(100, 100, 100, 255));
    }

    @Override
    public void onRender() {
        int x = this.getRenderX();
        int y = this.getRenderY();
        int lineColor = getColor(DrawType.LINE, lastState);

        GlStateManager.color(1, 1, 1);

		/*drawHorizontalLine(x, y, width + 1, 1, lineColor);
		drawVerticalLine(x, y + 1, height - 1, 1, lineColor);
		drawHorizontalLine(x, y + height, width + 1, 1, lineColor);
		drawVerticalLine(x + width, y + 1, height - 1, 1, lineColor);*/

        DrawImpl.drawRect(x + 3, y + 2, width - 1, height - 3, lineColor);

        int index = 0;

        for(int h = y; h < y + height - 5; h++) {
            DrawImpl.drawRect(x + 5, h + 3, width - 5, 1, /*disabled ? */lightenColor(index, 0, color).getRGB()/* : darkenColor(index, 7, color).getRGB()*/);
            index++;
        }

        if(startType <= 0) {
            if(alphaSlider.getParent() == null) {
                alphaSlider.setParent(getParent());
            }

            alphaSlider.onPreSort();
        }

        drawPicker();

        if(wantsToDrag) {
            mouseDragging = Mouse.isButtonDown(0);
            wantsToDrag = mouseDragging;
        }

        mouseDown = false;
        mouseDragging = false;
    }
}
