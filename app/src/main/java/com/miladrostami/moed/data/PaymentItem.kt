package com.miladrostami.moed.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_items")
data class PaymentItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val iconKey: String,
    val amount: Long,
    val firstDueAt: Long,
    val repeatRule: String,
    val installmentCount: Int?,
    val finalDueAt: Long?,
    val reminderMode: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "installments")
data class Installment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val paymentItemId: Long,
    val sequence: Int,
    val amount: Long,
    val dueAt: Long,
    val paidAt: Long? = null,
    val receiptUri: String? = null
)
