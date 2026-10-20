# KING Plus v10.0.74 — Discover follow notification identity (TEST)

- Discover Follow now saves the signed-in user's display name in the follow record, bounded to 60 characters.
- The existing server follow trigger can use that name in the notification instead of showing the generic “KING user” fallback. Push delivery still depends on the Cloud Function being deployed and the follower target having a valid FCM token.
- Keeps the Android 16 media URI crash fix, cross-category Party search, and transactional stale-member/seat cleanup.
- Version code 353; version name `10.0.74-discover-follow-name`.

Source regression checks and full Android CI build must pass before APK delivery. Two-device push, login, Party voice/video, production payment and game-round acceptance still require live services/devices.
