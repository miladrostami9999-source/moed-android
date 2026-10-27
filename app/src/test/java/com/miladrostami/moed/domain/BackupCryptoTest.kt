package com.miladrostami.moed.domain

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupCryptoTest {
    @Test fun encryptedBackupRoundTrips() {
        val original = "اطلاعات موعد".toByteArray()
        val encrypted = BackupCrypto.encrypt(original, "secure-password")
        assertArrayEquals(original, BackupCrypto.decryptOrLegacy(encrypted, "secure-password"))
    }

    @Test fun wrongPasswordIsRejected() {
        val encrypted = BackupCrypto.encrypt("test".toByteArray(), "correct-password")
        assertThrows(IllegalArgumentException::class.java) { BackupCrypto.decryptOrLegacy(encrypted, "wrong-password") }
    }
}
