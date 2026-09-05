# Supabase integration boundary

## Current status

AS Academy MainUi/Core currently have no Supabase client, project URL, API key, migration, or remote schema integration. Local user state is persisted by Core through `AcademyDatabase` and its repositories.

The Supabase projects currently visible through the connected account are not AS Academy projects. They must not be reused implicitly for Academy data.

## Intended architecture

AS Academy remains offline-first:

1. `MainUi` owns presentation and user interaction.
2. `Core` owns local persistence, progress rules, repositories, migrations, and backup contracts.
3. `AcademyDatabase` remains the local source of truth while the app is offline.
4. A future Supabase module may provide authenticated cloud sync/backup across devices.
5. Remote sync must sit behind a Core-facing sync contract instead of allowing screens to query Supabase directly.

## Security requirements

- Mobile clients may only use a Supabase publishable key.
- Secret/service-role keys must never be embedded in the Android APK, repository, CI logs, or course packages.
- Every exposed user-data table must have Row Level Security enabled.
- Policies must scope rows to the authenticated owner using `auth.uid()` and must include ownership checks for SELECT/INSERT/UPDATE/DELETE as appropriate.
- Authorization decisions must not trust user-editable metadata.
- Remote identifiers must include stable `courseId` and local entity IDs so multi-course data cannot collide.

## Candidate cloud data

A future sync schema can mirror user-owned state only, not bundled course content:

- lesson progress
- bookmarks
- user notes
- quiz history
- learning completion
- exercise drafts
- project progress
- achievements
- preferences/backup metadata

MainCourse content remains versioned package content and should not be duplicated into per-user cloud rows.

## Required before implementation

A dedicated Supabase project (or an explicitly approved existing project) must be selected. Before Android integration:

1. define Auth strategy;
2. create user-owned sync tables;
3. enable and test RLS;
4. run Supabase security/performance advisors;
5. obtain Project URL and publishable key;
6. add a Core sync abstraction and conflict/version strategy;
7. verify offline-first behavior and migration compatibility.

No remote schema or credential is created by this branch.
