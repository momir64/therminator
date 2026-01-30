package rs.moma.therminator.data.local

import kotlinx.browser.localStorage
import kotlinx.browser.window

class WebSecureStore : SecureStore {
    private val PASS_KEY = "password"
    private val XOR_KEY = 0x64

    private fun xor(bytes: ByteArray) = bytes.map { it.toInt().xor(XOR_KEY).toByte() }.toByteArray()

    override suspend fun save(password: String) {
        val bytes = password.encodeToByteArray()
        val base64 = window.btoa(xor(bytes).decodeToString())
        localStorage.setItem(PASS_KEY, base64)
    }

    override suspend fun load(): String? {
        val base64 = localStorage.getItem(PASS_KEY) ?: return null
        val original = xor(window.atob(base64).encodeToByteArray())
        return original.decodeToString()
    }

    override suspend fun clear() {
        localStorage.removeItem(PASS_KEY)
    }
}
