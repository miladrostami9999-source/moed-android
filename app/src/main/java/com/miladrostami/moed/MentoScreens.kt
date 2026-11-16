package com.miladrostami.moed

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miladrostami.moed.data.*
import com.miladrostami.moed.domain.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.ui.text.style.TextDirection
import java.text.DecimalFormat
import java.util.Calendar
import kotlinx.coroutines.launch

private val MI=Color(0xFF171A24);private val MP:Color @Composable get()=MaterialTheme.colorScheme.primary;private val MT:Color @Composable get()=if(MaterialTheme.colorScheme.surface.luminance()<.5f)Color(0xFF62D9B5) else Color(0xFF087B61);private val MM:Color @Composable get()=MaterialTheme.colorScheme.onSurfaceVariant;private val MS:Color @Composable get()=MaterialTheme.colorScheme.primaryContainer;private val ML=Color(0xFFE7E9F0);private val MR=Color(0xFFC43D54)
internal fun money(v:Long)=groupedAmount(v)
private fun fa(v:Any)=if(AppLanguage.code=="en")v.toString() else v.toString().map{"0123456789".indexOf(it).let{n->if(n>=0)"۰۱۲۳۴۵۶۷۸۹"[n] else it}}.joinToString("")
internal fun date(ms:Long):String{val p=PersianDate.from(ms);return "${fa(p.day)} ${persianMonthName(p.month)} ${fa(p.year)}"}

@Composable fun MentoOnboarding(auth:GoogleAuthManager,onUser:(LocalGoogleUser)->Unit,onOffline:()->Unit,message:(String)->Unit){
 val c=LocalContext.current;val scope=rememberCoroutineScope();var register by remember{mutableStateOf(false)}
 var name by remember{mutableStateOf("")};var email by remember{mutableStateOf("")};var phone by remember{mutableStateOf("")}
 Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(28.dp),horizontalAlignment=Alignment.CenterHorizontally){
  Spacer(Modifier.weight(.55f));Image(painterResource(R.drawable.mento_logo),"Mento",Modifier.size(104.dp));Spacer(Modifier.height(22.dp))
  Text(tx("پرداخت‌ها، مرتب و به‌موقع","Payments, organized and on time"),fontSize=25.sp,fontWeight=FontWeight.ExtraBold,textAlign=TextAlign.Center)
  Text(tx("حساب خودت را بساز؛ اطلاعات مالی همچنان خصوصی روی همین گوشی می‌ماند.","Create your account. Your financial data stays private on this phone."),Modifier.padding(top=10.dp),fontSize=14.sp,lineHeight=22.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,textAlign=TextAlign.Center)
  Spacer(Modifier.height(30.dp))
  Button({if(!auth.configured)message(tx("ورود گوگل تنظیم نشده","Google sign-in is not configured"))else scope.launch{runCatching{auth.signIn(c as Activity)}.onSuccess(onUser).onFailure{message(it.message?:tx("ورود انجام نشد","Sign-in failed"))}}},Modifier.fillMaxWidth().height(48.dp),shape=RoundedCornerShape(16.dp)){Icon(Icons.Outlined.AccountCircle,null);Spacer(Modifier.width(8.dp));Text(tx("ادامه با Google","Continue with Google"),fontWeight=FontWeight.Bold)}
  Spacer(Modifier.height(10.dp));OutlinedButton({register=true},Modifier.fillMaxWidth().height(48.dp),shape=RoundedCornerShape(16.dp)){Icon(Icons.Outlined.PersonAdd,null);Spacer(Modifier.width(8.dp));Text(tx("ساخت حساب بدون Google","Create account without Google"),fontWeight=FontWeight.Bold)}
  TextButton(onOffline){Text(tx("فعلاً به‌صورت مهمان ادامه می‌دهم","Continue as guest"))};Spacer(Modifier.weight(1f));Text(tx("Mento اطلاعات مالی شما را به فضای ابری ارسال نمی‌کند.","Mento does not upload your financial data to the cloud."),fontSize=11.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,textAlign=TextAlign.Center)
 }
 if(register)AlertDialog(onDismissRequest={register=false},title={Text(tx("ساخت حساب Mento","Create your Mento account"))},text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){OutlinedTextField(name,{name=it},Modifier.fillMaxWidth(),singleLine=true,label={Text(tx("نام و نام خانوادگی *","Full name *"))},isError=name.isBlank());OutlinedTextField(email,{email=it},Modifier.fillMaxWidth(),singleLine=true,label={Text(tx("ایمیل (اختیاری)","Email (optional)"))});OutlinedTextField(phone,{phone=it.filter(Char::isDigit)},Modifier.fillMaxWidth(),singleLine=true,label={Text(tx("شماره تماس (اختیاری)","Phone number (optional)"))});Text(tx("نام برای ساخت حساب الزامی است. حساب و اطلاعات فقط روی همین گوشی ذخیره می‌شوند.","Your name is required. Your account and data are stored on this phone."),fontSize=10.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}},confirmButton={Button({if(name.isBlank())message(tx("نام و نام خانوادگی را وارد کنید","Enter your full name"))else{onUser(auth.registerLocal(name,email,phone));register=false}}){Text(tx("ساخت حساب و ورود","Create account"))}},dismissButton={TextButton({register=false}){Text(tx("انصراف","Cancel"))}})
}

