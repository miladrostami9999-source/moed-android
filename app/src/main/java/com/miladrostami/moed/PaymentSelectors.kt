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

internal fun categoryIcon(category:String):ImageVector=when(category){
 "قسط"->Icons.Outlined.CreditCard;"وام"->Icons.Outlined.AccountBalance;"اجاره"->Icons.Outlined.Home;"قبض"->Icons.Outlined.ReceiptLong;"خرید"->Icons.Outlined.ShoppingCart;"آموزش"->Icons.Outlined.MenuBook;"درمان"->Icons.Outlined.HealthAndSafety;"حمل‌ونقل"->Icons.Outlined.DirectionsCar;"سرگرمی"->Icons.Outlined.SportsEsports;"عمومی"->Icons.Outlined.AccountBalanceWallet;else->Icons.Outlined.EditNote
}
internal fun reminderTitle(mode:String)=when(mode){"alarm"->tx("آلارم دقیق","Exact alarm");"both"->tx("اعلان و آلارم","Notification and alarm");else->tx("اعلان","Notification")}

@Composable internal fun SelectField(label:String,image:ImageVector,onClick:()->Unit,modifier:Modifier=Modifier){
 Surface(onClick=onClick,modifier=modifier.heightIn(min=48.dp),shape=RoundedCornerShape(12.dp),color=MaterialTheme.colorScheme.surface,border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant)){
  Row(Modifier.padding(horizontal=12.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically){Icon(image,null,Modifier.size(20.dp),tint=MaterialTheme.colorScheme.primary);Spacer(Modifier.width(8.dp));Text(label,Modifier.weight(1f),fontSize=13.sp,fontWeight=FontWeight.Medium,maxLines=1,overflow=TextOverflow.Ellipsis);Icon(Icons.Outlined.ExpandMore,null,Modifier.size(18.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant)}
 }
}

@Composable internal fun CategoryDialog(selected:String,choose:(String)->Unit,dismiss:()->Unit){
 AlertDialog(onDismissRequest=dismiss,shape=RoundedCornerShape(24.dp),containerColor=MaterialTheme.colorScheme.surface,
  title={Text(tx("نوع پرداخت","Payment type"),fontWeight=FontWeight.Bold,fontSize=19.sp)},
  text={LazyColumn(Modifier.heightIn(max=360.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
   items(listOf("قسط","وام","اجاره","قبض","خرید","آموزش","درمان","حمل‌ونقل","سرگرمی","عمومی","دلخواه")){category->SelectionOption(lx(category),if(category=="دلخواه")tx("نام دسته را خودت انتخاب کن","Create your own category")else null,categoryIcon(category),selected==category,{choose(category)})}
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

@Composable private fun SelectionOption(title:String,description:String?,image:ImageVector,selected:Boolean,onClick:()->Unit){
 Surface(onClick=onClick,shape=RoundedCornerShape(12.dp),color=if(selected)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,border=BorderStroke(1.dp,if(selected)MaterialTheme.colorScheme.primary.copy(.5f)else MaterialTheme.colorScheme.outlineVariant)){
  Row(Modifier.fillMaxWidth().padding(10.dp),verticalAlignment=Alignment.CenterVertically){Icon(image,null,Modifier.size(20.dp),tint=if(selected)MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary);Spacer(Modifier.width(10.dp));Column(Modifier.weight(1f)){Text(title,fontSize=13.sp,fontWeight=if(selected)FontWeight.Bold else FontWeight.Medium,color=if(selected)MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface);description?.let{Text(it,fontSize=10.sp,lineHeight=15.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}};if(selected){Spacer(Modifier.width(6.dp));Icon(Icons.Outlined.CheckCircle,null,Modifier.size(18.dp),tint=MaterialTheme.colorScheme.primary)}}
 }
}
