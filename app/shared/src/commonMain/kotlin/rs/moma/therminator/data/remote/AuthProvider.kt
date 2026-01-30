package rs.moma.therminator.data.remote

import kotlin.io.encoding.Base64

object AuthProvider {
    var password: String? = null

    fun getHeaderValue(): String? = password?.let {
        Base64.encode(it.encodeToByteArray())
    }
}