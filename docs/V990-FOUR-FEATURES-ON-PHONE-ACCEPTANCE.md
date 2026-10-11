# KING Plus v9.9.0 — Four-feature mobile acceptance

This is a combined runtime fix release on signed v9.8.7, not a complete port of Bolo Hi proprietary game engines/services/assets.

## Party and Mic
- On Phone A create/join Party. On Phone B join using a DIFFERENT Firebase account.
- On a seat tap Mic ON. Without Android microphone permission, the app must ask; Firebase Mic must NOT switch on. Grant permission and tap Mic again.
- Test remote mute/unmute, Seat Invite, seat Kick, Exit and Rejoin. Both devices must display server-validated seat state and actual 2-way audio.
- Android compilation does not prove live voice streaming.

## Animated emoji and free gifts
- Send an emoji A -> B. Only current server-timestamp events animate; old cached events do not replay.
- Send a FREE gift A -> B. A animates after Firebase batch commit, B via its live event listener; no paid Diamond changes.
- With network disconnected, there must be no success toast or simulated paid Gift.
- Artwork is independently developed; do not copy third-party proprietary assets.

## Room multiplayer games
- Open Games from a joined Party. Firebase server confirms membership. Games screen may NEVER join a Party by creating a member document.
- A and B tap Ready, Host starts a round, both make a move, finish, show winner, rematch, reconnect.
- Online Ludo: Test a separate real 2-player Firebase match: code, ready, dice, turns, tokens, result, rematch, connection loss.
- Existing Ludo transaction logic is preserved, not rebuilt in this release.

## VIP wallet
- With no wallet server record, show "not established", not a false confirmed zero.
- With cached or pending write only, no "Verified Diamonds" claim.
- LIVE_RECHARGE stays false and payment buttons cannot charge. Paid recharge, VIP unlocks and true verified purchased diamonds require approved provider and independent server verification.

## Required release criteria
- CI: Patch applied, static safety assertions, Android assembleDebug, SAME signing certificate as v9.8.7, higher version code.
- Real two-phone tests remain PENDING until physically performed.
- Spark Firestore rules and Mumbai default database unchanged.
