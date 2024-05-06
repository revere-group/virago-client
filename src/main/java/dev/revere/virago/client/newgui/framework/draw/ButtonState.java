package dev.revere.virago.client.newgui.framework.draw;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public enum ButtonState {
	NORMAL(""), HOVER("Hover"), ACTIVE("Active"), POPUP("Popup"), DISABLED("Disabled"), HOVERACTIVE("HoverActive");

	String state;
	
	ButtonState(String state) {
		this.state = state;
	}
	
	@Override
	public String toString() {
		return state;
	}
}
