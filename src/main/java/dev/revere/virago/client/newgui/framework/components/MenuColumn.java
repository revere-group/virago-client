package dev.revere.virago.client.newgui.framework.components;

import dev.revere.virago.client.newgui.framework.MenuComponent;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public class MenuColumn extends MenuComponent {
	protected int column = 0;
	
	public MenuColumn(int column) {
		super(0, 0, 0, 0);
		this.column = column;
	}
	
	public int getColumn() {
		return column;
	}
	
	public void setColumn(int column) {
		this.column = column;
	}
}