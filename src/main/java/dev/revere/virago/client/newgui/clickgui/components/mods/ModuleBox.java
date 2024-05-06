package dev.revere.virago.client.newgui.clickgui.components.mods;

import dev.revere.virago.Virago;
import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.client.newgui.IngameMenu;
import dev.revere.virago.client.newgui.framework.MenuComponent;
import dev.revere.virago.client.newgui.framework.draw.ButtonState;
import dev.revere.virago.client.newgui.framework.draw.DrawType;
import dev.revere.virago.client.services.FontService;
import dev.revere.virago.util.render.RenderUtils;
import dev.revere.virago.util.render.RoundedUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public class ModuleBox extends MenuComponent {
	protected static final int INACTIVE = new Color(10, 90, 32, IngameMenu.MENU_ALPHA).getRGB();
	protected static final int ACTIVE = new Color(90, 10, 12, IngameMenu.MENU_ALPHA).getRGB();
	private final FontService fontService = Virago.getInstance().getServiceManager().getService(FontService.class);
	protected static final int COG_BORDER = new Color(57, 57, 59, IngameMenu.MENU_ALPHA).getRGB();
	
	//protected static final ResourceLocation COG = AssetUtils.getResource("/gui/cog.png");
	
	private static final int MIN_SPACING = 8;
	
	protected AbstractModule module;
	protected ButtonState lastState = ButtonState.NORMAL;
	protected boolean mouseDown;
	
	private List<String> lines = new ArrayList<>();
	private int tHeight;
	
	public ModuleBox(AbstractModule module, int x, int y, int width, int height) {
		super(x, y, width, height);
		this.module = module;
		
		String text = module.getName().toUpperCase();
		
		String[] words = text.split(" ");
		StringBuilder curWord = new StringBuilder();
		
		for(String word : words) {
			String toAdd = word;
			
			if(!curWord.toString().isEmpty()) {
				toAdd = " " + toAdd;
			}
			
			if(fontService.getProductSans28().getStringWidth(curWord.toString() + toAdd) + MIN_SPACING > width) {
				lines.add(curWord.toString());
				curWord.setLength(0);
				toAdd = word;
			}
			
			curWord.append(toAdd);
		}
		
		lines.add(curWord.toString());
		
		tHeight = 0;
		
		for(String line : lines) {
			tHeight += fontService.getProductSans28().getHeight();
		}
	}
	
	@Override
	public void onInitColors() {
		setColor(DrawType.BACKGROUND, ButtonState.NORMAL, new Color(35, 35, 35, 255));
		setColor(DrawType.BACKGROUND, ButtonState.ACTIVE, new Color(35, 35, 35, 255));
		setColor(DrawType.BACKGROUND, ButtonState.HOVER, new Color(0, 0, 0, 255));
		setColor(DrawType.BACKGROUND, ButtonState.HOVERACTIVE, new Color(0, 0, 0, 255));
		setColor(DrawType.BACKGROUND, ButtonState.DISABLED, new Color(35, 35, 35, 255));

		setColor(DrawType.LINE, ButtonState.NORMAL, new Color(36, 36, 38, 255));
		setColor(DrawType.LINE, ButtonState.ACTIVE, new Color(36, 36, 38, 255));
		setColor(DrawType.LINE, ButtonState.HOVER, new Color(36, 36, 38, 255));
		setColor(DrawType.LINE, ButtonState.HOVERACTIVE, new Color(36, 36, 38, 255));
		setColor(DrawType.LINE, ButtonState.DISABLED, new Color(100, 100, 100, 255));

		setColor(DrawType.TEXT, ButtonState.NORMAL, new Color(213, 213, 213, 255));
		setColor(DrawType.TEXT, ButtonState.ACTIVE, new Color(213, 213, 213, 255));
		setColor(DrawType.TEXT, ButtonState.HOVER, new Color(213, 213, 213, 255));
		setColor(DrawType.TEXT, ButtonState.HOVERACTIVE, new Color(213, 213, 213, 255));
		setColor(DrawType.TEXT, ButtonState.DISABLED, new Color(255, 255, 255, 255));
	}
	
	@Override
	public void onMouseClick(int button) {
		if(button == 0) {
			mouseDown = true;
		}
	}
	
	@Override
	public boolean passesThrough() {
		if(disabled || parent == null) {
			return true;
		}

		int x = this.getRenderX();
		int y = this.getRenderY();
		int mouseX = parent.getMouseX();
		int mouseY = parent.getMouseY();
		
		if(mouseDown) {
			mouseDown = false;
			
			if(mouseX >= x && mouseX <= x + width) {
				if(mouseY >= y && mouseY <= y + height + 1) {
					
					if(mouseX >= x + 10 && mouseX <= x + width - 11) {
						if(mouseY >= y + height - 10 - 20 && mouseY <= y + height - 10) {
							if(module.isEnabled()) {
								module.setEnabled(false);
							} else {
								module.setEnabled(true);
							}
							onToggle();
						}
					}
					
					if(!module.getSettings().isEmpty()) {
						if(mouseX >= x + width - 14 - 17 - 4 && mouseX <= x + width - 14 - 17 - 4 + 24) {
							if(mouseY >= y + 14 - 4 && mouseY <= y + 14 - 3 + 23) {
								onOpenSettings();
							}
						}
					}
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public void onPreSort() {
		int x = this.getRenderX();
		int y = this.getRenderY();
		int mouseX = parent.getMouseX();
		int mouseY = parent.getMouseY();
		
		ButtonState state = ButtonState.NORMAL;
		
		if(!disabled) {
			if(mouseX >= x && mouseX <= x + width) {
				if(mouseY >= y && mouseY <= y + height) {
					state = ButtonState.HOVER;
				}
			}
		} else {
			state = ButtonState.DISABLED;
		}
		
		lastState = state;
	}
	
	@Override
	public void onRender() {
		int backgroundColor = getColor(DrawType.BACKGROUND, lastState);
		int lineColor = getColor(DrawType.LINE, lastState);
		int textColor = getColor(DrawType.TEXT, lastState);

		int x = this.getRenderX();
		int y = this.getRenderY();

		int defaultColor = getColor(DrawType.BACKGROUND, ButtonState.NORMAL);
		int drawColor = defaultColor;

		//GlStateManager.color(1,1,1);

		RoundedUtils.round(x, y, width, height, 8f, new Color(43, 44, 48, 255));

		int yPos = y + (height / 2) - tHeight / 2 + 10;

		for (String line : lines) {
			fontService.getProductSans28().drawString(line, x + (float) width / 2 - fontService.getProductSans28().getStringWidth(line) / 2, yPos, textColor);
			yPos += fontService.getProductSans28().getHeight();
		}

		int mouseX = parent.getMouseX();
		int mouseY = parent.getMouseY();

		if (mouseX >= x + 10 && mouseX <= x + width - 11) {
			if (mouseY >= y + height - 10 - 20 && mouseY <= y + height - 10) {
				drawColor = backgroundColor;
			}
		}

		/*
		if (!Objects.equals(module.getIcon(), "")) {
			RenderUtils.drawImage(new ResourceLocation(module.getIcon()), x + width / 2 - 25,  y + 20, 50, 50);
		}
		 */

		RoundedUtils.round(x + 10, y + height - 10 - 20, width - 20, 25, 4, module.isEnabled() ? new Color(0, 200, 0, 255) : new Color(200, 0, 0, 225));
		String text = module.isEnabled() ? "ENABLED" : "DISABLED";

		fontService.getProductSans().drawString(text, x + (float) width / 2 - (double) fontService.getProductSans().getStringWidth(text) / 2, y + height - 10 - 14.5f, -1);
		fontService.getProductSans28().drawString(text, x + (float) width / 2 - fontService.getProductSans28().getStringWidth(text) / 2, y + height - 10 - 15, -1);

		if (!module.getSettings().isEmpty()) {
			drawColor = defaultColor;

			if (mouseX >= x + width - 14 - 17 - 4 && mouseX <= x + width - 14 - 17 - 4 + 24) {
				if (mouseY >= y + 14 - 4 && mouseY <= y + 14 - 3 + 23) {
					drawColor = Color.black.getRGB();
				}
			}

			/*drawShadowUp(x + width - 14 - 17 - 4, y + 14 - 4, 25);
			drawShadowLeft(x + width - 14 - 17 - 4, y + 14 - 4, 25);
			drawShadowDown(x + width - 14 - 17 - 4, y + 14 - 4 + 25, 25);
			drawShadowRight(x + width - 14 - 17 - 4 + 25, y + 14 - 4, 25);*/

			//rip.athena.client.gui.framework.draw.DrawImpl.drawRect(x + width - 14 - 17 - 4, y + 14 - 4, 25, 25, COG_BORDER);
			//rip.athena.client.gui.framework.draw.DrawImpl.drawRect(x + width - 14 - 17 - 3, y + 14 - 3, 23, 23, drawColor);
			//RoundedUtils.drawGradientRound(x + width - 14 - 17 - 3, y + 14 - 3, 23, 23, 6, Athena.INSTANCE.getThemeManager().getTheme().getFirstColor(), Athena.INSTANCE.getThemeManager().getTheme().getFirstColor(), Athena.INSTANCE.getThemeManager().getTheme().getSecondColor(), Athena.INSTANCE.getThemeManager().getTheme().getSecondColor());

			RoundedUtils.round(x + width - 14 - 17 - 3, y + 14 - 3, 23, 23, 9, new Color(10,10,10, 150));


			drawImage(new ResourceLocation("Athena/gui/menu/settings.png"), x + width - 14 - 17, y + 14, 17, 17);
		}
	}
	
	public AbstractModule getModule() {
		return module;
	}
	
	public void onOpenSettings() {}
	public void onToggle() {}
}
