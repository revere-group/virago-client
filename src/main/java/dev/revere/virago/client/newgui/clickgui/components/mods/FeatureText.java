package dev.revere.virago.client.newgui.clickgui.components.mods;

import dev.revere.virago.Virago;
import dev.revere.virago.client.modules.render.HUD;
import dev.revere.virago.client.newgui.framework.components.MenuLabel;
import dev.revere.virago.client.newgui.framework.draw.ButtonState;
import dev.revere.virago.client.newgui.framework.draw.DrawType;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.client.services.ModuleService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public class FeatureText extends MenuLabel {
	public FeatureText(String text, String tooltip, int x, int y) {
		super(text, tooltip, x, y);
	}
	
	public FeatureText(String text, int x, int y) {
		super(text, x, y);
	}

	private final FontService fontService = Virago.getInstance().getServiceManager().getService(FontService.class);
	
	@Override
	public void onInitColors() {
		super.onInitColors();
		
		setColor(DrawType.TEXT, ButtonState.NORMAL, new Color(255, 255, 255, 255));
		setColor(DrawType.TEXT, ButtonState.HOVER, new Color(255, 255, 255, 255));
		setColor(DrawType.TEXT, ButtonState.DISABLED, new Color(255, 255, 255, 255));
	}
	
	@Override
	public void drawText(String text, int x, int y, int color) {
		fontService.getProductSans().drawString(text, x, y, color);
	}
	
	@Override
	public int getStringWidth(String text) {
		return fontService.getProductSans().getStringWidth(text);
	}
	
	@Override
	public int getStringHeight(String text) {
		return fontService.getProductSans().getHeight();
	}
}
