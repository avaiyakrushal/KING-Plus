# Bolo Hi Me/Profile → KING Plus route coverage (v10.0.81)

v10.0.81 continues the KING Plus Profile Center work by completing one Safety Center sub-option with account-scoped behavior.

| Profile option | KING Plus behavior in this TEST build |
| --- | --- |
| Safety Center → My Report History | Loads up to 30 immutable report-submission receipts for the signed-in account from Firestore; most recent first |
| Account privacy | A user can read only receipts under their own Firebase UID; receipts cannot be edited or deleted by clients |
| Moderation queue | Each receipt must be created atomically with the matching private report. Normal accounts cannot read moderation records or decisions |
| Offline/legacy entries | When cloud history is unavailable or empty, existing device-local entries can be shown with a local-history label |

## Verification boundary

`tools/me-profile-parity-v10081/check_source.py` checks app wiring and rule structure. `tests/firestore/user-report-history-v10081.test.mjs` exercises owner access, atomic pairing, immutability, and cross-account denial in the Firestore emulator. CI additionally compiles and inspects the TEST APK. Firebase rule deployment and signed-in, multi-device testing remain necessary before calling this feature production-ready. Other Me/Profile options still have TEST-only or backend-dependent behavior; this change does not claim full Bolo Hi parity.
