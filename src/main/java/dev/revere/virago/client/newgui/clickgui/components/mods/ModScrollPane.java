package dev.revere.virago.client.newgui.clickgui.components.mods;

import dev.revere.virago.client.newgui.framework.MenuComponent;
import dev.revere.virago.client.newgui.framework.MenuPriority;
import dev.revere.virago.client.newgui.framework.components.MenuDraggable;
import dev.revere.virago.client.newgui.framework.components.MenuScrollPane;
import dev.revere.virago.client.newgui.framework.draw.ButtonState;
import dev.revere.virago.client.newgui.framework.draw.DrawType;
import dev.revere.virago.util.render.ColorUtil;
import dev.revere.virago.util.render.RenderUtils;
import dev.revere.virago.util.render.RoundedUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Collections;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public class ModScrollPane extends MenuScrollPane {
	private boolean fullHeightScroller;

	public ModScrollPane(int x, int y, int width, int height, boolean fullHeightScroller) {
		super(x, y, width, height);
		
		this.fullHeightScroller = fullHeightScroller;
	}
	
	@Override
	public void onInitColors() {
		super.onInitColors();
		
		setColor(DrawType.BACKGROUND, ButtonState.POPUP, new Color(35, 35, 35, 255));
		setColor(DrawType.BACKGROUND, ButtonState.NORMAL, new Color(35, 35, 35, 255));
		setColor(DrawType.BACKGROUND, ButtonState.ACTIVE, new Color(25, 24, 29, 255));
		setColor(DrawType.BACKGROUND, ButtonState.HOVER, new Color(30, 30, 30, 255));
		setColor(DrawType.BACKGROUND, ButtonState.HOVERACTIVE, new Color(25, 24, 29, 255));
		setColor(DrawType.BACKGROUND, ButtonState.DISABLED, new Color(100, 100, 100, 255));	
	
		setColor(DrawType.LINE, ButtonState.POPUP, new Color(25, 24, 29, 255));
		setColor(DrawType.LINE, ButtonState.NORMAL, new Color(23, 23, 25, 255));
		setColor(DrawType.LINE, ButtonState.ACTIVE, new Color(35, 35, 38, 255));
		setColor(DrawType.LINE, ButtonState.HOVER, new Color(35, 35, 38, 255));
		setColor(DrawType.LINE, ButtonState.HOVERACTIVE, new Color(35, 35, 38, 255));
		setColor(DrawType.LINE, ButtonState.DISABLED, new Color(100, 100, 100, 255));
	}
		
	@Override
	public void onRender() {
		int x = this.getRenderX();
		int y = this.getRenderY();
		final int mouseX = parent.getMouseX();
		final int mouseY = parent.getMouseY();
		int height = this.height;

		Collections.sort(components, (a, b) -> Integer.compare(a.getPriority().getPriority(), b.getPriority().getPriority()));
		int maxY = 0;
		
		for(MenuComponent component : components) {
			if(component.getParent() == null)
				component.setParent(getParent());
			
			int tempY = component.getY() + component.getHeight();
			
			if(tempY > maxY)
				maxY = tempY;
		}
		
		maxY -= height;
		
		maxY += 3;
		
		int backgroundColor = getColor(DrawType.BACKGROUND, lastState);
		int lineColor = getColor(DrawType.LINE, lastState);
		int textColor = getColor(DrawType.TEXT, lastState);
		
		int scrollerX = x + width - scrollerWidth;
		int scrollerY = y + 1;
		int scrollerHeight = height - 1;
		ButtonState scrollerState = ButtonState.HOVER;
		
		if((mouseX >= scrollerX && mouseX <= scrollerX + scrollerWidth) || (wantsToDrag && dragging)) {
			if((mouseY >= scrollerY && mouseY <= scrollerY + scrollerHeight) || (wantsToDrag && dragging)) {
				scrollerState = ButtonState.ACTIVE;
				
				if(!wantsToDrag) {
					wantsToDrag = mouseDown;
				}
			}
		}
		
		int desiredChange = theY;
		
		float scrollerSizeDelta = (float)height / (maxY + height);
		
		if(scrollerSizeDelta <= 1) {
			if (mouseX >= x && mouseX <= x + width) {
				if (mouseY >= y && mouseY <= y + height) {
					
					if(scroll > 0) {
						if(desiredChange + (scrollAmount) <= 0) {
							desiredChange += (scrollAmount);
						} else {
							desiredChange = 0;
						}
					} else if(scroll < 0) {
						if(desiredChange - (scrollAmount) >= -maxY) {
							desiredChange -= (scrollAmount);
						} else {
							desiredChange = -maxY;
						}
					}
				}
			}
		}
		
		int newSize = Math.round(scrollerSizeDelta * scrollerHeight);
		
		if(scrollerSizeDelta > 1) {
			newSize = 0;
		}
		
		for(MenuComponent component : components) {
			if(component.getParent() == null) {
				component.setParent(getParent());
			}
			
			component.setRenderOffsetX(x);
			component.setRenderOffsetY(y + theY);
			
			if(component.getWidth() > width) {
				component.setWidth(width - 1);
			}
			
			if(component.getWidth() > width - scrollerWidth && scrollerSizeDelta < 1) {
				component.setWidth(width - scrollerWidth - 1);
			}
		}

		Collections.sort(components, (a, b) -> Integer.compare(a.getPriority().getPriority(), b.getPriority().getPriority()));
		Collections.reverse(components);
		
		int passThroughIndex = -1;
		int index = components.size();
		
		for(MenuComponent component : components) {
			if(!component.passesThrough() && passThroughIndex == -1)
				passThroughIndex = index;
			
			index--;
		}
		
		Collections.reverse(components);	
		
		final int oldIndex = index;
		
		index = oldIndex;
		
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(getRenderX() - 5, Minecraft.getMinecraft().displayHeight - (getRenderY() + getHeight()), getWidth() + 5, getHeight() - 1);
		
		for(MenuComponent component : components) {
			boolean inViewport = component.getRenderY() >= y - component.getHeight() && component.getRenderY() <= y + component.getHeight() + height;
			boolean override = !component.passesThrough();
			
			if(!inViewport && !override) {
				component.onPreSort();
			}
			
			if(inViewport || override) {
				if(index >= passThroughIndex - 1){
					parent.setMouseX(mouseX);
					parent.setMouseY(mouseY);
					
					if(wantsToDrag || (parent.getMouseY() <= getRenderY() || parent.getMouseY() >= getRenderY() + this.height) && !wantsToDrag && !((component.getPriority() == getPriority() && getPriority().getPriority() > MenuPriority.HIGH.getPriority()))) {
						parent.setMouseX(Integer.MAX_VALUE);
						parent.setMouseY(Integer.MAX_VALUE);
					}
				} else if(component instanceof MenuDraggable) {
					index++;
					continue;
				} else {
					parent.setMouseX(Integer.MAX_VALUE);
					parent.setMouseY(Integer.MAX_VALUE);
				}
				
				component.onPreSort();
				component.onRender();
			}
			
			index++;
		}
		
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		GL11.glPopMatrix();
		
		if(passThroughIndex == -1) {
			theY = desiredChange;
			
			if(wantsToDrag && (mouseDown || dragging) && scrollerSizeDelta < 1) {
				float scrollerDelta = (float)(mouseY - (y + minOffset * 2)) / (height - minOffset * 4);
				
				if(scrollerDelta > 1) {
					scrollerDelta = 1;
				} else if(scrollerDelta < 0) {
					scrollerDelta = 0;
				}
				
				theY = Math.round(-scrollerDelta * maxY);
			}
		} else {
			if(scrollerState == ButtonState.ACTIVE) {
				scrollerState = ButtonState.HOVER;
			}
		}
		
		float scrollerDelta = (float)-theY / (scrollerHeight + maxY);
		int newY = scrollerY + Math.round(scrollerHeight * scrollerDelta);

		if(newSize > 4) {
			if(scrollerSizeDelta < 1) {
				scrollerHeight -= 3;
				GlStateManager.color(1, 1,1);

				RoundedUtils.round(scrollerX, y + 2, scrollerWidth, scrollerHeight, 4, new Color(43, 44, 48, 255));
				RoundedUtils.round(scrollerX, newY - 3, scrollerWidth, newSize, 4, new Color(ColorUtil.getColor(false)));
			}
		}
		
		mouseDown = false;
		
		if(wantsToDrag) {
			dragging = Mouse.isButtonDown(0);
			wantsToDrag = dragging; 
		}
	}
	
	public void setFullHeightScroller(boolean fullHeightScroller) {
		this.fullHeightScroller = fullHeightScroller;
	}
}
