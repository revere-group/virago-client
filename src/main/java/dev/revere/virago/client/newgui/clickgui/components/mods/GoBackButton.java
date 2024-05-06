package dev.revere.virago.client.newgui.clickgui.components.mods;

import dev.revere.virago.Virago;
import dev.revere.virago.client.modules.render.HUD;
import dev.revere.virago.client.newgui.IngameMenu;
import dev.revere.virago.client.newgui.framework.components.MenuButton;
import dev.revere.virago.client.newgui.framework.draw.ButtonState;
import dev.revere.virago.client.newgui.framework.draw.DrawType;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.client.services.ModuleService;
import dev.revere.virago.util.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public class GoBackButton extends MenuButton {
	public GoBackButton(int x, int y) {
		super("GO BACK", x, y, 120, 25);
	}
	private final FontService fontService = Virago.getInstance().getServiceManager().getService(FontService.class);

	@Override
	public void onInitColors() {
		super.onInitColors();
		
		setColor(DrawType.TEXT, ButtonState.NORMAL, new Color(80, 80, 82, 255));
		setColor(DrawType.TEXT, ButtonState.ACTIVE, new Color(126, 126, 126, 255));
		setColor(DrawType.TEXT, ButtonState.HOVER, new Color(126, 126, 126, 255));
		setColor(DrawType.TEXT, ButtonState.HOVERACTIVE, new Color(126, 126, 126, 255));
		setColor(DrawType.TEXT, ButtonState.DISABLED, new Color(255, 255, 255, 255));
		
		setColor(DrawType.LINE, ButtonState.NORMAL, new Color(27, 27, 29, IngameMenu.MENU_ALPHA));
		setColor(DrawType.LINE, ButtonState.ACTIVE, new Color(36, 36, 38, IngameMenu.MENU_ALPHA));
		setColor(DrawType.LINE, ButtonState.HOVER, new Color(32, 32, 34, IngameMenu.MENU_ALPHA));
		setColor(DrawType.LINE, ButtonState.HOVERACTIVE, new Color(42, 42, 44, IngameMenu.MENU_ALPHA));
		setColor(DrawType.LINE, ButtonState.DISABLED, new Color(120, 120, 120, IngameMenu.MENU_ALPHA));
		setColor(DrawType.LINE, ButtonState.POPUP, new Color(120, 120, 120, IngameMenu.MENU_ALPHA));
		
		setColor(DrawType.BACKGROUND, ButtonState.NORMAL, new Color(35, 35, 35, IngameMenu.MENU_ALPHA));
		setColor(DrawType.BACKGROUND, ButtonState.ACTIVE, new Color(29, 29, 32, IngameMenu.MENU_ALPHA));
		setColor(DrawType.BACKGROUND, ButtonState.HOVER, new Color(25, 25, 28, IngameMenu.MENU_ALPHA));
		setColor(DrawType.BACKGROUND, ButtonState.HOVERACTIVE, new Color(36, 36, 40, IngameMenu.MENU_ALPHA));
		setColor(DrawType.BACKGROUND, ButtonState.DISABLED, new Color(100, 100, 100, IngameMenu.MENU_ALPHA));		
	}
	
	@Override
	public boolean passesThrough() {
		if(disabled) {
			return true;
		}
		
		int width = (this.width == -1 && this.height == -1) ? (getStringWidth(text) + minOffset * 2) : this.width;
		int height = (this.width == -1 && this.height == -1) ? (getStringHeight(text) + minOffset * 2) : this.height;
		
		int x = this.getRenderX();
		int y = this.getRenderY();
		int mouseX = parent.getMouseX();
		int mouseY = parent.getMouseY();
		
		if(mouseDown) {
			if(mouseX >= x - 20 && mouseX <= x + width) {
				if(mouseY >= y && mouseY <= y + height + 1) {
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
		
		if(!disabled) {
			if(mouseX >= x - 20 && mouseX <= x + width) {
				if(mouseY >= y && mouseY <= y + height + 1) {
					state = ButtonState.HOVER;
					
					if(mouseDown) {
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
		
		int rounding = 10;
		
		height += 4;
		width += 4;
		x -= 2;
		y -= 2;

		GlStateManager.color(1, 1,1);

		RenderUtils.drawRoundedRect(x - 4, y - 4, x + width + 5, y + height + 5, rounding, new Color(30, 31, 35, 255).getRGB());
		RenderUtils.drawRoundedRect(x - 3, y - 3, x + width + 4, y + height + 4, rounding, 335544320);
		RenderUtils.drawRoundedRect(x - 2, y - 2, x + width + 3, y + height + 3, rounding, 436207616);
		
		height -= 4;
		width -= 4;
		x += 2;
		y += 2;
		
		/*DrawUtils.drawRoundedRect(x - 1, y - 1, x + width + 2, y + height + 2, rounding, lineColor);
		DrawUtils.drawRoundedRect(x, y, x + width + 1, y + height + 1, rounding, lineColor);
		DrawUtils.drawRoundedRect(x + 1, y + 1, x + width, y + height, rounding, backgroundColor);*/

		fontService.getProductSans28().drawString(text, x + (width / 2 - getStringWidth(text) / 2), y + height / 2 - (getStringHeight(text) / 2), -1);

		x -= 20;
		width = 30;
		
		height += 4;
		width += 4;
		x -= 2;
		y -= 2;
		
		RenderUtils.drawRoundedRect(x - 4, y - 4, x + width + 5, y + height + 5, rounding, new Color(30, 31, 35, 255).getRGB());
		RenderUtils.drawRoundedRect(x - 3, y - 3, x + width + 4, y + height + 4, rounding, new Color(43, 44, 48, 255).getRGB());
		RenderUtils.drawRoundedRect(x - 2, y - 2, x + width + 3, y + height + 3, rounding, new Color(30, 31, 35, 255).getRGB());

		height -= 4;
		width -= 4;
		x += 2;
		y += 2;
		
		RenderUtils.drawRoundedRect(x - 1, y - 1, x + width + 2, y + height + 2, rounding, lineColor);
		RenderUtils.drawRoundedRect(x, y, x + width + 1, y + height + 1, rounding, new Color(43, 44, 48, 255).getRGB());
		RenderUtils.drawRoundedRect(x + 1, y + 1, x + width, y + height, rounding, new Color(30, 31, 35, 255).getRGB());
		
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
