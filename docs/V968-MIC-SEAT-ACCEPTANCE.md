# KING Plus v9.6.8 — Party Mic & Seat cross-phone acceptance

## Source-derived defects in v9.6.7
1. `requestSeat(no)` wrote `seat_requests/<uid>_<seatNo>`; deployed `firestore.rules` only allows `seat_requests/<auth.uid>`. This caused real Seat Requests to fail with `PERMISSION_DENIED`.
2. `toggleMic()` switched the local UI to ON and attached the native Jitsi voice engine **before** the Firestore seat update succeeded. Missing/changed/occupied seats could display a false Mic ON or start voice despite server rejecting the update.
3. The Seat snapshot listener and two duplicate Seat Lock listeners did not reject callbacks from an old room. A stale callback could overwrite `mySeat`, `micOn` or display incorrect locks after switching rooms.
4. Unspecified `micOn` fields were displayed as enabled due to `!Boolean.FALSE.equals(...)`, instead of requiring `Boolean.TRUE`.

## Implemented
- Seat requests are one document per signed-in UID, with bounded valid seat numbers; read-before-write detects already-pending request. All async actions check the Party room's authenticated UID + visit generation.
- Mic toggles are serialized, requiring the current user to own the current occupied Seat and a Firestore transaction to commit before activating UI/voice. A Mic-OFF action mutes native audio immediately while the write completes for privacy. Failed writes do not start voice.
- The Seat listener rejects stale room callbacks and treats missing mic status as OFF.
- One guarded Seat Lock listener updates both old and new UI states, reducing simultaneous Firestore listeners/memory.
- v9.6.7 member presence sessions, v9.6.6 Party crash guards, v9.6.5 Follow ID, v9.6.4 image memory cap and v9.6.2 Ludo turn controls remain.

## On-phone acceptance checks
Use distinct Google/Firebase accounts for each participant, not one account on several phones.

1. Phone A creates a public Party; phones B/C join via exact shared link. Confirm all 3 distinct UIDs appear.
2. B taps an empty/locked Mic Seat and requests it. A (host) sees one Seat Request under that exact user; B tapping again should say **request pending**, not create multiple requests.
3. A approves B's request. B's assigned seat appears on all phones with Mic **OFF** initially.
4. B taps Mic ON exactly once. It should become active only after Firestore acknowledges its Seat ownership; A/C see corresponding Mic ON status. B then taps Mic OFF; native audio must be muted immediately and other phones see OFF.
5. Host turns **Mute All** ON; nonmoderator B cannot enable Mic. Restore Mute All OFF and retry.
6. Host locks/unlocks a free Seat; A/B/C show the same lock state. Run 10 repetitions while switching rooms.
7. B leaves Party A while a Mic or Seat Request is pending and joins Party D. The old room must not override the current Mic state or show an old Seat Request confirmation.
8. On low-memory Android, keep Party open for 15 minutes and switch room/seat repeatedly. Report a new KING Plus crash report if any; native process termination requires separate profiling.
9. Regression: Follows/UID lookup, Party invite link, Online Ludo and Live Emoji are still functional.

## Confidence and limitations
Passing GitHub unit tests, static safety checks, and `assembleDebug` proves the APK can build and policy checks are present, **not** full device interoperability. No Bolo Hi proprietary code/assets were reused. Test & debug build; do not treat as production ready.
