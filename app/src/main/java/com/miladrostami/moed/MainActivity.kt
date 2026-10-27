package com.miladrostami.moed

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clip
import androidx.core.content.FileProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.window.Popup
import kotlin.math.cos
import kotlin.math.sin
import com.miladrostami.moed.data.*
import com.miladrostami.moed.domain.*
import com.miladrostami.moed.ui.theme.MoedTheme
import java.io.File
import java.util.*
import kotlinx.coroutines.launch

class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){installSplashScreen();super.onCreate(savedInstanceState);setContent{val prefs=remember{getSharedPreferences("mento_display",MODE_PRIVATE)};var theme by remember{mutableStateOf(prefs.getString("theme","system")?:"system")};var language by remember{mutableStateOf(prefs.getString("language","fa")?:"fa")};AppLanguage.code=language;MoedTheme(theme){CompositionLocalProvider(LocalLayoutDirection provides if(language=="en")LayoutDirection.Ltr else LayoutDirection.Rtl){MoedApp(themeMode=theme,setTheme={value:String->theme=value;prefs.edit().putString("theme",value).apply()},language=language,setLanguage={value:String->language=value;AppLanguage.code=value;prefs.edit().putString("language",value).apply()})}}}}}

private val Ink=Color(0xFF20212B);private val Accent:Color @Composable get()=MaterialTheme.colorScheme.primary;private val Muted:Color @Composable get()=MaterialTheme.colorScheme.onSurfaceVariant;private val Soft:Color @Composable get()=MaterialTheme.colorScheme.primaryContainer;private val Danger:Color @Composable get()=MaterialTheme.colorScheme.error
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun MoedApp(themeMode:String="system",setTheme:(String)->Unit={},language:String="fa",setLanguage:(String)->Unit={},vm:AppViewModel=viewModel()){
 val context=LocalContext.current;val auth=remember{GoogleAuthManager(context.applicationContext)};var user by remember{mutableStateOf(auth.currentUser())};val data by vm.payments.collectAsState();var tab by remember{mutableIntStateOf(0)};var add by remember{mutableStateOf(false)};var editing by remember{mutableStateOf<PaymentWithInstallments?>(null)};var selected by remember{mutableStateOf<PaymentWithInstallments?>(null)};var previewPath by remember{mutableStateOf<String?>(null)};var toast by remember{mutableStateOf<String?>(null)};var successKey by remember{mutableIntStateOf(0)}
 var pendingUndo by remember{mutableStateOf<Installment?>(null)}
 var onboarded by remember{mutableStateOf(auth.hasCompletedOnboarding)}
 LaunchedEffect(user?.id){vm.setActiveOwner(user?.id?:"local",claimLegacy=user!=null)}
 LaunchedEffect(data,selected?.item?.id){selected?.item?.id?.let{id->selected=data.firstOrNull{it.item.id==id}}}
 val snackbar=remember{SnackbarHostState()};LaunchedEffect(toast){toast?.let{snackbar.showSnackbar(it);toast=null}}
 if(!onboarded){MentoOnboarding(auth,{user=it;auth.completeOnboarding();onboarded=true},{auth.completeOnboarding();onboarded=true},{toast=it});return}
 CompositionLocalProvider(LocalLayoutDirection provides if(language=="en")LayoutDirection.Ltr else LayoutDirection.Rtl){key(language){Scaffold(snackbarHost={SnackbarHost(snackbar)},topBar={TopBar(user){tab=3}},floatingActionButton={if(tab==0)ExtendedFloatingActionButton(onClick={add=true},icon={Icon(Icons.Outlined.Add,tx("افزودن","Add"))},text={Text(tx("پرداخت جدید","New payment"))},containerColor=Accent,contentColor=Color.White)},bottomBar={NavigationBar(containerColor=MaterialTheme.colorScheme.surface){listOf(Icons.Outlined.Home to tx("خانه","Home"),Icons.Outlined.CalendarMonth to tx("تقویم","Calendar"),Icons.Outlined.BarChart to tx("گزارش","Reports"),Icons.Outlined.PersonOutline to tx("پروفایل","Profile")).forEachIndexed{i,p->NavigationBarItem(tab==i,{tab=i},{Icon(p.first,p.second)},label={Text(lx(p.second))})}}}){pad->Crossfade(tab,label="main-navigation"){page->when(page){0->MentoHomeScreen(data,{selected=it},{if(it.paidAt==null){successKey++;vm.togglePaid(it)}else{pendingUndo=it}},Modifier.padding(pad));1->MentoCalendarScreen(data,{selected=it},Modifier.padding(pad));2->MentoReportScreen(data,Modifier.padding(pad));else->MentoProfileScreen(vm,auth,user,{user=it},{toast=it},themeMode,setTheme,language,setLanguage,Modifier.padding(pad))}}}}}
 if(add||editing!=null)MentoAddSheet(editing,{add=false;editing=null}){title,cat,icon,amount,date,repeat,count,mode,offsets,hour,minute->editing?.let{vm.update(it,title,cat,icon,amount,date,repeat,count,mode,offsets,hour,minute)}?:vm.add(title,cat,icon,amount,date,repeat,count,mode,offsets,hour,minute);add=false;editing=null;toast=tx("اطلاعات با موفقیت ذخیره شد","Payment saved")}
 selected?.let{row->DetailSheet(row,{selected=null},{if(it.paidAt==null){successKey++;vm.togglePaid(it)}else{pendingUndo=it}},{ins,uri->vm.attach(ins,uri);toast=tx("رسید ذخیره شد","Receipt saved")},{ins->vm.deleteReceipt(ins);toast=tx("رسید حذف شد","Receipt removed")},{path->previewPath=path},{selected=null;editing=row},{vm.delete(row.item.id);selected=null;toast=tx("آیتم حذف شد","Payment deleted")})}
 previewPath?.let{ReceiptPreview(it,{previewPath=null}){toast=it}}
 pendingUndo?.let { installment -> AlertDialog(onDismissRequest={pendingUndo=null},title={Text(tx("لغو ثبت پرداخت؟","Undo payment?"))},text={Text(tx("این قسط دوباره در انتظار پرداخت قرار می‌گیرد. رسید آن حفظ می‌شود.","This installment will become unpaid again. Its receipt will be kept."))},confirmButton={TextButton(onClick={vm.togglePaid(installment);pendingUndo=null}){Text(tx("بله، تیک برداشته شود","Undo payment"))}},dismissButton={TextButton(onClick={pendingUndo=null}){Text(tx("انصراف","Cancel"))}}) }
 SuccessBurst(successKey)
}

