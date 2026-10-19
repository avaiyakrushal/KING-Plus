# KING Plus v10.0.90 Agency Visibility TEST

## Me and account menu
- Agency & Agent entry points are shown only to accounts with a trusted Firebase Super Admin claim or an active Agency/Agent server record.
- Offline or unverified accounts do not see the Agency entry.
- Firestore remains authoritative; the Android app cannot grant itself Agency or Agent access.

## Verification
- Source regression check covers Me and All Me Options visibility and server-authorized roles.
- Full Android build and signed TEST APK package checks are required before publishing artifacts.
