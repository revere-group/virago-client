package dev.revere.virago.client.newgui;

import dev.revere.virago.Virago;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.client.newgui.clickgui.components.mods.CategoryButton;
import dev.revere.virago.client.newgui.clickgui.pages.ModsPage;
import dev.revere.virago.client.newgui.framework.Menu;
import dev.revere.virago.client.newgui.framework.MenuComponent;
import dev.revere.virago.client.newgui.framework.MinecraftMenuImpl;
import dev.revere.virago.client.newgui.framework.components.MenuButton;
import dev.revere.virago.client.newgui.framework.components.MenuDraggable;
import dev.revere.virago.client.newgui.framework.components.MenuScrollPane;
import dev.revere.virago.client.newgui.framework.draw.DrawImpl;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.util.render.RenderUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public class IngameMenu extends MinecraftMenuImpl implements DrawImpl {
	public static int MENU_ALPHA = 255;
	public static int MENU_TOP_BG_COLOR = new Color(30, 30, 30, MENU_ALPHA).getRGB();
	public static int MENU_PANE_BG_COLOR = new Color(35, 35, 35, MENU_ALPHA).getRGB();
	public static int MENU_HEADER_TEXT_COLOR = new Color(255, 255, 255, MENU_ALPHA).getRGB();
	public static int MENU_LINE_COLOR = new Color(25, 25, 28, IngameMenu.MENU_ALPHA).getRGB();
	private final FontService fontService = Virago.getInstance().getServiceManager().getService(FontService.class);

	public static PageManager pageManager;
	public static Category category = Category.MODS;

	private static boolean initd;
	private static int savedWidth = -1;
	private static int savedHeight = -1;

	public IngameMenu(AbstractModule module, Menu menu) {
		super(module, menu);

		pageManager = new PageManager(this, menu);
	}

	@Override
	public void initGui() {
		if(initd) {
			menu.getComponents().clear();
			initd = false;
		}

		if(!initd) {
			for(IPage page : pageManager.getPages().values()) {
				page.onInit();
			}

			menu.addComponent(new MenuDraggable(0, 0, menu.getWidth(), 58));

			int x = 175;
			//int y = 58 / 2 + 2;

			int y = 59 + 60;
			int height = 32;
			int width = 205;
			int x2 = 0;

			for(Category category : Category.values()) {
				MenuButton comp = new CategoryButton(category, new ResourceLocation(category.getIcon()), x2, y, width, height) {
					@Override
					public void onAction() {
						if(IngameMenu.category != null) {
							pageManager.getPage(IngameMenu.category).onUnload();
						}

						IngameMenu.category = category;

						for(MenuComponent component : menu.getComponents()) {
							if(component instanceof CategoryButton) {
								CategoryButton button = (CategoryButton) component;
								button.setActive(component == this);
							}
						}

						initPage();
					}
				};

				if(category == IngameMenu.category) {
					comp.setActive(true);
				}

				menu.addComponent(comp);
				x += Virago.getInstance().getServiceManager().getService(FontService.class).getProductSans28().getStringWidth(category.getName()) + 20;
				y += 40;
			}

			initPage();
			initd = true;
		}

		if(category != null) {
			pageManager.getPage(category).onOpen();
		}

		super.initGui();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if(savedWidth != mc.displayWidth || savedHeight != mc.displayHeight) {
			savedWidth = mc.displayWidth;
			savedHeight = mc.displayHeight;
			ScaledResolution sr = new ScaledResolution(mc);
			menu.setX(sr.getScaledWidth() / 2);
			menu.setY(sr.getScaledHeight() / 2);
		}

		//DrawUtils.drawWatermark(width, height);

		GlStateManager.pushMatrix();
		float value = guiScale / new ScaledResolution(mc).getScaleFactor();
		GlStateManager.scale(value, value, value);


		RenderUtils.drawRoundedRect(menu.getX(), menu.getY(),  menu.getWidth(), menu.getHeight(), 16, new Color(30, 31, 35, 255).getRGB());

		fontService.getProductSans28().drawString("Virago Client", menu.getX() + 40, menu.getY() + 17, -1);

			drawVerticalLine(menu.getX() + 215, menu.getY() + 60, menu.getHeight() - 60, 3, new Color(43, 44, 48, 255).getRGB());
			drawShadowDown(menu.getX(), menu.getY() + 58, menu.getWidth());

		GlStateManager.color(1,1,1);

		if(category != null) {
			pageManager.getPage(category).onRender();
		}

		GlStateManager.popMatrix();

		super.drawScreen(mouseX, mouseY, partialTicks);

		GlStateManager.pushMatrix();
		GlStateManager.scale(value, value, value);

		for(MenuComponent component : menu.getComponents()) {
			if(component instanceof MenuScrollPane) {
				MenuScrollPane scrollpane = (MenuScrollPane) component;

				scrollpane.drawExtras();
			}
		}

		GlStateManager.popMatrix();

		GlStateManager.color(1,1,1);
	}

	public void initPage() {
		List<MenuComponent> remove = new ArrayList<>();

		for(MenuComponent component : menu.getComponents()) {
			if(component instanceof CategoryButton || component instanceof MenuDraggable) {
				continue;
			}

			remove.add(component);
		}

		menu.getComponents().removeAll(remove);

		pageManager.getPage(category).onLoad();
	}

	public void openSettings(AbstractModule parent) {
		if(category != null) {
			pageManager.getPage(category).onUnload();
		}

		category = Category.MODS;

		pageManager.getPage(ModsPage.class, Category.MODS).activeModule = parent;

		initPage();
	}

	@Override
	public void onGuiClosed() {
		if(category != null) {
			pageManager.getPage(category).onClose();
		}

		super.onGuiClosed();

		/*
		new Thread(() -> {
			Config config = Athena.INSTANCE.getConfigManager().getLoadedConfig();

			if(config != null) {
				config.save();
			}
		}).start();
		 */
	}
}
