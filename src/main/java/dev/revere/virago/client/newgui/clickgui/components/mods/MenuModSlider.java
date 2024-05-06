package dev.revere.virago.client.newgui.clickgui.components.mods;

import dev.revere.virago.Virago;
import dev.revere.virago.client.newgui.framework.components.MenuSlider;
import dev.revere.virago.client.newgui.framework.draw.ButtonState;
import dev.revere.virago.client.newgui.framework.draw.DrawType;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.util.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;;
import net.minecraft.client.renderer.entity.Render;
import org.lwjgl.input.Mouse;

import java.awt.*;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public class MenuModSlider extends MenuSlider {
	private final FontService fontService = Virago.getInstance().getServiceManager().getService(FontService.class);

	public MenuModSlider(double startValue, double minValue, double maxValue, int precision, int x, int y, int width, int height) {
		super((float) startValue, (float) minValue, (float) maxValue, precision, x, y, width, height);
	}
	
	public MenuModSlider(float startValue, float minValue, float maxValue, int precision, int x, int y, int width, int height) {
		super(startValue, minValue, maxValue, precision, x, y, width, height);
	}
	
	public MenuModSlider(int startValue, int minValue, int maxValue, int x, int y, int width, int height) {
		super(startValue, minValue, maxValue, x, y, width, height);
	}
	
	@Override
	public void onInitColors() {
		super.onInitColors();
		
		setColor(DrawType.TEXT, ButtonState.NORMAL, new Color(162, 162, 162, 255));
		setColor(DrawType.TEXT, ButtonState.ACTIVE, new Color(182, 182, 182, 255));
		setColor(DrawType.TEXT, ButtonState.HOVER, new Color(182, 182, 182, 255));
		setColor(DrawType.TEXT, ButtonState.HOVERACTIVE, new Color(182, 182, 182, 255));
		setColor(DrawType.TEXT, ButtonState.DISABLED, new Color(100, 100, 100, 255));

		setColor(DrawType.BACKGROUND, ButtonState.ACTIVE, new Color(231, 27, 44, 255));
		setColor(DrawType.BACKGROUND, ButtonState.HOVER, new Color(100, 40, 40, 255));
		setColor(DrawType.BACKGROUND, ButtonState.HOVERACTIVE, new Color(239, 46, 90, 255));

		setColor(DrawType.BACKGROUND, ButtonState.NORMAL, new Color(231, 27, 44, 255));
		setColor(DrawType.BACKGROUND, ButtonState.POPUP, new Color(35, 35, 35, 255));
		
		setColor(DrawType.LINE, ButtonState.NORMAL, new Color(62, 62, 62, 255));
		setColor(DrawType.LINE, ButtonState.ACTIVE, new Color(78, 78, 78, 255));
		setColor(DrawType.LINE, ButtonState.HOVER, new Color(69, 69, 69, 255));
		setColor(DrawType.LINE, ButtonState.HOVERACTIVE, new Color(90, 90, 90, 255));
		setColor(DrawType.LINE, ButtonState.DISABLED, new Color(150, 150, 150, 255));
		setColor(DrawType.LINE, ButtonState.POPUP, new Color(120, 120, 120, 255));
	}
	
	@Override
	public void onRender() {
		int x = this.getRenderX();
		int y = this.getRenderY();
		int width = this.width;
		int height = this.height;
		int mouseX = parent.getMouseX();
		
		int linePopupColor = getColor(DrawType.LINE, ButtonState.POPUP);
		int backgroundPopupColor = getColor(DrawType.BACKGROUND, ButtonState.POPUP);
		int backgroundColor = getColor(DrawType.BACKGROUND, lastState);
		int lineColor = getColor(DrawType.LINE, lastState);
		int textColor = getColor(DrawType.TEXT, lastState);

		GlStateManager.color(1,1,1);

		RenderUtils.drawRoundedRect(x, y, x + width, y + height, 12.0f, new Color(43, 44, 48, 255).getRGB());
		RenderUtils.drawRoundedRect(x + 1, y + 1, x + width - 1, y + height - 1, 12.0f, new Color(30, 31, 35, 255).getRGB()
		);

		/*drawHorizontalLine(x, y, width + 1, 1, lineColor);
		drawVerticalLine(x, y + 1, height - 1, 1, lineColor);
		drawHorizontalLine(x, y + height, width + 1, 1, lineColor);
		drawVerticalLine(x + width, y + 1, height - 1, 1, lineColor);*/
		
		String data = "";
		
		if(isFloat) {
			data = getValue() + "/" + getMaxValue();
		} else {
			data = getIntValue() + "/" + Math.round(getMaxValue());
		}
		
		float diff = maxValue - minValue;
		
		int linePos = Math.round((width + 1) * (value - minValue) / (diff));
		
		if(linePos + 1 >= width) {
			linePos -= minOffset;
		} else if(x + linePos - 1 <= x) {
			linePos += minOffset;
		}

		//DrawUtils.drawRoundedRect(x + 1, y + 1, x + linePos, y + height - 1, 6, Athena.INSTANCE.getThemeManager().getTheme().getFirstColor().getRGB());
//		RoundedUtils.drawRoundedRect(x + 1, y + 1, linePos, height - 1, 6 ,Athena.INSTANCE.getThemeManager().getTheme().getFirstColor().getRGB());
		//rip.athena.client.gui.framework.draw.DrawImpl.drawRect(x + 1, y + 1, linePos, height - 1, backgroundColor);

		int cursorPos = linePos;
		int cursorWidth = 20;

		if(cursorPos < cursorWidth) {
			cursorPos = cursorWidth;
		}

		RenderUtils.drawRoundedRect(x + cursorPos - cursorWidth + 7, y, x + cursorPos, y + height, 2, -1);

//		DrawUtils.drawRoundedRect(x + cursorPos - cursorWidth, y, x + cursorWidth + 2, y + height + 1, 4, linePopupColor);
//		DrawUtils.drawRoundedRect(x + 1 + cursorPos - cursorWidth, y + 1, cursorWidth, height - 1, 4, Athena.INSTANCE.getThemeManager().getPrimaryTheme().getTextColor());
//		drawText(">", x + 3 + cursorPos - (cursorWidth / 2) - getStringWidth(">") / 2, y + height / 2 - getStringHeight(">") / 2, textColor);
		if(wantToDrag || (mouseDown && lastState == ButtonState.HOVER)) {
			if(mouseDown) {
				wantToDrag = true;
			}
			
			float wantedValue = minValue + (mouseX - minOffset - x) * diff / (width - minOffset * 2);
			
			if(wantedValue > maxValue) {
				wantedValue = maxValue;
			} else if(minValue > wantedValue) {
				wantedValue = minValue;
			}

			final float oldValue = value;
			
			value = wantedValue;
			
			if(oldValue != value) {
				onAction();
			}
		}
		
		if(wantToDrag) {
			mouseDragging = Mouse.isButtonDown(0);
			wantToDrag = mouseDragging;
		}
		
		mouseDragging = false;
		mouseDown = false;
	}

	@Override
	public void drawText(String text, int x, int y, int color) {
		fontService.getProductSans28().drawString(text, x, y, color);
	}

	@Override
	public int getStringWidth(String string) {
		return fontService.getProductSans28().getStringWidth(string);
	}

	@Override
	public int getStringHeight(String string) {
		return fontService.getProductSans().getHeight();
	}
}