@Composable private fun LegacyMentoOnboarding(auth:GoogleAuthManager,onUser:(LocalGoogleUser)->Unit,onOffline:()->Unit,message:(String)->Unit){ }

@Composable private fun PaymentCard(r:PaymentWithInstallments,i:Installment,open:()->Unit,pay:()->Unit){val done=i.paidAt!=null;Card(onClick=open,shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(MaterialTheme.colorScheme.surface),border=BorderStroke(1.dp,if(done)MT.copy(.45f) else MaterialTheme.colorScheme.outline),modifier=Modifier.animateContentSize()){Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically){Surface(color=if(done)MT.copy(.12f) else MS,shape=RoundedCornerShape(14.dp),modifier=Modifier.size(44.dp)){Box(contentAlignment=Alignment.Center){Icon(icon(r.item.iconKey),null,tint=if(done)MT else MP)}};Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(r.item.title,fontWeight=FontWeight.Bold);Text(if(done)tx("انجام شد · ${date(i.dueAt)}","Paid · ${date(i.dueAt)}") else "${lx(r.item.category)} · ${date(i.dueAt)}",fontSize=11.sp,color=if(done)MT else if(i.dueAt<System.currentTimeMillis())MR else MM);Text("${money(i.amount)} ${tx("تومان","toman")}",fontSize=12.sp,fontWeight=FontWeight.SemiBold)};Surface(onClick=pay,shape=CircleShape,color=if(done)MT else MaterialTheme.colorScheme.surface,border=BorderStroke(1.5.dp,if(done)MT else MP),modifier=Modifier.size(36.dp)){Box(contentAlignment=Alignment.Center){Icon(Icons.Outlined.Done,if(done)tx("انجام شده","Paid") else tx("ثبت پرداخت","Mark as paid"),tint=if(done)MaterialTheme.colorScheme.surface else MP,modifier=Modifier.size(19.dp))}}}}}

