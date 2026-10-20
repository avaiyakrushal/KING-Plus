# KING Plus v10.0.87 Profile Cloud Videos TEST

## Me → My Videos
- Added account-scoped cloud profile video uploads, with video MIME validation and a 10 MB limit.
- Signed-in devices can see the cloud list; the owner can play or delete uploads from the Me page.
- Kept the device-only saved video archive separate.
- Added owner checks for profile video records and a video MIME check for the Firebase Storage path. No local coin, XP or VIP reward is created.

## Verification
- Source regression checks cover the route, account checks, MIME/size limits, live list and delete flow.
- Firestore emulator test checks signed-in cross-device visibility and rejects another user's create/delete attempts.
- CI compiles and packages the Android TEST APK and checks package, version, signature and RTC libraries.
- Two-phone playback/privacy acceptance remains open. Firestore rules must be deployed before cloud records work against the production Firebase project.
