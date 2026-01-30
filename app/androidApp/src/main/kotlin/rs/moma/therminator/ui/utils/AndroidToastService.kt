package rs.moma.therminator.ui.utils

import android.content.Context
import android.widget.Toast
import android.os.Handler
import android.os.Looper

class AndroidToastService(private val context: Context) : ToastService {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var currentToast: Toast? = null

    override fun show(text: String) {
        if (Looper.myLooper() == Looper.getMainLooper())
            showInternal(text)
        else
            mainHandler.post { showInternal( text) }
    }

    private fun showInternal(text: CharSequence) {
        currentToast?.cancel()
        currentToast = Toast.makeText(context.applicationContext, text, Toast.LENGTH_SHORT)
        currentToast?.show()
    }
}
