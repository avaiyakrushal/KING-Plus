# KING Plus vs BoloHi — Functional Parity & Release Gates
Audit date: 2026-10-13 IST | KING version v9.9.61 TEST | PR #62 | successful build 38372293823
Status: DRAFT/TEST. Neither final BoloHi parity nor production launch has been proven.

## Evidence and interpretation
- BoloHi official feature reference: https://play.google.com/store/apps/details?id=com.live.hey (listing updated May 27, 2026): live voice/video party rooms, karaoke, gifts, Ludo, Sheep Fight, Werewolf, Spy, Draw & Guess, Bingo, 3D avatars, official channels, VIP and events.
- Historical user reference files in Library: com.live.hey-5-30-5-arm64-v8a-android60.apk; 1000011619.mp4; 1000011621.mp4; 1000011626.mp4. The full reference app has NOT been run live in this audit. Historical BoloHi APK and current listing may differ.
- KING Plus v9.9.61 verified signed-debug APK/source GitHub Actions run: https://github.com/avaiyakrushal/KING-Plus/actions/runs/38372293823; draft https://github.com/avaiyakrushal/KING-Plus/pull/62.
- KING v9.9.55 Gift receipt retry; v9.9.56 mic consent; v9.9.57 voice reconnect; v9.9.58–61 roster, role privacy, fair Tic Tac Toe and persisted Host role. CI PASS means code/build/emulator pass, not physical two-device acceptance.

## Feature-by-feature comparison
| Priority | BoloHi capability | KING Plus evidence | Remaining acceptance | Status |
|---|---|---|---|---|
| P0 | 2D live voice room, create/join, seats | Firebase room + native Jitsi code, reconnect, mic consent tests | Two physical phones, live bidirectional audio, private room security, kick/leave/rejoin and interruption | Phone/prod NOT VERIFIED |
| P0 | Host/Admin/Moderators, remote seat management | Firestore roles, seat rules, emulator tests | Two-phone roles and correct permission enforcement; no remote microphone forced ON | Partial emulator |
| P0 | Live video party and calls | Video room layouts/logic | Real mutual video with consent, recovery, provider-level authorization and revocation | NOT VERIFIED |
| P0 | Ludo/Sheep and online competition | Source modules exist; prior feature patches | Full official game rules, real opponents, synchronized turns and winner/rematch, disconnection | NOT VERIFIED complete |
| P0 | Werewolf, Spy, Draw & Guess, Bingo | Real social Ready/member/role start; v9.9.61 private Host role | End-to-end voting/reveal/scoring, synchronized sketch/guess, complete Bingo rules and rematch on A/B | PARTIAL |
| P0 | Tic Tac Toe, RPS, Dice, Coin | Firestore server transactions, roster, immutable turn and result tests | Physical A/B replay, duplicate/reconnect, same final winner on both devices | Emulator only |
| P0 | Gifts to a real recipient with animation and unique sound | Sound FIFO, GiftBurst, recipient receipt and retry | A sends twice, B sees 2 effects/sounds in order; durable acknowledgement after app killed; no double deduction | PARTIAL |
| P0 | Diamond/Beans/Crystal and paid gifts | Paid recharge/VIP flags OFF | Server-authoritative wallet, verified payment, refund, duplicate/replay protection, production rules | NO-GO paid |
| P1 | 3D avatar, clothing/face and movement | Partial visuals/3D features | Fully controllable rigged avatar, persisted customization, 3D shared room movement & performance | NOT VERIFIED |
| P1 | Profile DP, charisma, noble/level, gift wall | Partial profile/social features | Tap another room user's DP and see authentic real-time profile, badges, gift records and level | NOT VERIFIED complete |
| P1 | Family, official channels, VIP benefits, events | Partial modules/UI | Membership roles, stable rankings, enforced privileges and fair official rewards | NOT VERIFIED complete |
| P0 | Google, phone/WhatsApp OTP, Facebook | Some auth flows and Google login reports | Actual provider success/wrong/expiry/retry, WhatsApp routing, Facebook cancel/success | NO-GO incomplete |
| P0 | App stability/navigation | Signed v9.9.61 debug build successful | Three cold starts, all navigation, non-destructive upgrade, Android 16 tests, error recovery | NOT VERIFIED phone |

## What DONE means
Do not use XML count, Drawable count, number of buttons, animations, successful Gradle build or emulator PASS as parity percentage. Every feature needs real signed-in end-to-end action, Firestore/RTC security, expected other-phone result, negative/abuse tests, no crash and retained test proof.

## Priority release gates and exact acceptance
1. P0 Firebase: audit and version production rules in the intended Firebase project; denied cross-account reads/writes for roles, seats, rounds, invitations, gift receipts and wallets. Emulator rules alone do not deploy production.
2. P0 RTC: only room-authorized users may join provider-side conference; revoke access after kick/leave/logout. Public Jitsi URL alone cannot establish that; two actual phones must exchange audio with opt-in and mute protection.
3. P0 Party: A host and B member create/join, claim seat simultaneously, mute, assign/revoke admin, open member DP, exit and reconnect via Wi-Fi/mobile. Both show same state; no ghost member.
4. P0 Games: Ready, start, correct turn, invalid/duplicate action, result, cancel/rematch, reconnect across two phones for every advertised game. Backend result and anti-cheat must govern real-value outcomes.
5. P0 Gift: A sends gift x2 to B while both in Party; B sees exact matching two animations and sounds; B sends receiver-only receipts only after actual display; network loss/reopen does not double-credit or fake delivery.
6. P0 Wallet: keep LIVE_RECHARGE=false and PAID_VIP_ACTIVE=false until server payment verification, atomic immutable ledger, refund/reversal tests, duplicate receipt protection and production rules are deployed.
7. P0 Identity/stability: real provider Login/OTP tests, Android 16 crash navigation, device permissions denied/allowed, lifecycle, upgrade preserves data and signer.
8. P1 3D, family, VIP, noble/level, profiles and events: actual persisted and synchronized features, not sample numbers or static graphics.

## Merge and launch policy
- Development TEST: permitted with stated limitations and artifact version.
- Do not merge as '100% final' or launch paid Party/RTC before gates pass; PR #62 remains draft.
- A/B physical phone, account/provider setup, production Firebase and trusted payment backend cannot be proven by GitHub Actions alone.
- Preserve KING Plus original implementation; do not copy BoloHi proprietary code, images or trademarks.

## Acceptance evidence row
Feature | Android phone A/B/model | APK SHA256/versionCode | Firebase project | room & round | account IDs redacted | actions | expected | actual | PASS/FAIL/BLOCKED | timestamp and video/log
Reference links: https://github.com/avaiyakrushal/KING-Plus/pull/62 | https://github.com/avaiyakrushal/KING-Plus/actions/runs/38372293823 | https://play.google.com/store/apps/details?id=com.live.hey