package dev.revere.virago.api.compose

import dev.revere.virago.api.skia.Renderer
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display

/**
 * Custom GUI wrapper for the client
 */
public abstract class Screen : GuiScreen() {
    protected val renderer: Renderer = Renderer

    override fun initGui() {
        init()
    }

    override fun drawScreen(p_73863_0_: Int, mouseX: Int, mouseY: Float) {
        render(Mouse.getX().toFloat(), Display.getHeight() - Mouse.getY().toFloat())
    }

    override fun mouseClicked(p_73864_0_: Int, mouseX: Int, mouseY: Int) {
        click(Mouse.getX().toFloat(), Display.getHeight() - Mouse.getY().toFloat(), mouseY)
    }

    override fun mouseReleased(p_73864_0_: Int, mouseX: Int, mouseY: Int) {
        release(Mouse.getX().toFloat(), Display.getHeight() - Mouse.getY().toFloat(), mouseY)
    }

    override fun keyTyped(p_73869_0_: Char, typedChar: Int) {
        key(typedChar, p_73869_0_)

        if(typedChar == Keyboard.KEY_ESCAPE) {
            Minecraft.getMinecraft().displayGuiScreen(null)
        }
    }

    override fun onGuiClosed() {
        close()
    }

    public open fun init() {}
    public open fun render(mx: Float, my: Float) {}
    public open fun click(mx: Float, my: Float, state: Int) {}
    public open fun release(mx: Float, my: Float, state: Int) {}
    public open fun key(key: Int, char: Char) {}
    public open fun close() {}
}