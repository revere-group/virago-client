package dev.revere.virago.client.gui.newgui.framework.draw;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public class MenuColorType {
	Map<String, Color> colors;
	
	public MenuColorType() {
		colors = new HashMap<>();
	}
	
	public int getColor(DrawType type, ButtonState state) {
		if(colors.containsKey(type.toString() + state.toString())) 
			return colors.get(type.toString() + state.toString()).getRGB();
		else
			return colors.get(type.toString()).getRGB();
	}

	public void setColor(DrawType type, ButtonState state, Color color) {
		colors.put(type.toString() + state.toString(), color);
	}
}
