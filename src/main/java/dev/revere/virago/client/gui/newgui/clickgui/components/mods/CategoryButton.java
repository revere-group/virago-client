package dev.revere.virago.client.gui.newgui.clickgui.components.mods;

import dev.revere.virago.Virago;
import dev.revere.virago.client.gui.newgui.Category;
import dev.revere.virago.client.gui.newgui.framework.components.MenuButton;
import dev.revere.virago.client.gui.newgui.framework.draw.ButtonState;
import dev.revere.virago.client.gui.newgui.framework.draw.DrawType;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.util.render.RenderUtils;
import dev.revere.virago.util.render.RoundedUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public class CategoryButton extends MenuButton {
	public CategoryButton(Category category, int x, int y) {
		super(category.getName(), x, y);
	}

	public CategoryButton(Category category, int x, int y, int width, int height) {
		super(category.getName(), x, y, width, height);
	}

	private ResourceLocation image;
	private final FontService fontService = Virago.getInstance().getServiceManager().getService(FontService.class);
	public CategoryButton(Category category, ResourceLocation image, int x, int y, int width, int height) {
		super(category.getName(), x, y, width, height);
		this.image = image;
	}

	public CategoryButton(String category, ResourceLocation image, int x, int y, int width, int height) {
		super(category, x, y, width, height);
		this.image = image;
	}

	@Override
	public void onInitColors() {
		setColor(DrawType.TEXT, ButtonState.NORMAL, new Color(30, 30, 30, 255));
		setColor(DrawType.TEXT, ButtonState.ACTIVE, new Color(30, 30, 30 , 255));
		setColor(DrawType.TEXT, ButtonState.HOVER, new Color(25, 25, 25, 255));
		setColor(DrawType.TEXT, ButtonState.HOVERACTIVE, new Color(20, 20, 20, 255));
		setColor(DrawType.TEXT, ButtonState.DISABLED, new Color(255, 255, 255, 255));
		
		super.onInitColors();
	}
	
	@Override
	public void onRender() {
		int x = this.getRenderX();
		int y = this.getRenderY();
		int width = (this.width == -1 && this.height == -1) ? (getStringWidth(text) + minOffset * 2) : this.width;
		int height = (this.width == -1 && this.height == -1) ? (getStringHeight(text) + minOffset * 2) : this.height;

		if (isActive()) {
			RoundedUtils.round(x + 30, y, width - 40, height, 10, new Color(10, 10, 10, 150));
		}

		fontService.getProductSans().drawString(text, x + 70, y + height / 2 - (getStringHeight(text) / 2) + 2, -1);

		RenderUtils.drawImage(image, x + 35, y + 3, 25, 25);
		GlStateManager.color(1,1,1);

		mouseDown = false;
	}
	
	@Override
	public int getStringWidth(String string) {
		return fontService.getProductSans().getStringWidth(string);
	}
	
	@Override
	public int getStringHeight(String string) {
		return fontService.getProductSans().getHeight();
	}
}
