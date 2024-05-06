package dev.revere.virago.client.newgui.clickgui.components.mods;

import dev.revere.virago.Virago;
import dev.revere.virago.client.newgui.IngameMenu;
import dev.revere.virago.client.newgui.framework.TextPattern;
import dev.revere.virago.client.newgui.framework.components.MenuTextField;
import dev.revere.virago.client.newgui.framework.draw.ButtonState;
import dev.revere.virago.client.newgui.framework.draw.DrawType;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.util.render.RenderUtils;
import dev.revere.virago.util.render.RoundedUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.*;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public class SearchTextfield extends MenuTextField {
	protected final static Color PLACEHOLDER_COLOR = new Color(80, 80, 82, IngameMenu.MENU_ALPHA);
	private final FontService fontService = Virago.getInstance().getServiceManager().getService(FontService.class);
	public SearchTextfield(TextPattern pattern, int x, int y, int width, int height) {
		super(pattern, x, y, width, height);
	}
	
	@Override
	public void onInitColors() {
		super.onInitColors();
		
		setColor(DrawType.LINE, ButtonState.NORMAL, new Color(27, 27, 29, IngameMenu.MENU_ALPHA));
		setColor(DrawType.LINE, ButtonState.ACTIVE, new Color(36, 36, 38, IngameMenu.MENU_ALPHA));
		setColor(DrawType.LINE, ButtonState.HOVER, new Color(32, 32, 34, IngameMenu.MENU_ALPHA));
		setColor(DrawType.LINE, ButtonState.HOVERACTIVE, new Color(42, 42, 44, IngameMenu.MENU_ALPHA));
		setColor(DrawType.LINE, ButtonState.DISABLED, new Color(120, 120, 120, IngameMenu.MENU_ALPHA));

		setColor(DrawType.BACKGROUND, ButtonState.NORMAL, new Color(35, 35, 35, IngameMenu.MENU_ALPHA));
		setColor(DrawType.BACKGROUND, ButtonState.ACTIVE, new Color(29, 29, 32, IngameMenu.MENU_ALPHA));
		setColor(DrawType.BACKGROUND, ButtonState.HOVER, new Color(25, 25, 28, IngameMenu.MENU_ALPHA));
		setColor(DrawType.BACKGROUND, ButtonState.HOVERACTIVE, new Color(36, 36, 40, IngameMenu.MENU_ALPHA));
		setColor(DrawType.BACKGROUND, ButtonState.DISABLED, new Color(100, 100, 100, IngameMenu.MENU_ALPHA));	
	}
	
	@Override
	public void onRender() {
		int x = this.getRenderX();
		int y = this.getRenderY();
		int width = this.width + minOffset * 2;
		int height = this.height;
		int mouseX = parent.getMouseX();
		
		if(tab) {
			if(!Keyboard.isKeyDown(Keyboard.KEY_TAB)) {
				tab = false;
			}
		}
		
		int backgroundColor = getColor(DrawType.BACKGROUND, lastState);
		int lineColor = getColor(DrawType.LINE, lastState);
		int textColor = getColor(DrawType.TEXT, lastState);

		GlStateManager.color(1, 1,1);

		//RoundedUtils.drawRoundedRect(x - 4, y - 4, x + width + 5, y + height + 5, 24, 83886080);
		RoundedUtils.round(x - 2, y - 2, width + 3, height + 3, 4, new Color(587202560));

		/*DrawUtils.drawRoundedRect(x - 1, y - 1, x + width + 2, y + height + 2, 10, lineColor);
		DrawUtils.drawRoundedRect(x, y, x + width + 1, y + height + 1, 10, lineColor);
		DrawUtils.drawRoundedRect(x + 1, y + 1, x + width, y + height, 10, backgroundColor);*/
		
		String textToDraw = text;
		
		if(isPasswordField()) {
			StringBuilder builder = new StringBuilder();
			
			for(int i = 0; i < textToDraw.length(); i++) {
				builder.append("*");
			}
			
			textToDraw = builder.toString();
		}
		
		boolean drawPointer = false;
		
		if(focused) {
			if((System.currentTimeMillis() - lineTime) % lineRefreshTime * 2 >= lineRefreshTime) {
				drawPointer = true;
			}
		}
		
		int labelWidth;
		labelWidth = fontService.getProductSans().getStringWidth(textToDraw + 1);

		int comp = 0;
		int toRender = index;
		while(labelWidth >= width) {
			if(comp < index){
				textToDraw = textToDraw.substring(1);
				labelWidth = fontService.getProductSans().getStringWidth(textToDraw + 1);

				toRender--;
			} else if(comp > index){
				textToDraw = textToDraw.substring(0, textToDraw.length() - 1);
				labelWidth = fontService.getProductSans().getStringWidth(textToDraw + 1);
			}
			
			comp++;
		}
		
		if(drawPointer) {
			if(toRender > textToDraw.length()) {
				toRender = textToDraw.length() - 1; 
			}
			
			if(toRender < 0) {
				toRender = 0;
			}
			
			int textHeight;

			textHeight = fontService.getProductSans().getHeight();
			drawVerticalLine(x + 10 + fontService.getProductSans28().getStringWidth(textToDraw.substring(0, toRender)) + 1, y + height / 2 - textHeight / 2, textHeight, 1, textColor);
		}
		
		int renderIndex = comp;
		int renderStopIndex = comp + textToDraw.length();
		
		while(index > text.length()) {
			index--;
		}
		
		int xAdd = 0;


		if(textToDraw.isEmpty() && !isFocused()) {
			textToDraw = "SEARCH MODS...";
			xAdd = 5;
			textColor = PLACEHOLDER_COLOR.getRGB();
		}

		fontService.getProductSans28().drawString(textToDraw, x + 10 + minOffset + xAdd, y + 2 + (float) height / 2 - (float) fontService.getProductSans28().getHeight() / 2, -1);
		if(lastState == ButtonState.HOVER && mouseDown) {
			focused = true;
			lineTime = getLinePrediction();
			
			int position = x;
			
			if(mouseX < position) {
				index = 0;
				return;
			}
			
			float bestDiff = 1000;
			int bestIndex = -1;
			
			for(int i = renderIndex; i < renderStopIndex; i++) {
				if(text.length() <= i) {
					continue;
				}
				
				int diff = Math.abs(mouseX - position); 
				
				if(bestDiff > diff) {
					bestDiff = diff;
					bestIndex = i;
				}
				
				position += fontService.getProductSans28().getStringWidth(text.charAt(i) + "");
			}
			
			if(mouseX > position) {
				index = text.length();
			} else if(bestIndex != -1) {
				index = bestIndex;
			} else {
				index = 0;
			}
		}

		RenderUtils.drawImage(new ResourceLocation("Athena/gui/menu/search.png"), x + width - 30, y + 5, 20, 20);
		
		mouseDown = false;
	}
}
