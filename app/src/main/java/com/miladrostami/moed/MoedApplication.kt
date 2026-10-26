package com.miladrostami.moed

import android.app.Application
import androidx.room.Room
import com.miladrostami.moed.data.MoedDatabase

class MoedApplication : Application() {
    val database by lazy {
        Room.databaseBuilder(this, MoedDatabase::class.java, "moed.db")
            .fallbackToDestructiveMigration()
            .build()
    }
}
