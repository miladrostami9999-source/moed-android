package com.miladrostami.moed

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.miladrostami.moed.data.*
import com.miladrostami.moed.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class AppViewModel(app:Application):AndroidViewModel(app){
 private val dao=(app as MoedApplication).database.paymentDao()
 private val activeOwner=MutableStateFlow("local")
 val payments=combine(dao.observeAll(),activeOwner){rows,owner->rows.filter{it.item.ownerKey==owner}}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 fun setActiveOwner(owner:String,claimLegacy:Boolean=false){viewModelScope.launch{if(claimLegacy&&owner!="local"&&dao.countForOwner(owner)==0)dao.claimLocalData(owner);activeOwner.value=owner}}
 fun add(title:String,category:String,icon:String,amount:Long,date:PersianDate,repeat:String,count:Int,mode:String,offsets:Set<Int>,hour:Int,minute:Int){viewModelScope.launch{
  val dates=(0 until count).map{if(repeat=="monthly")addPersianMonths(date,it).toMillis() else date.toMillis()}
  val normalized=offsets.ifEmpty{setOf(0)}.sortedDescending();val item=PaymentItem(title=title,category=category,iconKey=icon,amount=amount,firstDueAt=dates.first(),repeatRule=repeat,installmentCount=count,finalDueAt=dates.last(),reminderMode=mode,reminderDaysBefore=normalized.first(),reminderHour=hour,reminderMinute=minute,reminderOffsets=normalized.joinToString(","),ownerKey=activeOwner.value)
  val id=dao.addItem(item,dates.mapIndexed{index,due->Installment(paymentItemId=0,sequence=index+1,amount=amount,dueAt=due)})
  dates.forEachIndexed{index,due->scheduleReminders(id,index,title,due,normalized,mode,hour,minute)}
 }}
 fun update(row:PaymentWithInstallments,title:String,category:String,icon:String,amount:Long,date:PersianDate,repeat:String,count:Int,mode:String,offsets:Set<Int>,hour:Int,minute:Int){viewModelScope.launch{
  row.installments.forEach{cancelReminders(row.item.id,it.sequence-1)}
  val hasPaid=row.installments.any{it.paidAt!=null}
  val finalAmount=if(hasPaid)row.item.amount else amount;val finalRepeat=if(hasPaid)row.item.repeatRule else repeat;val finalCount=if(hasPaid)row.item.installmentCount else count
  val dates=if(hasPaid)row.installments.sortedBy{it.sequence}.map{it.dueAt} else (0 until finalCount).map{if(finalRepeat=="monthly")addPersianMonths(date,it).toMillis() else date.toMillis()}
  val normalized=offsets.ifEmpty{setOf(0)}.sortedDescending();val item=row.item.copy(title=title,category=category,iconKey=icon,amount=finalAmount,firstDueAt=dates.first(),repeatRule=finalRepeat,installmentCount=finalCount,finalDueAt=dates.last(),reminderMode=mode,reminderDaysBefore=normalized.first(),reminderHour=hour,reminderMinute=minute,reminderOffsets=normalized.joinToString(","))
  val installments=if(hasPaid)row.installments else dates.mapIndexed{index,due->Installment(paymentItemId=row.item.id,sequence=index+1,amount=finalAmount,dueAt=due)}
  dao.replaceSchedule(item,installments)
  installments.filter{it.paidAt==null}.forEach{scheduleReminders(row.item.id,it.sequence-1,title,it.dueAt,normalized,mode,hour,minute)}
 }}
 fun togglePaid(i:Installment){viewModelScope.launch{val row=dao.getById(i.paymentItemId);val paid=i.paidAt==null;dao.updateInstallment(i.copy(paidAt=if(paid)System.currentTimeMillis() else null));if(paid)cancelReminders(i.paymentItemId,i.sequence-1) else row?.let{r->scheduleReminders(i.paymentItemId,i.sequence-1,r.item.title,i.dueAt,offsets(r.item),r.item.reminderMode,r.item.reminderHour,r.item.reminderMinute)}}}
 fun attach(i:Installment,uri:Uri){viewModelScope.launch{val dir=File(getApplication<Application>().filesDir,"receipts").apply{mkdirs()};i.receiptPath?.let{File(it).delete()};val ext=getApplication<Application>().contentResolver.getType(uri)?.substringAfterLast('/')?:"bin";val file=File(dir,"receipt_${i.id}.$ext");getApplication<Application>().contentResolver.openInputStream(uri)?.use{input->file.outputStream().use{input.copyTo(it)}};dao.updateInstallment(i.copy(receiptPath=file.absolutePath))}}
 fun deleteReceipt(i:Installment){viewModelScope.launch{i.receiptPath?.let{File(it).delete()};dao.updateInstallment(i.copy(receiptPath=null))}}
 fun delete(id:Long){viewModelScope.launch{dao.getById(id)?.installments?.forEach{cancelReminders(id,it.sequence-1);it.receiptPath?.let{path->File(path).delete()}};dao.deletePayment(id)}}
 fun export(uri:Uri,password:String,onDone:(Result<Unit>)->Unit){viewModelScope.launch{onDone(runCatching<Unit>{BackupManager.export(getApplication(),uri,dao,password);Unit})}}
 fun import(uri:Uri,password:String,onDone:(Result<Unit>)->Unit){viewModelScope.launch{onDone(runCatching<Unit>{BackupManager.import(getApplication(),uri,dao,password);Unit})}}
 fun import(uri:Uri,password:String,replace:Boolean,onDone:(Result<Unit>)->Unit){viewModelScope.launch{onDone(runCatching<Unit>{BackupManager.import(getApplication(),uri,dao,password,replace);Unit})}}
 fun inspectBackup(uri:Uri,password:String,onDone:(Result<BackupSummary>)->Unit){viewModelScope.launch{onDone(runCatching{BackupManager.inspect(getApplication(),uri,password)})}}
 private fun offsets(item:PaymentItem)=item.reminderOffsets.split(",").mapNotNull{it.toIntOrNull()}.ifEmpty{listOf(item.reminderDaysBefore)}
 private fun scheduleReminders(itemId:Long,index:Int,title:String,due:Long,offsets:List<Int>,mode:String,hour:Int,minute:Int){offsets.forEachIndexed{slot,days->val at=AlarmScheduler.atTime(due-days*86_400_000L,hour,minute);if(mode=="notification"||mode=="both")AlarmScheduler.schedule(getApplication(),AlarmScheduler.reminderId(itemId,index,slot,false),title,at,false);if(mode=="alarm"||mode=="both")AlarmScheduler.schedule(getApplication(),AlarmScheduler.reminderId(itemId,index,slot,true),title,at,true)}}
 private fun cancelReminders(itemId:Long,index:Int){repeat(8){slot->AlarmScheduler.cancel(getApplication(),AlarmScheduler.reminderId(itemId,index,slot,false));AlarmScheduler.cancel(getApplication(),AlarmScheduler.reminderId(itemId,index,slot,true))}}
}
