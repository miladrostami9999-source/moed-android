package com.miladrostami.moed.domain

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.miladrostami.moed.MoedApplication
import com.miladrostami.moed.data.PaymentDao
import com.miladrostami.moed.tx
import java.io.File
import java.io.FileOutputStream
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class AutoBackupStatus(val enabled: Boolean, val lastBackupAt: Long?, val fileExists: Boolean)

/**
 * Keeps one password-encrypted Mento backup in filesDir/auto_backup/.
 *
 * Only that folder is included in Android's own cloud backup (see res/xml/backup_rules.xml and
 * data_extraction_rules.xml), so Google's backup for this phone carries the encrypted file to a new
 * phone. The database, its Keystore key and the saved password are never part of that backup.
 */
object AutoBackup {
    private const val PREFS = "mento_auto_backup"
    private const val ALIAS = "mento-autobackup-key"
    private const val DIR = "auto_backup"
    private const val FILE_NAME = "mento-auto.mbak"
    private const val WORK_NAME = "mento_auto_backup"
    /** Android's cloud backup is limited to 25 MB per app, so big receipt collections are left out. */
    private const val MAX_RECEIPT_BYTES = 8L * 1024 * 1024
    const val MIN_PASSWORD = 6

    fun file(context: Context): File = File(context.filesDir, "$DIR/$FILE_NAME")

    fun hasRestorableFile(context: Context): Boolean = file(context).let { it.isFile && it.length() > 0 }

    fun status(context: Context): AutoBackupStatus {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val last = prefs.getLong("last", 0L).takeIf { it > 0L }
        return AutoBackupStatus(prefs.getBoolean("enabled", false), last, hasRestorableFile(context))
    }

    /** Saves the password (wrapped by the Keystore) and creates the first backup right away. */
    suspend fun enable(context: Context, dao: PaymentDao, password: String) {
        require(password.length >= MIN_PASSWORD) {
            tx("رمز بکاپ باید حداقل ۶ کاراکتر باشد", "Backup password must contain at least 6 characters")
        }
        val wrapped = wrap(password)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("pw", wrapped.first).putString("iv", wrapped.second).putBoolean("enabled", true).apply()
        runNow(context, dao).getOrElse {
            disable(context)
            throw it
        }
    }

    fun disable(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
        File(context.filesDir, DIR).deleteRecursively()
    }

    /** Writes a fresh encrypted backup. Does nothing (and succeeds) while automatic backup is off. */
    suspend fun runNow(context: Context, dao: PaymentDao): Result<Unit> = runCatching {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean("enabled", false)) return@runCatching
        val password = unwrap(prefs.getString("pw", null)!!, prefs.getString("iv", null)!!)
        val directory = File(context.filesDir, DIR).apply { mkdirs() }
        val temp = File(directory, "$FILE_NAME.tmp")
        val receiptBytes = File(context.filesDir, "receipts").listFiles().orEmpty().sumOf { it.length() }
        try {
            FileOutputStream(temp).use { BackupManager.writeTo(context, it, dao, password, receiptBytes <= MAX_RECEIPT_BYTES) }
            val target = file(context)
            if (!temp.renameTo(target)) { temp.copyTo(target, overwrite = true); temp.delete() }
        } finally {
            temp.delete()
        }
        prefs.edit().putLong("last", System.currentTimeMillis()).apply()
    }

    /** Queues a backup a few seconds from now; repeated calls replace the pending one. */
    fun schedule(context: Context) {
        if (!status(context).enabled) return
        val request = OneTimeWorkRequestBuilder<AutoBackupWorker>().setInitialDelay(3, TimeUnit.SECONDS).build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    private fun wrap(text: String): Pair<String, String> {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()) }
        val payload = cipher.doFinal(text.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(payload, Base64.NO_WRAP) to Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
    }

    private fun unwrap(payload: String, iv: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP)))
        }
        return String(cipher.doFinal(Base64.decode(payload, Base64.NO_WRAP)), Charsets.UTF_8)
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(
                KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            generateKey()
        }
    }
}

class AutoBackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val dao = (applicationContext as MoedApplication).database.paymentDao()
        return AutoBackup.runNow(applicationContext, dao).fold(
            { Result.success() },
            { if (runAttemptCount < 2) Result.retry() else Result.failure() }
        )
    }
}
