package dev.revere.virago.client.newgui.framework.components;

import dev.revere.virago.client.newgui.framework.MenuComponent;
import dev.revere.virago.client.newgui.framework.MenuPriority;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public class MenuNoDrag extends MenuComponent {
	protected boolean mouseDown = false;
		
	public MenuNoDrag(int x, int y, int width, int height){
		super(x, y, width, height);
		setPriority(MenuPriority.LOW);
	}
	
	@Override
	public void onMouseClick(int button) {
		if(button == 0) {
			mouseDown = true;
		}
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
				if(mouseY >= y && mouseY <= y + height) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	@Override
	public void onRender() {
		mouseDown = false;
	}
	
	public void onAction() {}
}