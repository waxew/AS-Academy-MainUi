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
