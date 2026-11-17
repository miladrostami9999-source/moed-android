package com.miladrostami.moed

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import com.miladrostami.moed.domain.MAX_CUSTOM_DAYS
import com.miladrostami.moed.domain.customDaysRule
import com.miladrostami.moed.domain.faNumber
import com.miladrostami.moed.domain.isCustomRule
import com.miladrostami.moed.domain.repeatRules
import com.miladrostami.moed.domain.repeatStepDays

internal const val CUSTOM_CATEGORY="دلخواه"
internal val builtInCategories=listOf("قسط","وام","اجاره","قبض","خرید","آموزش","درمان","آرایشی بهداشتی","حمل‌ونقل","بیمه","اشتراک","سرگرمی","عمومی")

internal fun categoryIcon(category:String):ImageVector=when(category){
 "قسط"->Icons.Outlined.CreditCard;"وام"->Icons.Outlined.AccountBalance;"اجاره"->Icons.Outlined.Home;"قبض"->Icons.Outlined.ReceiptLong;"خرید"->Icons.Outlined.ShoppingCart;"آموزش"->Icons.Outlined.MenuBook;"درمان"->Icons.Outlined.HealthAndSafety;"حمل‌ونقل"->Icons.Outlined.DirectionsCar;"سرگرمی"->Icons.Outlined.SportsEsports;"عمومی"->Icons.Outlined.AccountBalanceWallet;"آرایشی بهداشتی"->Icons.Outlined.Spa;"بیمه"->Icons.Outlined.Security;"اشتراک"->Icons.Outlined.Subscriptions;else->Icons.Outlined.EditNote
}
internal fun reminderTitle(mode:String)=when(mode){"alarm"->tx("آلارم دقیق","Exact alarm");"both"->tx("اعلان و آلارم","Notification and alarm");else->tx("اعلان","Notification")}

internal fun frequencyTitle(rule:String)=when(rule){
 "once"->tx("یک‌بار","One-time")
 "quarterly"->tx("هر ۳ ماه","Every 3 months")
 "semiannual"->tx("هر ۶ ماه","Every 6 months")
 "yearly"->tx("سالانه","Yearly")
 "monthly"->tx("ماهانه","Monthly")
 else->if(isCustomRule(rule))tx("هر ${faNumber(repeatStepDays(rule))} روز","Every ${repeatStepDays(rule)} days") else tx("ماهانه","Monthly")
}
private fun frequencyHint(rule:String)=when(rule){
 "once"->tx("یک پرداخت در تاریخ انتخابی","A single payment on the chosen date")
 "quarterly"->tx("هر ۳ ماه یک‌بار پرداخت می‌شود","Paid once every 3 months")
 "semiannual"->tx("هر ۶ ماه یک‌بار پرداخت می‌شود","Paid once every 6 months")
 "yearly"->tx("هر سال در همان تاریخ","Paid once a year on the same date")
 "monthly"->tx("هر ماه در همان روز","Paid every month on the same day")
 else->tx("فاصلهٔ دلخواه بین پرداخت‌ها بر حسب روز","Your own number of days between payments")
}
private fun frequencyIcon(rule:String):ImageVector=when(rule){
 "once"->Icons.Outlined.Event
 "quarterly"->Icons.Outlined.Repeat
 "semiannual"->Icons.Outlined.Update
 "yearly"->Icons.Outlined.Autorenew
 "monthly"->Icons.Outlined.CalendarMonth
 else->Icons.Outlined.Tune
}

@Composable internal fun FrequencyDialog(selected:String,choose:(String)->Unit,dismiss:()->Unit){
 var customOpen by remember{mutableStateOf(isCustomRule(selected))}
 var days by remember{mutableStateOf(repeatStepDays(selected).takeIf{it>0}?.toString()?:"45")}
 val daysValue=days.toIntOrNull()
 val valid=daysValue!=null&&daysValue in 1..MAX_CUSTOM_DAYS
 AlertDialog(onDismissRequest=dismiss,shape=RoundedCornerShape(24.dp),containerColor=MaterialTheme.colorScheme.surface,
  title={Text(tx("تکرار پرداخت","Payment frequency"),fontWeight=FontWeight.Bold,fontSize=19.sp)},
  text={LazyColumn(Modifier.heightIn(max=420.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
   items(repeatRules){rule->SelectionOption(frequencyTitle(rule),frequencyHint(rule),frequencyIcon(rule),!customOpen&&rule==selected,{choose(rule)})}
   item{SelectionOption(tx("تنظیم دستی","Custom interval"),frequencyHint("days:1"),Icons.Outlined.Tune,customOpen,{customOpen=true})}
   if(customOpen)item{
    OutlinedTextField(days,{days=it.filter(Char::isDigit).take(4)},Modifier.fillMaxWidth(),singleLine=true,
     label={Text(tx("هر چند روز یک‌بار؟","Repeat every how many days?"))},
     suffix={Text(tx("روز","days"))},
     isError=days.isNotEmpty()&&!valid,
     supportingText={Text(tx("عددی بین ۱ تا ${faNumber(MAX_CUSTOM_DAYS)}؛ مثلاً ۴۵ یا ۷۰","A number from 1 to $MAX_CUSTOM_DAYS, for example 45 or 70"))},
     keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),shape=RoundedCornerShape(15.dp))
   }
  }},
  confirmButton={if(customOpen)TextButton({choose(customDaysRule(daysValue?:1))},enabled=valid){Text(tx("ثبت","Apply"))} else TextButton(dismiss){Text(tx("بستن","Close"))}},
  dismissButton={if(customOpen)TextButton(dismiss){Text(tx("بستن","Close"))}})
}

