package rs.moma.therminator

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import org.koin.core.context.startKoin
import rs.moma.therminator.ui.theme.*
import org.w3c.dom.HTMLStyleElement
import kotlinx.browser.document
import rs.moma.therminator.di.*

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin {
        modules(listOf(sharedModule, webModule))
    }

    setupColors()

    ComposeViewport("compose-root") {
        App()
    }
}

private fun setupColors() {
    val style = document.createElement("style") as HTMLStyleElement
    style.innerHTML = """
        :root {
          --toast-edge-color: ${OutlineColor.toCss()};
          --toast-bg-color: ${CardColor.toCss()};
          --bg-color: ${BackgroundColor.toCss()};
        }
    """.trimIndent()
    document.head!!.appendChild(style)
}