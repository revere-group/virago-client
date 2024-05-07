package dev.revere.virago.client.gui.newgui.framework.draw;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public enum DrawType {
	LINE("line"), BACKGROUND("background"), TEXT("text");
	
	String type;
	
	DrawType(String type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return type;
	}
}
