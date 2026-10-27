package com.miladrostami.moed

import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.os.Build
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miladrostami.moed.domain.AlarmScheduler
import com.miladrostami.moed.ui.theme.MoedTheme

class AlarmActivity:ComponentActivity(){
 private var player:MediaPlayer?=null
 private val notificationId get()=intent.getLongExtra("id",1L)
 override fun onCreate(state:Bundle?){super.onCreate(state);if(Build.VERSION.SDK_INT>=27){setShowWhenLocked(true);setTurnScreenOn(true)}else{@Suppress("DEPRECATION") window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)};startSound();setContent{MoedTheme{AlarmView(intent.getStringExtra("title")?:"موعد پرداخت",{stopAlarm()},{snooze()})}}}
 private fun startSound(){runCatching{val uri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)?:RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);player=MediaPlayer().apply{setDataSource(this@AlarmActivity,uri);setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build());isLooping=true;prepare();start()}}}
 private fun stopAlarm(){getSystemService(NotificationManager::class.java).cancel(notificationId.toInt());finish()}
 private fun snooze(){AlarmScheduler.schedule(this,notificationId,intent.getStringExtra("title")?:"موعد پرداخت",System.currentTimeMillis()+10*60*1000L,true);stopAlarm()}
 override fun onDestroy(){player?.runCatching{stop()};player?.release();player=null;super.onDestroy()}
 @Suppress("DEPRECATION")
 @android.annotation.SuppressLint("MissingSuperCall")
 override fun onBackPressed(){/* Alarm must be explicitly stopped or snoozed. */}
}

@Composable private fun AlarmView(title:String,stop:()->Unit,snooze:()->Unit){Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(28.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Surface(shape=CircleShape,color=MaterialTheme.colorScheme.primaryContainer,modifier=Modifier.size(118.dp)){Box(contentAlignment=Alignment.Center){Icon(Icons.Outlined.Alarm,null,Modifier.size(58.dp),tint=MaterialTheme.colorScheme.primary)}};Spacer(Modifier.height(30.dp));Text("زمان پرداخت رسیده",fontSize=27.sp,fontWeight=FontWeight.ExtraBold,textAlign=TextAlign.Center);Spacer(Modifier.height(12.dp));Text(title,fontSize=18.sp,color=MaterialTheme.colorScheme.onSurfaceVariant,textAlign=TextAlign.Center);Spacer(Modifier.height(48.dp));Button(stop,Modifier.fillMaxWidth().height(58.dp),shape=RoundedCornerShape(18.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFC43D54))){Icon(Icons.Outlined.Stop,null);Spacer(Modifier.width(8.dp));Text("توقف آلارم",fontWeight=FontWeight.Bold)};Spacer(Modifier.height(12.dp));OutlinedButton(snooze,Modifier.fillMaxWidth().height(54.dp),shape=RoundedCornerShape(18.dp)){Icon(Icons.Outlined.Snooze,null);Spacer(Modifier.width(8.dp));Text("۱۰ دقیقه بعد")}}}
