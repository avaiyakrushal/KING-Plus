# KING Plus v10.0.79 Me Profile Routes TEST

## Me/Profile changes

- Add Bolo Hi-style drawer shortcuts for Coin Mall, Data Center, Web Diamond recharge, Share Gift/Gift Wall and Social Media, routed to existing KING Plus screens.
- Add a Data Center menu page linking to signed-in profile, social, gift, wallet, room and activity views.
- Add direct Me menu entries for Profile Center, My Gallery, Followers/Friends, Backpack/Collection and Notifications.
- Route Safety Help, blocked users and report history to their own destinations.
- Give Privacy Policy and Terms rows distinct pages, and disclose when official document links are not configured.
- Scope local report history to the signed-in KING account on the device.
- Remove stale task-completion labels and unverified XP claims; activity shortcuts remain available, while task reward completion is explicitly not enabled in this TEST build.

## Verification

- `tools/me-profile-parity-v10079/check_source.py`
- Existing source regression checks listed in the Android CI workflow.
- APK/device/backend acceptance is still pending; this TEST build does not enable unverified paid balance, VIP, Noble or task rewards.
