# KING Plus v9.6.2 — Online Ludo turn integrity

Scope: standalone Online Ludo and Party-linked Ludo. Independently implemented gameplay; never copy Bolo Hi proprietary game code or assets.

## Implemented source changes
- Only the Ludo match owner can start a match and only when all required two/four players are marked ready. Checks happen against the latest Firestore transaction document, not a stale client view.
- Skip idle turn is offered only to a *different* player; it requires at least 60 seconds since the latest server-stamped state update, verified inside the transaction.
- Client reconnect/view listener ignores events for a previous match or a destroyed Activity.
- Reject invalid game piece indices before reading board state.
- Expanded local crash breadcrumb to identify OnlineLudoActivity in a future native-process exit.
- Firestore rule updates mirror host-only start/cancel/rematch and 60-second skip and require revision increments / server timestamps.

## Important deployment boundary
Commit and source packaging DO NOT deploy Firestore rules. A server-side rollout requires Firebase project credentials and an explicit successful rules-deploy job. Until then, older clients can still bypass client-only gameplay checks, so server authorization is not production-verified. This release remains a debug/test build (no billing; OTP unchanged).

## Two-phone acceptance test
1. Install same build on phones A and B; sign in as distinct Firebase users.
2. A creates an **Online Ludo 2 player** room; B joins using exact 6-character code.
3. Verify B cannot start the match; A cannot start until both players select Ready.
4. A starts. Only the player whose turn it is may roll dice and move a highlighted piece; both phones display same turn/dice/piece positions.
5. During the first 59 seconds of inactivity, opponent's Skip action must fail. At 60+ seconds of inactivity, the opponent may skip; current-turn player may not skip their own turn.
6. Leave and reopen the game on one phone; verify room code and synchronized state survive reconnect.
7. Repeat room open/close 10 times and watch for native-crash diagnostics. Report any crash with the installed APK version and crash time.

## Automated checks
- Pure Java test suite: 17 cases covering owners, ready players, match state, skipped turns, current-turn protection, server timestamp and invalid values.
- Staged-source checks assert Firestore guard expressions and preserve prior Party voice, emoji, link and crash-report changes.
- Gradle assembleDebug build in GitHub Actions; phone behavior is not covered by CI.
