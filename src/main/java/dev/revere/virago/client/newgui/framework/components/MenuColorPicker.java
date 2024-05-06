package dev.revere.virago.client.newgui.framework.components;

import dev.revere.virago.client.newgui.framework.MenuComponent;
import dev.revere.virago.client.newgui.framework.draw.ButtonState;
import dev.revere.virago.client.newgui.framework.draw.DrawType;

import java.awt.*;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public class MenuColorPicker extends MenuComponent {

	public MenuColorPicker(int x, int y, int width, int height, int defaultColor) {
		super(x, y, width, height);
	}
	
	@Override
	public void onInitColors() {
		setColor(DrawType.BACKGROUND, ButtonState.NORMAL, new Color(35, 35, 35, 255));
		setColor(DrawType.BACKGROUND, ButtonState.ACTIVE, new Color(81, 108, 255, 255));
		setColor(DrawType.BACKGROUND, ButtonState.HOVER, new Color(50, 50, 50, 255));
		setColor(DrawType.BACKGROUND, ButtonState.HOVERACTIVE, new Color(100, 120, 255, 255));
		setColor(DrawType.BACKGROUND, ButtonState.POPUP, new Color(10, 10, 10, 255));	
		setColor(DrawType.BACKGROUND, ButtonState.DISABLED, new Color(100, 100, 100, 255));	

		setColor(DrawType.LINE, ButtonState.NORMAL, new Color(10, 10, 10, 255));
		setColor(DrawType.LINE, ButtonState.ACTIVE, new Color(10, 10, 10, 255));
		setColor(DrawType.LINE, ButtonState.HOVER, new Color(10, 10, 10, 255));
		setColor(DrawType.LINE, ButtonState.HOVERACTIVE, new Color(10, 10, 10, 255));
		setColor(DrawType.LINE, ButtonState.POPUP, new Color(100, 120, 255, 255));
		setColor(DrawType.LINE, ButtonState.DISABLED, new Color(100, 100, 100, 255));

		setColor(DrawType.TEXT, ButtonState.NORMAL, new Color(200, 200, 200, 255));
		setColor(DrawType.TEXT, ButtonState.ACTIVE, new Color(235, 235, 235, 255));
		setColor(DrawType.TEXT, ButtonState.HOVER, new Color(225, 225, 225, 255));
		setColor(DrawType.TEXT, ButtonState.HOVERACTIVE, new Color(235, 235, 235, 255));
		setColor(DrawType.TEXT, ButtonState.POPUP, new Color(100, 100, 100, 255));	
		setColor(DrawType.TEXT, ButtonState.DISABLED, new Color(255, 255, 255, 255));
	}
	
	@Override
	public void onMouseClick(int button) {

		
	}
	
	@Override
	public void onMouseClickMove(int button) {

	}
	
	@Override
	public boolean onExitGui(int key) {

		return false;
	}
	
	@Override
	public boolean passesThrough() {
		int x = this.getRenderX();
		int y = this.getRenderY();
		int mouseX = parent.getMouseX();
		int mouseY = parent.getMouseY();

		return true;
	}
	
	@Override
	public void onPreSort() {
		int x = this.getRenderX();
		int y = this.getRenderY();
		int mouseX = parent.getMouseX();
		int mouseY = parent.getMouseY();
		ButtonState state = ButtonState.NORMAL;
	}
	
	@Override
	public void onRender() {
		int x = this.getRenderX();
		int y = this.getRenderY();
	}

	public void onAction() {}
	public void onMiniAction() {}
}
