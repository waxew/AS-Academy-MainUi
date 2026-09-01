# MainUi Integration Contract

## ورودی MainUi

MainUi باید فقط این داده‌ها را از میزبان دریافت کند:

- `courseId`
- Course Package provider
- Branding configuration
- Capability flags
- App version/update information

## خروجی/تعامل با Core

MainUi برای عملیات زیر از Core استفاده می‌کند:

- progress state
- lesson completion
- bookmarks
- notes
- quiz result/history
- search index
- settings
- achievements
- backup/update services

## ممنوع

- hard-code کردن درس Kotlin/Python/... در UI
- ساخت دیتابیس جدا برای هر Course
- پیاده‌سازی مجدد Quiz/Search/Progress در Course App
- navigation مستقل و ناسازگار در Course Appها

## Course App entry point

Course app فقط باید MainUi shell را با `courseId` و config خودش launch کند. در مرحله انتقال، adapter سازگاری با `AcademyCourseApp` موجود در Core حفظ می‌شود تا build اپ‌ها نشکند؛ پس از تثبیت MainUi، UI قدیمی Core به API/engine مشترک محدود می‌شود.
