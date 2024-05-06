package dev.revere.virago.client.newgui.framework;

import dev.revere.virago.client.newgui.framework.components.MenuDraggable;
import dev.revere.virago.client.newgui.framework.components.MenuScrollPane;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public class Menu {
	private String title;
	private int x;
	private int y;
	private int width;
	private int height;
	private int mouseX;
	private int mouseY;
	private List<MenuComponent> components;

	/**
	 * Constructs a new instance of Menu with the specified title, width, and height.
	 *
	 * @param title  The title of the menu.
	 * @param width  The width of the menu.
	 * @param height The height of the menu.
	 */
	public Menu(String title, int width, int height) {
		this.title = title;
		this.x = 0;
		this.y = 0;
		this.width = width;
		this.height = height;
		components = new CopyOnWriteArrayList<>();
	}

	/**
	 * Renders the menu on the screen, including its components.
	 *
	 * @param mouseX The X position of the mouse.
	 * @param mouseY The Y position of the mouse.
	 */
	public void onRender(int mouseX, int mouseY) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		
		Collections.sort(components, (a, b) -> Integer.compare(a.getPriority().getPriority(), b.getPriority().getPriority()));
		Collections.reverse(components);
		
		int passThroughIndex = -1;
		int index = components.size();
		
		for(MenuComponent component : components) {
			component.setRenderOffsetX(x);
			component.setRenderOffsetY(y);
			
			if(!component.passesThrough() && passThroughIndex == -1)
				passThroughIndex = index;
			
			index--;
		}
		
		Collections.reverse(components);	
		
		final int oldIndex = index;
		
		index = oldIndex;
		
		for(MenuComponent component : components) {
			if(index >= passThroughIndex - 1){
				this.mouseX = mouseX;
				this.mouseY = mouseY;
			} else if(component instanceof MenuDraggable) {
				index++;
				continue;
			} else {
				this.mouseX = Integer.MAX_VALUE;
				this.mouseY = Integer.MAX_VALUE;
			}
			
			component.onPreSort();
			component.onRender();
			index++;
		}
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}

	/**
	 * Handles mouse click events on the menu and its components.
	 *
	 * @param button The button that was clicked.
	 */
	public void onMouseClick(int button) {
		Collections.sort(components, (a, b) -> Integer.compare(a.getPriority().getPriority(), b.getPriority().getPriority()));
		Collections.reverse(components);
		boolean returnMode = false;
		for(MenuComponent component : components) {
			if(!returnMode || component instanceof MenuScrollPane) {
				component.onMouseClick(button);
			}
			
			if(!component.passesThrough()) {
				returnMode = true;
			}
		}
	}

	/**
	 * Handles mouse click and move events on the menu and its components.
	 *
	 * @param button The button that was clicked.
	 */
	public void onMouseClickMove(int button) {
		Collections.sort(components, (a, b) -> Integer.compare(a.getPriority().getPriority(), b.getPriority().getPriority()));
		Collections.reverse(components);
		
		boolean returnMode = false;
		for(MenuComponent component : components) {
			if(!returnMode || component instanceof MenuScrollPane) {
				component.onMouseClickMove(button);
			}
			
			if(!component.passesThrough()) {
				returnMode = true;
			}
		}
	}

	/**
	 * Handles key down events on the menu and its components.
	 *
	 * @param character The character that was typed.
	 * @param key       The key code of the pressed key.
	 */
	public void onKeyDown(char character, int key) {
		for(MenuComponent component : components)
			component.onKeyDown(character, key);
	}

	/**
	 * Handles the menu exit event, triggered when a specific key is pressed.
	 *
	 * @param key The key code of the pressed key.
	 * @return True if the menu exit event should be cancelled, false otherwise.
	 */
	public boolean onMenuExit(int key) {
		boolean cancel = false;
		
		for(MenuComponent component : components) {
			if(component.onExitGui(key)) {
				cancel = true;
			}
		}
		
		return cancel;
	}

	/**
	 * Handles mouse scroll events on the menu and its components.
	 *
	 * @param scroll The amount of scrolling that occurred.
	 */
	public void onScroll(int scroll) {
		components.sort(Comparator.comparingInt(a -> a.getPriority().getPriority()));
		Collections.reverse(components);
		
		for(MenuComponent component : components) {
			component.onMouseScroll(scroll);
			
			if(!component.passesThrough())
				break;
		}
	}

	/**
	 * Sets the location of the menu.
	 *
	 * @param x The X position of the menu.
	 * @param y The Y position of the menu.
	 */
	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Adds a component to the menu.
	 *
	 * @param component The component to be added.
	 */
	public void addComponent(MenuComponent component) {
		if(!components.contains(component)) {
			component.setParent(this);
			components.add(component);
		}
	}

	/**
	 * Retrieves the title of the menu.
	 *
	 * @return The menu title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the title of the menu.
	 *
	 * @param title The menu title to be set.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Retrieves the X position of the menu.
	 *
	 * @return The X position.
	 */
	public int getX() {
		return x;
	}

	/**
	 * Sets the X position of the menu.
	 *
	 * @param x The X position to be set.
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * Retrieves the Y position of the menu.
	 *
	 * @return The Y position.
	 */
	public int getY() {
		return y;
	}

	/**
	 * Sets the Y position of the menu.
	 *
	 * @param y The Y position to be set.
	 */
	public void setY(int y) {
		this.y = y;
	}

	/**
	 * Retrieves the width of the menu.
	 *
	 * @return The menu width.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Sets the width of the menu.
	 *
	 * @param width The menu width to be set.
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Retrieves the height of the menu.
	 *
	 * @return The menu height.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Sets the height of the menu.
	 *
	 * @param height The menu height to be set.
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * Retrieves the X position of the mouse.
	 *
	 * @return The X position of the mouse.
	 */
	public int getMouseX() {
		return mouseX;
	}

	/**
	 * Sets the X position of the mouse.
	 *
	 * @param mouseX The X position of the mouse to be set.
	 */
	public void setMouseX(int mouseX) {
		this.mouseX = mouseX;
	}

	/**
	 * Retrieves the Y position of the mouse.
	 *
	 * @return The Y position of the mouse.
	 */
	public int getMouseY() {
		return mouseY;
	}

	/**
	 * Sets the Y position of the mouse.
	 *
	 * @param mouseY The Y position of the mouse to be set.
	 */
	public void setMouseY(int mouseY) {
		this.mouseY = mouseY;
	}

	/**
	 * Retrieves the list of components in the menu.
	 *
	 * @return The list of components.
	 */
	public List<MenuComponent> getComponents() {
		return components;
	}

	/**
	 * Sets the list of components in the menu.
	 *
	 * @param components The list of components to be set.
	 */
	public void setComponents(List<MenuComponent> components) {
		this.components = components;
	}
}