# Bolo Hi 5.30.5 to KING Plus v10.0.72 audit (TEST)

## Reference and method

- Reference APK SHA-256: `6b9184ce120607c21f9ed9259ca207e65971e737393f0b114277ce73bff0caf2`.
- The Bolo APK passed ZIP integrity verification and has 13,523 entries, including 4,065 layout entries, 8,680 drawable entries, 125 assets, 86 native libraries and 14 DEX files. Counts include resource variants; they do not prove every screen was visited.
- KING Plus comparison is based on its source and CI build. Neither app was exercised on a phone in this session.
- Bolo Hi code, logos, art, sound and other proprietary assets were not copied. KING Plus retains its own app identity.

| Journey | Bolo Hi static evidence | KING Plus v10.0.72 | Remaining parity work |
|---|---|---|---|
| Install / first launch | Compiled login/home resource families | Native Android app `com.kingplus.social`; CI full build and signature pass | Physical Android 16 install, launch and upgrade acceptance |
| Login / account | Login resource family | Google, Facebook and phone OTP paths | Real provider credentials, fresh accounts, OTP delivery and recovery |
| Home / room discovery | Home, category, room and live-game resources | Live rooms, category aliases, covers, room ID entry, cross-category text search and cursor pages | Search results require paging; backend index needed for immediate global text results |
| Discover / social | Discover and follow resources | Hot/New/Nearby/Follow tabs, public profiles, live follow listener and duplicate-action guard | Two-account follow, notification and stale-state tests |
| Party entry / exit | Party stage and room-control resources | Join/leave, host/moderation controls, seat cleanup, presence, reconnect guards and lobby navigation | Investigate any device report of unexpected exit; test host close, kick, network drop and rejoin on two phones |
| Voice / video | Voice-room resources; assets do not prove live sessions | 8/10/12-seat layouts, mic consent and test voice bridge | Authenticated RTC credentials, two-way call, mute privacy, network handoff and rejoin |
| Chat / gifts / music | Chat, gift, music assets | Live room chat, server gift flow/receipts and shared music controls | Two-device receipts plus matching gift animations and sounds |
| Direct messages | Chat and history resources | Live threads, bounded history and server-confirmed read receipts | Two-device message, push and read acceptance |
| Profile / Me / photos | Profile, avatar and gallery resources | Editable profile, live photo refresh, public profiles and moments; Android 16 URI crash fix | Permission revoke, rotation, large-photo and device acceptance |
| Wallet / recharge / VIP | Wallet, recharge, gift and VIP resources | Server wallet, Play Billing verification foundation and VIP screens | Production purchase receipts, entitlement grants/expiry/refunds and ledger validation |
| Family / events / ranking | Family, social and rank resources | Existing family/social/event/rank routes | Two-account role sync, live rewards and event acceptance |
| Games | Multiple game-room resources and packaged game assets | Existing game catalog and server rules | Test every advertised game round, move, finish, rematch and reconnect against reference behavior |

## Changes in v10.0.70–v10.0.72

- v10.0.70 replaced direct `ImageView.setImageURI()` usage with a bounded eager decoder because Android can defer opening a document URI until view measurement and throw the reported `SecurityException`.
- v10.0.71 makes Party text search cross-category. It searches live rooms in 50-item cursor pages; it does not claim a full-text backend or instant all-results index.
- v10.0.72 restores a five-minute stale-member window so a temporary mobile network drop is less likely to remove a reconnecting participant.
- The v10.0.71 full Android CI build, source regression checks, APK signature/package/version/native-runtime checks and APK ZIP integrity passed. The v10.0.72 CI build must pass before its APK is delivered. The APK is a signed TEST build. Physical-device, live-provider and two-phone gates remain open.
