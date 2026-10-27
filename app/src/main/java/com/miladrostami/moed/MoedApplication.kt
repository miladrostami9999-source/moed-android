package com.miladrostami.moed

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.miladrostami.moed.data.MoedDatabase

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
    val database by lazy {
        Room.databaseBuilder(this, MoedDatabase::class.java, "moed.db")
            .addMigrations(migration1To2, migration2To3)
            .build()
    }
}
