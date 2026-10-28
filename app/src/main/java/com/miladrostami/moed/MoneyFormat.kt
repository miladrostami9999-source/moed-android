package com.miladrostami.moed

/** ASCII commas are intentional: the Persian locale separator is drawn above the baseline. */
fun groupedAmount(value:Long,language:String=AppLanguage.code):String {
 val ascii=java.lang.String.format(java.util.Locale.US,"%,d",value)
 return if(language=="en")ascii else ascii.map{if(it in '0'..'9')"۰۱۲۳۴۵۶۷۸۹"[it-'0']else it}.joinToString("")
}

fun amountInput(value:String):String=value.mapNotNull{it.digitToIntOrNull()?.toString()}.joinToString("").trimStart('0').ifEmpty{"0"}

fun amountWords(value:Long,language:String=AppLanguage.code):String {
 if(language=="en")return englishAmount(value)+" toman"
 if(value==0L)return "صفر تومان"
 val units=listOf("","یک","دو","سه","چهار","پنج","شش","هفت","هشت","نه","ده","یازده","دوازده","سیزده","چهارده","پانزده","شانزده","هفده","هجده","نوزده")
 val tens=listOf("","","بیست","سی","چهل","پنجاه","شصت","هفتاد","هشتاد","نود")
 val hundreds=listOf("","صد","دویست","سیصد","چهارصد","پانصد","ششصد","هفتصد","هشتصد","نهصد")
 val scales=listOf("","هزار","میلیون","میلیارد","تریلیون","کوادریلیون","کوینتیلیون")
 fun group(n:Int):String {
  val parts=mutableListOf<String>();if(n>=100)parts+=hundreds[n/100]
  val rest=n%100
  if(rest in 1..19)parts+=units[rest] else if(rest>=20){parts+=tens[rest/10];if(rest%10>0)parts+=units[rest%10]}
  return parts.joinToString(" و ")
 }
 var remaining=value.coerceAtLeast(0);var scale=0;val parts=mutableListOf<String>()
 while(remaining>0){val n=(remaining%1000).toInt();if(n>0)parts.add(0,group(n)+(if(scale==0)"" else " "+scales[scale]));remaining/=1000;scale++}
 return parts.joinToString(" و ")+" تومان"
}
