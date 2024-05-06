package dev.revere.virago.client.newgui.framework;

import dev.revere.virago.api.module.AbstractModule;
import dev.revere.virago.util.input.BindType;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

/**
 * @author Athena Development
 * @project Athena-Client
 * @date 6/2/2023
 */

public class MinecraftMenuImpl extends GuiScreen {
	protected AbstractModule module;
	protected Menu menu;
	protected boolean ready = false;
	protected float guiScale = 1;

	/**
	 * Constructs a new instance of MinecraftMenuImpl.
	 *
	 * @param module The module associated with the menu.
	 * @param menu    The menu to be displayed.
	 */
	public MinecraftMenuImpl(AbstractModule module, Menu menu) {
		this.module = module;
		this.menu = menu;
	}

	/**
	 * Initializes the GUI, enabling repeat events for keyboard input.
	 */
	@Override
	public void initGui() {

		/*if (OpenGlHelper.shadersSupported && mc.getRenderViewEntity() instanceof EntityPlayer) {
			if (mc.entityRenderer.theShaderGroup != null) {
				mc.entityRenderer.theShaderGroup.deleteShaderGroup();
			}
			mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
		}*/

		Keyboard.enableRepeatEvents(true);
	}

	/**
	 * Renders the screen, scaling and rendering the menu, and handling feature binds.
	 *
	 * @param mouseX        The X position of the mouse.
	 * @param mouseY        The Y position of the mouse.
	 * @param partialTicks  The partial ticks for animation.
	 */
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		GlStateManager.pushMatrix();
		float value = guiScale / new ScaledResolution(mc).getScaleFactor();
		GlStateManager.scale(value, value, value);
		menu.onRender(Math.round((float)mouseX / value), Math.round((float)mouseY / value));
		GlStateManager.popMatrix();
		
		onMouseScroll(Mouse.getDWheel());
		
		if(module != null) {
			if(module.isEnabled() && module.isBound()) {
				if(module.getBindType() == BindType.HOLD) {
					if(!Keyboard.isKeyDown(module.getKey())) {
						mc.displayGuiScreen(null);
					}
				}
			}
		}
	}

	/**
	 * Handles mouse click events on the screen.
	 *
	 * @param mouseX        The X position of the mouse.
	 * @param mouseY        The Y position of the mouse.
	 * @param mouseButton   The button that was clicked.
	 * @throws IOException  If an I/O exception occurs.
	 */
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		menu.onMouseClick(mouseButton);
	}

	/**
	 * Handles mouse click and move events on the screen.
	 *
	 * @param mouseX                The X position of the mouse.
	 * @param mouseY                The Y position of the mouse.
	 * @param clickedMouseButton   The button that was clicked.
	 * @param timeSinceLastClick    The time since the last click event.
	 */
	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		menu.onMouseClickMove(clickedMouseButton);
    }

	/**
	 * Handles mouse scroll events.
	 *
	 * @param scroll    The scroll value.
	 */
	public void onMouseScroll(int scroll) {
		menu.onScroll(scroll);
	}

	/**
	 * Handles key press events.
	 *
	 * @param typedChar The character that was typed.
	 * @param keyCode   The key code of the pressed key.
	 * @throws IOException  If an I/O exception occurs.
	 */
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			if(menu.onMenuExit(keyCode)) {
				return;
			}
			
			mc.displayGuiScreen(null);
		} else {
			menu.onKeyDown(typedChar, keyCode);
		}
	}

	/**
	 * Indicates whether the game should be paused when the menu is displayed.
	 *
	 * @return False, as the game should not be paused.
	 */
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	/**
	 * Called when the GUI is closed, disabling the associated feature and resetting the state.
	 */
	@Override
	public void onGuiClosed() {
		if(module != null) {
			module.setEnabled(false);
		}
		/*if (mc.entityRenderer.theShaderGroup != null) {
			mc.entityRenderer.theShaderGroup.deleteShaderGroup();
			mc.entityRenderer.theShaderGroup = null;
		}*/

		ready = false;
		super.onGuiClosed();
	}

	/**
	 * Retrieves the module associated with the menu.
	 *
	 * @return The associated module.
	 */
	public AbstractModule getModule() {
		return module;
	}
}