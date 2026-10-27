package com.miladrostami.moed

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.miladrostami.moed.data.*
import com.miladrostami.moed.domain.*

private val NightBlue=Color(0xFF102441)
private val Mint=Color(0xFF82E7C5)
private val Blue=Color(0xFF648BFF)
private val ChartColors=listOf(Blue,Color(0xFF43C7AC),Color(0xFF9BAEF5),Color(0xFF76B5CD),Color(0xFF788A9B))

@Composable fun MentoHomeScreen(data:List<PaymentWithInstallments>,open:(PaymentWithInstallments)->Unit,toggle:(Installment)->Unit,modifier:Modifier){
 val now=System.currentTimeMillis();val current=PersianDate.from(now)
 val month=data.flatMap{r->r.installments.filter{val p=PersianDate.from(it.dueAt);p.year==current.year&&p.month==current.month}.map{r to it}}
 val pending=month.filter{it.second.paidAt==null}.sortedBy{it.second.dueAt}
 val total=month.sumOf{it.second.amount};val paid=month.filter{it.second.paidAt!=null}.sumOf{it.second.amount}
 val overdue=pending.count{dayStart(it.second.dueAt)<dayStart(now)}
 var filter by remember{mutableStateOf("all")};var query by remember{mutableStateOf("")};var searchOpen by remember{mutableStateOf(false)}
 val shown=month.filter{(r,i)->(r.item.title.contains(query,true)||lx(r.item.category).contains(query,true))&&when(filter){"pending"->i.paidAt==null;"late"->i.paidAt==null&&dayStart(i.dueAt)<dayStart(now);"paid"->i.paidAt!=null;else->true}}.sortedWith(compareBy<Pair<PaymentWithInstallments,Installment>>{it.second.paidAt!=null}.thenBy{it.second.dueAt})
 val completion by animateFloatAsState(if(total==0L)0f else paid.toFloat()/total,label="monthly-progress")
 LazyColumn(modifier.fillMaxSize().padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(top=12.dp,bottom=112.dp)){
  item{Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(tx("نمای ماه شما","Your month at a glance"),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant);Text("${persianMonthName(current.month)} ${faNumber(current.year)}",fontSize=26.sp,fontWeight=FontWeight.ExtraBold)};Surface(color=MaterialTheme.colorScheme.surface,shape=RoundedCornerShape(14.dp),border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant)){Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Outlined.Today,null,Modifier.size(16.dp),tint=MaterialTheme.colorScheme.primary);Spacer(Modifier.width(6.dp));Text(tx("امروز ${faNumber(current.day)}","Today ${faNumber(current.day)}"),fontSize=12.sp,fontWeight=FontWeight.Medium)}}}}
  item{Card(shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=NightBlue,contentColor=Color.White)){
   Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(NightBlue,Color(0xFF193B5A))))){Canvas(Modifier.matchParentSize()){drawCircle(Mint.copy(.06f),size.width*.48f,Offset(size.width,size.height*.1f));drawCircle(Blue.copy(.08f),size.width*.36f,Offset(0f,size.height))};Column(Modifier.padding(22.dp)){
    Row(verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(6.dp).background(Mint,CircleShape));Spacer(Modifier.width(7.dp));Text(tx("تعهدهای همین ماه","DUE THIS MONTH"),fontSize=11.sp,color=Color(0xFFD2DEEC),letterSpacing=.6.sp)}
    Text(tx("اینقدر باید بدی!","This is what you owe!"),Modifier.padding(top=8.dp),fontSize=28.sp,lineHeight=35.sp,fontWeight=FontWeight.Black,color=Color.White)
    Row(Modifier.padding(top=8.dp),verticalAlignment=Alignment.Bottom){Text(money(pending.sumOf{it.second.amount}),fontSize=36.sp,lineHeight=44.sp,fontWeight=FontWeight.ExtraBold,color=Mint,maxLines=1,modifier=Modifier.weight(1f),overflow=TextOverflow.Ellipsis);Text(tx("تومان","toman"),Modifier.padding(bottom=7.dp),fontSize=13.sp,color=Color(0xFFCFDCEB))}
    HorizontalDivider(Modifier.padding(vertical=12.dp),color=Color.White.copy(.12f))
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(tx("${faNumber(pending.size)} پرداخت باقی مانده","${faNumber(pending.size)} payments left"),fontSize=12.sp,color=Color.White.copy(.8f));Text(tx("${faNumber((completion*100).toInt())}٪ پرداخت شده","${faNumber((completion*100).toInt())}% paid"),fontSize=12.sp,color=Mint)}
    LinearProgressIndicator(progress={completion},modifier=Modifier.fillMaxWidth().padding(top=10.dp).height(5.dp).clip(CircleShape),color=Mint,trackColor=Color.White.copy(.12f))
   }}
  }}
  item{Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(tx("پرداخت‌های این ماه","Monthly payments"),fontSize=18.sp,fontWeight=FontWeight.Bold);Text(tx("${faNumber(month.size)} مورد · به ترتیب سررسید","${faNumber(month.size)} payments · earliest due first"),fontSize=10.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)};IconButton({searchOpen=!searchOpen;if(!searchOpen)query=""}){Icon(if(searchOpen)Icons.Outlined.Close else Icons.Outlined.Search,tx("جست‌وجوی پرداخت‌ها","Search payments"),tint=MaterialTheme.colorScheme.primary)}} }
  if(searchOpen)item{OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),singleLine=true,shape=RoundedCornerShape(16.dp),placeholder={Text(tx("عنوان یا دسته را جست‌وجو کن","Search title or category"),fontSize=13.sp)},leadingIcon={Icon(Icons.Outlined.Search,null)},trailingIcon={if(query.isNotEmpty())IconButton({query=""}){Icon(Icons.Outlined.Close,tx("پاک کردن جست‌وجو","Clear search"))}})}
  item{LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){items(listOf("all" to tx("همه","All"),"pending" to tx("در انتظار","Pending"),"late" to tx("عقب افتاده · ${faNumber(overdue)}","Overdue · ${faNumber(overdue)}"),"paid" to tx("انجام شده","Paid"))){(key,title)->FilterChip(filter==key,{filter=key},label={Text(title,fontSize=12.sp)},shape=RoundedCornerShape(12.dp))}}}
  if(shown.isEmpty())item{DashboardEmpty(if(month.isEmpty())tx("یک شروع مرتب","A fresh start")else tx("اینجا همه‌چیز مرتبه","All clear here"),if(month.isEmpty())tx("با دکمهٔ پرداخت جدید، اولین موعدت را اضافه کن.","Use New payment to add your first due date.")else tx("پرداختی مطابق این فیلتر پیدا نشد.","No payments match this filter."))}
  items(shown,key={it.second.id}){(r,i)->DashboardPayment(r,i,{open(r)},{toggle(i)})}
 }
}

