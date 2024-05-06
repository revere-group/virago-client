package dev.revere.virago.client.newgui;

import dev.revere.virago.client.newgui.clickgui.pages.ModsPage;
import dev.revere.virago.client.newgui.framework.Menu;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public class PageManager {
	private IngameMenu parent;
	private Menu menu;
	private Map<Category, Page> pages;

	public PageManager(IngameMenu parent, Menu menu) {
		this.parent = parent;
		this.menu = menu;
		this.pages = new HashMap<>();
		init();
	}

	private void init() {
		Minecraft mc = Minecraft.getMinecraft();

		pages.put(Category.MODS, new ModsPage(mc, menu, parent));
	}

	public Map<Category, Page> getPages() {
		return pages;
	}

	public <T extends Page> T getPage(Category category) {
		return (T) pages.get(category);
	}

	public <T extends Page> T getPage(Class<T> cast, Category category) {
		return (T) pages.get(category);
	}
}
