package dev.revere.virago.client.newgui.framework.components;

import dev.revere.virago.Virago;
import dev.revere.virago.client.newgui.framework.MenuComponent;
import dev.revere.virago.client.newgui.framework.MenuPriority;
import dev.revere.virago.client.newgui.framework.TextPattern;
import dev.revere.virago.client.newgui.framework.draw.ButtonState;
import dev.revere.virago.client.newgui.framework.draw.DrawType;
import dev.revere.virago.client.notification.NotificationType;
import dev.revere.virago.client.services.NotificationService;
import dev.revere.virago.util.render.RenderUtils;
import dev.revere.virago.util.render.RoundedUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public class MenuTextField extends MenuComponent {
	protected String text;
	protected int minOffset = 2;
	protected int index = 0;
	protected int lineRefreshTime = 1000;
	protected long lineTime = 0;
	protected boolean passwordField = false;
	protected boolean mouseDown = false;
	protected boolean focused = false;
	protected boolean tab = true;
	
	protected TextPattern pattern;
	protected ButtonState lastState = ButtonState.NORMAL;
	
	public MenuTextField(TextPattern pattern, int x, int y, int width, int height) {
		super(x, y, width, height);
		this.pattern = pattern;
		text = "";
	}
	
	@Override
	public void onInitColors() {
		setColor(DrawType.BACKGROUND, ButtonState.NORMAL, new Color(35, 35, 35, 255));
		setColor(DrawType.BACKGROUND, ButtonState.ACTIVE, new Color(65, 65, 65, 255));
		setColor(DrawType.BACKGROUND, ButtonState.HOVER, new Color(50, 50, 50, 255));
		setColor(DrawType.BACKGROUND, ButtonState.HOVERACTIVE, new Color(65, 65, 65, 255));
		setColor(DrawType.BACKGROUND, ButtonState.DISABLED, new Color(75, 75, 75, 255));	
		
		setColor(DrawType.LINE, ButtonState.NORMAL, new Color(10, 10, 10, 255));
		setColor(DrawType.LINE, ButtonState.ACTIVE, new Color(10, 10, 10, 255));
		setColor(DrawType.LINE, ButtonState.HOVER, new Color(20, 20, 20, 255));
		setColor(DrawType.LINE, ButtonState.HOVERACTIVE, new Color(20, 20, 20, 255));
		setColor(DrawType.LINE, ButtonState.DISABLED, new Color(50, 50, 50, 255));

		setColor(DrawType.TEXT, ButtonState.NORMAL, new Color(200, 200, 200, 255));
		setColor(DrawType.TEXT, ButtonState.ACTIVE, new Color(235, 235, 235, 255));
		setColor(DrawType.TEXT, ButtonState.HOVER, new Color(225, 225, 225, 255));
		setColor(DrawType.TEXT, ButtonState.HOVERACTIVE, new Color(235, 235, 235, 255));
		setColor(DrawType.TEXT, ButtonState.DISABLED, new Color(255, 255, 255, 255));
	}
	
	@Override
	public boolean onExitGui(int button) {
		focused = false;
		return false;
	}
	
	@Override
	public void onMouseClick(int button) {
		if(button == 0) {
			mouseDown = true;
		} else if(button == 1 && focused) {
			try {
				String text = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
				
				if(!setText(text)) {
					Virago.getInstance().getServiceManager().getService(NotificationService.class).notify(NotificationType.ERROR, "Error", "Clipboard doesnt match the desired data.");
				} else {
					index = text.length();
				}
			} catch (HeadlessException | UnsupportedFlavorException | IOException e) {
				Virago.getInstance().getServiceManager().getService(NotificationService.class).notify(NotificationType.ERROR, "Error", "Invalid clipboard data.");
				e.printStackTrace();
			};
		}
	}
	
	@Override
	public boolean passesThrough() {
		if(disabled) {
			return true;
		}
		
		if(focused) {
			return false;
		}
		
		if(mouseDown) {
			int x = this.getRenderX();
			int y = this.getRenderY();
			int mouseX = parent.getMouseX();
			int mouseY = parent.getMouseY();	
			
			if(mouseX >= x && mouseX <= x + this.width + minOffset * 2) {
				if(mouseY >= y && mouseY <= y + height) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	@Override
	public void onKeyDown(char character, int key) {
		if(focused) {
			final int oldIndex = index;
			
			boolean found = true;
			boolean wantRender = false;
			
			switch(key) {
				case Keyboard.KEY_RETURN:
					onEnter();
					focused = false;
					break;
					
				case Keyboard.KEY_TAB:
					if(tab) {
						break;
					}
					
					onTab();
					focused = false;
					break;
				
				case Keyboard.KEY_LEFT:
					if(index - 1 >= 0) {
						index--;
					}
					
					wantRender = true;
					break;
				
				case Keyboard.KEY_RIGHT:					
					if(index + 1 <= text.length()) {
						index++;
					}
					
					wantRender = true;
					break;
				
				case Keyboard.KEY_BACK:
				case Keyboard.KEY_DELETE:
					if(!text.isEmpty() && index - 1 >= 0) {
						text = new StringBuilder(text).deleteCharAt(index - 1).toString();
						onAction();
					}
					
					if(index - 1 >= 0) {
						index--;
					}
					
					wantRender = true;
					break;
				
				default:
					found = false;
					break;
			}
			
			if(wantRender) {
				if(oldIndex != index) {
					lineTime = getLinePrediction();
				}
			}
			
			if(found) {
				return;
			}
			
			if(pattern != TextPattern.NONE) {
				if(pattern == TextPattern.NUMBERS_ONLY && !Character.isDigit(character) && !(text.length() == 0 && character == '-')) {
					return;
				}
				
				if(pattern == TextPattern.TEXT_ONLY && !Character.isAlphabetic(character)) {
					return;
				}
				
				if(pattern == TextPattern.TEXT_AND_NUMBERS && character != ' ' && !Character.isAlphabetic(character) && !Character.isDigit(character)) {
					return;
				}
			}
			
			if((character + "").matches("[A-Za-z0-9\\s_\\+\\-\\.,!@�#\\$%\\^&\\*\\(\\);\\\\/\\|<>\"'\\[\\]\\?=]")) {
				try {
					if(pattern == TextPattern.NUMBERS_ONLY && character != '-') {
						Integer.valueOf(new StringBuilder(text).insert(index, character).toString());
					}
					
					lineTime = getLinePrediction();
					text = new StringBuilder(text).insert(index, character).toString();
					index++;
					onAction();
				} catch(NumberFormatException e) {
				}
			}
		}
	}
	
	
	@Override
	public void onPreSort() {
		int x = this.getRenderX();
		int y = this.getRenderY();
		int width = this.width + minOffset * 2;
		int height = this.height;
		int mouseX = parent.getMouseX();
		int mouseY = parent.getMouseY();
		
		ButtonState state = ButtonState.NORMAL;
		if(mouseDown) {
			focused = false;
		}
		
		if(!disabled) {
			if(mouseX >= x && mouseX <= x + width) {
				if(mouseY >= y && mouseY <= y + height) {
					state = ButtonState.HOVER;
					
					if(mouseDown) {
						onClick();
						focused = true;
						lineTime = System.currentTimeMillis();	
					}
				}
			}
		} else {
			state = ButtonState.DISABLED;
		}
		
		setPriority(focused ? MenuPriority.HIGH : MenuPriority.MEDIUM);
		
		lastState = state;
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

		//rip.athena.client.gui.framework.draw.DrawImpl.drawRect(x + 1, y + 1, width - 1, height - 1, backgroundColor);

		RenderUtils.drawRoundedRect(x - 1, y - 1, x + width + 1, y + height + 1, 4.0f, lineColor);
		RenderUtils.drawRoundedRect(x, y, x + width, y + height, 4.0f, new Color(35,35,35, 255).getRGB());

		/*drawHorizontalLine(x, y, width + 1, 1, lineColor);
		drawVerticalLine(x, y + 1, height - 1, 1, lineColor);
		drawHorizontalLine(x, y + height, width + 1, 1, lineColor);
		drawVerticalLine(x + width, y + 1, height - 1, 1, lineColor);*/
		
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
		
		int labelWidth = getStringWidth(textToDraw + 1);
		int comp = 0;
		int toRender = index;
		while(labelWidth >= width) {
			if(comp < index){
				textToDraw = textToDraw.substring(1);
				labelWidth = getStringWidth(textToDraw + 1);
				toRender--;
			} else if(comp > index){
				textToDraw = textToDraw.substring(0, textToDraw.length() - 1);
				labelWidth = getStringWidth(textToDraw + 1);
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
			
			drawVerticalLine(x + getStringWidth(textToDraw.substring(0, toRender)) + 1, y + 2, height - 3, 1, textColor);
		}
		
		int renderIndex = comp;
		int renderStopIndex = comp + textToDraw.length();
		
		while(index > text.length()) {
			index--;
		}
		
		drawText(textToDraw, x + minOffset, y + height / 2 - getStringHeight(textToDraw) / 2, textColor);
				
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
				
				position += getStringWidth(text.charAt(i) + "");
			}
			
			if(mouseX > position) {
				index = text.length();
			} else if(bestIndex != -1) {
				index = bestIndex;
			} else {
				index = 0;
			}
		}
		
		mouseDown = false;
	}
	
	protected long getLinePrediction() {
		return System.currentTimeMillis() - (lineRefreshTime / 2);
	}

	public int getIntValue() {
		if(pattern == TextPattern.NUMBERS_ONLY && text.equals("-")) {
			return 0;
		}
		
		try {
			return Integer.valueOf(text);
		} catch(NumberFormatException e) {
			return 0;
		}
	}
	
	public String getText() {
		if(pattern == TextPattern.NUMBERS_ONLY && text.equals("-")) {
			return "-0";
		}
		
		return text;
	}

	public boolean setText(String text) {
		StringBuilder builder = new StringBuilder();
		
		if(pattern == TextPattern.NONE) {
			this.text = text;
			return true;
		}
		
		for(char character : text.toCharArray()) {
			if(pattern == TextPattern.NUMBERS_ONLY && !Character.isDigit(character) && !(builder.length() == 0 && text.length() > 0 && character == '-')) {
				return false;
			}
			
			if(pattern == TextPattern.TEXT_ONLY && !Character.isAlphabetic(character)) {
				return false;
			}
			
			if(pattern == TextPattern.TEXT_AND_NUMBERS && character != ' ' && !Character.isAlphabetic(character) && !Character.isDigit(character)) {
				return false;
			}
			
			if((character + "").matches("[A-Za-z0-9\\s_\\+\\-\\.,!@�#\\$%\\^&\\*\\(\\);\\\\/\\|<>\"'\\[\\]\\?=]")) {
				try {
					if(pattern == TextPattern.NUMBERS_ONLY && character != '-')
						Integer.valueOf(new StringBuilder(builder.toString()).insert(builder.toString().length(), character).toString());

					builder.append(character);
				} catch(NumberFormatException e) {
					return false;
				}
			}
		}
		
		this.text = builder.toString();
		return true;
	}
	
	public void setCursor(int i) {
		if(i >= 0) {
			index = i;
		}
	}

	public boolean isPasswordField() {
		return passwordField;
	}

	public void setPasswordField(boolean passwordField) {
		this.passwordField = passwordField;
	}

	public boolean isFocused() {
		return focused;
	}

	public void setFocused(boolean focused, boolean tab) {
		if(focused) {
			lineTime = System.currentTimeMillis();
		}
			
		if(tab) {
			this.tab = true;
		}
		
		this.focused = focused;
	}

	public void onClick() {}
	public void onTab() {}
	public void onEnter() {}
	public void onAction() {}
}