private data class IC(val key:String,val label:String,val image:ImageVector,val group:String)
private val icons=listOf(
 // مالی
 IC("card","کارت",Icons.Outlined.CreditCard,"مالی"),
 IC("bank","بانک",Icons.Outlined.AccountBalance,"مالی"),
 IC("wallet","کیف پول",Icons.Outlined.AccountBalanceWallet,"مالی"),
 IC("receipt","رسید",Icons.Outlined.ReceiptLong,"مالی"),
 IC("savings","پس‌انداز",Icons.Outlined.Savings,"مالی"),
 IC("insurance","بیمه",Icons.Outlined.Shield,"مالی"),
 IC("subscription","اشتراک",Icons.Outlined.Subscriptions,"مالی"),
 // خانه
 IC("home","خانه",Icons.Outlined.Home,"خانه"),
 IC("chair","مبلمان",Icons.Outlined.Chair,"خانه"),
 IC("kitchen","آشپزخانه",Icons.Outlined.Kitchen,"خانه"),
 IC("tv","لوازم برقی",Icons.Outlined.Tv,"خانه"),
 IC("bolt","انرژی",Icons.Outlined.Bolt,"خانه"),
 // خودرو
 IC("car","خودرو",Icons.Outlined.DirectionsCar,"خودرو"),
 IC("fuel","سوخت",Icons.Outlined.LocalGasStation,"خودرو"),
 IC("repair","تعمیرات",Icons.Outlined.Handyman,"خودرو"),
 IC("bike","موتور",Icons.Outlined.TwoWheeler,"خودرو"),
 // خرید
 IC("cart","خرید",Icons.Outlined.ShoppingCart,"خرید"),
 IC("food","خوراک",Icons.Outlined.Restaurant,"خرید"),
 IC("fastfood","فست‌فود",Icons.Outlined.Fastfood,"خرید"),
 IC("clothes","پوشاک",Icons.Outlined.Checkroom,"خرید"),
 // شخصی
 IC("book","آموزش",Icons.Outlined.MenuBook,"شخصی"),
 IC("health","درمان",Icons.Outlined.HealthAndSafety,"شخصی"),
 IC("fitness","ورزش",Icons.Outlined.FitnessCenter,"شخصی"),
 IC("phone","موبایل",Icons.Outlined.PhoneAndroid,"شخصی"),
 IC("laptop","رایانه",Icons.Outlined.Laptop,"شخصی"),
 IC("beauty","آرایشی",Icons.Outlined.Spa,"شخصی"),
 IC("face","زیبایی",Icons.Outlined.Face,"شخصی"),
 // افراد
 IC("man","مرد",Icons.Outlined.Man,"افراد"),
 IC("woman","زن",Icons.Outlined.Woman,"افراد"),
 IC("child","بچه",Icons.Outlined.ChildCare,"افراد"),
 IC("family","خانواده",Icons.Outlined.FamilyRestroom,"افراد"),
 // تفریح
 IC("flight","سفر",Icons.Outlined.Flight,"تفریح"),
 IC("sport","سرگرمی",Icons.Outlined.SportsSoccer,"تفریح"),
 IC("movie","فیلم",Icons.Outlined.Movie,"تفریح"),
 IC("music","موسیقی",Icons.Outlined.MusicNote,"تفریح"),
 // سایر
 IC("gift","هدیه",Icons.Outlined.CardGiftcard,"سایر"),
 IC("pets","حیوانات",Icons.Outlined.Pets,"سایر"),
 IC("star","ویژه",Icons.Outlined.StarBorder,"سایر"),
 IC("more","سایر",Icons.Outlined.MoreHoriz,"سایر")
)
internal fun icon(k:String)=icons.firstOrNull{it.key==k}?.image?:Icons.Outlined.CreditCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun MentoAddSheet(
 initial:PaymentWithInstallments?,
 existingCustomCategories:List<String> = emptyList(),
 dismiss:()->Unit,
 save:(String,String,String,Long,PersianDate,String,Int,String,Set<Int>,Int,Int)->Unit
){
 val locked=initial?.installments?.any{it.paidAt!=null}==true
 var title by remember{mutableStateOf(initial?.item?.title?:"")}
 val predefined = PREDEFINED_CATEGORIES.filter{it != "دلخواه"}
 var cat by remember{mutableStateOf(if(initial?.item?.category in predefined) initial?.item?.category?:"قسط" else "دلخواه")}
 var customCat by remember{mutableStateOf(if(cat=="دلخواه") initial?.item?.category.orEmpty() else "")}
 var ico by remember{mutableStateOf(initial?.item?.iconKey?:"card")}
 var raw by remember{mutableStateOf(initial?.item?.amount?.toString()?:"1000000")}
 var due by remember{mutableStateOf(initial?.item?.firstDueAt?.let(PersianDate::from)?:PersianDate.from())}
 var repeat by remember{mutableStateOf(initial?.item?.repeatRule?:"monthly")}
 var count by remember{mutableStateOf(initial?.item?.installmentCount?.toString()?:"12")}
 var mode by remember{mutableStateOf(initial?.item?.reminderMode?:"notification")}
 var offsets by remember{mutableStateOf(initial?.item?.reminderOffsets?.split(",")?.mapNotNull{it.toIntOrNull()}?.toSet()?.plus(0)?:setOf(3,0))}
 var hour by remember{mutableIntStateOf(initial?.item?.reminderHour?:9)}
 var minute by remember{mutableIntStateOf(initial?.item?.reminderMinute?:0)}
 var ip by remember{mutableStateOf(false)}
 var dp by remember{mutableStateOf(false)}
 var cm by remember{mutableStateOf(false)}
 var rm by remember{mutableStateOf(false)}
 var tp by remember{mutableStateOf(false)}
 val amount=raw.toLongOrNull()?:0L

 ModalBottomSheet(onDismissRequest=dismiss,dragHandle={BottomSheetDefaults.DragHandle()}){
  LazyColumn(Modifier.fillMaxWidth().padding(horizontal=18.dp),verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(bottom=36.dp)){
   item{Text(if(initial==null)tx("پرداخت جدید","New payment") else tx("ویرایش پرداخت","Edit payment"),fontSize=21.sp,fontWeight=FontWeight.ExtraBold);Text(tx("جزئیات را قدم‌به‌قدم وارد کنید","Enter your payment details"),fontSize=12.sp,color=MM)}
   item{Label("مشخصات");OutlinedTextField(title,{title=it},Modifier.fillMaxWidth(),label={Text(tx("عنوان","Title"))},singleLine=true,shape=RoundedCornerShape(15.dp));Spacer(Modifier.height(9.dp));Row(horizontalArrangement=Arrangement.spacedBy(9.dp)){SelectField(lx(cat),categoryIcon(cat),{cm=true},Modifier.weight(1f));OutlinedButton({ip=true},Modifier.widthIn(min=105.dp,max=150.dp).height(48.dp),shape=RoundedCornerShape(15.dp)){Icon(icon(ico),null);Spacer(Modifier.width(5.dp));Text(tx("آیکون","Icon"))}}}
   item{if(cat=="دلخواه"){Spacer(Modifier.height(9.dp));OutlinedTextField(customCat,{customCat=it},Modifier.fillMaxWidth(),label={Text(tx("نام دسته دلخواه","Custom category name"))},singleLine=true,shape=RoundedCornerShape(15.dp))}}
   item{Label("مبلغ");OutlinedTextField(amount.takeIf{it>0}?.let(::money)?:"",{raw=amountInput(it)},Modifier.fillMaxWidth(),enabled=!locked,label={Text(tx("مبلغ هر پرداخت","Amount per payment"))},suffix={Text(tx("تومان","toman"))},textStyle=LocalTextStyle.current.copy(textDirection=TextDirection.Ltr),singleLine=true,shape=RoundedCornerShape(15.dp));Text(words(amount),fontSize=11.sp,color=MP,modifier=Modifier.padding(8.dp));Slider(amount.coerceIn(1_000_000,20_000_000).toFloat(),{raw=it.toLong().toString()},valueRange=1_000_000f..20_000_000f,steps=18,enabled=!locked);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(tx("۱ میلیون","1 million"),fontSize=10.sp,color=MM);Text(tx("۲۰ میلیون","20 million"),fontSize=10.sp,color=MM)}}
   item{
    Label("زمان‌بندی")
    OutlinedButton({dp=true},Modifier.fillMaxWidth().height(48.dp),enabled=!locked,shape=RoundedCornerShape(15.dp)){
     Icon(Icons.Outlined.CalendarMonth,null);Spacer(Modifier.width(8.dp))
     Text(tx("اولین سررسید: ${date(due.toMillis())}","First due date: ${date(due.toMillis())}"))
     Spacer(Modifier.weight(1f));Icon(Icons.Outlined.ChevronLeft,null)
    }
    Spacer(Modifier.height(9.dp))
    val frequencies = listOf(
     "monthly" to tx("ماهانه","Monthly"),
     "quarterly" to tx("هر ۳ ماه","Every 3 months"),
     "semiannual" to tx("هر ۶ ماه","Every 6 months"),
     "yearly" to tx("سالانه","Yearly"),
     "once" to tx("یک‌بار","One-time")
    )
    LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)){
     items(frequencies){(key,label)->
      FilterChip(
       selected = repeat == key,
       onClick = {repeat = key; if(key=="once") count="1"},
       label = {Text(label,fontSize=12.sp)},
       enabled = !locked,
       shape = RoundedCornerShape(12.dp)
      )
     }
    }
    if(repeat != "once"){
     Spacer(Modifier.height(9.dp))
     OutlinedTextField(
      count,
      {count=it.filter(Char::isDigit)},
      Modifier.fillMaxWidth(),
      enabled=!locked,
      label={Text(tx("تعداد اقساط","Number of installments"))},
      supportingText={
       val n = (count.toIntOrNull()?:1).coerceAtLeast(1)
       val step = when(repeat){"quarterly"->3;"semiannual"->6;"yearly"->12;else->1}
       Text(tx("سررسید نهایی: ${date(addPersianMonths(due,(n-1)*step).toMillis())}","Final due date: ${date(addPersianMonths(due,(n-1)*step).toMillis())}"))
      },
      shape=RoundedCornerShape(15.dp)
     )
    }
   }
   item{Label("یادآوری");SelectField(reminderTitle(mode),if(mode=="alarm")Icons.Outlined.Alarm else Icons.Outlined.Notifications,{rm=true},Modifier.fillMaxWidth());Text(tx("چند زمان را هم‌زمان انتخاب کنید","Choose multiple reminder times"),fontSize=11.sp,color=MM);LazyRow(horizontalArrangement=Arrangement.spacedBy(7.dp)){items(listOf(7,3,1,0)){d->FilterChip(d in offsets,{if(d!=0)offsets=if(d in offsets)offsets-d else offsets+d},{Text(if(d==0)tx("روز موعد","On due date") else tx("${fa(d)} روز قبل","${fa(d)} days before"))},leadingIcon=if(d in offsets){{Icon(Icons.Outlined.Check,null,Modifier.size(16.dp))}}else null)}};OutlinedButton({tp=true},Modifier.fillMaxWidth().height(48.dp),shape=RoundedCornerShape(15.dp)){Icon(Icons.Outlined.Schedule,null);Spacer(Modifier.width(8.dp));Text(tx("ساعت یادآوری","Reminder time"));Spacer(Modifier.weight(1f));Text("${fa(hour.toString().padStart(2,'0'))}:${fa(minute.toString().padStart(2,'0'))}",fontWeight=FontWeight.Bold)}}
   item{Button({val n=(count.toIntOrNull()?:1).coerceIn(1,360);if(title.isNotBlank()&&amount>0&&offsets.isNotEmpty()&&(cat!="دلخواه"||customCat.isNotBlank()))save(title,if(cat=="دلخواه")customCat.trim() else cat,ico,amount,due,repeat,n,mode,offsets,hour,minute)},Modifier.fillMaxWidth().height(48.dp),shape=RoundedCornerShape(16.dp)){Text(tx("ذخیره و فعال‌سازی","Save and activate"),fontWeight=FontWeight.Bold)}}
  }
  if(cm) CategoryDialog(cat, existingCustomCategories, {cat=it;cm=false}) {cm=false}
  if(rm) ReminderDialog(mode,{mode=it;rm=false}){rm=false}
  if(ip) IconDialog(ico,{ico=it;ip=false}){ip=false}
  if(dp) DateDialog(due,{dp=false}){due=it;dp=false}
  if(tp) TimeDialog(hour,minute,{tp=false}){h,m->hour=h;minute=m;tp=false}
 }
}

