package com.miladrostami.moed

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miladrostami.moed.data.*
import com.miladrostami.moed.domain.*
import com.miladrostami.moed.ui.theme.MoedTheme
import java.io.File
import java.util.*

class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{MoedTheme{MoedApp()}}}}

private val Ink=Color(0xFF20212B);private val Accent=Color(0xFF5B5CE2);private val Muted=Color(0xFF777986);private val Soft=Color(0xFFF0F0FF);private val Danger=Color(0xFFBD4054)
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun MoedApp(vm:AppViewModel=viewModel()){
 val data by vm.payments.collectAsState();var tab by remember{mutableIntStateOf(0)};var add by remember{mutableStateOf(false)};var selected by remember{mutableStateOf<PaymentWithInstallments?>(null)};var toast by remember{mutableStateOf<String?>(null)}
 val snackbar=remember{SnackbarHostState()};LaunchedEffect(toast){toast?.let{snackbar.showSnackbar(it);toast=null}}
 CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl){Scaffold(snackbarHost={SnackbarHost(snackbar)},topBar={TopBar()},floatingActionButton={if(tab==0)FloatingActionButton({add=true},containerColor=Accent,contentColor=Color.White){Icon(Icons.Outlined.Add,"افزودن")}},bottomBar={NavigationBar(containerColor=Color.White){listOf(Icons.Outlined.Home to "خانه",Icons.Outlined.CalendarMonth to "تقویم",Icons.Outlined.BarChart to "گزارش",Icons.Outlined.Settings to "تنظیمات").forEachIndexed{i,p->NavigationBarItem(tab==i,{tab=i},{Icon(p.first,p.second)},label={Text(p.second)})}}}){pad->when(tab){0->HomeScreen(data,{selected=it},Modifier.padding(pad));1->CalendarScreen(data,{selected=it},Modifier.padding(pad));2->ReportScreen(data,Modifier.padding(pad));else->SettingsScreen(vm,{toast=it},Modifier.padding(pad))}}}
 if(add)AddSheet({add=false},{title,cat,icon,amount,date,repeat,count,mode,before->vm.add(title,cat,icon,amount,date,repeat,count,mode,before);add=false;toast="پرداخت با موفقیت اضافه شد"})
 selected?.let{DetailSheet(it,{selected=null},{vm.togglePaid(it);toast="وضعیت پرداخت تغییر کرد"},{ins,uri->vm.attach(ins,uri);toast="رسید ذخیره شد"},{vm.delete(it.item.id);selected=null;toast="آیتم حذف شد"})}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun TopBar() {
 TopAppBar(
  colors=TopAppBarDefaults.topAppBarColors(containerColor=Color(0xFFF7F7F9)),
  title={Row(verticalAlignment=Alignment.CenterVertically){
   Surface(color=Accent,shape=RoundedCornerShape(11.dp),modifier=Modifier.size(34.dp)){Icon(Icons.Outlined.CalendarMonth,null,Modifier.padding(7.dp),Color.White)}
   Spacer(Modifier.width(9.dp));Column{Text("موعد",fontWeight=FontWeight.Bold,fontSize=17.sp);Text("مدیریت پرداخت‌ها",fontSize=10.sp,color=Muted)}
  }},
  actions={Surface(color=Soft,shape=RoundedCornerShape(13.dp),modifier=Modifier.padding(end=12.dp).size(40.dp)){Box(contentAlignment=Alignment.Center){Text("مـ",color=Accent,fontWeight=FontWeight.Bold)}}}
 )
}

