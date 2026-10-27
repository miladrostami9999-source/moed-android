package com.miladrostami.moed.domain

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import java.io.InputStream
import java.io.OutputStream

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

    fun encryptedOutput(output:OutputStream,password:String):OutputStream {
        require(password.length >= 6) { "رمز بکاپ باید حداقل ۶ کاراکتر باشد" }
        val salt=ByteArray(16).also{SecureRandom().nextBytes(it)};val iv=ByteArray(12).also{SecureRandom().nextBytes(it)}
        output.write(magic);output.write(salt);output.write(iv)
        val cipher=Cipher.getInstance("AES/GCM/NoPadding").apply{init(Cipher.ENCRYPT_MODE,derive(password,salt),GCMParameterSpec(128,iv))}
        return CipherOutputStream(output,cipher)
    }

    fun decryptedInput(input:InputStream,password:String):InputStream {
        val prefix=ByteArray(magic.size);if(input.read(prefix)!=magic.size)throw IllegalArgumentException("فایل بکاپ ناقص است")
        if(!prefix.contentEquals(magic)) return java.io.SequenceInputStream(prefix.inputStream(),input)
        require(password.isNotEmpty()){"رمز بکاپ را وارد کنید"};val salt=input.readExactly(16);val iv=input.readExactly(12)
        val cipher=Cipher.getInstance("AES/GCM/NoPadding").apply{init(Cipher.DECRYPT_MODE,derive(password,salt),GCMParameterSpec(128,iv))}
        return CipherInputStream(input,cipher)
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

    private fun InputStream.readExactly(size:Int):ByteArray {
        val result=ByteArray(size);var offset=0
        while(offset<size){val count=read(result,offset,size-offset);if(count<0)throw IllegalArgumentException("فایل بکاپ ناقص است");offset+=count}
        return result
    }
}