@Composable private fun Label(t:String){Text(when(t){"مشخصات"->tx(t,"Details");"مبلغ"->tx(t,"Amount");"زمان‌بندی"->tx(t,"Schedule");"یادآوری"->tx(t,"Reminders");else->lx(t)},fontWeight=FontWeight.Bold,fontSize=14.sp,modifier=Modifier.padding(bottom=7.dp))}

@Composable private fun IconDialog(selected:String,choose:(String)->Unit,dismiss:()->Unit){
 var group by remember{mutableStateOf("")};var query by remember{mutableStateOf("")}
 val visible=icons.filter{(group.isEmpty()||it.group==group)&&(query.isBlank()||lx(it.label).contains(query,true)||lx(it.group).contains(query,true))}
 AlertDialog(onDismissRequest=dismiss,shape=RoundedCornerShape(22.dp),containerColor=MaterialTheme.colorScheme.surface,title={Text(tx("انتخاب آیکون","Choose an icon"),fontSize=18.sp,fontWeight=FontWeight.Bold)},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
  OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),singleLine=true,shape=RoundedCornerShape(12.dp),textStyle=LocalTextStyle.current.copy(fontSize=12.sp),leadingIcon={Icon(Icons.Outlined.Search,null,Modifier.size(18.dp))},placeholder={Text(tx("جست‌وجوی آیکون","Find an icon"),fontSize=12.sp)})
  LazyRow(horizontalArrangement=Arrangement.spacedBy(5.dp)){items(listOf("")+icons.map{it.group}.distinct()){g->FilterChip(group==g,{group=g},{Text(if(g.isEmpty())tx("همه","All")else lx(g),fontSize=10.sp)},shape=RoundedCornerShape(10.dp))}}
  if(visible.isEmpty())Text(tx("آیکونی پیدا نشد","No icons found"),fontSize=12.sp,color=MM)
  LazyVerticalGrid(columns=GridCells.Adaptive(50.dp),modifier=Modifier.heightIn(max=280.dp),horizontalArrangement=Arrangement.spacedBy(4.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
   gridItems(visible,key={it.key}){x->Surface(onClick={choose(x.key)},shape=RoundedCornerShape(10.dp),color=if(selected==x.key)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,border=BorderStroke(1.dp,if(selected==x.key)MP else MaterialTheme.colorScheme.outlineVariant)){
    Column(Modifier.padding(8.dp),horizontalAlignment=Alignment.CenterHorizontally){Icon(x.image,null,Modifier.size(22.dp),tint=if(selected==x.key)MaterialTheme.colorScheme.onPrimaryContainer else MP);Text(lx(x.label),fontSize=9.sp,maxLines=1)}
   }}
  }
 }},confirmButton={TextButton(dismiss){Text(tx("بستن","Close"))}})
}