@Composable private fun SuccessBurst(trigger:Int){
 val progress=remember{Animatable(1f)}
 LaunchedEffect(trigger){if(trigger>0){progress.snapTo(0f);progress.animateTo(1f,tween(720))}}
 if(progress.value<1f)Popup(alignment=Alignment.Center){Canvas(Modifier.size(150.dp)){val p=progress.value;val green=Color(0xFF17A67A);val fade=(1f-p).coerceIn(0f,1f);drawCircle(green.copy(alpha=.14f*fade),radius=(34+32*p).dp.toPx());drawCircle(green.copy(alpha=.8f*fade),radius=(25+10*p).dp.toPx(),style=androidx.compose.ui.graphics.drawscope.Stroke(2.2.dp.toPx()));val c=center;val checkAlpha=(1f-(p-.55f).coerceAtLeast(0f)/.45f).coerceIn(0f,1f);drawLine(green.copy(alpha=checkAlpha),c+Offset(-13.dp.toPx(),0f),c+Offset(-3.dp.toPx(),10.dp.toPx()),strokeWidth=4.dp.toPx(),cap=StrokeCap.Round);drawLine(green.copy(alpha=checkAlpha),c+Offset(-3.dp.toPx(),10.dp.toPx()),c+Offset(16.dp.toPx(),-12.dp.toPx()),strokeWidth=4.dp.toPx(),cap=StrokeCap.Round);repeat(8){i->val a=i*Math.PI/4-Math.PI/2;val r=(33+30*p).dp.toPx();drawCircle(if(i%2==0)green.copy(alpha=.65f*fade)else Color(0xFF315BFF).copy(alpha=.55f*fade),radius=2.2.dp.toPx()*(1-p*.4f),center=c+Offset((cos(a)*r).toFloat(),(sin(a)*r).toFloat()))}}}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun TopBar(user:LocalGoogleUser?,openProfile:()->Unit) {
 TopAppBar(
  colors=TopAppBarDefaults.topAppBarColors(containerColor=MaterialTheme.colorScheme.background),
  title={Row(verticalAlignment=Alignment.CenterVertically){
   Surface(color=Color.Transparent,shape=RoundedCornerShape(11.dp),modifier=Modifier.size(36.dp)){Image(painterResource(R.drawable.mento_logo),tx("لوگوی Mento","Mento logo"),Modifier.fillMaxSize())}
   Spacer(Modifier.width(9.dp));Column{Text("Mento",fontWeight=FontWeight.Bold,fontSize=17.sp);Text(tx("مدیریت هوشمند پرداخت‌ها","Keep your payments on track"),fontSize=10.sp,color=Muted)}
  }},
  actions={Surface(onClick=openProfile,color=Soft,shape=RoundedCornerShape(13.dp),modifier=Modifier.padding(end=12.dp).size(40.dp)){Box(contentAlignment=Alignment.Center){Text(user?.name?.trim()?.take(1)?.ifBlank{"م"}?:"م",color=Accent,fontWeight=FontWeight.Bold)}}}
 )
}

@OptIn(ExperimentalMaterial3Api::class) @Composable private fun DetailSheet(row:PaymentWithInstallments,dismiss:()->Unit,toggle:(Installment)->Unit,attach:(Installment,Uri)->Unit,removeReceipt:(Installment)->Unit,viewReceipt:(String)->Unit,edit:()->Unit,delete:()->Unit){var full by remember{mutableStateOf(false)};var confirmDelete by remember{mutableStateOf(false)};var target by remember{mutableStateOf<Installment?>(null)};val launcher=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri:Uri?->if(uri!=null)target?.let{attach(it,uri)}};val paid=row.installments.count{it.paidAt!=null};val remain=row.installments.filter{it.paidAt==null};ModalBottomSheet(onDismissRequest=dismiss,dragHandle={BottomSheetDefaults.DragHandle()},sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=!full)){LazyColumn(Modifier.fillMaxWidth().padding(horizontal=18.dp),verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(bottom=25.dp)){
 item{Row(verticalAlignment=Alignment.CenterVertically){Surface(color=Soft,shape=RoundedCornerShape(14.dp),modifier=Modifier.size(46.dp)){Box(contentAlignment=Alignment.Center){Icon(iconFor(row.item.iconKey),null,tint=Accent)}};Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(row.item.title,fontWeight=FontWeight.Bold,fontSize=17.sp);Text("${lx(row.item.category)} · ${if(row.item.repeatRule=="monthly")tx("ماهانه","Monthly") else tx("یک‌بار","One-time")}",fontSize=10.sp,color=Muted)};IconButton(edit){Icon(Icons.Outlined.Edit,tx("ویرایش","Edit"),tint=Accent)};IconButton({confirmDelete=true}){Icon(Icons.Outlined.Delete,tx("حذف","Delete"),tint=Danger)}}}
 item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(14.dp),verticalAlignment=Alignment.CenterVertically){Box(contentAlignment=Alignment.Center){CircularProgressIndicator(paid.toFloat()/row.item.installmentCount.coerceAtLeast(1),Modifier.size(96.dp),strokeWidth=9.dp,color=Accent,trackColor=Soft);Column(horizontalAlignment=Alignment.CenterHorizontally){Text("${faNumber((paid*100/row.item.installmentCount.coerceAtLeast(1)))}${tx("٪","%")}",fontWeight=FontWeight.Bold);Text(tx("پرداخت","Paid"),fontSize=9.sp,color=Muted)}};Column(Modifier.weight(1f)){Stat(tx("قسط بعدی","Next installment"),remain.firstOrNull()?.let{formatMoney(it.amount)}?:tx("تسویه","Settled"));Stat(tx("تعداد باقی‌مانده","Installments left"),faNumber(remain.size));Stat(tx("مانده کل","Total outstanding"),"${formatMoney(remain.sumOf{it.amount})} ${tx("تومان","toman")}")}}}
 item{Button({full=!full},Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Soft,contentColor=Accent)){Text(if(full)tx("بستن جزئیات","Hide schedule") else tx("مشاهده برنامه کامل اقساط","View full installment schedule"))}}
 if(full)items(row.installments,key={it.id}){ins->InstallmentRow(ins,{toggle(ins)},{target=ins;launcher.launch(arrayOf("image/*","application/pdf"))},{removeReceipt(ins)},{ins.receiptPath?.let(viewReceipt)})}
 }};if(confirmDelete)AlertDialog(onDismissRequest={confirmDelete=false},title={Text(tx("حذف این آیتم؟","Delete this item?"))},text={Text(tx("تمام اقساط و رسیدهای مرتبط از فهرست حذف می‌شوند. این کار قابل بازگشت نیست.","All related installments and receipts will be deleted. This cannot be undone."))},confirmButton={TextButton(delete){Text(tx("حذف","Delete"),color=Danger)}},dismissButton={TextButton({confirmDelete=false}){Text(tx("انصراف","Cancel"))}}) }
