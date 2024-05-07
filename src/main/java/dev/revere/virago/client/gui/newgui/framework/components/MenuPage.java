package dev.revere.virago.client.gui.newgui.framework.components;

import dev.revere.virago.client.gui.newgui.framework.MenuComponent;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public class MenuPage extends MenuComponent {
	protected int page = 0;
	
	public MenuPage(int page) {
		super(0, 0, 0, 0);
		this.page = page;
	}
	
	public int getPage() {
		return page;
	}
	
	public void setPage(int page) {
		this.page = page;
	}
}