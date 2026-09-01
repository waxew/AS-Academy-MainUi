# AS Academy MainUi

`AS-Academy-MainUi` پوسته و Design System واحد تمام اپ‌های آموزشی AS Academy است.

## معماری جدید

```text
Course App (Kotlin / Python / PHP / ...)
          |
          v
      MainUi
          |
          v
       Core
          ^
          |
     MainCourse
```

- `AS-Academy-Core`: موتور، Navigation contract، Room، Progress، Quiz/Exercise/Search، updater و سرویس‌های مشترک.
- `AS-Academy-MainUi`: تمام UI/UX مشترک، Screenها، Drawer/Profile، Theme، Componentها و نمایش Course Package.
- `AS-Academy-MainCourse`: تمام درس‌ها، سرفصل‌ها، آزمون‌ها، Quizها، تمرین‌ها و پروژه‌ها.
- Course App: ورودی بسیار نازک شامل Application ID، نسخه، branding و capability اختصاصی.

## Viewer یکپارچه Android

ماژول `academy-viewer` یک APK مستقل برای مرور یکپارچه محتوای `AS-Academy-MainCourse` است. در CI آخرین Snapshot پوشه `courses/` داخل Assets برنامه قرار می‌گیرد؛ بنابراین Viewer به یک Course خاص hard-code نشده است.

قابلیت‌های فعلی Viewer شامل Dashboard دوره‌ها، جست‌وجوی سراسری، فیلتر نوع محتوا، Reader، RTL، Dark Theme، Drawer و Back navigation صحیح است.

## نسخه Web / PWA

پوشه `web/` نسخه وب Responsive و نصب‌پذیر MainCourse را نگه می‌دارد. Workflow مستقل `.github/workflows/pages.yml` آخرین `AS-Academy-MainCourse` را دریافت می‌کند، Search Index می‌سازد و سایت را روی GitHub Pages منتشر می‌کند.

نسخه وب شامل موارد زیر است:

- مشاهده یکپارچه همه Courseها
- جست‌وجوی سراسری و داخل هر دوره
- Lesson / Exercise / Quiz / Project / Glossary / Lab / Reference Reader
- پشتیبانی از JSON، Markdown و فایل‌های متنی/کدنویسی
- Bookmark محلی با `localStorage`
- Light / Dark Theme
- PWA و Service Worker برای Cache محتوای بازشده
- Responsive برای موبایل، تبلت و Desktop
- Sync دوره‌ای با MainCourse بدون نیاز به فهرست‌نویسی دستی دوره‌ها

## صفحات الزامی MainUi

- Home / Continue Learning
- Course Levels
- Chapters / Lessons
- Lesson Reader
- Search
- Bookmark
- Notes
- Exercises
- Quiz / Exam
- Projects / Capstone
- Glossary
- Progress / Achievement
- Settings
- Profile / Drawer
- About / Share / Update

تمام دکمه‌ها باید route یا action واقعی داشته باشند و placeholder تعاملی مجاز نیست.

## قواعد

1. UI مشترک نباید در Course Appها کپی شود.
2. تغییر قابل استفاده برای دو یا چند Course باید اینجا یا Core انجام شود.
3. MainUi محتوا را hard-code نمی‌کند؛ داده را از Course Package می‌گیرد.
4. RTL و فارسی first-class هستند.
5. Back navigation باید state و back stack را حفظ کند.
6. UI باید Offline-first باشد و وضعیت Progress/Bookmark/Note را از Core دریافت کند.
7. Android Viewer و Web Viewer باید از همان `AS-Academy-MainCourse` تغذیه شوند تا محتوای دو پلتفرم از هم منشعب نشود.
