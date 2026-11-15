package com.example.naepapp.core.storage

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "naep_tokens")

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context
) : TokenStorage {

    private object PreferencesKeys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val alias = "naep_token_encryption_key"

    init {
        if (!keyStore.containsAlias(alias)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        return (keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry).secretKey
    }

    private fun encrypt(input: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(input.toByteArray(Charsets.UTF_8))
        val combined = iv + encrypted
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    private fun decrypt(encryptedStr: String): String {
        val combined = Base64.decode(encryptedStr, Base64.NO_WRAP)
        val iv = combined.copyOfRange(0, 12)
        val encrypted = combined.copyOfRange(12, combined.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        val decrypted = cipher.doFinal(encrypted)
        return String(decrypted, Charsets.UTF_8)
    }

    override suspend fun save(accessToken: String, refreshToken: String) {
        val encAccess = encrypt(accessToken)
        val encRefresh = encrypt(refreshToken)
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ACCESS_TOKEN] = encAccess
            prefs[PreferencesKeys.REFRESH_TOKEN] = encRefresh
        }
    }

    override suspend fun access(): String? {
        val prefs = context.dataStore.data.first()
        val encAccess = prefs[PreferencesKeys.ACCESS_TOKEN] ?: return null
        return try {
            decrypt(encAccess)
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun refresh(): String? {
        val prefs = context.dataStore.data.first()
        val encRefresh = prefs[PreferencesKeys.REFRESH_TOKEN] ?: return null
        return try {
            decrypt(encRefresh)
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.remove(PreferencesKeys.ACCESS_TOKEN)
            prefs.remove(PreferencesKeys.REFRESH_TOKEN)
        }
    }
}