@Composable private fun HomeScreen(data:List<PaymentWithInstallments>,open:(PaymentWithInstallments)->Unit,modifier:Modifier){
 val unpaid=data.flatMap{it.installments}.filter{it.paidAt==null}.sortedBy{it.dueAt};val total=unpaid.sumOf{it.amount};val next=unpaid.firstOrNull();var filter by remember{mutableStateOf("همه")}
 LazyColumn(modifier.fillMaxSize().padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(9.dp),contentPadding=PaddingValues(bottom=100.dp)){
  item{Row(Modifier.fillMaxWidth().padding(vertical=8.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.Bottom){Text("سلام 👋",fontSize=22.sp,fontWeight=FontWeight.Bold);Text(faDate(System.currentTimeMillis()),fontSize=11.sp,color=Muted)}}
  item{Card(colors=CardDefaults.cardColors(containerColor=Ink),shape=RoundedCornerShape(22.dp)){Column(Modifier.padding(17.dp)){Text("باقی‌مانده کل تعهدات",fontSize=11.sp,color=Color.White.copy(.65f));Text("${formatMoney(total)} تومان",fontSize=25.sp,fontWeight=FontWeight.Bold,color=Color.White);HorizontalDivider(Modifier.padding(vertical=13.dp),color=Color.White.copy(.12f));if(next==null)Text("همه‌چیز پرداخت شده است",color=Color.White) else {val owner=data.first{r->r.installments.any{it.id==next.id}};Row(verticalAlignment=Alignment.CenterVertically){DateBadge(next.dueAt);Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(owner.item.title,color=Color.White,fontWeight=FontWeight.Bold);Text("قسط ${faNumber(next.sequence)} از ${faNumber(owner.item.installmentCount)}",fontSize=10.sp,color=Color.White.copy(.65f))};Text("${formatMoney(next.amount)}",color=Color.White,fontWeight=FontWeight.Bold)}}}}}
  item{Text("پرداخت‌های من",fontSize=15.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=9.dp));Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("همه","نزدیک","معوق","تسویه").forEach{x->FilterChip(filter==x,{filter=x},{Text(x,fontSize=11.sp)})}}}
  val shown=data.filter{r->when(filter){"نزدیک"->r.installments.any{it.paidAt==null&&it.dueAt in System.currentTimeMillis()..System.currentTimeMillis()+7*86400000L};"معوق"->r.installments.any{it.paidAt==null&&it.dueAt<System.currentTimeMillis()};"تسویه"->r.installments.all{it.paidAt!=null};else->true}}
  if(shown.isEmpty())item{EmptyCard("هنوز پرداختی در این بخش نیست")}
  items(shown,key={it.item.id}){PaymentRow(it){open(it)}}
 }
}
@Composable private fun PaymentRow(row:PaymentWithInstallments,onClick:()->Unit){val next=row.installments.firstOrNull{it.paidAt==null};val paid=row.installments.count{it.paidAt!=null};Card(onClick=onClick,shape=RoundedCornerShape(17.dp),colors=CardDefaults.cardColors(containerColor=Color.White),border=BorderStroke(1.dp,Color(0xFFE8E8ED))){Row(Modifier.fillMaxWidth().padding(11.dp),verticalAlignment=Alignment.CenterVertically){Surface(color=Soft,shape=RoundedCornerShape(13.dp),modifier=Modifier.size(42.dp)){Box(contentAlignment=Alignment.Center){Icon(iconFor(row.item.iconKey),null,tint=Accent)}};Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(row.item.title,fontWeight=FontWeight.Bold,maxLines=1,overflow=TextOverflow.Ellipsis);Text(if(next==null)"تسویه شده" else "${faNumber(row.item.installmentCount-paid)} قسط مانده · ${faDate(next.dueAt)}",fontSize=10.sp,color=if(next!=null&&next.dueAt<System.currentTimeMillis())Danger else Muted)};Column(horizontalAlignment=Alignment.End){Text(formatMoney(next?.amount?:row.item.amount),fontSize=12.sp,fontWeight=FontWeight.Bold);Text("تومان",fontSize=9.sp,color=Muted)};Icon(Icons.Outlined.ChevronLeft,null,tint=Muted)}}}
@Composable private fun DateBadge(millis:Long){val p=PersianDate.from(millis);Surface(color=Color.White.copy(.12f),shape=RoundedCornerShape(12.dp)){Column(Modifier.padding(horizontal=10.dp,vertical=5.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(faNumber(p.day),color=Color.White,fontWeight=FontWeight.Bold);Text(persianMonthName(p.month),fontSize=9.sp,color=Color.White.copy(.7f))}}}
@Composable private fun EmptyCard(text:String){Surface(shape=RoundedCornerShape(18.dp),color=Color.White,border=BorderStroke(1.dp,Color(0xFFE8E8ED))){Box(Modifier.fillMaxWidth().padding(28.dp),contentAlignment=Alignment.Center){Text(text,color=Muted,fontSize=12.sp)}}}

@OptIn(ExperimentalMaterial3Api::class) @Composable private fun AddSheet(dismiss:()->Unit,save:(String,String,String,Long,PersianDate,String,Int,String,Int)->Unit){
 var title by remember{mutableStateOf("")};var amount by remember{mutableStateOf("")};var category by remember{mutableStateOf("قسط")};var icon by remember{mutableStateOf("card")};var repeat by remember{mutableStateOf("monthly")};var count by remember{mutableStateOf("12")};var mode by remember{mutableStateOf("notification")};var before by remember{mutableIntStateOf(1)};var date by remember{mutableStateOf(PersianDate.from())};var picker by remember{mutableStateOf(false)}
 ModalBottomSheet(onDismissRequest=dismiss,dragHandle={BottomSheetDefaults.DragHandle()}){LazyColumn(Modifier.fillMaxWidth().padding(horizontal=18.dp),verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(bottom=28.dp)){
  item{Text("افزودن پرداخت",fontSize=20.sp,fontWeight=FontWeight.Bold)}
  item{Text("نوع پرداخت",fontSize=11.sp,color=Muted);Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("قسط","وام","اجاره","قبض","دلخواه").forEach{FilterChip(category==it,{category=it},{Text(it,fontSize=10.sp)})}}}
  item{Text("انتخاب آیکون",fontSize=11.sp,color=Muted);LazyRow(horizontalArrangement=Arrangement.spacedBy(7.dp)){items(listOf("card","bank","home","bolt","car","book","heart","star")){x->Surface(onClick={icon=x},shape=RoundedCornerShape(12.dp),color=if(icon==x)Accent else Color.White,border=BorderStroke(1.dp,if(icon==x)Accent else Color(0xFFE8E8ED)),modifier=Modifier.size(42.dp)){Box(contentAlignment=Alignment.Center){Icon(iconFor(x),null,tint=if(icon==x)Color.White else Ink)}}}}}
  item{OutlinedTextField(title,{title=it},Modifier.fillMaxWidth(),label={Text("عنوان")},singleLine=true)}
  item{OutlinedTextField(amount,{amount=it.filter(Char::isDigit)},Modifier.fillMaxWidth(),label={Text("مبلغ هر پرداخت (تومان)")},singleLine=true)}
  item{OutlinedButton({picker=true},Modifier.fillMaxWidth()){Icon(Icons.Outlined.CalendarMonth,null);Spacer(Modifier.width(7.dp));Text("تاریخ اولین سررسید: ${faNumber(date)}")}}
  item{Text("تکرار",fontSize=11.sp,color=Muted);SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()){SegmentedButton(repeat=="monthly",{repeat="monthly"},SegmentedButtonDefaults.itemShape(0,2)){Text("ماهانه")};SegmentedButton(repeat=="once",{repeat="once";count="1"},SegmentedButtonDefaults.itemShape(1,2)){Text("یک‌بار")}}}
  if(repeat=="monthly")item{OutlinedTextField(count,{count=it.filter(Char::isDigit)},Modifier.fillMaxWidth(),label={Text("تعداد اقساط")},supportingText={Text("آخرین سررسید: ${faNumber(addPersianMonths(date,(count.toIntOrNull()?:1)-1))}")})}
  item{Text("نوع یادآوری",fontSize=11.sp,color=Muted);SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()){listOf("notification" to "اعلان","alarm" to "آلارم","both" to "هر دو").forEachIndexed{i,p->SegmentedButton(mode==p.first,{mode=p.first},SegmentedButtonDefaults.itemShape(i,3)){Text(p.second)}}};Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf(0,1,3,7).forEach{x->FilterChip(before==x,{before=x},{Text(if(x==0)"روز موعد" else "${faNumber(x)} روز قبل",fontSize=10.sp)})}}}
  item{Button({val a=amount.toLongOrNull();val c=count.toIntOrNull();if(title.isNotBlank()&&a!=null&&a>0&&c!=null&&c>0)save(title,category,icon,a,date,repeat,c.coerceAtMost(360),mode,before)},Modifier.fillMaxWidth().height(48.dp)){Text("ذخیره و فعال‌سازی یادآور")}}
 }}
 if(picker)PersianDatePicker(date,{picker=false},{date=it;picker=false})
}

