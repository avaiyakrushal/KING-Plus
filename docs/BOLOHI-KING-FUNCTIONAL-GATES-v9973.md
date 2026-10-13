# BoloHi reference → KING Plus functional release gate (13 October 2026)

**Source of truth:** User's BoloHi v5.30.5 APK + two videos (reference behavior, *not* an installed two-user runtime audit); BoloHi's official Play Store description (https://play.google.com/store/apps/details?id=com.live.hey); actual signed KING v9.9.72 source and v9.9.73 TEST code. BoloHi copyrighted assets or private source are NOT copied.

## BoloHi visible / advertised behavior vs KING functional gap

| Priority | Reference behavior | What KING really implements | What still has to PASS |
| --- | --- | --- | --- |
| P0 | Voice Party, mic seats, cohost/admin, room password/invites, chat | Firestore room/members/8-10-12 atomic seats and RTC/Jitsi reconnect + mic consent code | **2 physical signed-in Android phones:** real bidirectional audio, remote mute without forced unmute, cohost seat assignment, password rejection, kick/rejoin, Wifi/4G toggles; verified RTC provider auth |
| P0 | Synchronous in-room team/versus minigames | Tic Tac Toe turns, roles + Spy/Werewolf voting, Draw word private, 13 ordinary Party score/choice modes and frozen live Ready roster | Full start→real moves→winner on both phones, reconnect and rematch for EACH named game; 2/4 submitted must not declare winner, 4/4 only, no ghost/spectator; full Ludo/Sheep Fight/Bingo mechanics not proven |
| P0 | Real receiver Gift animations/sounds/combo and wallet levels | Distinct GiftBurst sound, durable receiver ACK, network reconnect, playback-once history; no paid wallet | Android A sends 2 different Gifts to B, B hears 2 corresponding sounds and sees animations in order, B kills/reopens and no duplicate, lost network resumes, persistent receipt; real paid wallet requires external provider |
| P0 | Google/Facebook/mobile authentication | Partial Google auth and Firebase / provider integration | Two genuine devices and fresh accounts: SMS OTP delivery, Facebook OAuth, account linking, Firestore deployed rules matching tested source |
| P0 | Paid Diamonds/Crystal recharge and VIP privileges | Paid Recharge and VIP flags explicitly disabled | Payment merchant setup/receipts+refund webhooks, server-verified immutable wallet ledger, regulatory and store-billing requirements; no fake/manual client credit |
| P1 | User DP popup/Profile/Gift wall/Fame/Medals/Noble/Family | Social screens and partial Firestore entities | From a live Party user tap profile; verified online history, family roles, levels, gift wall synced across accounts |
| P1 | 3D avatar movement/virtual stage and interactive world | Basic visuals, no proven complete shared Unity-scale world | Real multi-avatar 3D synchronized movement, avatar editing, gestures, real-time stage, reasonable Android performance |

## What v9.9.73 actually changes

An ordinary Party round with four frozen live Ready players can no longer be marked a winner by the authentic Android client after only 2/4 or 3/4 have submitted. All 13 ordinary modes now expose all-participant progress, automatically settle when **every** frozen UID has a current verified move, and transactionally reread all corresponding game move docs before writing the result. In incomplete or disconnected games use **Cancel without a winner → Rematch** rather than invent a winner.

Source path: `tools/all-played-v9973/apply.py`; pure Java test `KingPartyAllPlayed9973Test.java`; multi-account Firebase test `all-players-finish-firestore.test.mjs`. Signed v9.9.73 build at Github Actions workflow `king-all-played-v9973.yml`.

## Manual four-phone/two-phone acceptance details

1. On two actual phones with distinct Firebase accounts, log in and enter same real Party Room; verify each signed-in UID is a current member.
2. A host creates 8-seat room, B accepts mic, test A→B and B→A real spoken words, Wi-Fi to mobile transition, app background/reconnect. Test cohost, lock seat, kick B and ensure access actually revoked.
3. For 4 Ready players (can use emulator for count but physical voice requires devices), run Wheel. After two submit ensure BOTH clients display **2/4 waiting**, no winner, Host Finish disabled. After four valid moves results equal on both clients, no duplicate finish; press rematch.
4. Disconnect B before sending Gifting; A sends two distinct free gifts; B recovers and plays each effect and unique sound at most once; verify receipt and no spending/credit duplication. Test process kill/reopen.
5. Verify real FCM/login/provider/Firestore rules were deployed to production project. No production release from CI emulator tests only.
6. Record Android model, APK versionCode, SHA256, Firebase project (redact sensitive), account UIDs (redact), test step PASS/FAIL, room+round IDs, screenshots or device logs.

**Release status:** v9.9.73 is TEST, PR draft; 100% BoloHi functional parity NOT verified. A malicious Host can still manipulate client-side randomness without a trusted server arbiter. Do not represent mini-games as secure wagering or fully anti-cheat.
