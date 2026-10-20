# KING Plus v10.0.88 Profile Liked Moments TEST

## Me → Profile
- Added a live, signed-in account count to the Liked Moments shortcut.
- The count follows likes/unlikes from other signed-in devices and opens the existing Liked feed.
- Kept listeners scoped to the current account and stopped them when Me is not visible.
- Updated the Profile Center My Videos description to reflect cloud uploads and device-local videos.
- Tapping the server-verified KING ID on Me/About copies it to the clipboard; stale-account and unissued IDs are not copied.

## Verification
- Source checker verifies the count query, listener lifecycle and existing Liked feed route.
- Firestore emulator test verifies live count changes across two clients using the same account.
- CI runs all existing profile, privacy and moment-rule tests plus Android compile/package/signature/RTC checks.
- Trusted rewards/VIP and complete profile parity remain open.
