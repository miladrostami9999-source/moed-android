package com.miladrostami.moed.domain

import android.content.Context
import android.net.Uri
import com.miladrostami.moed.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupManager {
 suspend fun export(context:Context,uri:Uri,dao:PaymentDao)=withContext(Dispatchers.IO){
  val root=JSONArray()
  dao.getAll().forEach { row ->
   val item=JSONObject().apply {
    put("title",row.item.title);put("category",row.item.category);put("icon",row.item.iconKey)
    put("amount",row.item.amount);put("first",row.item.firstDueAt);put("repeat",row.item.repeatRule)
    put("count",row.item.installmentCount);put("final",row.item.finalDueAt?:JSONObject.NULL)
    put("mode",row.item.reminderMode);put("before",row.item.reminderDaysBefore)
   }
   val installments=JSONArray()
   row.installments.forEach { installment -> installments.put(JSONObject().apply{
    put("sequence",installment.sequence);put("amount",installment.amount);put("due",installment.dueAt)
    put("paid",installment.paidAt?:JSONObject.NULL);put("receipt",installment.receiptPath?:JSONObject.NULL)
   }) }
   root.put(JSONObject().put("item",item).put("installments",installments))
  }
  context.contentResolver.openOutputStream(uri)?.use { output ->
   ZipOutputStream(output).use { zip ->
    zip.putNextEntry(ZipEntry("moed.json"));zip.write(root.toString().toByteArray());zip.closeEntry()
    context.filesDir.resolve("receipts").listFiles()?.forEach { file ->
     zip.putNextEntry(ZipEntry("receipts/${file.name}"));file.inputStream().use{it.copyTo(zip)};zip.closeEntry()
    }
   }
  }
 }

 suspend fun import(context:Context,uri:Uri,dao:PaymentDao)=withContext(Dispatchers.IO){
  var json="[]"
  context.contentResolver.openInputStream(uri)?.use { input ->
   ZipInputStream(input).use { zip ->
    var entry=zip.nextEntry
    while(entry!=null){
     if(entry.name=="moed.json") json=zip.readBytes().decodeToString()
     else if(entry.name.startsWith("receipts/")){
      val file=context.filesDir.resolve(entry.name);file.parentFile?.mkdirs();file.outputStream().use{zip.copyTo(it)}
     }
     zip.closeEntry();entry=zip.nextEntry
    }
   }
  }
  val rows=JSONArray(json)
  val data=(0 until rows.length()).map { index ->
   val row=rows.getJSONObject(index);val item=row.getJSONObject("item");val entries=row.getJSONArray("installments")
   PaymentWithInstallments(
    PaymentItem(title=item.getString("title"),category=item.getString("category"),iconKey=item.getString("icon"),amount=item.getLong("amount"),firstDueAt=item.getLong("first"),repeatRule=item.getString("repeat"),installmentCount=item.getInt("count"),finalDueAt=if(item.isNull("final"))null else item.getLong("final"),reminderMode=item.getString("mode"),reminderDaysBefore=item.getInt("before")),
    (0 until entries.length()).map { i -> val x=entries.getJSONObject(i);Installment(paymentItemId=0,sequence=x.getInt("sequence"),amount=x.getLong("amount"),dueAt=x.getLong("due"),paidAt=if(x.isNull("paid"))null else x.getLong("paid"),receiptPath=if(x.isNull("receipt"))null else x.getString("receipt")) }
   )
  }
  dao.replaceAll(data)
 }
}
