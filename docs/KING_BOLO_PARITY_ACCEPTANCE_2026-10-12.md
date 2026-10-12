# KING Plus versus user-supplied Bolo Hi — acceptance audit (2026-10-12)

## The inspected APKs
- Bolo Hi reference: `com.live.hey` v5.30.5, ARM64 Android 6.0 APK provided by user.
- KING baseline: `com.kingplus.social` v9.9.26 APK from the verified release.
- These ZIP entries were directly counted: Bolo Hi 13,523, KING 1,987; res/layout entries Bolo 4,065, KING 168; ARM64 library entries Bolo 43, KING 21. Bolo Hi bundles game packages for Flying Knife, Ludo and Sheep under assets/game/, KING implements original game logic rather than importing their copyrighted assets. File counts are **not** a feature-completion percentage.

## Verified KING functionality (automated code/CI, not production acceptance)
- Auth/UI: Google Firebase code exists, Royal Neon original KING UI, screen rendering compiles.
- Party: room discovery, moderator/seat transactions, invitations and Firebase member guards exist.
- Games: Party Chess moves and two-account Firestore rules previously emulated; Ludo, tap-based Sheep Sprint, Flying Knife physics; new v9.9.27 GO-based Reaction minigame has pure-Java tests.
- Video: real six-seat Firestore claim and recipient-consent match code with emulator tests from prior releases.
- Stability: same signed package version upgrade, missing old roundId handled safely in two RoomGameActivity paths.
- RTC: current app side membership verification before native/web Jitsi calls; both audio/video muted at startup.

## Not verified / blockers for an actual **Final** application
1. Exact full Bolo Hi UI/screens/assets, all minigame rules and room experiences are not recreated, and no proprietary code/graphics should be copied directly.
2. Fully rigged 3D animated customizable avatars, coordinated movement and live room rendering are not equivalent.
3. Two physical signed-in phones must pass mic, speaker, leave/join, background/foreground, video permissions, invite, host seat and simultaneous games in production; GitHub compilation alone is insufficient.
4. Production deployment of changed Firestore security rules/functions is not confirmed by any test APK build. A green Firebase emulator test is not a production deployment.
5. The public Jitsi provider URL is not server-token-authenticated; app-side membership cannot fully secure a public meeting.
6. Facebook and real mobile/WhatsApp OTP providers need external identity setup, budget/provider credentials and error-path tests.
7. Legitimate user diamond recharge requires enabled billing/provider, receipt verification, server crediting and refunds; test coins must never be represented as real money.
8. Source is built/signed with a stable CI debug key. A true public Final release needs a securely controlled production signing identity, app store policy checks and privacy review.
9. Anti-cheat for client-reported reaction/Sheep/Flying Knife scores needs a trusted game server if prizes/diamonds depend on results.

## Exit criteria
- All planned screens and buttons open without crash on two physical Android phones.
- Multi-user roles, mic-seat ownership, live reactions, video sessions, real game rules and complete Gift sounds/animations receive owner acceptance.
- Release-only backend credentials stay server-side, no fabricated online/paid results.
- Production auth + Firestore + billing + RTC and signer are configured and independently checked.
- Final is only a label after these gates pass; incremental debug APKs remain TEST.