@Composable private fun Stat(k:String,v:String){Row(Modifier.fillMaxWidth().padding(vertical=5.dp),horizontalArrangement=Arrangement.SpaceBetween){Text(lx(k),fontSize=10.sp,color=Muted);Text(lx(v),fontSize=11.sp,fontWeight=FontWeight.Bold)};HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant)}
@Composable private fun InstallmentRow(i:Installment,toggle:()->Unit,attach:()->Unit,remove:()->Unit,view:()->Unit){Surface(shape=RoundedCornerShape(14.dp),border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant),color=MaterialTheme.colorScheme.surface){Row(Modifier.fillMaxWidth().padding(10.dp),verticalAlignment=Alignment.CenterVertically){IconButton(toggle){Icon(if(i.paidAt==null)Icons.Outlined.RadioButtonUnchecked else Icons.Outlined.CheckCircle,null,tint=if(i.paidAt==null)Muted else Color(0xFF19765A))};Column(Modifier.weight(1f)){Text(tx("قسط ${faNumber(i.sequence)}","Installment ${faNumber(i.sequence)}"),fontWeight=FontWeight.Bold,fontSize=12.sp);Text("${faDate(i.dueAt)} · ${if(i.paidAt==null)tx("در انتظار","Pending") else tx("پرداخت شده","Paid")}",fontSize=9.sp,color=Muted)};Text(formatMoney(i.amount),fontSize=11.sp,fontWeight=FontWeight.Bold);if(i.receiptPath!=null){IconButton(view){Icon(Icons.Outlined.Visibility,tx("مشاهده رسید","View receipt"),tint=Accent)};IconButton(remove){Icon(Icons.Outlined.DeleteOutline,tx("حذف رسید","Remove receipt"),tint=Danger)}};IconButton(attach){Icon(Icons.Outlined.AttachFile,if(i.receiptPath==null)tx("افزودن رسید","Attach receipt") else tx("جایگزینی رسید","Replace receipt"),tint=Muted)}}}}

private fun faDate(millis:Long):String{val p=PersianDate.from(millis);return "${faNumber(p.day)} ${persianMonthName(p.month)} ${faNumber(p.year)}"}
private fun iconFor(key:String)=when(key){"bank"->Icons.Outlined.AccountBalance;"home"->Icons.Outlined.Home;"bolt"->Icons.Outlined.Bolt;"car"->Icons.Outlined.DirectionsCar;"book"->Icons.Outlined.MenuBook;"heart"->Icons.Outlined.FavoriteBorder;"star"->Icons.Outlined.StarBorder;else->Icons.Outlined.CreditCard}

