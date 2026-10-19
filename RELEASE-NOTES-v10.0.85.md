# KING Plus v10.0.85 Liked Moments Profile TEST

## Me → Profile → Liked Moments

- Added a Liked tab in Community and a direct entry from Me Profile Center and the Me shortcuts.
- Loads the signed-in user's actual Firestore like records, fetches the source Moments from the server, and orders visible posts by creation time.
- Like state stays live; unliking a post removes its card from the Liked list immediately.
- Account and view-generation checks prevent results from a previous account or closed screen from appearing.

## Verification boundary

The CI runs the source regression checks, Firestore privacy/history emulator tests, Android compile, and APK package/signature checks. Signed-in multi-device UI QA remains necessary; this TEST feature does not imply full Bolo Hi parity.
