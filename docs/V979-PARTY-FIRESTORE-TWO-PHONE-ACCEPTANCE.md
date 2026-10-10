# KING Plus v9.7.9 — “Party Room connection failed: client is offline” repair

## Reported failure (user's Android screenshot, Oct 10, 2026)
The user sees a full-screen panel:
```
⚠ Party Room connection failed
Cannot verify signed-in Party membership
Failed to get document because the client is offline.
```
Two different phones cannot meet in the same Party. This is a **Firestore transport/network failure during membership verification**. It is distinct from a denied Firestore permission, invalid Room ID or application process crash. The screenshot alone cannot prove whether the root cause is a phone network, Firebase project configuration, SDK transport, VPN/Private DNS or Firestore service; device testing is required.

## Source-confirmed root of inadequate recovery
In v9.7.8, `PartyActivity.joinMemberThenOpen891()` requests `/live_rooms/<roomId>/members/<user.uid>` using `Source.SERVER`. A Firestore `UNAVAILABLE/client is offline` error went immediately to a failure panel.
The previous `Retry secure connection` action **simply invoked the same Firestore reads again**. No Firebase `enableNetwork()` or token refresh was performed.
The pre-existing network callback only refreshed the Lobby when connectivity returned; it did **not recover an interrupted room join** or revalidate active room membership.

## Fixed in Android v9.7.9
1. Distinguish retryable `UNAVAILABLE`, `DEADLINE_EXCEEDED` and offline transport failures from Firestore `PERMISSION_DENIED` / missing rooms; never bypass authorization.
2. Retry first calls `FirebaseFirestore.enableNetwork()`, refreshes the **current Firebase Auth ID token**, reads the same exact Room ID from `Source.SERVER`, confirms it exists/is not closed and only then starts member join.
3. One automatic recovery attempt on the first **genuine transport failure** during membership read (not an unbounded retry loop).
4. When Android validated internet returns, the active room revalidates its member entry; a pending room join may retry once.
5. 18-second timeout for recovery, with cancellation when leaving the room, switching accounts, navigating to lobby or destroying the Activity.
6. Display actionable transport/sign-in hint plus short room code and Firebase UID prefix. Do not show a room as joined until membership write is acknowledged by Firestore.
7. Retains existing v9.7.7 timeout screen, v9.7.8 signal reporter, voice/Mic/Seat, Follow, Ludo and earlier Firebase presence code.

## Verify using two real Android phones
- Install **the exact same v9.7.9 APK** on both phones. Use **two separate Google/Firebase accounts**, not the same Gmail account. One Firebase UID means one logical member across devices.
- Ensure both phones have working Wi-Fi/mobile data and no VPN/Private DNS blocking Firebase. If an error says client offline, switch Wi-Fi to mobile data and tap **Retry secure connection**.
- Phone A creates a **Public** Party Room. Copy the **full Room invite/link**, not merely the six-digit user ID. Note the full Room ID and host identity.
- Phone B follows A's exact Room invite or the room's six-digit **room code**, not A's six-digit **user ID**. Both phones must display **the same Room ID**.
- Confirm membership count 2 and distinct user entries A/B, both mic OFF. Repeat join/leave 10 times. Mic/Seat requests must update both phones.
- Return to Lobby, disconnect/reconnect internet, rejoin Room. Retry must not leave indefinite spinner, must not open previous room, and must not incorrectly claim a successful join from local cache.
- If `PERMISSION_DENIED` persists, verify deployed `firestore.rules` in project `king-plus-2f365`, signed-in UID, public/private Room access and Firebase bans.
- If `client is offline` persists despite an internet connection, test mobile data instead of Wi-Fi, turn off VPN/Private DNS, confirm Google Sign-In works, and share a fresh screenshot **with APK version and which of the two phones fails**.
- No script can confirm real-world two-phone connection; only actual signed-in physical devices can validate it.

## Automated checks
`KingPartyConnection979Test.java` verifies offline classification, forbids treating permission errors as transport recoveries, validates account/room generation guards, and checks distinct UIDs.
GitHub Actions Android compile verifies the patch with prior features. A green Build is not multi-device runtime confirmation.
