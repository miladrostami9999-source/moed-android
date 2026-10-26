package com.miladrostami.moed.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payment_items ORDER BY firstDueAt")
    fun observeItems(): Flow<List<PaymentItem>>

    @Query("SELECT * FROM installments WHERE paymentItemId = :itemId ORDER BY sequence")
    fun observeInstallments(itemId: Long): Flow<List<Installment>>

    @Insert suspend fun insertItem(item: PaymentItem): Long
    @Insert suspend fun insertInstallments(items: List<Installment>)

    @Transaction
    suspend fun addItem(item: PaymentItem, installments: List<Installment>) {
        val id = insertItem(item)
        insertInstallments(installments.map { it.copy(paymentItemId = id) })
    }
}
