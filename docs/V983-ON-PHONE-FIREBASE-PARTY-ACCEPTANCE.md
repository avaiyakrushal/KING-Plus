# KING Plus v9.8.3 — Phone-first Firebase / Party Room Connection Test

## Built on last successful source
v9.8.2 verified source (not outdated tracked root app), including Party session callback isolation, free gifts/emoji, games, Live Ludo and Mic/Seat. No Firebase rule or Google Cloud billing changes.

## What was added
1. **Settings → Help & Feedback → Test Firebase / Party Room connection**
2. The same **Test Firebase / Party connection** shortcut on the Party Join failure screen.
3. Room ID input supports either exactly six digits (Friendly Room Code) or an exact case-sensitive Firestore Room document ID. It **rejects KING User ID pasted as a document path** if it is not a valid Room alias.
4. Non-destructive four-stage test: Android validated network flag; signed-in Firebase UID prefix and authenticated token refresh; Firebase Firestore network re-enable and authenticated **SERVER-only** profile/Room read; optional current member-document server read.
5. Distinguishes **PERMISSION_DENIED**, **UNAUTHENTICATED**, **UNAVAILABLE**, **DEADLINE_EXCEEDED**, **RESOURCE_EXHAUSTED** and missing/closed/private rooms. Does not claim a room joined merely because a cached document exists.
6. **Copy Test Report** shares a bounded report with build, room reference, short Firebase UID prefix and each server result. It never copies a user's Firebase ID token, OTP, full UID or password.
7. Does not write any room/member/seat state. Will not sign users out or spend diamonds. Firebase free-plan quotas still apply to reads.

## Phone acceptance — two different Google accounts
1. Install the same v9.8.3 APK on Phone A and B without uninstalling first, if Android confirms signing compatibility.
2. Sign in to *different* Google/Firebase accounts. Open Settings → Help & Feedback → **Test Firebase / Party Room connection** on both. Run without a Room Code: both should show valid Internet, Firebase Token refreshed and Firestore server reachable.
3. A creates a public Party and shares **Room Code** (not KING User ID); B pastes the code in the test field. Both should resolve the same room and Host UID prefix.
4. Before joining, B should display **not joined yet**; after joining through the actual Party screen, B should show **joined**. A can verify its own member state too.
5. If the Room does not appear, B should report **no Party Room with this code**. For an ambiguous six-digit code, use the exact full Room invitation/ID.
6. If membership fails, tap **Copy Test Report** and send the text, with the Android phone model and Wi‑Fi/mobile data status. Avoid sharing any OTP or payment details.
7. Re-test Party Mic/Seat/Gift/Emoji, room switching and 18 local games; the diagnostic must not start a new Party or generate phantom members.
8. Repeat after toggling Wi‑Fi/mobile data. A genuine Firebase Rules/permission error must be displayed, not hidden behind a local cache.

## Known limitations
- No two-phone physical run was performed by ChatGPT; Android compilation and 20 local helper tests are not proof of live server accessibility.
- Real SMS OTP remains blocked until the owner configures an approved SMS provider and independently hosted trusted backend.
- Paid Diamond Recharge remains **disabled** until an approved merchant and secure purchase verification are connected. No Google Cloud Billing was enabled for this build.
