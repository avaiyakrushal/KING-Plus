# KING Plus v9.6.3 — actual Online Ludo modes and Firebase deployment status

## Changes in this build
- Ludo home: **CREATE 1 VS 1** creates a 2-player real-time `ludo_matches` document.
- **CREATE 4P** creates a 4-player match.
- **JOIN CODE** opens entry for an existing six-character Ludo match.
- **RECONNECT** opens the previously saved match code for this Firebase account, if available.
- **OFFLINE** opens `KingOfflineLudoActivity` (no Firestore match).
- Replaced fake Championship/Wacko modes and inert Shop/Rank/Event/Reward placeholders with accurate labels. The future UI may differ from Bolo Hi until implemented.
- Preserved existing v9.6.2 turn and 60-second timeout guards, v9.6.1 native-crash diagnostics, v9.6.0 Emoji and v9.5.9 Party invitations.

## Critical backend blocker: Firestore Rules Admin permission
Workflow: https://github.com/avaiyakrushal/KING-Plus/actions/runs/37874354772
- Firebase CLI dry run FAILED at Google Service Usage API with HTTP 403 `Permission denied to get service firestore.googleapis.com`.
Workflow: https://github.com/avaiyakrushal/KING-Plus/actions/runs/37874489494
- Direct Firebase Admin SDK `releaseFirestoreRulesetFromSource` FAILED: `The caller does not have permission`.
- The `FIREBASE_SERVICE_ACCOUNT_JSON` secret **does exist** and matches Firebase project `king-plus-2f365`; it currently lacks the required Google IAM privilege(s).
- Therefore **v9.6.2 tightened Firestore rules have NOT been deployed to production**. APK compile alone does not validate remote multiplayer authorization.
- An authorized project owner should grant `Firebase Rules Admin` (`roles/firebaserules.admin`) to the service account identified in the GitHub secret, verify Firebase Rules API access, and re-run `firebase-v963-direct-admin-rules.yml`. Firebase CLI approach also requires Service Usage read permission (`roles/serviceusage.serviceUsageViewer`). Grant least-privilege roles only.

## Two-phone checks after authorization is fixed
1. Install v9.6.3 on phones A/B, sign in with two different KING Plus accounts.
2. Phone A: Game > Ludo > **CREATE 1 VS 1**. The app should create a 2-player room and show a six-character code.
3. Phone B: Game > Ludo > **JOIN CODE**; enter A's code. Verify both phones show the same two players and state.
4. Both tap Ready. Host begins match. Roll dice on the current-turn player's device, move legal pieces, verify turn sync on both phones.
5. Before 60 seconds of inactivity, other player must not skip. After 60 seconds, opposing player can skip but current-turn player cannot.
6. Close/relaunch Ludo on B; tap **RECONNECT**, verify prior game state reopens.
7. Repeat with **CREATE 4P** and four accounts; confirm all four Ready before match start.
8. Repeat using Party Room Ludo (separate entry) and monitor crash-report dialogs.
9. Do not claim Bolo Hi parity or crash-free operation based solely on compilation.

## Stability
Production native crash is not yet isolated to a named native library. Native crash diagnostics in v9.6.1+ report actual install version and PID for crashes during this APK installation only.
