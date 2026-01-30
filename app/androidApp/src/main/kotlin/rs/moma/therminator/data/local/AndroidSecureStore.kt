package rs.moma.therminator.data.local

import androidx.datastore.preferences.core.stringPreferencesKey
import android.security.keystore.KeyGenParameterSpec.Builder
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.Preferences
import android.security.keystore.KeyProperties.*
import androidx.datastore.preferences.core.edit
import javax.crypto.spec.GCMParameterSpec
import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.first
import javax.crypto.KeyGenerator
import android.content.Context
import java.security.KeyStore
import javax.crypto.SecretKey
import android.util.Base64
import javax.crypto.Cipher

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("secure_store")

class AndroidSecureStore(private val context: Context) : SecureStore {
    private val PASS_KEY = stringPreferencesKey("password")
    private val TRANSFORMATION = "AES/GCM/NoPadding"
    private val KEY_ALIAS = "therminator_key"

    private fun getOrCreateKey(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        ks.getKey(KEY_ALIAS, null)?.let { return it as SecretKey }

        val kg = KeyGenerator.getInstance("AES", "AndroidKeyStore")
        kg.init(
            Builder(KEY_ALIAS, PURPOSE_ENCRYPT or PURPOSE_DECRYPT)
                .setEncryptionPaddings(ENCRYPTION_PADDING_NONE)
                .setBlockModes(BLOCK_MODE_GCM)
                .build()
        )
        return kg.generateKey()
    }

    override suspend fun save(password: String) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
        val encrypted = Base64.encodeToString(cipher.doFinal(password.toByteArray()), Base64.NO_WRAP)
        context.dataStore.edit { it[PASS_KEY] = "$iv:$encrypted" }
    }

    override suspend fun load(): String? {
        val stored = context.dataStore.data.first()[PASS_KEY] ?: return null
        val (ivStr, dataStr) = stored.split(":")
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val key = getOrCreateKey()
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, Base64.decode(ivStr, Base64.NO_WRAP)))
        return String(cipher.doFinal(Base64.decode(dataStr, Base64.NO_WRAP)))
    }

    override suspend fun clear() {
        context.dataStore.edit { it.remove(PASS_KEY) }
    }
}