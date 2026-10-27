package com.miladrostami.moed.data
import androidx.room.*

@Entity(tableName="payment_items") data class PaymentItem(
 @PrimaryKey(autoGenerate=true) val id:Long=0,val title:String,val category:String,val iconKey:String,
 val amount:Long,val firstDueAt:Long,val repeatRule:String,val installmentCount:Int,val finalDueAt:Long?,
 val reminderMode:String,val reminderDaysBefore:Int=1,val reminderHour:Int=9,val reminderMinute:Int=0,
 val reminderOffsets:String="1",
 val createdAt:Long=System.currentTimeMillis())

@Entity(tableName="installments") data class Installment(
 @PrimaryKey(autoGenerate=true) val id:Long=0,val paymentItemId:Long,val sequence:Int,val amount:Long,
 val dueAt:Long,val paidAt:Long?=null,val receiptPath:String?=null)

data class PaymentWithInstallments(@Embedded val item:PaymentItem,
 @Relation(parentColumn="id",entityColumn="paymentItemId") val installments:List<Installment>)
