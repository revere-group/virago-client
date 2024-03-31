package dev.revere.virago.api.skia


import org.lwjgl.opengl.*

public object UIState {
    private val pixelStores = HashMap<Int, Int>()

    private var lastActiveTexture = 0
    private var lastProgram = 0
    private var lastSampler = 0
    private var lastVertexArray = 0
    private var lastArrayBuffer = 0

    private var lastBlendSrcRgb = 0
    private var lastBlendDstRgb = 0
    private var lastBlendSrcAlpha = 0
    private var lastBlendDstAlpha = 0
    private var lastBlendEquationRgb = 0
    private var lastBlendEquationAlpha = 0

    public fun backup() {
        GL11.glPushClientAttrib(GL11.GL_ALL_CLIENT_ATTRIB_BITS)
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)

        lastActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE)
        lastProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM)
        lastSampler = GL11.glGetInteger(GL33.GL_SAMPLER_BINDING)
        lastArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING)
        lastVertexArray = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING)

        pixelStoreParameters.forEach {
            pixelStores[it] = GL11.glGetInteger(it)
        }

        lastBlendSrcRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB)
        lastBlendDstRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB)
        lastBlendSrcAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA)
        lastBlendDstAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA)
        lastBlendEquationRgb = GL11.glGetInteger(GL20.GL_BLEND_EQUATION_RGB)
        lastBlendEquationAlpha = GL11.glGetInteger(GL20.GL_BLEND_EQUATION_ALPHA)
    }

    public fun restore() {
        GL11.glPopAttrib()
        GL11.glPopClientAttrib()

        GL20.glUseProgram(lastProgram)
        GL33.glBindSampler(0, lastSampler)
        GL13.glActiveTexture(lastActiveTexture)
        GL30.glBindVertexArray(lastVertexArray)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, lastArrayBuffer)
        GL20.glBlendEquationSeparate(lastBlendEquationRgb, lastBlendEquationAlpha)
        GL14.glBlendFuncSeparate(lastBlendSrcRgb, lastBlendDstRgb, lastBlendSrcAlpha, lastBlendDstAlpha)

        pixelStoreParameters.forEach {
            GL11.glPixelStorei(it, pixelStores[it]!!)
        }
    }
}

private val pixelStoreParameters = intArrayOf(
    GL11.GL_PACK_SWAP_BYTES,
    GL11.GL_PACK_LSB_FIRST,
    GL11.GL_PACK_ROW_LENGTH,
    GL12.GL_PACK_IMAGE_HEIGHT,
    GL11.GL_PACK_SKIP_PIXELS,
    GL11.GL_PACK_SKIP_ROWS,
    GL12.GL_PACK_SKIP_IMAGES,
    GL11.GL_PACK_ALIGNMENT,
    GL11.GL_UNPACK_SWAP_BYTES,
    GL11.GL_UNPACK_LSB_FIRST,
    GL11.GL_UNPACK_ROW_LENGTH,
    GL12.GL_UNPACK_IMAGE_HEIGHT,
    GL11.GL_UNPACK_SKIP_PIXELS,
    GL11.GL_UNPACK_SKIP_ROWS,
    GL12.GL_UNPACK_SKIP_IMAGES,
    GL11.GL_UNPACK_ALIGNMENT
)
