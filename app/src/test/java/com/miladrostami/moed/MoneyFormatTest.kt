package com.miladrostami.moed
import org.junit.Assert.*
import org.junit.Test

class MoneyFormatTest {
 @Test fun groupingUsesBaselineCommaInBothLanguages(){
  assertEquals("۱,۰۰۰,۰۰۰",groupedAmount(1_000_000,"fa"))
  assertEquals("12,000,000",groupedAmount(12_000_000,"en"))
  assertFalse(groupedAmount(12_000_000,"fa").contains('٬'))
  assertEquals("۰",groupedAmount(0,"fa"))
 }
 @Test fun wordsPreserveEveryDigit(){
  assertEquals("یک میلیون و یک تومان",amountWords(1_000_001,"fa"))
  assertEquals("دوازده میلیون و سیصد و چهل و پنج هزار و ششصد و هفتاد و هشت تومان",amountWords(12_345_678,"fa"))
  assertEquals("one million two hundred thirty-four thousand five hundred sixty-seven toman",amountWords(1_234_567,"en"))
  assertEquals("صفر تومان",amountWords(0,"fa"))
 }
 @Test fun formattedAndTypedAmountsRoundTrip(){
  assertEquals("12345678",amountInput("۱۲,۳۴۵,۶۷۸"))
  assertEquals("12345678",amountInput("١٢٬٣٤٥٬٦٧٨"))
  assertEquals("1000000",amountInput(groupedAmount(1_000_000,"fa")))
  assertEquals("0",amountInput(""))
 }
}
