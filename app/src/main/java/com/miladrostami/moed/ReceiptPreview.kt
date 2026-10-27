package com.miladrostami.moed

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ReceiptPreview(path:String,dismiss:()->Unit,message:(String)->Unit){
    val context=LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue=null,key1=path){this.value=withContext(Dispatchers.IO){renderReceipt(File(path))}}
    AlertDialog(onDismissRequest=dismiss,title={Text("پیش‌نمایش رسید")},text={Column{if(bitmap==null)Text("پیش‌نمایش این فایل در دسترس نیست؛ می‌توانید آن را با برنامه دیگری باز کنید.") else Image(bitmap!!.asImageBitmap(),"رسید",Modifier.fillMaxWidth().heightIn(max=520.dp),contentScale=ContentScale.Fit)}},confirmButton={TextButton({runCatching{val file=File(path);val uri=FileProvider.getUriForFile(context,"${context.packageName}.files",file);val type=if(file.extension.equals("pdf",true))"application/pdf" else "image/*";context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri,type).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))}.onFailure{message("برنامه‌ای برای بازکردن فایل پیدا نشد")}}){Text("بازکردن کامل")}},dismissButton={TextButton(dismiss){Text("بستن")}})
}

private fun renderReceipt(file:File):Bitmap?=runCatching{
    if(file.extension.equals("pdf",true)){
        ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY).use{descriptor->PdfRenderer(descriptor).use{renderer->renderer.openPage(0).use{page->Bitmap.createBitmap(page.width*2,page.height*2,Bitmap.Config.ARGB_8888).also{it.eraseColor(Color.WHITE);page.render(it,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)}}}}
    }else BitmapFactory.decodeFile(file.absolutePath)
}.getOrNull()
