# Current update: KING Plus 2.7.1

Online coins/gifts/rankings and notifications/moderation code added.
**Deployment and APK/device validation are pending.**
See `backend/SETUP.md` and `CHANGELOG-v2.7.1.txt`.

## Earlier 2.7 merge notes

# v2.7 update

Debug builds include free local mobile test login. Enter a valid mobile number,
then test OTP **123456**. No SMS is sent and phone ownership is not verified.
Release builds use the existing Firebase phone flow. Google/Facebook still
require provider configuration. See CHANGELOG-v2.7.txt for merge scope and validation.

# KING Plus v2.5

This version advances the v2.1 Family & Agency prototype and replaces the fake mobile test PIN flow with Firebase-ready authentication.

## Changes
- Real Firebase Phone Authentication flow (SMS OTP)
- Google sign-in wired to Firebase Authentication
- Test PIN `123456` removed
- Firebase setup checks with clear in-app messages when config is missing
- Family & Agency screens from v2.1 retained
- Existing Party, room, profile, wallet, missions and local demo features retained

## Firebase status / remaining setup
- The previously supplied `google-services.json` is included in `app/google-services.json`.
- Its Android package matches `com.kingplus.social`.
- Enable **Phone** in Firebase Authentication and allow the required SMS region(s) for real OTP.
- Add SHA-1 and SHA-256 fingerprints for Android app verification.
- The supplied config currently has no OAuth client entry, so Google sign-in still needs Google provider/SHA configuration and then a refreshed `google-services.json`.

The Gradle project uses Firebase Android BoM 34.19.0, Firebase Auth, Google Play services Auth 22.0.0 and Google services Gradle plugin 4.5.0.

## Facebook
The Facebook button no longer performs a fake local login. It displays a setup notice until Meta App credentials and Firebase Facebook provider configuration are added.

## Note
Family/agency and most social data are still local prototype data. A production backend, moderation, account storage, live rooms and server-side permissions are still required for a production social app.


## v2.5 full feature pass
See `CHANGELOG-v2.5.txt` and `FEATURE_STATUS.md` for the expanded UI, local feature behavior, and the production backend work that still requires external services.
