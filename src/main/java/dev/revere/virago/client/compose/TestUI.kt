package dev.revere.virago.client.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.revere.virago.api.compose.ComposeUI

public object TestUI : ComposeUI() {
    // @Composable is required
    // Unit is pretty much a lambda in Kotlin
    // If u don't do @Composable & call composable invocations ull get an error during compilation
    override val contents: @Composable () -> Unit
        get() = @Composable {
            TestUI()
        }
}

@Composable
private fun TestUI() {
    Box(Modifier.fillMaxSize().background(Color(15, 15, 15))) {
        Button({ println("this works") }) {
            Text("test")
        }
    }
}