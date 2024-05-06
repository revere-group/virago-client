package dev.revere.virago.client.newgui.framework.components;

import dev.revere.virago.client.newgui.framework.MenuComponent;
import dev.revere.virago.client.newgui.framework.MenuPriority;
import dev.revere.virago.client.newgui.framework.draw.ButtonState;
import dev.revere.virago.client.newgui.framework.draw.DrawImpl;
import dev.revere.virago.client.newgui.framework.draw.DrawType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public class MenuLabel extends MenuComponent {
	protected String text;
	protected String tooltip;
	
	protected ButtonState lastState = ButtonState.NORMAL;
	protected int minOffset = 2;
	protected boolean mouseDown = false;
	
	public MenuLabel(String text, String tooltip, int x, int y) {
		super(x, y, 0, 0);
		this.text = text;
		this.tooltip = tooltip;
	}
	
	public MenuLabel(String text, int x, int y) {
		super(x, y, 0, 0);
		this.text = text;
		this.tooltip = "";
	}
	
	
	@Override
	public void onInitColors() {
		setColor(DrawType.BACKGROUND, ButtonState.POPUP, new Color(10, 10, 10, 255));

		setColor(DrawType.LINE, ButtonState.POPUP, new Color(100, 120, 255, 255));

		setColor(DrawType.TEXT, ButtonState.NORMAL, new Color(200, 200, 200, 255));
		setColor(DrawType.TEXT, ButtonState.ACTIVE, new Color(235, 235, 235, 255));
		setColor(DrawType.TEXT, ButtonState.HOVER, new Color(225, 225, 225, 255));
		setColor(DrawType.TEXT, ButtonState.HOVERACTIVE, new Color(235, 235, 235, 255));
		setColor(DrawType.TEXT, ButtonState.POPUP, new Color(100, 100, 100, 255));	
		setColor(DrawType.TEXT, ButtonState.DISABLED, new Color(200, 200, 200, 255));
	}
	
	@Override
	public boolean passesThrough() {
		if (disabled) {
			return true;
		}
		
		int x = this.getRenderX();
		int y = this.getRenderY();
		int mouseX = parent.getMouseX();
		int mouseY = parent.getMouseY();
		
		if(mouseDown) {
			if(mouseX >= x && mouseX <= x + width) {
				if(mouseY >= y && mouseY <= y + height + 1) {
					return false;
				}
			}
		}
		return true;
	}
	
	
	@Override
	public void onMouseClick(int button) {
		if(button == 0) {
			mouseDown = true;
		}
	}
	
	@Override
	public void onPreSort() {
		int x = this.getRenderX();
		int y = this.getRenderY();
		int mouseX = parent.getMouseX();
		int mouseY = parent.getMouseY();
		ButtonState state = ButtonState.NORMAL;
		
		if(!disabled) {
			if(mouseX >= x && mouseX <= x + width) {
				if(mouseY >= y && mouseY <= y + height) {
					state = ButtonState.HOVER;
					
					if(mouseDown) {
						onAction();
					}
				}
			}
		} else {
			state = ButtonState.DISABLED;
		}
		
		if(state == ButtonState.HOVER || state == ButtonState.HOVERACTIVE) {
			setPriority(MenuPriority.HIGHEST);
		} else {
			setPriority(MenuPriority.LOW);
		}
		
		lastState = state;
	}
	
	@Override
	public void onRender() {		
		int x = this.getRenderX();
		int y = this.getRenderY();
		
		drawText(text, x, y, getColor(DrawType.TEXT, lastState)); // TEXT BOOLEAN SETTING
		drawTooltip();
		
		mouseDown = false;
	}
	
	public void drawTooltip() {
		if(tooltip.length() > 0 && (lastState == ButtonState.HOVER || lastState == ButtonState.HOVERACTIVE)) {
			int tipWidth = getStringWidth(tooltip) + minOffset * 2;
			int tipHeight = getStringHeight(tooltip)+ minOffset * 2;
			int lineColor = getColor(DrawType.LINE, ButtonState.POPUP);
			int mouseX = parent.getMouseX();
			int mouseY = parent.getMouseY() - tipHeight;
			
			ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
			
			if(mouseX + tipWidth >= res.getScaledWidth()) {
				mouseX -= tipWidth;
			}

			DrawImpl.drawRect(mouseX, mouseY, tipWidth, tipHeight, getColor(DrawType.BACKGROUND, ButtonState.POPUP));
			drawHorizontalLine(mouseX, mouseY, tipWidth + 1, 1, lineColor);
			drawVerticalLine(mouseX, mouseY + 1, tipHeight - 1, 1, lineColor);
			drawHorizontalLine(mouseX, mouseY + tipHeight, tipWidth + 1, 1, lineColor);
			drawVerticalLine(mouseX + tipWidth, mouseY + 1, tipHeight - 1, 1, lineColor);
			drawText(tooltip, mouseX + minOffset, mouseY + minOffset, getColor(DrawType.TEXT, ButtonState.POPUP));
		}
	}
	
	@Override
	public int getHeight() {
		return getStringHeight(text);
	}
	
	@Override
	public int getWidth() {
		return getStringWidth(text);
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTooltip() {
		return tooltip;
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	public void onAction() {}
}
