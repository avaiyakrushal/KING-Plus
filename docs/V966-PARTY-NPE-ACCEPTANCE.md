# KING Plus v9.6.6 — verified Party null room join NPE fix

## User supplied actionable crash
- Device: vivo V2575, Android 16
- Report timestamp: 2026-10-09 19:15:19
- Exception: `java.lang.NullPointerException: Provided document path must not be null`
- Exact stack: `PartyActivity.checkCrowdCapacity930(PartyActivity.java:682)` <- `joinMemberThenOpen891(...:678)` <- Firestore `OnFailureListener`.

## Confirmed race in v9.6.5
`joinMemberThenOpen891()` created a Firestore member query; when it failed, the delayed `OnFailureListener` called `checkCrowdCapacity930(write)`. Meanwhile `renderLobby()` could execute `roomId=null`. The capacity method read the mutable field with `db.collection("live_rooms").document(roomId)`, which throws when `roomId` is null. The capacity code also ignored whether the callback was for an old room or a now-closed Activity.

## Fix
- Capture a non-null Firebase room reference, active authenticated UID, and join generation token before any async callback starts.
- On every callback (member lookup, room capacity, member count, member write, fallback write), ignore stale responses after switching rooms, returning to lobby, changing accounts, or Activity destruction.
- Invalidate pending join callbacks when rendering the lobby or destroying the Activity.
- Use the originally captured `DocumentReference` for the entire capacity-check chain, not the mutable `roomId` field.
- Fail with an informative join error when the current room cannot be verified, instead of falling through to a failed or unauthorized join.
- Preserve v9.6.5 Follow/King ID improvements, v9.6.4 bitmap cache, and v9.6.2 Ludo safeguards.

## Automated checks
- 15 pure Java unit tests for valid join, null lobby room, wrong room/user, stale token, destroyed Activity, duplicate callbacks.
- CI source checks confirm `checkCrowdCapacity930` contains no `document(roomId)` and every async step checks the current join generation.
- Gradle `assembleDebug` must succeed before the APK is offered.

## Phone acceptance checks
1. Update directly to v9.6.6 without uninstalling if Android accepts the signature. Use real Google/Firebase sign-in.
2. Phone A creates a public room. Phone B joins using the full invite link, then leaves immediately, repeating 10 times.
3. During a slow network connection, tap Join, immediately Back or another lobby tab, then rejoin the same or a different room. There should be **no** `Provided document path must not be null` exception.
4. Two separately authenticated Google accounts must appear as two distinct members. Only the selected room is entered; no stale callbacks should pull a user into the previous room.
5. Repeat with 3+ phones, checking member count and that each account has a unique Firebase UID.
6. Confirm Follow/unfollow from Social and Public Profile updates one underlying relationship, and that Ludo/Emoji continue to run.
7. Remain in a Party for 15 minutes on a lower-memory phone and check for `Low memory kill`. This is a separate failure from the fixed NullPointerException; compilation alone cannot guarantee its elimination.

## No backend rule changes
v9.6.6 only updates Android logic and uses the already deployed existing Firestore rules. Two-phone runtime testing is still required.
