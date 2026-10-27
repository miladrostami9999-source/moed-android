package com.miladrostami.moed.domain

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object BackupCrypto {
    private val magic = "MOED1".toByteArray()
    private const val iterations = 120_000

    fun encrypt(data: ByteArray, password: String): ByteArray {
        require(password.length >= 6) { "رمز بکاپ باید حداقل ۶ کاراکتر باشد" }
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val key = derive(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        }
        return magic + salt + iv + cipher.doFinal(data)
    }

    fun decryptOrLegacy(data: ByteArray, password: String): ByteArray {
        if (!data.take(magic.size).toByteArray().contentEquals(magic)) return data
        require(password.isNotEmpty()) { "رمز بکاپ را وارد کنید" }
        require(data.size > magic.size + 28) { "فایل بکاپ معتبر نیست" }
        val salt = data.copyOfRange(magic.size, magic.size + 16)
        val iv = data.copyOfRange(magic.size + 16, magic.size + 28)
        val payload = data.copyOfRange(magic.size + 28, data.size)
        return try {
            Cipher.getInstance("AES/GCM/NoPadding").apply {
                init(Cipher.DECRYPT_MODE, derive(password, salt), GCMParameterSpec(128, iv))
            }.doFinal(payload)
        } catch (_: Exception) { throw IllegalArgumentException("رمز اشتباه است یا فایل بکاپ آسیب دیده") }
    }

    private fun derive(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, 256)
        val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        spec.clearPassword()
        return SecretKeySpec(bytes, "AES")
    }
}