@Composable private fun PersianDatePicker(initial:PersianDate,dismiss:()->Unit,select:(PersianDate)->Unit){
 var y by remember{mutableIntStateOf(initial.year)};var m by remember{mutableIntStateOf(initial.month)};var day by remember{mutableIntStateOf(initial.day)}
 AlertDialog(
  onDismissRequest=dismiss,title={Text("انتخاب تاریخ شمسی")},
  text={Column{MonthHeader(y,m,{if(m==1){m=12;y--}else m--},{if(m==12){m=1;y++}else m++});WeekHeader();CalendarGrid(y,m,day){day=it}}},
  confirmButton={TextButton({select(PersianDate(y,m,day.coerceAtMost(persianMonthDays(y,m))))}){Text("تأیید")}},
  dismissButton={TextButton(dismiss){Text("انصراف")}}
 )
}
@Composable private fun MonthHeader(year:Int,month:Int,previous:()->Unit,next:()->Unit){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){IconButton(previous){Icon(Icons.Outlined.ChevronRight,null)};Column(horizontalAlignment=Alignment.CenterHorizontally){Text(persianMonthName(month),fontWeight=FontWeight.Bold);Text(faNumber(year),fontSize=11.sp,color=Muted)};IconButton(next){Icon(Icons.Outlined.ChevronLeft,null)}}}
@Composable private fun WeekHeader(){Row(Modifier.fillMaxWidth()){listOf("ش","ی","د","س","چ","پ","ج").forEach{Text(it,Modifier.weight(1f),textAlign=androidx.compose.ui.text.style.TextAlign.Center,fontSize=10.sp,color=Muted)}}}
@Composable private fun CalendarGrid(year:Int,month:Int,selected:Int,onSelect:(Int)->Unit){val first=Calendar.getInstance().apply{timeInMillis=PersianDate(year,month,1).toMillis()};val offset=first.get(Calendar.DAY_OF_WEEK)%7;val days=persianMonthDays(year,month);Column{repeat(6){week->Row(Modifier.fillMaxWidth()){repeat(7){col->val d=week*7+col-offset+1;if(d in 1..days){Surface(onClick={onSelect(d)},shape=CircleShape,color=if(selected==d)Accent else Color.Transparent,modifier=Modifier.weight(1f).aspectRatio(1f)){Box(contentAlignment=Alignment.Center){Text(faNumber(d),fontSize=11.sp,color=if(selected==d)Color.White else Ink)}}}else Spacer(Modifier.weight(1f).aspectRatio(1f))}}}}}

