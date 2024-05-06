package dev.revere.virago.client.newgui;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public enum Category {
	MODS("MODS", "Athena/gui/menu/mods.png");

	private String name;
	private String icon;

	Category(String name, String icon) {
		this.name = name;
		this.icon = icon;
	}
	
	public String getName() {
		return name;
	}
	public String getIcon() {
		return icon;
	}
}
