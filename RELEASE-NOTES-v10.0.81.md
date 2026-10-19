# KING Plus v10.0.81 Profile Report History TEST

## Me/Profile Safety Center

- My Report History now loads up to 30 of the signed-in account's report submission receipts from Firestore, newest first, so the same account can review them on another device.
- The receipt is written atomically with its private moderation report. It includes the reported target and submitted reason, but no moderation status or decision.
- Firestore rules limit receipt reads to the owning account, reject forged or unpaired receipts, and prevent receipt edits and deletes. Moderation reports remain unreadable to normal users.
- If the server cannot be reached, the screen clearly says the cloud history did not load and may show legacy device-local history. A report submission failure no longer claims that an unverified local copy was saved.

## Verification boundary

The CI workflow runs Firestore emulator security tests and builds/verifies a TEST APK package, version, signature, and native libraries. Firebase production rules still need deployment, and account sign-in plus report submissions need device validation. This is not production moderation infrastructure and does not expose moderation decisions.
