# Mento

اپلیکیشن آفلاین و امن مدیریت اقساط، وام، اجاره و پرداخت‌های دوره‌ای.

## اصول پروژه

- رایگان و بدون سرور اجباری
- ذخیره اطلاعات در Room روی گوشی
- ذخیره رسیدها با Storage Access Framework
- بکاپ و بازیابی محلی در قالب ZIP
- ورود Google اختیاری؛ برنامه بدون حساب هم کار می‌کند
- رابط فارسی و تاریخ شمسی
- ساخت APK با GitHub Actions

## ساخت APK

هر Push روی شاخه `main` یک Debug APK در بخش Actions تولید می‌کند. برای ساخت محلی:

```bash
gradle assembleDebug
```

## قابلیت‌های نسخه فعلی

- صفحه خانه، فیلتر وضعیت و موعد بعدی
- ثبت اقساط ماهانه و پرداخت‌های یک‌باره
- انتخاب‌گر گرافیکی تاریخ شمسی
- دسته‌بندی و آیکون دلخواه
- جزئیات و نمودار پیشرفت هر تعهد
- ثبت پرداخت و پیوست تصویر یا PDF رسید
- تقویم شمسی و گزارش مالی
- اعلان و آلارم دقیق با درخواست مجوزهای لازم
- بکاپ و بازیابی محلی شامل داده‌ها و رسیدها
- بکاپ قابل‌انتقال با رمزگذاری AES-256 و رمز انتخابی کاربر
- ویرایش امن آیتم، جست‌وجو و تأیید حذف
- انتخاب ساعت یادآوری و Snooze ده‌دقیقه‌ای آلارم
- پیش‌نمایش داخلی تصویر و صفحه اول PDF رسید
- حفظ اطلاعات هنگام خروج یا تغییر حساب Google

## فعال‌سازی ورود Google

ورود Google با Credential Manager پیاده‌سازی شده و فقط به تنظیم OAuth نیاز دارد:

1. در Google Auth Platform یک پروژه و OAuth consent screen بسازید.
2. یک OAuth Client از نوع Android برای پکیج `com.miladrostami.moed` و SHA-1 کلید امضای ثابت ایجاد کنید.
3. یک OAuth Client از نوع Web application بسازید.
4. مقدار Web Client ID را در Secret مخزن GitHub با نام `GOOGLE_WEB_CLIENT_ID` ذخیره کنید.

ورود Google اختیاری است؛ تمام قابلیت‌های اصلی بدون حساب و اینترنت کار می‌کنند و خروج از حساب داده‌های محلی را حذف نمی‌کند.

## ساخت APK امضاشده و فعال‌سازی Google

### ۱. ساخت کلید دائمی

روی رایانه‌ای که Java/JDK نصب است اجرا کنید:

```bash
keytool -genkeypair -v -keystore mento-release.jks -alias mento -keyalg RSA -keysize 2048 -validity 10000
keytool -list -v -keystore mento-release.jks -alias mento
```

فایل و رمز را در جای امن نگه دارید؛ بدون این کلید امکان انتشار آپدیت با همان امضا وجود ندارد.

### ۲. Secretهای GitHub

در مسیر `Settings > Secrets and variables > Actions` این Secretها را بسازید:

- `SIGNING_KEYSTORE_BASE64`: محتوای Base64 فایل `mento-release.jks`
- `SIGNING_STORE_PASSWORD`: رمز keystore
- `SIGNING_KEY_ALIAS`: مقدار alias واقعی کلید شما (`mento` یا مقداری که قبلاً ساخته‌اید)
- `SIGNING_KEY_PASSWORD`: رمز کلید
- `GOOGLE_WEB_CLIENT_ID`: شناسه Web OAuth که در مرحله بعد ساخته می‌شود

در PowerShell ویندوز می‌توانید Base64 را با این فرمان کپی کنید:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("mento-release.jks")) | Set-Clipboard
```

### ۳. تنظیم Google Auth Platform

1. یک پروژه Google Cloud ایجاد و صفحه رضایت OAuth را با Audience نوع External تنظیم کنید.
2. تا پیش از انتشار، ایمیل‌های آزمایش‌کننده را در Test users اضافه کنید.
3. OAuth Client نوع Android را با پکیج `com.miladrostami.moed` و SHA-1 خروجی فرمان `keytool -list` بسازید.
4. OAuth Client نوع Web application بسازید و Client ID آن را در Secret با نام `GOOGLE_WEB_CLIENT_ID` قرار دهید.
5. از تب Actions، گردش‌کار Build Android APK را دوباره اجرا کنید. خروجی `mento-release-apk` نسخه امضاشده و دارای ورود Google است.

داده‌های مالی به حساب Google آپلود نمی‌شوند؛ حساب فقط برای هویت و پروفایل است و پایگاه داده، رسیدها و بکاپ روی دستگاه باقی می‌مانند.
