package com.miladrostami.moed.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao interface PaymentDao {
 @Transaction @Query("SELECT * FROM payment_items ORDER BY firstDueAt") fun observeAll():Flow<List<PaymentWithInstallments>>
 @Transaction @Query("SELECT * FROM payment_items ORDER BY firstDueAt") suspend fun getAll():List<PaymentWithInstallments>
 @Transaction @Query("SELECT * FROM payment_items WHERE id=:id") suspend fun getById(id:Long):PaymentWithInstallments?
 @Insert suspend fun insertItem(item:PaymentItem):Long
 @Insert suspend fun insertInstallments(items:List<Installment>)
 @Update suspend fun updateItem(item:PaymentItem)
 @Update suspend fun updateInstallment(item:Installment)
 @Query("DELETE FROM payment_items WHERE id=:id") suspend fun deleteItem(id:Long)
 @Query("DELETE FROM installments WHERE paymentItemId=:id") suspend fun deleteInstallmentsForItem(id:Long)
 @Query("DELETE FROM installments") suspend fun clearInstallments()
 @Query("DELETE FROM payment_items") suspend fun clearItems()
 @Query("SELECT COUNT(*) FROM payment_items WHERE ownerKey=:owner") suspend fun countForOwner(owner:String):Int
 @Query("UPDATE payment_items SET ownerKey=:owner WHERE ownerKey='local'") suspend fun claimLocalData(owner:String)
 @Transaction suspend fun addItem(item:PaymentItem,installments:List<Installment>):Long { val id=insertItem(item);insertInstallments(installments.map{it.copy(paymentItemId=id)});return id }
 @Transaction suspend fun deletePayment(id:Long){deleteInstallmentsForItem(id);deleteItem(id)}
 @Transaction suspend fun replaceSchedule(item:PaymentItem,installments:List<Installment>){updateItem(item);deleteInstallmentsForItem(item.id);insertInstallments(installments.map{it.copy(id=0,paymentItemId=item.id)})}
 @Transaction suspend fun replaceAll(data:List<PaymentWithInstallments>){clearInstallments();clearItems();data.forEach{r->val id=insertItem(r.item.copy(id=0));insertInstallments(r.installments.map{it.copy(id=0,paymentItemId=id)})}}
 @Transaction suspend fun mergeAll(data:List<PaymentWithInstallments>){data.forEach{r->val id=insertItem(r.item.copy(id=0));insertInstallments(r.installments.map{it.copy(id=0,paymentItemId=id)})}}
}
