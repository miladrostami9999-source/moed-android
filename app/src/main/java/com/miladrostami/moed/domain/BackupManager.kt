package com.miladrostami.moed.domain

import android.content.Context
import android.net.Uri
import com.miladrostami.moed.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.*
import java.util.zip.*

object BackupManager {
 suspend fun export(context:Context,uri:Uri,dao:PaymentDao)=withContext(Dispatchers.IO){
  val root=JSONArray();dao.getAll().forEach{r->root.put(JSONObject().apply{put("item",JSONObject().apply{put("title",r.item.title);put("category",r.item.category);put("icon",r.item.iconKey);put("amount",r.item.amount);put("first",r.item.firstDueAt);put("repeat",r.item.repeatRule);put("count",r.item.installmentCount);put("final",r.item.finalDueAt?:JSONObject.NULL);put("mode",r.item.reminderMode);put("before",r.item.reminderDaysBefore)});put("installments",JSONArray().apply{r.installments.forEach{i->put(JSONObject().apply{put("sequence",i.sequence);put("amount",i.amount);put("due",i.dueAt);put("paid",i.paidAt?:JSONObject.NULL);put("receipt",i.receiptPath?:JSONObject.NULL)})}}})}
  context.contentResolver.openOutputStream(uri)?.use{out->ZipOutputStream(out).use{zip->zip.putNextEntry(ZipEntry("moed.json"));zip.write(root.toString().toByteArray());zip.closeEntry();val dir=context.filesDir.resolve("receipts");dir.listFiles()?.forEach{f->zip.putNextEntry(ZipEntry("receipts/${f.name}"));f.inputStream().use{it.copyTo(zip)};zip.closeEntry()}}}
 }
 suspend fun import(context:Context,uri:Uri,dao:PaymentDao)=withContext(Dispatchers.IO){
  var json="[]";context.contentResolver.openInputStream(uri)?.use{input->ZipInputStream(input).use{zip->generateSequence{zip.nextEntry}.forEach{e->if(e.name=="moed.json")json=zip.readBytes().decodeToString() else if(e.name.startsWith("receipts/")){val file=context.filesDir.resolve(e.name);file.parentFile?.mkdirs();file.outputStream().use{zip.copyTo(it)}}}}}
  val rows=JSONArray(json);val data=(0 until rows.length()).map{idx->val r=rows.getJSONObject(idx);val i=r.getJSONObject("item");val a=r.getJSONArray("installments");PaymentWithInstallments(PaymentItem(title=i.getString("title"),category=i.getString("category"),iconKey=i.getString("icon"),amount=i.getLong("amount"),firstDueAt=i.getLong("first"),repeatRule=i.getString("repeat"),installmentCount=i.getInt("count"),finalDueAt=if(i.isNull("final"))null else i.getLong("final"),reminderMode=i.getString("mode"),reminderDaysBefore=i.getInt("before")),(0 until a.length()).map{j->val x=a.getJSONObject(j);Installment(paymentItemId=0,sequence=x.getInt("sequence"),amount=x.getLong("amount"),dueAt=x.getLong("due"),paidAt=if(x.isNull("paid"))null else x.getLong("paid"),receiptPath=if(x.isNull("receipt"))null else x.getString("receipt"))})};dao.replaceAll(data)
 }
}
