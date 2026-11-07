# KING Plus v10.0.107 — TEST source

This source bundle is for `com.kingplus.social`, Android version code `386`
and version name `10.0.107-source-package-fix`. It continues the existing
KING Plus Android application; it is not a new project.

## Current project handoff — 2026-11-07

- Existing repository: [avaiyakrushal/KING-Plus](https://github.com/avaiyakrushal/KING-Plus)
- Current work branch: `work/king-bolohi-parity-start`
- Current review: [PR #181](https://github.com/avaiyakrushal/KING-Plus/pull/181) (draft)
- Home includes original KING Plus discovery shortcuts and preserves individual
  Party Stage, Family, VIP & Rank, KTV, PK, Match and Games destinations.
- Live room data continues to come from the existing Firebase-backed room list;
  no demo rooms are inserted.
- Multi Video verifies Firebase room membership from the server before joining
  Jitsi, starts with microphone and camera muted, and gates media controls on
  the authenticated video-seat owner. Activity teardown now attempts a
  UID-verified seat release.

## Validation status

The GitHub Actions workflow
`.github/workflows/kingplus-android-verification.yml` runs the documented
Python source checks and builds a debug APK with JDK 17, Gradle 8.9 and Android
SDK 35. The latest completed verification before this README update passed all
source checks and produced a debug APK artifact.

Source checks and a successful debug build do **not** establish:
- successful installation or rendering on a physical Android phone;
- working two-way voice/video between two separate phones;
- production privacy/security for Jitsi room access;
- production billing, release signing, or Play Store readiness.

Multi Video currently uses the public `meet.jit.si` service after the app's
Firebase membership check. This client-side check is not equivalent to
server-side Jitsi authentication; do not treat the room as a private,
production-secure conference until an authenticated Jitsi deployment or
equivalent server-side access control is verified.

## Run source checks

From the repository root:

```sh
python3 tools/android16-media-uri-v10070/check_source.py
python3 tools/four-pages-v10048/check_source.py
python3 tools/party-gifts-me-v10064/check_source.py
python3 tools/home-me-party-v10067/check_source.py
python3 tools/discover-follow-name-v10074/check_source.py
python3 tools/social-follow-name-v10075/check_source.py
python3 tools/discover-hot-pages-v10076/check_source.py
python3 tools/party-rejoin-choice-v10077/check_source.py
```

## Build a local debug APK

This repository currently does not include a Gradle wrapper. Use JDK 17,
Gradle 8.9 and Android SDK 35, then run:

```sh
gradle --no-daemon :app:assembleDebug
```

The debug APK is written to
`app/build/outputs/apk/debug/app-debug.apk`. The current Gradle release
configuration still uses the debug signing configuration; this is not an
original-certificate or production-signed release APK.

## Earlier v10.0.77 changes retained

The Android 16 crash report showed `ImageView` lazily opening a document URI
without a current provider grant. App image views use a bounded eager decoder
that catches revoked/missing URI access and leaves a fallback. Party search
spans room categories and keeps cursor paging for additional results. Party
cleanup rechecks member presence and removes stale membership and seats in a
transaction, so reconnect cleanup does not eject a user based only on an old
snapshot. Discover and Social follow records include the follower display name,
and the Discover Hot list sorts and pages by server level rank.

The release gate state remains recorded in `release/final-release-evidence.json`.
No feature should be marked passed solely because its source or UI exists.
