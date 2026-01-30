@file:OptIn(ExperimentalWasmJsInterop::class)

package rs.moma.therminator.ui.utils

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsName
import kotlin.js.JsAny
import kotlin.js.js

@JsName("toastr")
external object Toastr {
    var options: JsAny
    fun success(message: String)
}

fun createToastrOptions(): JsAny = js(
    """({
        positionClass: "toast-bottom-right",
        preventDuplicates: true,
        showDuration: "50",
        hideDuration: "100",
        timeOut: "3000"
    })"""
)

class WebToastService : ToastService {
    init {
        Toastr.options = createToastrOptions()
    }

    override fun show(text: String) {
        Toastr.success(text)
    }
}