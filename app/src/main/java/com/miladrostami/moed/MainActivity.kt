package com.miladrostami.moed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miladrostami.moed.ui.theme.MoedTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MoedTheme { MoedApp() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun MoedApp() {
    var showAdd by remember { mutableStateOf(false) }
    Scaffold(
        topBar = { TopAppBar(title = { Text("موعد", fontWeight = FontWeight.Bold) }, actions = { IconButton(onClick = {}) { Icon(Icons.Outlined.Notifications, null) } }) },
        floatingActionButton = { FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Outlined.Add, null) } },
        bottomBar = { NavigationBar { NavigationBarItem(true, {}, { Icon(Icons.Outlined.Home, null) }, label = { Text("خانه") }); NavigationBarItem(false, {}, { Icon(Icons.Outlined.CalendarMonth, null) }, label = { Text("تقویم") }) } }
    ) { padding -> HomeScreen(Modifier.padding(padding)) }
    if (showAdd) AddPaymentSheet(onDismiss = { showAdd = false })
}

@Composable private fun HomeScreen(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("سلام 👋", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF20212B)), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) { Text("باقی‌مانده کل تعهدات", color = Color.White.copy(.65f), fontSize = 12.sp); Text("۰ تومان", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(18.dp)); Text("اولین پرداخت خود را اضافه کنید", color = Color.White) }
        }
        Text("پرداخت‌های من", fontWeight = FontWeight.Bold)
        Box(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(18.dp)).padding(22.dp), contentAlignment = Alignment.Center) { Text("هنوز پرداختی ثبت نشده است", color = Color.Gray) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun AddPaymentSheet(onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }; var monthly by remember { mutableStateOf(true) }; var openDate by remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("افزودن پرداخت", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("عنوان") })
            OutlinedTextField("", {}, Modifier.fillMaxWidth(), label = { Text("مبلغ") })
            OutlinedButton({ openDate = true }, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.CalendarMonth, null); Spacer(Modifier.width(8.dp)); Text("انتخاب تاریخ سررسید") }
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(monthly, { monthly = true }, SegmentedButtonDefaults.itemShape(0, 2)) { Text("ماهانه") }
                SegmentedButton(!monthly, { monthly = false }, SegmentedButtonDefaults.itemShape(1, 2)) { Text("یک‌بار") }
            }
            if (monthly) OutlinedTextField("۱۲", {}, Modifier.fillMaxWidth(), label = { Text("تعداد اقساط") })
            Button({}, Modifier.fillMaxWidth()) { Text("ذخیره") }
        }
    }
    if (openDate) PersianDatePickerDialog(onDismiss = { openDate = false })
}

@Composable private fun PersianDatePickerDialog(onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(onDismiss) { Text("تأیید") } }, dismissButton = { TextButton(onDismiss) { Text("انصراف") } }, title = { Text("انتخاب تاریخ شمسی") }, text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { Text("آبان ۱۴۰۵", fontWeight = FontWeight.Bold); Text("نسخه بعدی: شبکه کامل تقویم شمسی، تغییر ماه و انتخاب روز", color = Color.Gray) } })
}