@Composable private fun QuickMetric(title:String,value:String,caption:String,image:androidx.compose.ui.graphics.vector.ImageVector,modifier:Modifier,attention:Boolean=false){
 Surface(modifier,shape=RoundedCornerShape(20.dp),color=MaterialTheme.colorScheme.surface,border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant)){
  Column(Modifier.padding(14.dp)){Icon(image,null,Modifier.size(20.dp),tint=if(attention)MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary);Text(title,Modifier.padding(top=10.dp),fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(value,fontSize=20.sp,fontWeight=FontWeight.Bold,maxLines=1,overflow=TextOverflow.Ellipsis);Text(caption,fontSize=10.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}
 }
}

@Composable private fun DashboardPayment(r:PaymentWithInstallments,i:Installment,open:()->Unit,pay:()->Unit){
 val done=i.paidAt!=null;val late=!done&&dayStart(i.dueAt)<dayStart(System.currentTimeMillis());val green=if(MaterialTheme.colorScheme.surface.luminance()<.5f)Mint else Color(0xFF087B61)
 Card(onClick=open,shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface),border=BorderStroke(1.dp,if(done)green.copy(.3f)else MaterialTheme.colorScheme.outlineVariant),modifier=Modifier.animateContentSize()){
  Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically){Surface(color=MaterialTheme.colorScheme.primaryContainer,shape=RoundedCornerShape(14.dp),modifier=Modifier.size(44.dp)){Box(contentAlignment=Alignment.Center){Icon(icon(r.item.iconKey),null,tint=MaterialTheme.colorScheme.onPrimaryContainer,modifier=Modifier.size(22.dp))}};Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){
   Text(r.item.title,fontWeight=FontWeight.Bold,fontSize=14.sp,maxLines=1,overflow=TextOverflow.Ellipsis)
   Text("${lx(r.item.category)} · ${date(i.dueAt)}",fontSize=10.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(top=3.dp))
   Text("${money(i.amount)} ${tx("تومان","toman")}",fontSize=15.sp,fontWeight=FontWeight.SemiBold,modifier=Modifier.padding(top=7.dp))
   if(done||late)Text(if(done)tx("پرداخت شده","Paid")else tx("موعد گذشته","Overdue"),fontSize=10.sp,color=if(done)green else MaterialTheme.colorScheme.error,modifier=Modifier.padding(top=3.dp))
  };Spacer(Modifier.width(8.dp));FilledTonalIconButton(onClick=pay,modifier=Modifier.size(48.dp),colors=IconButtonDefaults.filledTonalIconButtonColors(containerColor=if(done)green.copy(.13f)else MaterialTheme.colorScheme.primaryContainer,contentColor=if(done)green else MaterialTheme.colorScheme.onPrimaryContainer)){
   Icon(if(done)Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,if(done)tx("لغو پرداخت با تأیید","Undo payment with confirmation")else tx("ثبت پرداخت","Mark as paid"),Modifier.size(25.dp))
  }}
 }
}

