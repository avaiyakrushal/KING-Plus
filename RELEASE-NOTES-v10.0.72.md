# KING Plus v10.0.72 — Party reconnect grace (TEST)

- Party host stale-member cleanup now uses a five-minute grace window. This reduces false room exits when a phone loses mobile data or Firestore takes time to reconnect.
- Keeps v10.0.71 cross-category Party search and the v10.0.70 Android 16 media URI crash fix.
- Version code 351; version name `10.0.72-party-reconnect-grace`.

Source checks and full Android CI build must pass before APK delivery. Two-phone reconnect, voice/video, provider login, payment and game-round acceptance still require live device/backend testing.
