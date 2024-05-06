package dev.revere.virago.client.newgui;

import dev.revere.virago.client.newgui.framework.Menu;
import dev.revere.virago.client.newgui.framework.draw.DrawImpl;
import net.minecraft.client.Minecraft;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public abstract class Page implements IPage, DrawImpl {
	protected Minecraft mc;
	protected Menu menu;
	protected IngameMenu parent;
	
	public Page(Minecraft mc, Menu menu, IngameMenu parent) {
		this.mc = mc;
		this.menu = menu;
		this.parent = parent;
	}
}