@Composable private fun DateDialog(initial:PersianDate,dismiss:()->Unit,choose:(PersianDate)->Unit){
 var year by remember{mutableIntStateOf(initial.year)}
 var month by remember{mutableIntStateOf(initial.month)}
 var day by remember{mutableIntStateOf(initial.day)}
 AlertDialog(onDismissRequest=dismiss,shape=RoundedCornerShape(22.dp),containerColor=MaterialTheme.colorScheme.surface,
  title={Text(tx("انتخاب تاریخ","Select date"),fontWeight=FontWeight.Bold)},
  text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){
   Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
    OutlinedTextField(year.toString(),{year=it.filter(Char::isDigit).toIntOrNull()?:year},Modifier.weight(1f),label={Text(tx("سال","Year"))},singleLine=true)
    OutlinedTextField(month.toString(),{month=it.filter(Char::isDigit).toIntOrNull()?.coerceIn(1,12)?:month},Modifier.weight(1f),label={Text(tx("ماه","Month"))},singleLine=true)
    OutlinedTextField(day.toString(),{day=it.filter(Char::isDigit).toIntOrNull()?.coerceIn(1,31)?:day},Modifier.weight(1f),label={Text(tx("روز","Day"))},singleLine=true)
   }
   Text("${fa(day)} ${persianMonthName(month)} ${fa(year)}",fontSize=14.sp,fontWeight=FontWeight.Medium)
  }},
  confirmButton={Button({choose(PersianDate(year,month,day));dismiss()}){Text(tx("تأیید","Confirm"))}},
  dismissButton={TextButton(dismiss){Text(tx("انصراف","Cancel"))}}
 )
}