@Composable internal fun SelectField(label:String,image:ImageVector,onClick:()->Unit,modifier:Modifier=Modifier,enabled:Boolean=true){
 Surface(onClick=onClick,enabled=enabled,modifier=modifier.heightIn(min=48.dp),shape=RoundedCornerShape(12.dp),color=if(enabled)MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.4f),border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant)){
  Row(Modifier.padding(horizontal=12.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically){Icon(image,null,Modifier.size(20.dp),tint=MaterialTheme.colorScheme.primary);Spacer(Modifier.width(8.dp));Text(label,Modifier.weight(1f),fontSize=13.sp,fontWeight=FontWeight.Medium,maxLines=1,overflow=TextOverflow.Ellipsis);Icon(Icons.Outlined.ExpandMore,null,Modifier.size(18.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant)}
 }
}

@Composable internal fun CategoryDialog(selected:String,saved:List<CustomCategory>,choose:(String)->Unit,remove:(CustomCategory)->Unit,dismiss:()->Unit){
 AlertDialog(onDismissRequest=dismiss,shape=RoundedCornerShape(24.dp),containerColor=MaterialTheme.colorScheme.surface,
  title={Text(tx("نوع پرداخت","Payment type"),fontWeight=FontWeight.Bold,fontSize=19.sp)},
  text={LazyColumn(Modifier.heightIn(max=360.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
   items(builtInCategories){category->SelectionOption(lx(category),null,categoryIcon(category),selected==category,{choose(category)})}
   if(saved.isNotEmpty())item{Text(tx("دسته‌های من","My categories"),Modifier.padding(top=6.dp,start=4.dp),fontSize=11.sp,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.onSurfaceVariant)}
   items(saved,key={it.name}){custom->SelectionOption(custom.name,null,icon(custom.iconKey),selected==custom.name,{choose(custom.name)},trailing={IconButton({remove(custom)},Modifier.size(32.dp)){Icon(Icons.Outlined.DeleteOutline,tx("حذف دسته","Delete category"),Modifier.size(18.dp),tint=MaterialTheme.colorScheme.error)}})}
   item{SelectionOption(lx(CUSTOM_CATEGORY),tx("یک دستهٔ جدید بساز؛ برای دفعات بعد ذخیره می‌شود","Create a new category; it is saved for next time"),Icons.Outlined.AddCircleOutline,selected==CUSTOM_CATEGORY,{choose(CUSTOM_CATEGORY)})}
  }},confirmButton={TextButton(dismiss){Text(tx("بستن","Close"))}})
}

@Composable internal fun ReminderDialog(selected:String,choose:(String)->Unit,dismiss:()->Unit){
 AlertDialog(onDismissRequest=dismiss,shape=RoundedCornerShape(24.dp),containerColor=MaterialTheme.colorScheme.surface,
  title={Text(tx("روش یادآوری","Reminder style"),fontWeight=FontWeight.Bold,fontSize=19.sp)},
  text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
   listOf("notification","alarm","both").forEach{mode->SelectionOption(reminderTitle(mode),when(mode){"alarm"->tx("هشدار صوتی در ساعت انتخابی","An audible alert at your chosen time");"both"->tx("هم اعلان، هم هشدار صوتی","A notification plus an audible alert");else->tx("یادآوری در نوار اعلان گوشی","A reminder in your notification tray")},if(mode=="alarm")Icons.Outlined.Alarm else if(mode=="both")Icons.Outlined.NotificationsActive else Icons.Outlined.Notifications,mode==selected,{choose(mode)})}
   Text(tx("اعلان روز موعد برای همهٔ پرداخت‌ها فعال می‌ماند.","The due-date notification stays enabled for every payment."),fontSize=10.sp,lineHeight=16.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(top=4.dp))
  }},confirmButton={TextButton(dismiss){Text(tx("بستن","Close"))}})
}

@Composable private fun SelectionOption(title:String,description:String?,image:ImageVector,selected:Boolean,onClick:()->Unit,trailing:(@Composable ()->Unit)?=null){
 Surface(onClick=onClick,shape=RoundedCornerShape(12.dp),color=if(selected)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,border=BorderStroke(1.dp,if(selected)MaterialTheme.colorScheme.primary.copy(.5f)else MaterialTheme.colorScheme.outlineVariant)){
  Row(Modifier.fillMaxWidth().padding(10.dp),verticalAlignment=Alignment.CenterVertically){Icon(image,null,Modifier.size(20.dp),tint=if(selected)MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary);Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(title,fontSize=13.sp,fontWeight=if(selected)FontWeight.Bold else FontWeight.Medium,color=if(selected)MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface);description?.let{Text(it,fontSize=10.sp,lineHeight=15.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}};if(selected){Spacer(Modifier.width(6.dp));Icon(Icons.Outlined.CheckCircle,null,Modifier.size(18.dp),tint=MaterialTheme.colorScheme.primary)};trailing?.invoke()}
 }
}
