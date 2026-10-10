# KING Plus v9.8.2 — Multi-phone Party Room Session Isolation

## Baseline and observed problem
Uses **compiled and GitHub-published v9.8.1 source**, not outdated tracked `app/`.
A previous user's screenshot showed Firestore `Failed to get document because the client is offline` and two phones failing to meet. v9.7.9 introduced secure `enableNetwork()` + token refresh + exact server Room lookup, but real device success is still not established.

Source inspection of v9.8.1 found **10 unguarded/stale Room listeners**. Firestore `ListenerRegistration.remove()` stops future listening but queued callbacks can still deliver after a Room is left, the same Room is re-entered, or a different account signs in. Before this fix:
- An old `selfMemberListener` callback could call `leaveRoom()` inside the *new* Room.
- An old `roomBanListener` could unexpectedly kick a user from a different Party.
- Old `roleListener` could re-render a different Party using former cohost permissions.
- Old `seatsListener`, `seatLocksListener`, `membersListener`, `seatRequestsListener` could replace current-seat, microphone, participants and invitation UI.
- Old `messagesListener` and `eventsListener` could display an unrelated Room's Chat, Gifts, Emoji/reactions.
- An old `roomListener` could apply closed, locked or Mute All from a previous Party.

## v9.8.2 changes
1. Capture immutable `roomId`, Firebase `uid`, `presenceGeneration967` and newly added `roomListenerGeneration982` for each set of Room listeners.
2. Guard **every Firebase Room listener** on all four captured values plus Firebase's current signed-in account and Activity liveness. Old callbacks do **nothing** if the Room or user changes.
3. Increment listener generation and reset the event snapshot + processed event IDs whenever old Room listeners are cleared.
4. Add **Copy connection report** to the Party join failure screen. The report includes installed APK version, a short Room code, a truncated UID, Android validated network flag and a bounded error; it does **not** include ID tokens, SMS OTP, Firebase API secrets, private full UID or passwords.
5. Keep v9.7.9 secure network re-enable, explicit retry and authorization checks; no local cache is allowed to impersonate a successful server join.
6. Preserve v9.8.1 games, Frames, Gifts, Emoji, VIP truth, v9.6.8 Mic/Seat safeguards, v9.7.6 wallet read and v9.7.5 alternate OTP setup.

## Testable Android phone acceptance — do not call these done until phones tested
- Install **the same v9.8.2 build** on two or three Android phones and authenticate using **two/three different Firebase accounts**. Same Gmail account on multiple phones is intentionally the *same user*, not a separate room member.
- A creates a Public Party. B/C join with the **exact full Party invitation** or six-digit **Room Code**, not the user's six-digit KING ID. Membership should show A/B/C simultaneously.
- Have B leave room 1 and enter room 2 repeatedly while A removes B from room 1. Verify **room 1's member removal never closes B's room 2**.
- Have A ban/unban B from room 1 while B enters room 2. Old ban callbacks must not affect room 2; do not circumvent actual room 2 bans.
- Re-enter the **same** Room rapidly. Host toggles cohost roles, mic/seat-lock, and sends Chat/Gifts/Emoji. Prior-session callbacks should not change the new UI.
- Turn Wi-Fi off/on and tap **Retry secure connection**. No Room is marked joined until Firebase confirms membership. A `PERMISSION_DENIED` error should not be bypassed.
- For a continuing Party join failure, tap **Copy connection report**, paste the report here along with the phone model and whether Wi-Fi or mobile data was active. Do not paste OTPs or payment credentials.
- Re-test existing 18 local games, online Ludo and six Party rounds, Frame Gallery sync and GIFTS/Emoji. Check lower-memory Vivo/Android 16 for unexpected app restarts.

## Cautions
- Compilation and 17 pure Java checks do **not** prove 2+ real devices are connected or fully stable.
- This APK still does **not** activate paid Google Billing, Razorpay recharge, real SMS OTP provider onboarding or Cash-Out. Do not tell users that Diamonds can already be purchased.
- No Google Cloud billing, secret, IAM role or additional Firestore rule has been enabled for this patch.
