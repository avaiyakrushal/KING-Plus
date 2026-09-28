# KING Plus v3.0.0 Feature Status

## v3.0 work completed in source / release candidate
- Server wallet and gift hardening:
  - normal app clients cannot write production wallet or ledger collections;
  - gift writes go through Cloud Functions;
  - gift requests use client request IDs and server-side idempotency records to reduce double-charge risk;
  - wallet ledger entries can be read only by the involved account or an admin.
- Push notification expansion:
  - gift, room invite and room-message notifications remain supported;
  - direct-message and follow notification callable/trigger paths are added;
  - invalid FCM registration tokens are cleaned up by the v3 backend.
- Google Play recharge integration foundation:
  - Android uses Google Play Billing Library 9.1.0;
  - configured one-time product IDs are `king_coins_100`, `king_coins_600`, `king_coins_1300`;
  - the app never credits Play coins locally;
  - completed purchases send the purchase token to `verifyPlayPurchase`;
  - the Cloud Function verifies the purchase with Android Publisher API, checks the obfuscated KING Plus account ID when available, credits the server wallet exactly once, records a receipt/ledger row, and then attempts server-side consumption.
- Production-readiness UI explains server wallet, FCM, Play Billing and release-signing state.
- v3.0 release-candidate workflow builds a minified release APK and release AAB plus source ZIP.

## Production launch preparation added
- Manual `Firebase production deploy` workflow for Firestore rules and Cloud Functions using a repository service-account secret.
- Manual `Production signed release` workflow that refuses to build without private upload-key secrets and produces a production-signed APK/AAB.
- Production smoke checks validate Firebase package configuration, Billing product IDs, backend exports, Firestore wallet lock and required Android permissions before CI builds.
- `docs/PRODUCTION_LAUNCH_CHECKLIST.md` contains the two-device test matrix, Play Console setup, production signing, RTC and store launch gates.
- Firebase project mapping is already present for `king-plus-2f365` and deployment configuration points at `firestore.rules` plus `functions/`.

## External account setup still required before production launch
- Configure and authorize the Firebase deployment service account, then run the manual deploy workflow.
- Configure Firebase Authentication providers and production Android SHA fingerprints.
- For Facebook, create/configure the Meta developer app and Firebase Facebook provider; provider secrets must never be embedded in the APK.
- In Play Console create and activate the three one-time coin products, publish to internal testing, authorize Android Publisher API access for backend verification, and complete required store declarations.
- Create and securely store a private upload keystore, add its values as GitHub repository secrets, enable Play App Signing, then run the production signed release workflow.
- Choose/configure a production RTC provider and server-issued voice token flow; the current public Jitsi bridge remains test-only.
- Run the physical two-device end-to-end test matrix before rollout.

The current CI APK/AAB remains a release candidate until the account-side Firebase, Play Console, signing and RTC steps above are completed and validated.