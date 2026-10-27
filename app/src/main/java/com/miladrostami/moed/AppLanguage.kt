package com.miladrostami.moed

object AppLanguage { var code:String="fa" }
fun tx(fa:String,en:String):String=if(AppLanguage.code=="en")en else fa
fun lx(value:String):String=if(AppLanguage.code!="en")value else mapOf(
 "قسط" to "Installment","وام" to "Loan","اجاره" to "Rent","قبض" to "Bill","خرید" to "Shopping","آموزش" to "Education","درمان" to "Health","حمل‌ونقل" to "Transport","سرگرمی" to "Entertainment","عمومی" to "General","دلخواه" to "Custom",
 "مالی" to "Finance","خانه" to "Home","خودرو" to "Vehicle","شخصی" to "Personal","تفریح" to "Leisure","سایر" to "Other",
 "کارت" to "Card","بانک" to "Bank","کیف پول" to "Wallet","رسید" to "Receipt","پس‌انداز" to "Savings","مبلمان" to "Furniture","آشپزخانه" to "Kitchen","لوازم برقی" to "Appliances","انرژی" to "Utilities","سوخت" to "Fuel","تعمیرات" to "Repairs","موتور" to "Motorbike","خوراک" to "Food","فست‌فود" to "Fast food","پوشاک" to "Clothing","ورزش" to "Fitness","موبایل" to "Phone","رایانه" to "Computer","سفر" to "Travel","هدیه" to "Gift","حیوانات" to "Pets","ویژه" to "Special"
)[value]?:value
