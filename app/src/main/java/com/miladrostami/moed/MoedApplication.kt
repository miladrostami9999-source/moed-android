package com.miladrostami.moed

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.miladrostami.moed.data.MoedDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

class MoedApplication : Application() {
    private val migration1To2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE payment_items ADD COLUMN reminderHour INTEGER NOT NULL DEFAULT 9")
            db.execSQL("ALTER TABLE payment_items ADD COLUMN reminderMinute INTEGER NOT NULL DEFAULT 0")
        }
    }
    private val migration2To3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE payment_items ADD COLUMN reminderOffsets TEXT NOT NULL DEFAULT '1'")
        }
    }
    private val migration3To4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE payment_items ADD COLUMN ownerKey TEXT NOT NULL DEFAULT 'local'")
        }
    }
    val database by lazy {
        val builder = Room.databaseBuilder(this, MoedDatabase::class.java, "moed.db")
            .addMigrations(migration1To2, migration2To3, migration3To4)
        val existingDatabase = getDatabasePath("moed.db")
        if (!DatabaseSecurity.isPlaintextDatabase(existingDatabase)) {
            System.loadLibrary("sqlcipher")
            builder.openHelperFactory(SupportOpenHelperFactory(DatabaseSecurity.passphrase(this)))
        }
        builder.build()
    }
}
