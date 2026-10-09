# KING Plus v9.6.7 — Stable Party presence / 3-phone acceptance

## Why this release
A previously delivered user crash was `PartyActivity.checkCrowdCapacity930()` with `document(null)` while a Firebase join callback arrived after a room was left. Version 9.6.6 fixed that one join path. Inspection of the next successful source still found related live-presence reliability issues:
- `unregisterMember()` immediately deleted `members/<Firebase UID>` regardless of whether another phone/newer room visit had replaced the record.
- Host `cleanupStaleRoom910()` had async callbacks referring to mutable `roomId` and owner state. A late callback could read or delete seat data from a subsequently selected room.
- `heartbeat900()` and `ensureRoomMembership900()` could update room UI or retry writes after the user returned to the lobby.
- The members list used an optional, stale `uid` payload rather than the authoritative Firestore document ID.

## Source changes
1. Each fresh live-room visit captures a random `sessionId` and increments `presenceGeneration967`. Full/fallback/private member documents include both `sessionId` and `deviceId`.
2. `unregisterMember()` now uses a Firestore transaction; it removes the member only when the stored session equals the departing session (or legacy stored device matches). It cannot remove a newer member visit just because the Firebase UID matches.
3. `heartbeat900()`, fallback writes, and automatic member rejoin ignore callbacks from another room, Firebase account or destroyed Activity.
4. Host member and seat cleanup use a captured room reference and generation, server-only member/seat reads and age checks. No empty batches or cross-room seat cleanup.
5. Live member UI uses `DocumentSnapshot.getId()` as the Firebase UID and discards stale room listener callbacks.
6. `appVersion` in member payload is the actual Android APK version, not hard-coded v9.3.1.
7. Previously delivered code for v9.6.6 null-room crash, v9.6.5 Follow, v9.6.4 memory optimization and v9.6.2 Ludo is retained.

## Automated checks
- 23 pure-Java tests cover same-room callbacks, stale room/account generations, Activity teardown, session and legacy-device-safe deletion, and timestamp logic.
- CI source checks cover exact callback guards, server reads, transaction safety and previous features.
- GitHub Android Gradle build must succeed before claiming an APK is ready.

## Acceptance checks using phones, no PC
1. Install the v9.6.7 APK on 3 phones without uninstalling earlier versions if Android permits the signature.
2. Sign in using **three different Google/Firebase accounts**. In Social/Discover copy each verified Firebase UID, and ensure all three differ.
3. Phone A creates a public Party; B/C join via A's exact shared room invitation or six-digit code, and confirm each shows 3 distinct members by UID.
4. B leaves Room A and quickly enters another Party Room. Repeat 10 times during intermittent/slower network. Room A must not list B after leave; Room B must not receive A's delayed events.
5. B rejoins A, immediately leaves, then rejoins again; the previous exit's cleanup transaction must not remove the newer session.
6. Optional same-account comparison: A and another phone log into the SAME Google account. They intentionally have the same Firebase UID and remain **one account/member**, not two separate members. The older phone leaving must not delete the newer session of the same account.
7. Open Party Chat and Seat/Mic controls across phones, and leave/join repeatedly. Any crash report needs the installed build name and stacktrace.
8. Hold a room open for 15 minutes on a lower-memory phone; confirm whether Android reports `Low memory kill`. Native memory remains a separate stability issue until proven fixed.
9. Verify Follow, User ID, Online Ludo and Emoji are unaffected.

## Backend and release note
- No new Firestore rules or credentials are needed; this release changes only Android member metadata and client behavior under existing rules.
- CI passing is not equivalent to multi-device runtime passing. The current release remains a **debug APK** until tested.
