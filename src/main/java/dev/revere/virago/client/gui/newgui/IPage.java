package dev.revere.virago.client.gui.newgui;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public interface IPage {
	void onInit();
	
	void onRender();
	void onLoad();
	void onUnload();
	
	void onOpen();
	void onClose();
}
