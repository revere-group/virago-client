package dev.revere.virago.api.compose

import androidx.compose.runtime.Composable
import net.minecraft.client.Minecraft

/**
 * hi remi, if ur ever touching ui this is what all compose screens extend off
 * @see ComposeUI.build
 */
public abstract class ComposeUI {
    protected val mc = Minecraft.getMinecraft()
    protected abstract val contents: @Composable () -> Unit

    /**
     * Returns the GuiScreen for this screen
     */
    public fun build(): ScreenCompose {
        return ScreenCompose(contents)
    }
}