@OptIn(ExperimentalMaterial3Api::class) @Composable private fun DetailSheet(row:PaymentWithInstallments,dismiss:()->Unit,toggle:(Installment)->Unit,attach:(Installment,Uri)->Unit,delete:()->Unit){var full by remember{mutableStateOf(false)};var target by remember{mutableStateOf<Installment?>(null)};val launcher=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null)target?.let{attach(it,uri)}};val paid=row.installments.count{it.paidAt!=null};val remain=row.installments.filter{it.paidAt==null};ModalBottomSheet(onDismissRequest=dismiss,dragHandle={BottomSheetDefaults.DragHandle()},sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=!full)){LazyColumn(Modifier.fillMaxWidth().padding(horizontal=18.dp),verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(bottom=25.dp)){
 item{Row(verticalAlignment=Alignment.CenterVertically){Surface(color=Soft,shape=RoundedCornerShape(14.dp),modifier=Modifier.size(46.dp)){Box(contentAlignment=Alignment.Center){Icon(iconFor(row.item.iconKey),null,tint=Accent)}};Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(row.item.title,fontWeight=FontWeight.Bold,fontSize=17.sp);Text("${row.item.category} · ${if(row.item.repeatRule=="monthly")"ماهانه" else "یک‌بار"}",fontSize=10.sp,color=Muted)};IconButton(delete){Icon(Icons.Outlined.Delete,null,tint=Danger)}}}
 item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(14.dp),verticalAlignment=Alignment.CenterVertically){Box(contentAlignment=Alignment.Center){CircularProgressIndicator(paid.toFloat()/row.item.installmentCount.coerceAtLeast(1),Modifier.size(96.dp),strokeWidth=9.dp,color=Accent,trackColor=Soft);Column(horizontalAlignment=Alignment.CenterHorizontally){Text("${faNumber((paid*100/row.item.installmentCount.coerceAtLeast(1)))}٪",fontWeight=FontWeight.Bold);Text("پرداخت",fontSize=9.sp,color=Muted)}};Column(Modifier.weight(1f)){Stat("قسط بعدی",remain.firstOrNull()?.let{formatMoney(it.amount)}?:"تسویه");Stat("تعداد باقی‌مانده",faNumber(remain.size));Stat("مانده کل","${formatMoney(remain.sumOf{it.amount})} تومان")}}}
 item{Button({full=!full},Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Soft,contentColor=Accent)){Text(if(full)"بستن جزئیات" else "مشاهده برنامه کامل اقساط")}}
 if(full)items(row.installments,key={it.id}){ins->InstallmentRow(ins,{toggle(ins)},{target=ins;launcher.launch(arrayOf("image/*","application/pdf"))})}
 }} }
