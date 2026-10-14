# KING Plus v9.9.89 — Me, Home and Party fixes

Based on the user's supplied v9.9.88 source and APK. This is a TEST build, not full Bolo Hi parity.

Implemented:
- Favorite rooms also remain in Recent, sorted by actual latest visit; duplicate history records collapse correctly.
- Favorites and Recent are scoped to the signed-in account across Me and Party. Old device-wide records remain stored but are not assigned to an unknown owner. Room history is still device-local, not cross-device sync.
- Me wallet subscribes to live server snapshots, refreshes when returning, removes its listener on pause/destroy and ignores stale account callbacks. Cached/unconfirmed balances show an em dash.
- Home/Party lobby queries open rooms before applying its limit, preventing closed rooms from hiding active rooms. Load More expands the active room window; the loaded window is sorted by creation time.
- Existing Party activity handles a new invitation, verifies current room data with the server, asks before switching rooms and rejects stale lookup responses. Caller-supplied privacy flags are not trusted.

Validation in CI: history regression, original history regression with corrected expectation, full Android build and unchanged APK signing certificate.

Still not completed/verified: production Firebase deployment, two physical phone Voice/Video/gift/reconnect acceptance, all game end-to-end acceptance, real paid Diamond recharge, paid VIP/Noble entitlement backend, Family Rewards, complete shared 3D avatar parity and all earlier notification preferences at service level. No production deployment or fake paid entitlement is introduced here.
