package dev.revere.virago.client.gui.newgui.clickgui.components.mods;

import dev.revere.virago.client.gui.newgui.framework.draw.ButtonState;
import dev.revere.virago.client.gui.newgui.framework.draw.DrawType;

import java.awt.*;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public class FeatureValueText extends FeatureText {
	
	public FeatureValueText(String text, String tooltip, int x, int y) {
		super(text, tooltip, x, y);
	}
	
	public FeatureValueText(String text, int x, int y) {
		super(text, x, y);
	}

	@Override
	public void onInitColors() {
		super.onInitColors();
		
		setColor(DrawType.TEXT, ButtonState.NORMAL, new Color(158, 158, 158, 255));
		setColor(DrawType.TEXT, ButtonState.HOVER, new Color(178, 178, 178, 255));
		setColor(DrawType.TEXT, ButtonState.DISABLED, new Color(200, 200, 200, 255));
	}

}