@Composable private fun Stat(k:String,v:String){Row(Modifier.fillMaxWidth().padding(vertical=5.dp),horizontalArrangement=Arrangement.SpaceBetween){Text(k,fontSize=10.sp,color=Muted);Text(v,fontSize=11.sp,fontWeight=FontWeight.Bold)};HorizontalDivider(color=Color(0xFFE8E8ED))}
@Composable private fun InstallmentRow(i:Installment,toggle:()->Unit,attach:()->Unit){Surface(shape=RoundedCornerShape(14.dp),border=BorderStroke(1.dp,Color(0xFFE8E8ED)),color=Color.White){Row(Modifier.fillMaxWidth().padding(10.dp),verticalAlignment=Alignment.CenterVertically){IconButton(toggle){Icon(if(i.paidAt==null)Icons.Outlined.RadioButtonUnchecked else Icons.Outlined.CheckCircle,null,tint=if(i.paidAt==null)Muted else Color(0xFF19765A))};Column(Modifier.weight(1f)){Text("قسط ${faNumber(i.sequence)}",fontWeight=FontWeight.Bold,fontSize=12.sp);Text("${faDate(i.dueAt)} · ${if(i.paidAt==null)"در انتظار" else "پرداخت شده"}",fontSize=9.sp,color=Muted)};Text(formatMoney(i.amount),fontSize=11.sp,fontWeight=FontWeight.Bold);IconButton(attach){Icon(if(i.receiptPath==null)Icons.Outlined.AttachFile else Icons.Outlined.Description,"رسید",tint=if(i.receiptPath==null)Muted else Accent)}}}}

@Composable private fun CalendarScreen(data:List<PaymentWithInstallments>,open:(PaymentWithInstallments)->Unit,modifier:Modifier){var date by remember{mutableStateOf(PersianDate.from())};val due=data.filter{r->r.installments.any{PersianDate.from(it.dueAt)==date}};Column(modifier.fillMaxSize().padding(16.dp)){Text("تقویم پرداخت‌ها",fontSize=22.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(10.dp));PersianMonthInline(date){date=it};Spacer(Modifier.height(12.dp));Text("موعدهای ${faNumber(date)}",fontWeight=FontWeight.Bold);Spacer(Modifier.height(7.dp));if(due.isEmpty())EmptyCard("در این روز پرداختی ندارید") else due.forEach{PaymentRow(it){open(it)};Spacer(Modifier.height(7.dp))}}}
@Composable private fun PersianMonthInline(selected:PersianDate,onSelect:(PersianDate)->Unit){var y by remember{mutableIntStateOf(selected.year)};var m by remember{mutableIntStateOf(selected.month)};Card(shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=Color.White)){Column(Modifier.padding(10.dp)){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){IconButton({if(m==1){m=12;y--}else m--}){Icon(Icons.Outlined.ChevronRight,null)};Text("${persianMonthName(m)} ${faNumber(y)}",fontWeight=FontWeight.Bold);IconButton({if(m==12){m=1;y++}else m++}){Icon(Icons.Outlined.ChevronLeft,null)}};val first=Calendar.getInstance().apply{timeInMillis=PersianDate(y,m,1).toMillis()};val off=(first.get(Calendar.DAY_OF_WEEK)+0)%7;repeat(6){w->Row(Modifier.fillMaxWidth()){repeat(7){c->val d=w*7+c-off+1;if(d in 1..persianMonthDays(y,m))Surface(onClick={onSelect(PersianDate(y,m,d))},shape=CircleShape,color=if(selected==PersianDate(y,m,d))Accent else Color.Transparent,modifier=Modifier.weight(1f).aspectRatio(1f)){Box(contentAlignment=Alignment.Center){Text(faNumber(d),fontSize=10.sp,color=if(selected==PersianDate(y,m,d))Color.White else Ink)}}else Spacer(Modifier.weight(1f).aspectRatio(1f))}}}}}}

