# KING Plus v10.0.73 — Party stale-cleanup race fix (TEST)

- After the five-minute reconnect grace, host cleanup now re-reads each stale member inside a Firestore transaction before removing it.
- The transaction deletes a member and that same member’s owned seats together. If reconnect refreshed `lastSeenAt` after the earlier scan, the fresh timestamp prevents removal.
- Keeps v10.0.71 cross-category room search and the v10.0.70 Android 16 document-URI image crash fix.
- Version code 352; version name `10.0.73-party-cleanup-race-fix`.

Source checks and full Android CI build must pass before APK delivery. Two-phone room/reconnect, voice/video, provider login, payment and game-round acceptance still need live-device/backend testing.
