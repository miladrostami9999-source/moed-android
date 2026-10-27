package com.miladrostami.moed

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.File
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Protects the random SQLCipher passphrase with a non-exportable Android Keystore key. */
object DatabaseSecurity {
    private const val alias = "mento-database-key"
    private const val prefs = "mento_security"
    private const val encryptedKey = "encrypted_database_passphrase"
    private const val ivKey = "database_passphrase_iv"

    fun passphrase(context: Context): ByteArray {
        val store = context.getSharedPreferences(prefs, Context.MODE_PRIVATE)
        val encrypted = store.getString(encryptedKey, null)
        val iv = store.getString(ivKey, null)
        if (encrypted != null && iv != null) return decrypt(encrypted, iv)

        val value = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.ENCRYPT_MODE, keystoreKey())
        }
        val payload = cipher.doFinal(value)
        store.edit()
            .putString(encryptedKey, Base64.encodeToString(payload, Base64.NO_WRAP))
            .putString(ivKey, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .apply()
        return value
    }

    fun isPlaintextDatabase(file: File): Boolean {
        if (!file.exists() || file.length() < 16) return false
        return runCatching {
            file.inputStream().use { input ->
                val header = ByteArray(16)
                input.read(header) == header.size && String(header, Charsets.US_ASCII) == "SQLite format 3\u0000"
            }
        }.getOrDefault(false)
    }

    private fun decrypt(encrypted: String, iv: String): ByteArray =
        Cipher.getInstance("AES/GCM/NoPadding").run {
            init(Cipher.DECRYPT_MODE, keystoreKey(), GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP)))
            doFinal(Base64.decode(encrypted, Base64.NO_WRAP))
        }

    private fun keystoreKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(alias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(
                KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            generateKey()
        }
    }
}