@Composable private fun ReportScreen(data:List<PaymentWithInstallments>,modifier:Modifier){val all=data.flatMap{it.installments};val paid=all.filter{it.paidAt!=null}.sumOf{it.amount};val remaining=all.filter{it.paidAt==null}.sumOf{it.amount};Column(modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("گزارش مالی",fontSize=22.sp,fontWeight=FontWeight.Bold);Card(colors=CardDefaults.cardColors(containerColor=Ink),shape=RoundedCornerShape(22.dp)){Column(Modifier.fillMaxWidth().padding(18.dp)){Text("جمع کل تعهدات",color=Color.White.copy(.65f),fontSize=11.sp);Text("${formatMoney(paid+remaining)} تومان",color=Color.White,fontSize=25.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(18.dp));LinearProgressIndicator(if(paid+remaining==0L)0f else paid.toFloat()/(paid+remaining),Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),color=Color(0xFF7EE0BD),trackColor=Color.White.copy(.15f))}};Metric("پرداخت‌شده",paid,Color(0xFF19765A));Metric("باقی‌مانده",remaining,Accent);Metric("تعداد تعهدات",data.size.toLong(),Ink)}}
@Composable private fun Metric(title:String,value:Long,color:Color){Surface(shape=RoundedCornerShape(17.dp),color=Color.White,border=BorderStroke(1.dp,Color(0xFFE8E8ED))){Row(Modifier.fillMaxWidth().padding(16.dp),horizontalArrangement=Arrangement.SpaceBetween){Text(title,color=Muted);Text(if(title.startsWith("تعداد"))faNumber(value) else "${formatMoney(value)} تومان",fontWeight=FontWeight.Bold,color=color)}}}

@Composable private fun SettingsScreen(vm:AppViewModel,message:(String)->Unit,modifier:Modifier){val context=LocalContext.current;val notify=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){message(if(it)"دسترسی اعلان فعال شد" else "دسترسی اعلان داده نشد")};val export=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")){it?.let{vm.export(it){message("بکاپ ذخیره شد")}}};val restoreLauncher=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){it?.let{vm.import(it){message("بکاپ بازیابی شد")}}};Column(modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("تنظیمات",fontSize=22.sp,fontWeight=FontWeight.Bold);SettingCard(Icons.Outlined.Notifications,"اجازه اعلان","برای یادآوری سررسیدها"){if(Build.VERSION.SDK_INT>=33)notify.launch(Manifest.permission.POST_NOTIFICATIONS) else message("اعلان‌ها فعال هستند")};SettingCard(Icons.Outlined.Alarm,"اجازه آلارم دقیق","برای هشدار در ساعت مشخص"){if(Build.VERSION.SDK_INT>=31)context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))) else message("آلارم دقیق فعال است")};SettingCard(Icons.Outlined.Backup,"تهیه بکاپ محلی","اطلاعات و رسیدها در یک فایل ZIP"){export.launch("moed-backup-${PersianDate.from()}.zip")};SettingCard(Icons.Outlined.Restore,"بازیابی بکاپ","انتخاب فایل بکاپ از حافظه گوشی"){restoreLauncher.launch(arrayOf("application/zip"))};Spacer(Modifier.height(4.dp));Surface(color=Soft,shape=RoundedCornerShape(16.dp)){Text("ورود Google در نسخه بعد از ثبت OAuth Client فعال می‌شود. استفاده از برنامه بدون حساب کاملاً ممکن است و خروج هیچ داده‌ای را حذف نخواهد کرد.",Modifier.padding(14.dp),fontSize=10.sp,color=Muted)}}}
@Composable private fun SettingCard(icon:androidx.compose.ui.graphics.vector.ImageVector,title:String,sub:String,click:()->Unit){Card(onClick=click,shape=RoundedCornerShape(16.dp),colors=CardDefaults.cardColors(containerColor=Color.White),border=BorderStroke(1.dp,Color(0xFFE8E8ED))){Row(Modifier.fillMaxWidth().padding(13.dp),verticalAlignment=Alignment.CenterVertically){Icon(icon,null,tint=Accent);Spacer(Modifier.width(11.dp));Column(Modifier.weight(1f)){Text(title,fontWeight=FontWeight.Bold,fontSize=12.sp);Text(sub,fontSize=9.5.sp,color=Muted)};Icon(Icons.Outlined.ChevronLeft,null,tint=Muted)}}}

private fun faDate(millis:Long):String{val p=PersianDate.from(millis);return "${faNumber(p.day)} ${persianMonthName(p.month)} ${faNumber(p.year)}"}
private fun iconFor(key:String)=when(key){"bank"->Icons.Outlined.AccountBalance;"home"->Icons.Outlined.Home;"bolt"->Icons.Outlined.Bolt;"car"->Icons.Outlined.DirectionsCar;"book"->Icons.Outlined.MenuBook;"heart"->Icons.Outlined.FavoriteBorder;"star"->Icons.Outlined.StarBorder;else->Icons.Outlined.CreditCard}
