# KING Plus v9.8.6 – cached Firestore Room, server read failed

## Reported Android screen (10 Oct 2026)
Party Room 243970 failed after Firebase reconnect with the SDK message: "Failed to get document from server. (However, this document does exist in the local cache.)"

This is not proof the Room still exists on the SERVER. A Room must never be joined using a cached snapshot if the server has not verified membership, privacy/ban checks and capacity.

## Fix scope
- Show an accurate local-cache-only vs server-verified explanation after failure.
- Interpret Firestore permission denied, authentication expiry, rate/quota and unavailable/deadline conditions separately.
- Show the actual on-device Firebase project from FirebaseApp.getOptions().getProjectId in the safe read-only diagnostic screen. The intended project is king-plus-2f365.
- Do not disable Firestore Rules, trust local disk cache for entry, delete membership, enable billing or claim live server access without a two-phone test.
- Preserve the already implemented 18-second Party join timeout, 22-second diagnostic timeout, v9.8.4 live member watch and v9.8.5 connecting shortcut.

## Verification on the user's Android phone
1. Install verified v9.8.6 over v9.8.5. Don't uninstall and erase app state.
2. Open Settings > Help & Feedback > Test Firebase / Party Room connection, with no Room Code. It must show project king-plus-2f365, validated network, Firebase account, refreshed token, and Firestore SERVER profile read.
3. Enter the actual Room Code 243970 or host's exact full Room ID; run the test again.
4. If profile server test succeeds but Room fails: investigate Room document, project rules/privacy, owner, Room status and host-specific membership.
5. If even profile SERVER read fails: investigate Firestore service availability for king-plus-2f365, phone's Wi-Fi/mobile data, VPN/Private DNS, Firebase session and quota. Try mobile data and repeat.
6. If PERMISSION_DENIED appears: deployed security rules/access/account need correction; never replace SERVER with CACHE in the join flow.
7. Copy both phones' diagnostic reports; redact any private identifiers or secrets before sharing.
8. After connectivity is confirmed, use two DIFFERENT Firebase accounts on the same APK and same host Room, verify both members and seats live, then try microphone/chat.

## Status
CI Java helper tests and Android compilation do not validate remote Firebase availability. Physical two-phone proof remains pending, and server-side changes may be required. No billing changes.
