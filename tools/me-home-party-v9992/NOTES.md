# KING Plus v9.9.92 Me / Home / Party TEST
Derived from v9.9.91 built source, without resetting previous Firestore or screens.

- Me > Gallery: view signed-in user's published cloud Moments (text/photo), account check, refresh; old local posts remain archived.
- Saved Party Rooms in Me and Home: remove favorite/recent room with confirmation, per-account Firestore transaction, two-phone sync.
- Room entrance: re-check SERVER for room existence/closed before navigation; keep saved room on offline failure.
- Party deep invites: clear pending menu state after invalid, failed or cancelled switch, retaining current room.
- GitHub stores the delta compressed as changes.patch.gz.b64; workflow reconstructs a plain changes.patch and applies it to exact v9.9.91 tested source. The final source ZIP includes both plain patch and scripts.

Not Final: real two-physical-device Voice/Video/Gifting, full games/3D parity, production rules/storage, recharge/VIP services require separate validation.
