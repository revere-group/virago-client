package dev.revere.virago.client.gui.newgui.clickgui.components.mods;

import dev.revere.virago.Virago;
import dev.revere.virago.client.gui.newgui.framework.MenuPriority;
import dev.revere.virago.client.gui.newgui.framework.components.MenuDropdown;
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

public class MenuModList extends MenuDropdown {
	protected int cursorWidth = 25;
	private final FontService fontService = Virago.getInstance().getServiceManager().getService(FontService.class);

	public MenuModList(String[] values, int x, int y, int height) {
		super(values, x, y);
		this.height = height;
		width += cursorWidth * 2;
	}

	public MenuModList(Class<?> values, int x, int y, int height) {
		super(values, x, y);
		this.height = height;
		width += cursorWidth * 2;
	}
	
	@Override
	public void onInitColors() {
		super.onInitColors();
		
		setColor(DrawType.TEXT, ButtonState.NORMAL, new Color(162, 162, 162, 255));
		setColor(DrawType.TEXT, ButtonState.HOVER, new Color(182, 182, 182, 255));
		
		setColor(DrawType.BACKGROUND, ButtonState.NORMAL, new Color(35, 35, 35, 255));
		setColor(DrawType.BACKGROUND, ButtonState.HOVER, new Color(24, 24, 27, 255));
		
		setColor(DrawType.LINE, ButtonState.NORMAL, new Color(46, 46, 48, 255));
		setColor(DrawType.LINE, ButtonState.HOVER, new Color(53, 53, 55, 255));
		setColor(DrawType.LINE, ButtonState.POPUP, new Color(120, 120, 120, 255));
	}
	
	@Override
	public void onPreSort() {
		int x = this.getRenderX();
		int y = this.getRenderY();
		int width = this.width + textOffset;
		int mouseX = parent.getMouseX();
		int mouseY = parent.getMouseY();

		ButtonState state = ButtonState.NORMAL;

		if (!disabled) {
			boolean inRange = false;
			
			if (mouseX >= x && mouseX <= x + width + arrowOffset - 1) {
				if (mouseY >= y && mouseY <= y + height + 1) {
					state = ButtonState.HOVER;
					
					if(mouseDown) {
						if(mouseX < x + cursorWidth - 1) {
							if(index - 1 >= 0) {
								index--;
							} else {
								index = values.length - 1;
							}
							
							onAction();
						} else if(mouseX > x + width - cursorWidth - 1) {
							if(index + 1 < values.length) {
								index++;
							} else {
								index = 0;
							}
							
							onAction();
						}
					}
				}
			}
		} else {
			state = ButtonState.DISABLED;
		}
		
		if(state == ButtonState.HOVER || state == ButtonState.HOVERACTIVE) {
			setPriority(MenuPriority.HIGH);
		} else {
			setPriority(MenuPriority.MEDIUM);
		}
		
		lastState = state;
	}
	
	@Override
	public void onRender() {
		int x = this.getRenderX();
		int y = this.getRenderY();
		int width = this.width + textOffset + arrowOffset + 1;
		int height = this.height;
		
		int popupColor = getColor(DrawType.LINE, ButtonState.POPUP);
		int backgroundColor = getColor(DrawType.BACKGROUND, lastState);
		int lineColor = getColor(DrawType.LINE, ButtonState.NORMAL);
		int textColor = getColor(DrawType.TEXT, ButtonState.NORMAL);

		GlStateManager.color(1,1,1);

		RoundedUtils.round(x, y, width, height, 12.0f, new Color(43, 44, 48, 255));

		int mouseX = parent.getMouseX();
		int mouseY = parent.getMouseY();
		int defBg = getColor(DrawType.BACKGROUND, ButtonState.NORMAL);
		int cache = backgroundColor;
		
		if(mouseX > x + cursorWidth - 1) {
			backgroundColor = defBg;
		}

		drawText("<", x + (cursorWidth / 2) - getStringWidth("<") / 2, y + height / 2 - getStringHeight("<") / 2, -1);
		
		if(mouseX < x + width - cursorWidth - 1) {
			backgroundColor = defBg;
		} else {
			backgroundColor = cache;
		}

		drawText(">", x + width - cursorWidth + 3 + (cursorWidth / 2) - getStringWidth(">") / 2, y + height / 2 - getStringHeight(">") / 2, -1);
		
		String text = values[index].toUpperCase();
		drawText(text, x + width / 2 - getStringWidth(text) / 2, y + height / 2 - getStringHeight(text) / 2, -1);
		
		mouseDown = false;
	}

	@Override
	public void drawText(String text, int x, int y, int color) {
		fontService.getProductSans().drawString(text, x, y, color);
	}

	@Override
	public int getStringWidth(String string) {
		return fontService.getProductSans().getStringWidth(string);
	}

	@Override
	public int getStringHeight(String string) {
		return fontService.getProductSans().getHeight();
	}
}
