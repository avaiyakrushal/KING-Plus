# KING Plus Production Launch Checklist

This checklist separates work that is already implemented in the repository from account-side setup that must be completed in Firebase, Google Play Console, and the chosen RTC provider.

## 1. Firebase deploy
- Project: `king-plus-2f365`.
- Deploy `firestore.rules` and `functions/` only after production authentication providers are configured.
- Use the manual `Firebase production deploy` GitHub Actions workflow after the repository secret `FIREBASE_SERVICE_ACCOUNT_JSON` is configured with a least-privilege service account that can deploy Functions and Firestore rules.
- After deployment, test `sendGift`, `sendRoomInvite`, `verifyPlayPurchase`, `requestAccountDeletion`, moderation callables, and notification triggers with test accounts.

## 2. Authentication
- Android application ID: `com.kingplus.social`.
- Register the production upload/app-signing SHA-1 and SHA-256 fingerprints in Firebase/Google OAuth.
- Enable Google provider in Firebase Authentication.
- Enable Phone provider and complete production SMS/billing requirements.
- Facebook login requires a Meta developer app, Android package/key hashes, client credentials, and the Firebase Facebook provider. Never hard-code a Meta app secret in the APK.

## 3. Google Play recharge
Create and activate these one-time products in Play Console:
- `king_coins_100`
- `king_coins_600`
- `king_coins_1300`

Publish the app to an internal testing track before testing Billing. The backend must have Android Publisher API access to verify purchase tokens. Server verification must complete before coins are credited.

## 4. Production signing
Do not publish an APK/AAB signed with `kingplus-ci-debug.keystore`.

The manual `Production signed release` workflow expects these GitHub repository secrets:
- `KINGPLUS_RELEASE_KEYSTORE_BASE64`
- `KINGPLUS_RELEASE_STORE_PASSWORD`
- `KINGPLUS_RELEASE_KEY_ALIAS`
- `KINGPLUS_RELEASE_KEY_PASSWORD`

Keep the upload keystore private and backed up securely. Use Play App Signing for public Play Store distribution.

## 5. Voice production
The current Jitsi WebView path is suitable only for testing. Before public launch choose a controlled RTC provider such as LiveKit, Agora, Zego, or self-hosted WebRTC/Jitsi. Production voice must use server-issued room tokens, abuse controls, moderation, rate limits, and provider credentials that are never embedded as secrets in the APK.

## 6. End-to-end test matrix
Use at least two physical Android devices and two separate Firebase accounts.
- Google login succeeds on both devices.
- Phone OTP succeeds and cannot bypass verification.
- User A creates a live room; User B sees it without restarting.
- Chat appears on both devices in real time.
- Voice join/leave/mute is verified on two devices.
- Push: room invite, room message, direct message, follow, gift.
- Gift transfer debits and credits server wallets exactly once.
- Duplicate gift request does not double-charge.
- Play purchase credits only after backend verification.
- Pending/cancelled/refunded purchase never credits incorrectly.
- Report submission appears for admin; non-admin cannot read moderation queue.
- Ban/restriction behavior is enforced on room, message, follow and gift paths.
- In-app account deletion request creates only the signed-in user's deletion request and signs the user out after success.
- App survives reinstall, logout/login, network loss, rotation/background/foreground, and notification permission denial.

## 7. Play Store launch
Before production submission complete:
- Store listing, icon, screenshots, short/full description.
- Privacy Policy and Terms URLs.
- Data Safety form based on actual Firebase, FCM, Billing, moderation, analytics, and RTC data flows.
- Public account-deletion web URL/process that matches the in-app deletion request path and Play policy requirements.
- Content rating, ads declaration, target audience, app access instructions, and support contact.
- Internal test -> closed test if required -> production rollout.
- Upload a production-signed AAB built from the production workflow, not the CI test-signed RC.

## Launch gate
Do not call the build production-ready until all account-side items above are completed and the two-device end-to-end test passes.
