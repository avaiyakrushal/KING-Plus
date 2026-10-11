# KING Plus v9.9.8 + staged five-section source vs Bolo Hi v5.30.5 — A to Z gap audit (2026-10-11)

## Evidence and scope
- Bolo Hi reference: user's previously provided APK named `com.live.hey-5-30-5-arm64-v8a-android60(20261011-015217).apk` was located in their file library (186,863,101 bytes). A complete binary/runtime screen-by-screen inspection has **not** been performed. Use the public Bolo Hi feature descriptions and screenshots/videos previously supplied as the reference until actual runtime screenshots are tested.
- Bolo Hi public feature reference: [Google Play listing](https://play.google.com/store/apps/details?id=com.live.hey): 2D/3D chatrooms, adjustable face/outfit 3D avatar, virtual walking/decorating, live video/1:1 video, voice/KTV/theme rooms, Ludo, Sheep Fight, Werewolf, Spy, Draw & Guess, Bingo, Crazy Zoo, VIP/events/gifts.
- Latest *published, user-installable* KING Plus: [v9.9.8 APK](https://github.com/avaiyakrushal/KING-Plus/releases/tag/v9.9.8).
- Later **unreleased** development source: `work/king-plus-complete-five-sections`. [Combined ordered 5-patch Gradle validation SUCCESS](https://github.com/avaiyakrushal/KING-Plus/actions/runs/38130553711). [Integrated source artifact](https://github.com/avaiyakrushal/KING-Plus/actions/runs/38130689781). [Seat schema audit](https://github.com/avaiyakrushal/KING-Plus/actions/runs/38130809257). Android build != device acceptance.
- Unit/integration emulated Firebase Rules: 25/25 synthetic permission checks passed [run](https://github.com/avaiyakrushal/KING-Plus/actions/runs/38128178450). Production Firestore deployment and two-phone live acceptance still unconfirmed.
- No reproducible screenshots of every Bolo Hi screen paired with every KING screen; do not claim pixel-perfect UI parity.
- Status terminology: `CODE` means code exists but needs functional tests; `PARTIAL` means incomplete functionality; `MISSING` means key capability is not implemented; `BLOCKED` means external provider/service/configuration or owner authorization is required.

## 1. 3D World, Virtual Avatar and Video
| Target / expected action | Status | Final acceptance gate |
|---|---|---|
| OpenGL true depth-rendered 3D Party stage | CODE (staged) | Visually test on 2 Android devices, assess performance and 10 enter/leave cycles |
| 3D users appear on real occupied seats | CODE (staged) | A and B take/release seats, both see correct avatars and names; event listener removed on exit |
| 3D avatar head/face, hair, outfits and wardrobe | MISSING/PARTIAL | Personal avatar editor and original rigged model assets, persistent wardrobe sync |
| 3D walking/gestures/sitting/customizable space | MISSING | Multi-user synchronized position/movement, interactions, room furnishing, bounded data & performance |
| Multi-video room using existing Jitsi SDK | CODE (staged) | Two different UIDs actually publish/receive video, permission allow/deny, mute, rejoin, background |
| Secure private multi-video & 1:1 calling | BLOCKED | Authenticated provider/private room access, signaling/call invitation, tokens, termination, abuse and consent |
| Publisher/viewer live streaming | MISSING/UNVERIFIED | Live stream broadcast, safe viewer state, moderation, latency/reconnect tests |
| Low-end device 3D/video stability | UNVERIFIED | 10 iterations Room→Video→Game→Room, no crash/ANR, memory review |

## 2. Party Room and Voice Social
| Target / expected action | Status | Final acceptance gate |
|---|---|---|
| Live room create, search, code, invite and join | CODE | Same live room opened from KINGROOM/6-character invitation on two physical phones |
| Friend direct-room invite SERVER freshness | CODE (staged) | Closed/offline/nonmember room rejected; valid room joined |
| Member list, seats, DP, room profile, online status | CODE | Both devices show the same UID, DP, seat occupancy and status |
| Mic toggle/voice channel, queue, seat invite/lock | PARTIAL | Speaker A hears B; mic changes don't start duplicate calls; no echo leaks |
| Host/admin/cohost: assign/revoke | CODE | Rules enforce permissions; revoked moderator loses moderation access on phone B immediately |
| Kick, ban/unban, mute, announcements, billboard | CODE | Changes persist and immediately apply on second device; banned user cannot bypass |
| Private/password room access/abuse/cleanup | PARTIAL | Password access secure, nonmembers excluded, expired presence cleaned, no privilege escalation |
| Games and gifting in the same ongoing room | PARTIAL | Seamless return to voice with no duplicate session or white screen |

## 3. Multiplayer Games
| Target / expected action | Status | Final acceptance gate |
|---|---|---|
| 2/4 player Ludo board, pieces and turns | CODE | A+B real-time legal turns, capture, safe square, winner, rematch/reconnect |
| Ludo dice entropy | CODE (staged) | SHA256(timestamp sec+nano + room code + revision), 60k sanity tests pass; real server-authoritative fairness still missing |
| Real server-authoritative anti-cheat/game winner | MISSING | Trust boundary prevents client tampering, score forgery, paid prizes/illegal moves |
| Dice and Tic Tac Toe in Party | CODE | Same outcome/board from both devices; turn validation and cleanup |
| Werewolf and Spy role/vote/round logic | PARTIAL | Complete synchronized roles, timers, votes, outcomes and rejoin across devices |
| Draw & Guess live sketch/guess | PARTIAL | B sees A's strokes; guess/score/timer/winner synchronized with reconnection |
| Sheep Fight, Crazy Zoo, Bingo, mini-games | PARTIAL/DEMO | Actual interactive physics/strategy/game loops, not button-only scores |
| In-room games with uninterrupted voice | PARTIAL | Overlay/return leaves voice connected and stream permissions correct |

## 4. Gifts, Emojis, VIP, Noble and Diamonds
| Target / expected action | Status | Final acceptance gate |
|---|---|---|
| Distinct gift catalog (88) and action emojis (120) | CODE | Sender and receiver see gift identity, recipient, quantity, unique artwork/audio |
| 3D cinematic Gift/Kiss/Rocket/Car/Entrance | PARTIAL | Original 3D animation timeline, synced room effect, memory controls, accessibility mute |
| VIP Night unique synthetic sound | CODE (staged) | Dedicated ~1.45s fanfare tested with background/re-entry; no borrowed assets |
| Per-device Gift Audio ON/OFF | CODE (staged) | Party toggle persists; mic unaffected |
| VIP/Noble/Charisma/rank truthful entitlements | BLOCKED | Trusted server ledger and immutable grants, refunds/reversal, anti-fraud |
| Diamond Wallet + real Recharge/payment | BLOCKED | Approved billing provider, paid orders server validation, purchase receipt, replay protection, financial controls |
| Official events, rewards, family/fan club/ranking | PARTIAL | Realtime event scheduling, eligibility, secure reward fulfillment and abuse moderation |

## 5. Login, Friends, Search, Profile and Chat
| Target / expected action | Status | Final acceptance gate |
|---|---|---|
| Google auth | CODE | Two accounts A/B sign-in, persist on restart and route correctly |
| Facebook auth | BLOCKED/MISSING | Authorized Meta app, SDK, provider validation, account linking, cancellation |
| SMS/mobile OTP | CODE/UNVERIFIED | Real authenticated OTP on supported numbers, safe resend/rate limit, no crash |
| WhatsApp OTP | MISSING/BLOCKED | Approved Business messaging provider, template/consent and secure verification; no fake success |
| KING ID/UID Search/Discover/Follow/Follow Back | CODE | A finds B; reciprocal state correct from Firestore SERVER |
| Private messaging/inbox | CODE | A/B real messages across app restarts, delivery ordering, report/block and permissions |
| User DP/profile/collections, edit and sync | CODE/PARTIAL | Real server profile, image validation, proper level provenance, privacy and accurate follow count |
| HTTPS/size-bounded DP + server-owned identity | CODE (staged) | HTTPS-only, 3 MB cap, <=640px thumbnails; updates don't overwrite other device's edit |
| Block/report/content moderation/account deletion | PARTIAL/UNVERIFIED | Public reporting routes, block propagation, policy compliance and secure personal-data handling |
| 1:1 live video call | MISSING/UNVERIFIED | Actual P2P/RTC call signaling and media with auth, permissions, consent and private tokenization |

## A–Z non-feature release gates
1. **Design parity:** Compare Bolo Hi screenshots/videos with KING Plus on same size Android phones: Login, Home, Room, Seat grid, toolbar, Gift Shop, Game boards, Profile, frames, Settings; create distinct original graphics/logos, not proprietary copies.
2. **Firebase production:** Confirm exact deployed Rules, public_profiles indexes, follows/threads live data, roles, device UIDs, Storage/Auth/FCM and Security/App Check where applicable.
3. **Two actual devices:** Different users and Internet connections; ID→Follow→Message→Party Join→Seat→Mic→Game→Gift→Video; repeat after app restart and temporary network loss.
4. **Crash/ANR:** At least 10 room→gift→game→video→back loops; startup/login OTP, cold resume, low RAM, denied permissions, slow network, crash logs.
5. **Account and payment provider onboarding:** Meta, SMS, video RTC infrastructure, secure recharges; the assistant cannot independently authorize external merchant/business apps or fabricate credentials.
6. **User safety and privacy:** Terms, privacy policy, age limits, child-protection/report mechanisms, data retention/deletion, blocking and content moderation appropriate for public social video.
7. **Deployment:** Same package & signing certificate (upgrade without uninstall), Android compatibility, Play policy, permissions, proguard/resource testing, signed RELEASE artifact, checksum, verified final download URL.
8. **Honest completion:** Zero open blocking acceptance tests for the promised Bolo Hi feature scope. A Gradle green check is not 100% feature completeness.

## Decision
**NOT FINAL.** KING Plus is buildable and several feature anchors exist, but extensive gameplay, native 3D avatar editing/movement, production video calling, trusted VIP/recharge services, Facebook/WhatsApp auth, exact UI parity and physical two-device acceptance are unfinished. Do NOT infer a percentage from 12/17 code anchors; a CODE marker is not an accepted feature. Do not ship a falsely labeled final APK.
