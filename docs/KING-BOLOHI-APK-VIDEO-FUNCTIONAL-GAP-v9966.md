# BoloHi v5.30.5 reference APK/video → KING Plus v9.9.66 functional acceptance matrix

Verified on 2026-10-13. This audit records **observed user-visible behavior**, not a claim that the reference APK was logged into and tested with multiple physical devices. Reference was supplied by user: `com.live.hey-5-30-5-arm64-v8a-android60.apk`, `1000011619.mp4`, `1000011621.mp4`. KING Plus base last full signed TEST run: https://github.com/avaiyakrushal/KING-Plus/actions/runs/38381989034, `versionCode 245`, draft PR https://github.com/avaiyakrushal/KING-Plus/pull/73.

## What was actually observed in user reference

**BoloHi 5.30.5 APK structure** (ZIP inspection, not copied into KING): 11 classes DEX files, Unity native libraries `libunity.so`, `libil2cpp.so`, WebRTC/multimedia-related native libraries, substantial animation resources. Presence of a library does **not** verify a specific runtime feature, but establishes this is not just XML screens.

**Video `1000011619.mp4`** sample observed:
- Discover recommendation and Follow list (35 sec).
- Member DP/profile (65–185 sec), about me, gift wall, medals, avatar background, Edit Profile (215 sec).
- VIP cards and VIP benefit levels including VIP0/VIP28 (275–305 sec).
- Family page, Diamonds/Crystal view (335–365 sec). Pages loading/blank in recording cannot prove functional completion.

**Video `1000011621.mp4`** sample observed:
- Home/Party room directory (9 sec).
- A voice Party with eight visible numbered mic seats, in-room chat and moderation announcement (28–86 sec).
- Emoji/Reactions (65 sec), upgrades/levels (122 sec).
- Gifting shop, category tabs, quantity/gift send, Weekly Gift Card (141–178 sec).
- Pick Me activity (197 sec), party tools/Play Center (253 sec).
- Room settings including Billboard, Atmosphere, Administrator, password settings, auto-invite to seats and Room Management (272 sec).
- Room backgrounds picker (291 sec).
These are visual references only; no independent validation that the reference account had ability to perform backend/payment/RTC actions.

**Official feature reference**: https://play.google.com/store/apps/details?hl=en&id=com.live.hey confirms real voice/video Party, Ludo, Sheep Fight, Werewolf/Spy/Draw/Bingo, 3D avatar/world, gifts, VIP, events.

## KING gap matrix (production PASS must be from actual action, not files/screens/buttons)

| Priority | BoloHi actual reference screen/action | KING v9.9.66 evidence | Exit criteria before declaring complete |
| --- | --- | --- | --- |
| P0 | Eight-seater Party with voice, emoji/chat and moderator | Firebase room/member/seat/mic states, Jitsi reconnect and mic consent implementation | 2 signed-in physical Android phones: A host creates, B joins + takes seat, A/B bidirectional mic voice, owner remotely **mute** only, no forced unmute; kick/leave/rejoin, background+Wi-Fi/mobile, unauthorized RTC join denied at provider |
| P0 | Party controls: room announcement, administrator, room management, automatic seat invites and passwords | Room Settings/Role UI and Firestore rules | Each admin/seat/password action immediately reflected on B; A/B reconnect persists; no stale admin or kicked access; remote unmute rejected |
| P0 | Real social game role secrecy, voting and drawing | v9.9.58–66 Firebase real Ready/roster/TTT, voting privacy, random Spy/Draw secrets, retry private invite | A/B and 3+ player Werewolf/Spy: roles private, vote valid participants, actual winner same on devices, deny ghost votes; Draw sync sketch and one private secret, word survives screen reopen and error Retry works |
| P0 | Ludo, Sheep Fight, Bingo, Crazy Zoo and team gameplay | Some game modules, not all game rules verified | Complete game from start to winner with human second player, turn and state server verified, disconnect/rejoin and rematch; unsupported games labeled TEST rather than real multiplayer |
| P0 | Gift category, gift combo/special sounds and receiver animation | Receiver GiftBurst, per-gift sound FIFO, durable receiver ACK and overflow handling through v9.9.63 | A sends gift #1/#2 rapidly to B; B sees two ordered effects and distinct correct sounds; real playback ACK survives app kill/restart; no duplicate spending or receipt forgery; verified on 2 phones |
| P0 | Diamond/Crystal, VIP upgradable tiers and privilege effects | TEST/free gifts, LIVE_RECHARGE=false and PAID_VIP_ACTIVE=false | Server-side verified payment, ledger idempotency, refund/chargeback, permissions, wallet read/writes protected in deployed production rules, private gift/VIP access enforcement; otherwise paid NO-GO |
| P1 | Profile tabs, gift wall, medals, charisma/noble levels | Some social profile UI/data | Member DP click from live Party leads to real profile; server-derived history, levels/medals and gift wall persist and sync across 2 accounts |
| P1 | Family, follow/recommended discovery, room atmosphere/backgrounds | Some Family/Discover/Room Settings UI | Create/join/leave Family, leader/admin changes, background/atmosphere on two phones; actual ranking and event rewards, no hardcoded fake values |
| P1 | 3D avatar editor/movement and virtual space | Basic visual elements, no proven full live 3D world | Real user-controlled 3D character customization, shared movement/gestures/world presence and low-end performance; no assets/source copied from BoloHi |
| P0 | Google/Facebook/mobile OTP auth and Android 16 reliability | Partial provider setup | Real OTP and account linking tests, Facebook provider, production Firebase rules review, Android 16 device crash/fresh install/update verification |

## Release gates
- **Automated tests:** GitHub Actions successful source+signed debug TEST APK (code and emulator); v9.9.66 fixes runaway private role Firebase fetch on screen re-render and adds explicit user Retry.
- **External physical checks:** two independently signed-in phones for Party RTC, full game + gift replay, Android process kill/re-entry: **not yet done**. Never call these PASS from emulator alone.
- **External provider / payment:** trusted RTC auth, production Firestore rules deployment, real OTP/Facebook credentials and payment provider/merchant account: **not verified**, paid features remain disabled.
- **No IP copying:** behavior references only. KING uses its own source/art/assets.
- **Priority:** physical A/B Party/RTC gate → game lifecycle and secure moves/votes → gift playback/ACK/wallet ledger → profile/Family/VIP/3D parity.
- Track P0 issues #63 Party RTC, #64 all games, #65 gifting, #66 wallet, #68 auth/stability; P1 #67 3D/Family/VIP.
- For each acceptance test record APK versionCode, SHA-256, Firebase deployment project/rules SHA, both Android models, real roomID/roundID (redact UIDs), expected/actual, PASS/FAIL, log/video evidence.

The next TEST iteration should not spend engineering time counting XML/drawables. It must close an actual gameplay/RTC/gift correctness gap and must not be called final until the external gates pass.
