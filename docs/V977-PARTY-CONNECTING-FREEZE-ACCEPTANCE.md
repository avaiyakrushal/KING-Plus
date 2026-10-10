# KING Plus v9.7.7 — Party "Verifying your account and room access" freeze

## Reproduced in source
The user screenshot shows `Connecting to Party… Verifying your account and room access`, exactly the UI from `PartyActivity.showJoiningRoom964()`.
In v9.7.6, `joinMemberThenOpen891()` starts asynchronous Firestore reads and a member write but has no timeout or cancel control on the fullscreen connecting page.
A Firestore request with no timely callback, or a null members snapshot, leaves the user stuck on the loading page indefinitely.
Some Firebase error handlers previously showed a dismissible alert over the unchanged loading screen.

## Implemented in v9.7.7
- Session-safe 18-second watchdog for each room join. Stale callbacks for an older room, account or request cannot reopen the abandoned room.
- A visible loading spinner and immediate `Cancel and return to Party` button.
- Clear failure screen with `Retry secure connection` and `Back to Party list` rather than an indefinitely frozen overlay.
- Use Firestore `Source.SERVER` to verify member, room and capacity rather than relying on stale offline disk snapshots.
- Show a specific failure for a null membership list; do not continue after an unreadable member document.
- Stop and invalidate timeout callbacks on successful join, lobby transition, room changes and Activity destruction.
- Restore the successfully compiled v9.7.6 source and retain all Mic/Seat, Member Presence, Follow, Ludo, Emoji, Wallet and Mobile OTP code.

## Tests
- 12 pure Java state checks for current user/room/generation, cancellation, success and elapsed-time boundary.
- Build-time source assertions for the exact timeout, Firestore reads, logout guards and previous features.
- Successful GitHub Android `assembleDebug` and published debug APK: https://github.com/avaiyakrushal/KING-Plus/actions/runs/38014429612

## Android phone acceptance
1. Install v9.7.7 **over** older KING Plus where signatures allow to keep settings/data.
2. Using one Google-signed-in account, create a public Party; using a different Google account on another phone, join its exact Room ID. Confirm both users appear.
3. If Firebase responds normally, the loading page should close to Party Room once membership is confirmed.
4. If Firestore hangs or permission is rejected, the screen must show a failure after 18 seconds (not a forever-spinner). Retry should issue a fresh request; Back should return to the Party list.
5. Turn off mobile data during Join. Confirm the UI can be cancelled; after reconnect, Retry should work.
6. Verify no old room callback opens after returning to Lobby or quickly joining another Room.
7. If the new screen explicitly says Firebase `PERMISSION_DENIED`, save the text/screenshot. This client-side timeout fix **does not grant missing Firebase permissions**; backend room Rules, auth identity and connected project still need checking.
8. Keep Party Mic, Seat requests, Online Ludo, Social Follow and VIP/Wallet functionality unaffected.

## Important
The successful APK build is not proof that the specific two-phone Firebase room-access issue has been resolved. Multiple physical devices, distinct accounts and deployed Firestore Rules need runtime verification. New code is a stability/diagnostic fix, not a permissions bypass. No new billing APIs were enabled.