@Composable private fun TimeDialog(initialHour:Int,initialMinute:Int,dismiss:()->Unit,choose:(Int,Int)->Unit){
 var hour by remember{mutableIntStateOf(initialHour)}
 var minute by remember{mutableIntStateOf(initialMinute)}
 AlertDialog(onDismissRequest=dismiss,shape=RoundedCornerShape(22.dp),containerColor=MaterialTheme.colorScheme.surface,
  title={Text(tx("ساعت یادآوری","Reminder time"),fontWeight=FontWeight.Bold)},
  text={Row(horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically){
   OutlinedTextField(hour.toString().padStart(2,'0'),{hour=it.filter(Char::isDigit).toIntOrNull()?.coerceIn(0,23)?:hour},Modifier.width(80.dp),label={Text(tx("ساعت","Hour"))},singleLine=true)
   Text(":",fontSize=20.sp,fontWeight=FontWeight.Bold)
   OutlinedTextField(minute.toString().padStart(2,'0'),{minute=it.filter(Char::isDigit).toIntOrNull()?.coerceIn(0,59)?:minute},Modifier.width(80.dp),label={Text(tx("دقیقه","Minute"))},singleLine=true)
  }},
  confirmButton={Button({choose(hour,minute);dismiss()}){Text(tx("تأیید","Confirm"))}},
  dismissButton={TextButton(dismiss){Text(tx("انصراف","Cancel"))}}
 )
}

