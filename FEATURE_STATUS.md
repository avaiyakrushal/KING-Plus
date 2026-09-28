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

## External setup still required before production launch
- Deploy Firestore rules and Cloud Functions from this repository to the correct Firebase project.
- Configure Firebase Authentication providers, including the required Android SHA fingerprints.
- In Play Console create and activate the three one-time coin products, publish the app to an internal testing track, link the Cloud Functions service account, grant Android Publisher purchase/order permissions, and enable the Android Publisher API.
- Replace the CI test signing key with a private production upload key and use Play App Signing before public Play Store release.
- Configure/verify production FCM sender behavior, moderation admin claims, privacy/terms, support contact, data safety declarations and store policy requirements.
- Production voice still needs a dedicated RTC provider and production credentials if the current web voice bridge is not sufficient.

The v3.0.0 CI APK/AAB is a release candidate for testing. It is not represented as a store-production build until the external signing, Play Console and Firebase deployment steps above are completed.
