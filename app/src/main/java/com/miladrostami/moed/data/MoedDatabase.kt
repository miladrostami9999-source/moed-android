package com.miladrostami.moed.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PaymentItem::class, Installment::class], version = 1, exportSchema = false)
abstract class MoedDatabase : RoomDatabase() { abstract fun paymentDao(): PaymentDao }
