# KING Plus v9.8.4 — Two-Phone Live Party Member Sync Monitor

## Built from latest verified v9.8.3 source
Uses the **last compiled v9.8.3 source artifact**, not the older tracked root Android app. Preserves the verified APK signing identity, 18 local games, online Ludo, FREE emoji and Gift effects, verified User ID/Follow, Mic/Seat controls and Firebase session guards.

## New diagnosis tool
Inside **Settings → Help & Feedback → Test Firebase / Party Room connection**:
1. Enter exact six-digit **Room Code**, or full case-sensitive Firebase Room ID, not the six-digit KING **User ID**.
2. Tap **Run Firebase / Party Connection Test**. It verifies signed-in Firebase token, network and Firestore SERVER room read.
3. If the Room exists and is not closed, tap **Watch Party Members (Live)**. A Firestore snapshot listener shows the actual current Room **member documents**, whether your own UID is among them and at most eight **redacted UID prefixes**.
4. Cached/offline snapshots are labeled **CACHED only** and must never be mistaken for a live server result. A server snapshot is labeled **LIVE server snapshot**.
5. Tap **Copy Test Report**; includes the redacted live result and prior test results without exposing a complete Firebase UID, Google token, OTP, phone number or private key.
6. Leaving the screen or rerunning test unregisters the listener. Callbacks from a previous Room, Firebase account or test generation cannot change the current UI.
7. This feature **reads only**, never creates, joins, bans, deletes or alters a Room. It does not start audio or charge money.

**Member documents are not a real-time list of audible/active callers**: an offline/disconnected phone's record may remain for some time. The diagnostics distinguish documented membership from genuine online presence.

## On-phone acceptance for 2–3 devices
1. Install the **same v9.8.4** signed APK on Phone A and B, using distinct Google/Firebase accounts.
2. Both use Help → Test Firebase Connection without a room: ensure Firebase token and Firestore SERVER read work.
3. A creates a *public* Party and shares the Room's code. B joins normally.
4. Both enter the same Room Code in Help, run the server test and tap Watch. A's watch and B's watch should both display two member records (or more if real users have joined) and **This Firebase account: PRESENT**.
5. B leaves and rejoins: their live member snapshots should update automatically. A's Room must remain accessible.
6. Disconnect B's data/Wi-Fi: snapshot may say **CACHED only**; it must not claim a fresh Firebase server read until network returns.
7. Create another Room and rerun the test: previous Room member callbacks must not appear in the new watch.
8. Copy reports from both phones and compare version, Room Code, UID prefixes and database membership without sharing private tokens.
9. Repeat Mic/Seat, Follow, Frame Gallery and Ludo checks. A green Gradle build is **not** proof of a successful real multi-device test.

## Billing status
- Firebase Phone SMS and paid Diamond Recharge still need an independently configured SMS provider/merchant backend. No Google Cloud billing changes, credit card, Blaze or live payment checkout were added.
- Existing Google/Gmail login and the prepared low-cost Mobile OTP screen remain available, but actual SMS delivery is not enabled just by installing the APK.
