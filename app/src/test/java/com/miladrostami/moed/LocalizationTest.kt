package com.miladrostami.moed

import com.miladrostami.moed.domain.faNumber
import com.miladrostami.moed.domain.persianMonthName
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Test

class LocalizationTest {
 @After fun reset(){AppLanguage.code="fa"}
 @Test fun englishLabelsAndNumbers(){
  AppLanguage.code="en"
  assertEquals("Pending",lx("در انتظار"))
  assertEquals("3 months",lx("سه‌ماهه"))
  assertEquals("123",faNumber(123))
  assertEquals("Aban",persianMonthName(8))
 }
 @Test fun changingLanguageRestoresPersian(){
  AppLanguage.code="en"
  assertEquals("Dark",lx("تیره"))
  AppLanguage.code="fa"
  assertEquals("تیره",lx("تیره"))
  assertEquals("۱۲۳",faNumber(123))
 }
 @Test fun customNamesArePreserved(){
  AppLanguage.code="en"
  assertEquals("هزینه شخصی من",lx("هزینه شخصی من"))
 }
 @Test fun writtenAmountsIncludeRemainders(){
  assertEquals("one million two hundred thirty-four thousand five hundred sixty-seven",englishAmount(1_234_567))
  assertEquals("twenty million",englishAmount(20_000_000))
  assertEquals("zero",englishAmount(0))
 }
}
