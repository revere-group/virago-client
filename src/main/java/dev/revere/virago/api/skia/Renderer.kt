package dev.revere.virago.api.skia

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import org.jetbrains.skia.*
import org.jetbrains.skia.FramebufferFormat.Companion.GR_GL_RGBA8
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11

public object Renderer {
    private var context: DirectContext? = null
    public lateinit var surface: Surface
    public val canvas: Canvas get() = surface.canvas

    public fun init() {
        if(context == null) {
            context = DirectContext.makeGL()
        }
    }

    public fun createSurface() {
        createSurface(Display.getWidth(), Display.getHeight())
    }

    private fun createSurface(width: Int, height: Int) {
        val renderTarget = BackendRenderTarget.makeGL(width, height, 0, 8, Minecraft.getMinecraft().framebuffer.framebufferObject, GR_GL_RGBA8)
        surface = Surface.makeFromBackendRenderTarget(
            context!!, renderTarget, SurfaceOrigin.BOTTOM_LEFT, SurfaceColorFormat.RGBA_8888, ColorSpace.sRGB
        )!!
    }

    public fun prepareFlush() {
        UIState.backup()
        GlStateManager.clearColor(0f, 0f, 0f, 0f)
        context?.resetGLAll()

        GL11.glDisable(GL11.GL_ALPHA_TEST)
    }

    public fun flush() {
        context?.flush()
        UIState.restore()
    }
}