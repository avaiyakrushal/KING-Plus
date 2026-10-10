# KING Plus v9.8.5 — Party Connection Quick Test

## Implemented
- On the "Connecting to Party" verification page, the user can open Firebase / Room Connection Test directly, rather than wait for the existing 18-second Room join watchdog.
- Prior to opening the diagnostic, the app invalidates pending Room join and Firestore reconnect callbacks and returns to the Party lobby. An abandoned join must not unexpectedly reopen.
- The Firebase diagnostic takes the current Room document ID and remains read-only.
- The diagnostic now reports an actionable timeout after 22 seconds; a late Firebase callback is discarded rather than shown as a confirmed server connection.
- Keep the v9.8.4 read-only live member monitor and previously shipped features intact.
- These controls do NOT bypass server-side membership, private-room policies or Firestore Rules.

## Phone acceptance test — two different Firebase accounts
1. Install v9.8.5 over the earlier v9.8.4 on a compatible signed build. Do not uninstall the prior app just to update it.
2. Phone A creates a public Party and shares its Room invitation/code to Phone B. Log in using DIFFERENT Google/Firebase users.
3. On Phone B, tap Join and then Test Firebase / Room connection while the connecting screen is visible.
4. The Room ID should be prefilled in the test form. Confirm signed-in account, token refresh, Firestore SERVER response, room existence and member permissions.
5. If authentication, Room privacy or Firestore Rules reject access, a human-readable error must be displayed. The test should never join or change the Room.
6. Disable the network during the diagnostic. It should show a connection failure or an explicit timeout within roughly 22 seconds, not an indefinite spinner.
7. Use Copy Test Report to share sanitized results; no full UID, OTP, token or password should be copied.
8. Return to Party: no late callback from the abandoned join may open a Room. Retry Join after restoring Internet.
9. Verify Mic, Seat, invites, Chat, Ludo, Gifts and Party exit/rejoin with both phones.
10. On the diagnostic live member watch, distinguish cached data from server-confirmed data.

## Outstanding
The Android build and Java/source assertions check compilation and safety only. Testing the actual deployed Firestore backend on two separate physical phones is still required.
Actual SMS OTP and paid Diamond Recharge remain disabled until appropriately configured verified providers. No Google Cloud Billing changes.
