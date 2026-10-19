# Bolo Hi 5.30.5 to KING Plus v10.0.70 audit (TEST)

## Reference and method

- Reference APK: `com.live.hey-5-30-5-arm64-v8a-android60(20261018-014531).apk`
- SHA-256: `6b9184ce120607c21f9ed9259ca207e65971e737393f0b114277ce73bff0caf2`
- Archive integrity: verified; 13,523 ZIP entries, 4,065 layout entries, 8,680 drawable entries, 125 assets, 86 native libraries and 14 DEX files.
- Scope: static APK/resource inventory and KING Plus source review. Neither app was installed or exercised on phones here. Counts include resource variants and do not imply that every screen was individually visited.
- No Bolo Hi source code, logos, artwork, audio, or other proprietary assets were copied. KING Plus keeps its own app identity and implementation.

## Start-to-finish feature comparison

| User journey | Bolo Hi evidence | KING Plus v10.0.69 source | Remaining work / evidence needed |
|---|---|---|---|
| Install and first launch | Android package with 14 DEX files and extensive compiled resources | Native Android application, package `com.kingplus.social` | Build and install v10.0.70; verify launch and upgrade on Android 16 |
| Sign-in / account | Login resource family present; private provider behavior is not visible from an APK inventory | Google, Facebook and phone OTP entry paths are present | Configure real provider credentials, make fresh accounts, test recovery and account switching |
| Home / live-room discovery | Home, category, room and live-game resource families | Live room list, category aliases, cover art, room-ID entry and 50-item cursor paging | Deployed global search index; compare empty, loading, offline and long-list states on device |
| Discover / social | Discover and people-follow resource families | Hot/New/Nearby/Follow discovery, public profiles, live follow listener and duplicate-action guard | Two-account follow/unfollow, notification and stale-state tests |
| Party-room entry and exit | Party-room stage, room controls and animations are present | Join/leave, presence, seat and listener cleanup, reconnect guards and lobby navigation are implemented | Reproduce repeated enter/exit, back/home, host close and network drop on two physical phones |
| Voice, seats and video | Voice-room and game assets/resources are present; APK resources do not prove server credentials or a live media session | 8/10/12-seat layouts, mic consent and moderation; test-only voice bridge | Authenticated RTC provider, two-way audio/video, mute privacy, Wi-Fi/cellular handoff and rejoin |
| Room chat, gifts and music | Chat, gift, music and voice-game asset families | Live room chat, gift receipts/anti-duplicate flow and shared music controls | Compare each live gift animation/sound and test concurrent send/receive on two devices |
| Direct messages | Chat and message resource families | Live threads, bounded history and foreground/server-confirmed read receipts | Two-device send/read/push-notification acceptance |
| Profile / Me / photos | Me/profile, avatar, photo and gallery resources | Live profile/photo refresh, editable identity, public profile and moments | Full privacy, revoked permission, rotation and large-photo acceptance; Android 16 URI fix included in this update |
| Wallet / gifts / recharge / VIP | Wallet, gifts, recharge and VIP resource families | Server wallet, gift-score separation, Play Billing verification foundation and VIP screens | Production receipts, server entitlements, expiry, refunds and real-account ledger checks |
| Family / friends / rankings / events | Family, social and rank resource families | Family/social routes and existing ranking/event pages | Cross-account role/activity sync and real rewards acceptance |
| Games | Multiple game and game-room resources, including packaged game assets | Many game modes and server rules exist | Full round-by-round acceptance for every advertised mode; exact Bolo game behavior is not established |

## Change made from the reported Android 16 crash

The supplied crash shows `ImageView` lazily opening a Media Documents provider URI while measuring a view. Direct `setImageURI()` calls were present in KING Plus profile, post and Party cover/photo rendering. v10.0.70 replaces those calls with a bounded eager decoder using `ContentResolver.openInputStream()`. If permission was revoked or access fails, the view uses its fallback instead of letting the exception escape during layout. The Party cover picker still obtains access through `ACTION_OPEN_DOCUMENT` and requests persistable read permission.

The v10.0.70 source check confirms that there are no remaining direct `setImageURI()` calls in app Java sources and that all inspected app call sites route through the safe decoder. This is a source regression check, not a device reproduction or Android compilation.

## Release status

This archive is TEST source for versionCode 349 / `10.0.70-android16-media-crash-fix`. The environment has Java 17 but no Gradle executable, Android SDK/build tools, wrapper, or configured signing key, so an APK could not be built here. Automated source checks pass. Android rendering, real providers, production services, and two-phone parity remain unverified. The app can be made functionally similar while retaining KING Plus branding; this audit does not claim pixel-perfect or complete parity.
