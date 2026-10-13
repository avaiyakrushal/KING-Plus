# KING Plus — એક જ Final 16-Gate Release, અધૂરી APKને Final નહીં કહીએ

**Baseline:** v9.9.78 TEST, Android Build + Java/Firestore emulator tests PASS, [Actions run 38405932696](https://github.com/avaiyakrushal/KING-Plus/actions/runs/38405932696). Work is retained in draft PR chain, not merged to `main`. This baseline **is not a fully working Bolo Hi-style production app**.

**User's direction:** Include ALL original sixteen required functional areas in a **single integrated final acceptance and delivery**. Do not ask for approval after every small UI patch, do not count XML/Drawable parity as finished gameplay, do not call a TEST artifact a FINAL release.

## Verification required before Final APK/AAB

| # | Functional gate | Must actually work | Current gate |
| - | - | - | - |
| 1 | login | Google, Facebook and real SMS OTP, genuine fresh accounts | **UNVERIFIED** (external/physical check required) |
| 2 | party room | Host/cohost/admin/moderation/seat/room passwords, two live accounts | **UNVERIFIED** |
| 3 | rtc voice | Two physical phones, WiFi-to-cellular, remote-mute privacy, rejoin | **UNVERIFIED** (external/physical check required) |
| 4 | video | Real two-way or live Party video using authenticated provider | **UNVERIFIED** (external/physical check required) |
| 5 | ordinary multiplayer | All 13 live rounds start/moves/end/rematch/reconnect, no fake or ghost result | **UNVERIFIED** |
| 6 | full original games | True turn rules and win conditions for Ludo Sheep Fight Bingo and game catalog | **UNVERIFIED** |
| 7 | gifting | Correct different Gift animations and distinct sounds, dedup, receiver ACK | **UNVERIFIED** (external/physical check required) |
| 8 | paid diamonds | Approved merchant, webhook receipt signature, atomic ledger, idempotent credit | **UNVERIFIED** (external/physical check required) |
| 9 | vip noble | Real entitlement grants and expiration, noble privileges/gifts | **UNVERIFIED** (external/physical check required) |
| 10 | profiles | DP/profile stats/charm/medals/gift history on two accounts | **UNVERIFIED** |
| 11 | family social | Family roles, activity, chat/friends/followers, privileges synced | **UNVERIFIED** |
| 12 | pk ktv | PK score/settlement and KTV microphone and song queue on separate devices | **UNVERIFIED** (external/physical check required) |
| 13 | shared 3d world | Multiple real 3D avatars sync movement and stages on normal Android phones | **UNVERIFIED** (external/physical check required) |
| 14 | events ranking | Events/rankings/rewards accurately update for multiple accounts | **UNVERIFIED** |
| 15 | notifications safety | FCM, block/report/ban, moderation, security rules, privacy abuse acceptance | **UNVERIFIED** (external/physical check required) |
| 16 | production release | Deployed Firebase rules, provider setup, crash/security/regression tests, signed AAB/APK and version rollback | **UNVERIFIED** (external/physical check required) |

## What is already in signed TEST source, but NOT complete
- Current 13 ordinary game-round mechanics, frozen Ready roster, atomic host settlement, member check, scoped current round moves, bounded input score Rules; TEST/emulator coverage only.
- Party Jitsi RTC connection/rejoin and 8/10/12 seats code; 2-phone real audio/video/provider checks not supplied.
- Free Gift animation, per-item sound FIFO, durable recipient receipts, reconnection and dedup; true 2-device sound/animation checks still required.
- Mobile OTP/Facebook OAuth, Google live account linking, deployed production Firebase Rules, paid-wallet ledger, VIP entitlement and full 3D world must be proven independently.
- `LIVE_RECHARGE=false`, `PAID_VIP_ACTIVE=false` stay disabled until secured integrations are verified. Do not simulate user payments or remove consumer protections.

## One integrated delivery contract
1. Implement all achievable source changes on linked feature branch and preserve earlier signed APK/source. Package only the integrated **TEST** build for device validation.
2. Provider-owner configuration and external evidence: Firebase auth/provider/SHA rules deployment, verified merchant/webhooks, RTC auth, two actual Android phones; never ask the user to share secrets in chat and never claim these passed automatically.
3. Run release matrix for all 16. Each pass needs a trustworthy test/report/video or external provider verification link. Capture APK signing certificate and SHA-256.
4. Only when **all 16** are evidence-backed PASS, change release status to FINAL_CANDIDATE and allow a production release. Until then keep TEST builds, Draft PRs, and transparent NO-GO status.

### Automated guard
Run `python3 tools/integrated-release/check_release.py --mode test` to review outstanding work, and `--mode final` before anyone advertises a final artifact. The latter fails closed by default until every gate has verifiable evidence.

**Limits:** No GitHub Actions CI environment can simulate two physically distinct users' microphones, merchant approval, real OTP reception, production-provider configuration, or full Bolo Hi game catalog. Completing these requires actual services/devices and real testing, even if coding tasks are handled together.
