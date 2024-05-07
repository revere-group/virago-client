package dev.revere.virago.client.gui.newgui.clickgui.components.mods;

import dev.revere.virago.client.gui.newgui.framework.components.MenuCheckbox;
import dev.revere.virago.client.gui.newgui.framework.draw.ButtonState;
import dev.revere.virago.client.gui.newgui.framework.draw.DrawType;
import dev.revere.virago.util.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public class MenuModCheckbox extends MenuCheckbox {
	public MenuModCheckbox(int x, int y, int width, int height) {
		super("", x, y, width, height);
		textOffset = 0;
	}


	@Override
	public void onInitColors() {
		super.onInitColors();

		setColor(DrawType.BACKGROUND, ButtonState.NORMAL, new Color(0, 0, 0, 0));
		setColor(DrawType.BACKGROUND, ButtonState.ACTIVE, new Color(100, 100, 100, 255));
		setColor(DrawType.BACKGROUND, ButtonState.HOVER, new Color(70, 70, 70, 255));
		setColor(DrawType.BACKGROUND, ButtonState.HOVERACTIVE, new Color(150, 150, 150, 255));

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

		int backgroundColor = getColor(DrawType.BACKGROUND, lastState);
		int lineColor = getColor(DrawType.LINE, lastState);

		GlStateManager.color(1, 1, 1);

		if (backgroundColor == getColor(DrawType.BACKGROUND, ButtonState.ACTIVE)) {
			RenderUtils.drawRoundedRect(x, y, x + width, y + height, 4, new Color(35, 35, 35, 255).getRGB());
			RenderUtils.drawRoundedRect(x, y, x + width / 2, y + height, 4, new Color(20, 200, 50).getRGB());
		} else if (backgroundColor == getColor(DrawType.BACKGROUND, ButtonState.HOVER)) {
			RenderUtils.drawRoundedRect(x, y, x + width, y + height, 4, new Color(35, 35, 35, 255).getRGB());
			RenderUtils.drawRoundedRect(x + ((float) width / 2), y, x + width, y + height, 4, new Color(200, 50, 50).brighter().getRGB());
		} else if (backgroundColor == getColor(DrawType.BACKGROUND, ButtonState.HOVERACTIVE)) {
			RenderUtils.drawRoundedRect(x, y, x + width, y + height, 4, new Color(35, 35, 35, 255).getRGB());
			RenderUtils.drawRoundedRect(x, y, x + width / 2, y + height, 4, new Color(20, 200, 50).brighter().getRGB());
		} else {
			RenderUtils.drawRoundedRect(x, y, x + width, y + height, 4, new Color(35, 35, 35, 255).getRGB());
			RenderUtils.drawRoundedRect(x + ((float) width / 2), y, x + width, y + height, 4, new Color(200, 50, 50).getRGB());
		}

		drawTooltip();

		mouseDown = false;
	}
}
