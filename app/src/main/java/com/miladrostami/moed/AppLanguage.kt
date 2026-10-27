package com.miladrostami.moed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

object AppLanguage { var code by androidx.compose.runtime.mutableStateOf("fa") }
fun tx(fa:String,en:String):String=if(AppLanguage.code=="en")en else fa

fun englishAmount(value:Long):String {
 if(value==0L)return "zero"
 val small=listOf("zero","one","two","three","four","five","six","seven","eight","nine","ten","eleven","twelve","thirteen","fourteen","fifteen","sixteen","seventeen","eighteen","nineteen")
 val tens=listOf("","","twenty","thirty","forty","fifty","sixty","seventy","eighty","ninety")
 fun underThousand(n:Int):String=when {
  n<20->small[n]
  n<100->tens[n/10]+if(n%10==0)"" else "-"+small[n%10]
  else->small[n/100]+" hundred"+if(n%100==0)"" else " "+underThousand(n%100)
 }
 var n=value;val parts=mutableListOf<String>()
 for((scale,name)in listOf(1_000_000_000_000L to "trillion",1_000_000_000L to "billion",1_000_000L to "million",1_000L to "thousand")) {
  if(n>=scale){parts+=englishAmount(n/scale)+" "+name;n%=scale}
 }
 if(n>0)parts+=underThousand(n.toInt())
 return parts.joinToString(" ")
}
private val translations=mapOf(
 "قسط" to "Installment","وام" to "Loan","اجاره" to "Rent","قبض" to "Bill","خرید" to "Shopping","آموزش" to "Education","درمان" to "Health","حمل‌ونقل" to "Transport","سرگرمی" to "Entertainment","عمومی" to "General","دلخواه" to "Custom",
 "مالی" to "Finance","خانه" to "Home","خودرو" to "Vehicle","شخصی" to "Personal","تفریح" to "Leisure","سایر" to "Other",
 "کارت" to "Card","بانک" to "Bank","کیف پول" to "Wallet","رسید" to "Receipt","پس‌انداز" to "Savings","مبلمان" to "Furniture","آشپزخانه" to "Kitchen","لوازم برقی" to "Appliances","انرژی" to "Utilities","سوخت" to "Fuel","تعمیرات" to "Repairs","موتور" to "Motorbike","خوراک" to "Food","فست‌فود" to "Fast food","پوشاک" to "Clothing","ورزش" to "Fitness","موبایل" to "Phone","رایانه" to "Computer","سفر" to "Travel","هدیه" to "Gift","حیوانات" to "Pets","ویژه" to "Special"
,
"حساب خودت را بساز؛ اطلاعات مالی همچنان خصوصی روی همین گوشی می‌ماند." to "Create your account. Your financial data stays private on this phone.",
"ورود گوگل تنظیم نشده" to "Google sign-in is not configured",
"ورود انجام نشد" to "Sign-in failed",
"Mento اطلاعات مالی شما را به فضای ابری ارسال نمی‌کند." to "Mento does not upload your financial data to the cloud.",
"نام برای ساخت حساب الزامی است. حساب و اطلاعات فقط روی همین گوشی ذخیره می‌شوند." to "Your name is required. Your account and data are stored on this phone.",
"نام و نام خانوادگی را وارد کنید" to "Enter your full name",
"اطلاعات با موفقیت ذخیره شد" to "Payment saved",
"رسید ذخیره شد" to "Receipt saved",
"رسید حذف شد" to "Receipt removed",
"آیتم حذف شد" to "Payment deleted",
"مدیریت هوشمند پرداخت‌ها" to "Keep your payments on track",
"لوگوی Mento" to "Mento logo",
"ویرایش پرداخت" to "Edit payment",
"جزئیات را قدم‌به‌قدم وارد کنید" to "Enter your payment details",
"قسط بعدی" to "Next installment",
"تعداد باقی‌مانده" to "Installments left",
"مانده کل" to "Total outstanding",
"بستن جزئیات" to "Hide schedule",
"مشاهده برنامه کامل اقساط" to "View full installment schedule",
"تمام اقساط و رسیدهای مرتبط از فهرست حذف می‌شوند. این کار قابل بازگشت نیست." to "All related installments and receipts will be deleted. This cannot be undone.",
"مشاهده رسید" to "View receipt",
"حذف رسید" to "Remove receipt",
"افزودن رسید" to "Attach receipt",
"جایگزینی رسید" to "Replace receipt",
"برای این ماه پرداختی در این بخش نیست" to "No payments in this section for this month",
"برای این روز موعدی ثبت نشده" to "No payments due on this day",
"پرداخت‌شده در بازه" to "Paid during this period",
"در این بازه پرداخت ثبت‌شده‌ای ندارید" to "No payments recorded during this period",
"کاربر Mento" to "Mento user",
"اعلان‌ها فعال شدند" to "Notifications enabled",
"دسترسی اعلان داده نشد" to "Notification permission was not granted",
"بکاپ ذخیره شد" to "Backup saved",
"خطا" to "Something went wrong",
"فایل بکاپ معتبر نیست" to "Invalid backup file",
"حساب محلی" to "Local account",
"حساب Google متصل است" to "Google account connected",
"اتصال حساب Google" to "Connect Google account",
"اختیاری؛ داده‌های مالی همچنان محلی‌اند" to "Optional. Financial data stays on this phone.",
"OAuth تنظیم نشده" to "Google sign-in is not configured",
"ورود ناموفق" to "Sign-in failed",
"خارج شدید؛ داده‌ها حذف نشد" to "Signed out. Your data is still on this phone.",
"مجوز اعلان" to "Notification permission",
"یادآوری سررسیدها" to "Payment reminders",
"اعلان‌ها فعال‌اند" to "Notifications are enabled",
"مجوز آلارم دقیق" to "Exact alarm permission",
"اجرای هشدار در ساعت انتخابی" to "Remind me at the selected time",
"بهینه‌سازی باتری" to "Battery optimization",
"اگر یادآورها دیر اجرا می‌شوند، محدودیت باتری Mento را بررسی کنید" to "If reminders are delayed, check Mento’s battery restrictions.",
"بکاپ رمزدار" to "Encrypted backup",
"ذخیره روی گوشی" to "Save on this phone",
"بازیابی بکاپ" to "Restore backup",
"از فایل Mento" to "From a Mento backup file",
"ورود یا خروج Google داده‌های مالی را حذف نمی‌کند. پیش از حذف برنامه بکاپ بگیرید." to "Signing in or out of Google keeps your financial data. Create a backup before uninstalling Mento.",
"پروفایل ذخیره شد" to "Profile saved",
"حداقل ۶ کاراکتر" to "Use at least 6 characters",
"ادغام، اطلاعات فعلی را نگه می‌دارد. جایگزینی، اطلاعات فعلی را پاک می‌کند." to "Merge keeps your existing data. Replace removes your existing data.",
"بکاپ با اطلاعات فعلی ادغام شد" to "Backup merged with existing data",
"بکاپ جایگزین شد" to "Backup restored",
"پیش‌نمایش رسید" to "Receipt preview",
"پیش‌نمایش این فایل در دسترس نیست؛ می‌توانید آن را با برنامه دیگری باز کنید." to "Preview unavailable. You can open this file in another app.",
"برنامه‌ای برای بازکردن فایل پیدا نشد" to "No app found to open this file",
"بازکردن کامل" to "Open file",
"زمان پرداخت رسیده" to "Your payment is due",
"توقف آلارم" to "Stop alarm",
"۱۰ دقیقه بعد" to "Snooze 10 minutes",
"موعد پرداخت امروز است" to "Your payment is due today",
"زمان پرداخت نزدیک است" to "Your payment is coming up",
"یادآوری پرداخت" to "Payment reminder",
"آلارم سررسید" to "Payment alarms",
"یادآوری سررسید" to "Payment reminders",
"این ماه چیزی نمونده ✨" to "All paid for this month ✨",
"همه" to "All",
"در انتظار" to "Pending",
"معوق" to "Overdue",
"انجام‌شده" to "Paid",
"انجام شده" to "Paid",
"پرداخت شده" to "Paid",
"تسویه" to "Settled",
"ماهانه" to "Monthly",
"یک‌بار" to "One-time",
"۷ روزه" to "7 days",
"سه‌ماهه" to "3 months",
"سالانه" to "Yearly",
"تومان" to "toman",
"آلارم دقیق" to "Exact alarm",
"اعلان و آلارم" to "Notification and alarm",
"اعلان" to "Notification",
"اعلان آرام" to "Notification",
"روز موعد" to "On due date",
"ذخیره" to "Save",
"ظاهر برنامه" to "Appearance",
"تیره" to "Dark",
"روشن" to "Light",
"هماهنگ با گوشی" to "System default",
"پرداخت" to "Paid",
"قسط" to "Installment",
"بستن" to "Close",
"رسید" to "Receipt",
"ثبت پرداخت" to "Mark as paid",
"ویرایش" to "Edit",
"حذف" to "Delete"
)
fun lx(value:String):String=if(AppLanguage.code=="en")translations[value]?:value else value
