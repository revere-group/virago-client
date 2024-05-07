package dev.revere.virago.client.gui.newgui.clickgui.components.mods;

import dev.revere.virago.Virago;
import dev.revere.virago.api.module.EnumModuleType;
import dev.revere.virago.client.gui.newgui.IngameMenu;
import dev.revere.virago.client.gui.newgui.framework.components.MenuButton;
import dev.revere.virago.client.gui.newgui.framework.draw.ButtonState;
import dev.revere.virago.client.gui.newgui.framework.draw.DrawType;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.util.render.RoundedUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public class ModCategoryButton extends MenuButton {
	public static final int MAIN_COLOR = new Color(35, 35, 35, IngameMenu.MENU_ALPHA).getRGB();
	
	private final int IMAGE_SIZE = 22;
	
	private ResourceLocation image;
	private final FontService fontService = Virago.getInstance().getServiceManager().getService(FontService.class);
	
	public ModCategoryButton(EnumModuleType category, int x, int y, int width, int height) {
		super(category.getName(), x, y, width, height);
	}

	public ModCategoryButton(String text, int x, int y, int width, int height) {
		super(text, x, y, width, height);
	}

	public ModCategoryButton(String text, ResourceLocation resourceLocation, int x, int y, int width, int height) {
		super(text, x, y, width, height);
		this.image = resourceLocation;
	}

	@Override
	public void onInitColors() {
		super.onInitColors();
		
		setColor(DrawType.BACKGROUND, ButtonState.NORMAL, new Color(35, 35, 35, IngameMenu.MENU_ALPHA));
		setColor(DrawType.BACKGROUND, ButtonState.ACTIVE, new Color(25, 25, 25, IngameMenu.MENU_ALPHA));
		setColor(DrawType.BACKGROUND, ButtonState.HOVER, new Color(20, 20, 20, IngameMenu.MENU_ALPHA));
		setColor(DrawType.BACKGROUND, ButtonState.HOVERACTIVE, new Color(35, 35, 39, IngameMenu.MENU_ALPHA));
		setColor(DrawType.BACKGROUND, ButtonState.DISABLED, new Color(100, 100, 100, IngameMenu.MENU_ALPHA));	
	}
	
	@Override
	public void onRender() {
		int x = this.getRenderX();
		int y = this.getRenderY();
		int width = (this.width == -1 && this.height == -1) ? (getStringWidth(text) + minOffset * 2) : this.width;
		int height = (this.width == -1 && this.height == -1) ? (getStringHeight(text) + minOffset * 2) : this.height;
		
		int backgroundColor = getColor(DrawType.BACKGROUND, lastState);
		int textColor = getColor(DrawType.TEXT, lastState);

		GlStateManager.color(1, 1, 1);

		if (isActive()) {
			RoundedUtils.round(x + 17, y, width - 23, height - 4, 10, new Color(10, 10, 10, 150));
		}


		fontService.getProductSans().drawString(text, x + (width / 2 - getStringWidth(text) / 2), y + height / 2 - (getStringHeight(text) / 2) - 3, -1);

		//rip.athena.client.gui.framework.draw.DrawImpl.drawRect(x, y, width - 10, height, backgroundColor);
		/*DrawUtils.drawRoundedRect(x + 9, y - 1, x + width - 19, y + height + 1, 4, new Color(50,50,50,255).getRGB());
		DrawUtils.drawRoundedRect(x + 10, y, x + width - 20, y + height, 4, backgroundColor);

		if(Settings.customGuiFont) {
			rip.athena.client.utils.font.FontManager.getProductSansRegular(30).drawString(text, x + (width / 2 - getStringWidth(text) / 2) - 3, y + height / 2 - (getStringHeight(text) / 2), textColor);
		} else {
			Minecraft.getMinecraft().fontRendererObj.drawString(text, x + (width / 2 - getStringWidth(text) / 2), y + height / 2 - (getStringHeight(text) / 2), textColor);
		}*/
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