@Composable private fun DashboardEmpty(title:String,body:String){Surface(shape=RoundedCornerShape(22.dp),color=MaterialTheme.colorScheme.surface,border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant)){Column(Modifier.fillMaxWidth().padding(28.dp),horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Outlined.CheckCircle,null,Modifier.size(30.dp),tint=MaterialTheme.colorScheme.primary);Text(title,Modifier.padding(top=12.dp),fontWeight=FontWeight.Bold);Text(body,Modifier.padding(top=6.dp),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}

private fun dayStart(time:Long)=java.util.Calendar.getInstance().apply{timeInMillis=time;set(java.util.Calendar.HOUR_OF_DAY,0);set(java.util.Calendar.MINUTE,0);set(java.util.Calendar.SECOND,0);set(java.util.Calendar.MILLISECOND,0)}.timeInMillis

@Composable fun MentoReportScreen(data:List<PaymentWithInstallments>,modifier:Modifier){
 var range by remember{mutableStateOf(ReportRange.MONTH)};val now=System.currentTimeMillis();val window=reportWindow(range,now)
 val all=data.flatMap{r->r.installments.map{r.item.category to it}}
 val paid=all.filter{it.second.paidAt?.let{t->t in window.start..now}==true}
 val total=paid.sumOf{it.second.amount};val previous=all.filter{it.second.paidAt?.let{t->t in window.previousStart until window.start}==true}.sumOf{it.second.amount}
 val categories=paid.groupBy{it.first}.map{(k,v)->k to v.sumOf{it.second.amount}}.sortedByDescending{it.second}
 val due=all.filter{it.second.paidAt==null&&it.second.dueAt in window.start until window.end}.sumOf{it.second.amount}
 val average=if(paid.isEmpty())0L else total/paid.size
 val buckets=reportBuckets(paid.map{it.second},window,now)
 LazyColumn(modifier.fillMaxSize().padding(horizontal=20.dp),verticalArrangement=Arrangement.spacedBy(16.dp),contentPadding=PaddingValues(top=12.dp,bottom=100.dp)){
  item{Text(tx("نبض پرداخت‌ها","Payment insights"),fontSize=27.sp,fontWeight=FontWeight.ExtraBold);Text(tx("تصویر روشن‌تری از پولت داشته باش","See where your money goes"),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(top=4.dp))}
  item{LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){items(ReportRange.entries){r->FilterChip(range==r,{range=r},label={Text(when(r){ReportRange.WEEK->tx("۷ روز","7 days");ReportRange.MONTH->tx("این ماه","This month");ReportRange.QUARTER->tx("۳ ماه","3 months");ReportRange.YEAR->tx("امسال","This year")})},shape=RoundedCornerShape(12.dp))}}}
  item{Card(shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=NightBlue,contentColor=Color.White)){Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(NightBlue,Color(0xFF193B5A)))).padding(22.dp)){
   Row(verticalAlignment=Alignment.CenterVertically){Icon(Icons.Outlined.Insights,null,tint=Mint,modifier=Modifier.size(20.dp));Spacer(Modifier.width(8.dp));Text(tx("مجموع پرداخت‌های ثبت‌شده","TOTAL PAID"),fontSize=11.sp,color=Color.White.copy(.75f))}
   Text(money(total),fontSize=36.sp,fontWeight=FontWeight.ExtraBold,color=Mint,modifier=Modifier.padding(top=14.dp));Text(tx("تومان","toman"),fontSize=12.sp,color=Color.White.copy(.7f))
   Spacer(Modifier.height(14.dp));Text(if(previous==0L)tx("در دورهٔ قبل پرداختی ثبت نشده","No payments recorded in the previous period")else{val percent=(total.toDouble()-previous)*100/previous;tx("${faNumber(kotlin.math.abs(percent).toInt())}٪ ${if(percent>=0)"بیشتر" else "کمتر"} از دورهٔ قبل","${kotlin.math.abs(percent).toInt()}% ${if(percent>=0)"more" else "less"} than the previous period")},fontSize=11.sp,color=Color.White.copy(.8f))
   Text("${date(window.start)} — ${date(now)}",fontSize=10.sp,color=Color.White.copy(.6f),modifier=Modifier.padding(top=6.dp))
  }}}
  item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){QuickMetric(tx("تعداد پرداخت","Payments"),faNumber(paid.size),tx("در بازهٔ انتخابی","in this period"),Icons.Outlined.ReceiptLong,Modifier.weight(1f));QuickMetric(tx("میانگین پرداخت","Average payment"),money(average),tx("تومان در هر پرداخت","toman per payment"),Icons.Outlined.Analytics,Modifier.weight(1f))}}
  item{DashboardPanel(tx("روند پرداخت‌ها","Payment activity"),tx("مبلغ پرداخت‌شده در طول بازه","Amount paid over the selected period")){
   if(total==0L)Text(tx("پس از ثبت پرداخت، نمودار اینجا شکل می‌گیرد.","Your chart will appear after you record a payment."),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(vertical=20.dp))else{
    val max=buckets.maxOf{it.amount}.coerceAtLeast(1);val color=MaterialTheme.colorScheme.primary;val track=MaterialTheme.colorScheme.surfaceVariant;val rtl=LocalLayoutDirection.current==LayoutDirection.Rtl
    val desc=buckets.joinToString{ "${it.label}: ${money(it.amount)} ${tx("تومان","toman")}" }
    Canvas(Modifier.fillMaxWidth().height(140.dp).padding(top=18.dp).semantics{contentDescription=desc}){val slot=size.width/buckets.size;val width=slot*.48f;buckets.forEachIndexed{index,b->val x=slot*(if(rtl)buckets.lastIndex-index else index)+(slot-width)/2;drawRoundRect(track,Offset(x,0f),androidx.compose.ui.geometry.Size(width,size.height),androidx.compose.ui.geometry.CornerRadius(5.dp.toPx()));if(b.amount>0){val height=(b.amount.toFloat()/max)*size.height;drawRoundRect(color,Offset(x,size.height-height),androidx.compose.ui.geometry.Size(width,height),androidx.compose.ui.geometry.CornerRadius(5.dp.toPx()))}}}
    Row(Modifier.fillMaxWidth().padding(top=10.dp)){buckets.forEach{Text(it.label,Modifier.weight(1f),fontSize=9.sp,textAlign=androidx.compose.ui.text.style.TextAlign.Center,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
   }
  }}
  item{DashboardPanel(tx("پولت کجا رفته؟","Where did it go?"),tx("سهم هر دسته از پرداخت‌های ثبت‌شده","Each category’s share of recorded payments")){
   if(categories.isEmpty())Text(tx("هنوز پرداختی در این بازه ثبت نشده.","No payments recorded in this period yet."),fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(vertical=16.dp))else{
    Box(Modifier.fillMaxWidth().padding(vertical=14.dp),contentAlignment=Alignment.Center){val desc=categories.joinToString{"${lx(it.first)}: ${money(it.second)}"};Canvas(Modifier.size(164.dp).padding(10.dp).semantics{contentDescription=desc}){var start=-90f;categories.forEachIndexed{index,(_,amount)->val sweep=amount.toFloat()/total*360;drawArc(ChartColors[index%ChartColors.size],start,sweep,false,style=Stroke(18.dp.toPx(),cap=StrokeCap.Butt));start+=sweep}};Column(horizontalAlignment=Alignment.CenterHorizontally){Text(faNumber(categories.size),fontSize=30.sp,fontWeight=FontWeight.ExtraBold);Text(tx("دستهٔ هزینه","categories"),fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
    categories.forEachIndexed{index,(category,amount)->val fraction=amount.toFloat()/total;Row(Modifier.fillMaxWidth().padding(vertical=9.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(9.dp).background(ChartColors[index%ChartColors.size],CircleShape));Spacer(Modifier.width(9.dp));Column(Modifier.weight(1f)){Text(lx(category),fontSize=13.sp,fontWeight=FontWeight.Medium);LinearProgressIndicator(progress={fraction},modifier=Modifier.fillMaxWidth().padding(top=5.dp,end=18.dp).height(3.dp).clip(CircleShape),color=ChartColors[index%ChartColors.size],trackColor=MaterialTheme.colorScheme.surfaceVariant)};Column(horizontalAlignment=Alignment.End){Text(money(amount),fontSize=12.sp,fontWeight=FontWeight.SemiBold);Text("${faNumber((fraction*100).toInt())}${tx("٪","%")}",fontSize=10.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
   }
  }}
  item{Surface(shape=RoundedCornerShape(20.dp),color=MaterialTheme.colorScheme.secondaryContainer){Row(Modifier.fillMaxWidth().padding(18.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Outlined.EventNote,null,tint=MaterialTheme.colorScheme.onSecondaryContainer);Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(tx("تعهدهای بازِ همین بازه","Outstanding in this period"),fontSize=12.sp,color=MaterialTheme.colorScheme.onSecondaryContainer);Text("${money(due)} ${tx("تومان","toman")}",fontWeight=FontWeight.Bold,fontSize=19.sp,color=MaterialTheme.colorScheme.onSecondaryContainer,modifier=Modifier.padding(top=4.dp))}}}}
 }
}

@Composable private fun DashboardPanel(title:String,subtitle:String,content:@Composable ColumnScope.()->Unit){Surface(shape=RoundedCornerShape(24.dp),color=MaterialTheme.colorScheme.surface,border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant)){Column(Modifier.fillMaxWidth().padding(18.dp)){Text(title,fontWeight=FontWeight.Bold,fontSize=17.sp);Text(subtitle,fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(top=4.dp,bottom=10.dp));content()}}}
