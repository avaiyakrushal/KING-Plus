# KING Plus v3.0 Production Release Checklist

1. Firebase
   - Deploy `firestore.rules`.
   - Deploy Cloud Functions from `functions/`.
   - Enable Google/Phone authentication and register the APK/AAB signing SHA fingerprints.
   - Assign `admin=true` custom claims only to trusted moderators.

2. Google Play Billing
   - Create one-time products: `king_coins_100`, `king_coins_600`, `king_coins_1300`.
   - Activate them and publish KING Plus to an internal testing track.
   - Enable Android Publisher API.
   - Link the Cloud Functions service account in Play Console and grant the minimum permissions needed to verify/consume one-time products.
   - Test purchase, pending purchase, cancellation, duplicate callback, refund and multi-device restore paths.

3. Wallet / gifts
   - Seed test server wallets only through trusted admin tooling.
   - Verify clients cannot write `/wallets`, `/wallet_ledger`, `/wallet_operations`, or `/play_purchase_receipts`.
   - Confirm duplicate gift request IDs never debit twice.

4. Push / moderation
   - Test FCM token refresh and invalid-token cleanup.
   - Test gift, room invite, room message, direct message, follow and moderation notifications.
   - Verify report review and ban flows with admin claims.

5. Release signing and store
   - Create a private production upload key; never use the repository CI test key for public release.
   - Enroll in Play App Signing.
   - Build/sign the production AAB with the upload key.
   - Complete Privacy Policy, Terms, Data Safety, content rating, support contact and required store declarations.

6. Final device testing
   - Test clean install and upgrade from prior KING Plus builds.
   - Test Android 8 through current Android versions, notification permission, microphone permission, background/foreground behavior and network loss.
   - Run two-device room/chat/voice and purchase verification tests before public rollout.
