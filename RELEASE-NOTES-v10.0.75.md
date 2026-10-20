# KING Plus v10.0.75 — People follow notification identity (TEST)

- People/Social Follow now saves the signed-in user's display name in the follow record, bounded to 60 characters. Discover Follow already does this.
- The existing server follow trigger can use that name in the notification instead of showing the generic “KING user” fallback. Push delivery still depends on the Cloud Function being deployed and the followed account having a valid FCM token.
- Keeps the Android 16 media URI crash fix, cross-category Party search, and transactional stale-member/seat cleanup.
- Version code 354; version name `10.0.75-social-follow-name`.

Source regression checks and full Android CI build must pass before APK delivery. Two-device push, login, Party voice/video, production payment and game-round acceptance still require live services/devices.