// Remaining UI helpers from original file (Profile, Calendar, etc. are in other files or restored as needed)
@Composable private fun Avatar(name:String,uri:String?,size:Int){val c=LocalContext.current;val b=remember(uri){uri?.let{runCatching{c.contentResolver.openInputStream(Uri.parse(it))?.use(BitmapFactory::decodeStream)?.asImageBitmap()}.getOrNull()}};Surface(color=MS,shape=CircleShape,modifier=Modifier.size(size.dp)){if(b!=null)Image(b,null,Modifier.fillMaxSize(),contentScale=androidx.compose.ui.layout.ContentScale.Crop)else Box(contentAlignment=Alignment.Center){Text(name.trim().take(1).ifBlank{"م"},fontSize=(size/3).sp,fontWeight=FontWeight.Bold,color=MP)}}}
@Composable private fun Action(i:ImageVector,t:String,s:String,click:()->Unit){Card(onClick=click,shape=RoundedCornerShape(17.dp),colors=CardDefaults.cardColors(MaterialTheme.colorScheme.surface),border=BorderStroke(1.dp,MaterialTheme.colorScheme.outline)){Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically){Icon(i,null,tint=MP);Spacer(Modifier.width(11.dp));Column(Modifier.weight(1f)){Text(lx(t),fontWeight=FontWeight.Bold);Text(lx(s),fontSize=10.sp,color=MM)};Icon(Icons.Outlined.ChevronLeft,null,tint=MM)}}}
@Composable private fun Empty(t:String){Surface(shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surface,border=BorderStroke(1.dp,MaterialTheme.colorScheme.outline)){Text(lx(t),Modifier.fillMaxWidth().padding(26.dp),textAlign=TextAlign.Center,fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
private fun words(v:Long):String=if(v<=0)tx("مبلغ را وارد کنید","Enter an amount")else amountWords(v)
