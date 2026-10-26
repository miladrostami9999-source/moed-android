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
 val payments=dao.observeAll().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 fun add(title:String,category:String,icon:String,amount:Long,date:PersianDate,repeat:String,count:Int,mode:String,before:Int){viewModelScope.launch{
  val dates=(0 until count).map{if(repeat=="monthly")addPersianMonths(date,it).toMillis() else date.toMillis()}
  val item=PaymentItem(title=title,category=category,iconKey=icon,amount=amount,firstDueAt=dates.first(),repeatRule=repeat,installmentCount=count,finalDueAt=dates.last(),reminderMode=mode,reminderDaysBefore=before)
  val id=dao.addItem(item,dates.mapIndexed{index,due->Installment(paymentItemId=0,sequence=index+1,amount=amount,dueAt=due)})
  dates.forEachIndexed{index,due->AlarmScheduler.schedule(getApplication(),id*1000+index,title,due-before*86400000L,mode!="notification")}
 }}
 fun update(row:PaymentWithInstallments,title:String,category:String,icon:String,amount:Long,date:PersianDate,repeat:String,count:Int,mode:String,before:Int){viewModelScope.launch{
  row.installments.forEach{AlarmScheduler.cancel(getApplication(),row.item.id*1000+it.sequence-1)}
  val hasPaid=row.installments.any{it.paidAt!=null}
  val finalAmount=if(hasPaid)row.item.amount else amount;val finalRepeat=if(hasPaid)row.item.repeatRule else repeat;val finalCount=if(hasPaid)row.item.installmentCount else count
  val dates=if(hasPaid)row.installments.sortedBy{it.sequence}.map{it.dueAt} else (0 until finalCount).map{if(finalRepeat=="monthly")addPersianMonths(date,it).toMillis() else date.toMillis()}
  val item=row.item.copy(title=title,category=category,iconKey=icon,amount=finalAmount,firstDueAt=dates.first(),repeatRule=finalRepeat,installmentCount=finalCount,finalDueAt=dates.last(),reminderMode=mode,reminderDaysBefore=before)
  val installments=if(hasPaid)row.installments else dates.mapIndexed{index,due->Installment(paymentItemId=row.item.id,sequence=index+1,amount=finalAmount,dueAt=due)}
  dao.replaceSchedule(item,installments)
  installments.filter{it.paidAt==null}.forEach{AlarmScheduler.schedule(getApplication(),row.item.id*1000+it.sequence-1,title,it.dueAt-before*86400000L,mode!="notification")}
 }}
 fun togglePaid(i:Installment){viewModelScope.launch{val row=dao.getById(i.paymentItemId);val paid=i.paidAt==null;dao.updateInstallment(i.copy(paidAt=if(paid)System.currentTimeMillis() else null));val alarmId=i.paymentItemId*1000+i.sequence-1;if(paid)AlarmScheduler.cancel(getApplication(),alarmId) else row?.let{AlarmScheduler.schedule(getApplication(),alarmId,it.item.title,i.dueAt-it.item.reminderDaysBefore*86400000L,it.item.reminderMode!="notification")}}}
 fun attach(i:Installment,uri:Uri){viewModelScope.launch{val dir=File(getApplication<Application>().filesDir,"receipts").apply{mkdirs()};val ext=getApplication<Application>().contentResolver.getType(uri)?.substringAfterLast('/')?:"bin";val file=File(dir,"receipt_${i.id}.$ext");getApplication<Application>().contentResolver.openInputStream(uri)?.use{input->file.outputStream().use{input.copyTo(it)}};dao.updateInstallment(i.copy(receiptPath=file.absolutePath))}}
 fun delete(id:Long){viewModelScope.launch{dao.getById(id)?.installments?.forEach{AlarmScheduler.cancel(getApplication(),id*1000+it.sequence-1)};dao.deletePayment(id)}}
 fun export(uri:Uri,onDone:()->Unit){viewModelScope.launch{BackupManager.export(getApplication(),uri,dao);onDone()}}
 fun import(uri:Uri,onDone:()->Unit){viewModelScope.launch{BackupManager.import(getApplication(),uri,dao);onDone()}}
}
