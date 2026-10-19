# KING Plus v10.0.80 Safe Check-in TEST

## Me/Profile and reward integrity

- Replace the legacy device-only Daily Check-in reward with a clear unavailable state until a server-verified service exists.
- Replace legacy Missions progress and claim buttons with an unavailable state and a route to the current Tasks & Check-in page.
- Stop awarding client-side XP for locally sent room messages or profile moments. Local activity is not treated as server-verified progression.
- Ignore legacy device-stored mission counters when showing level achievements and local discover scores.
- Keep the Bolo Hi-inspired Me menu and nested KING Plus Profile Center routes introduced in v10.0.79.

## Verification

- Source checks: `tools/me-profile-parity-v10080/check_source.py` and prior source regression checks in the Android workflow.
- This source-only workspace cannot produce or verify an APK locally. Android CI build/signature validation and signed-in device tests remain required.
- No coins, XP, payment status or VIP entitlements are granted by these TEST screens.
