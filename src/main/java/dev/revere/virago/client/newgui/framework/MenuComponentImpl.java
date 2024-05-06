package dev.revere.virago.client.newgui.framework;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public interface MenuComponentImpl {
	default void onPreSort() {}
	default void onRender() {}
	default boolean onExitGui(int key) { return false; }
	default void onKeyDown(char character, int key) {}
	default void onMouseClick(int key) {}
	default void onMouseScroll(int scroll) {}
	default void onMouseClickMove(int key) {}
	default void onInitColors() {}
}
