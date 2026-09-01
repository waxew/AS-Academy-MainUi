# AS Academy MainUi architecture

`AS-Academy-MainUi` owns the reusable visual layer and the wiring from UI actions to `AS-Academy-Core` repositories.

It does not own course content. Course-specific lessons, quizzes, exercises, projects and glossaries live in `AS-Academy-MainCourse`.

Shared responsibilities include lesson screens, catalog, search UI, progress state, mark-as-studied actions, bookmarks/favorites, quiz/exercise/project UI, drawer, settings, profile, about and design system.

A visible action is not considered implemented until it persists or executes through the appropriate Core repository/engine and survives recreation where persistence is expected.
