# KING Plus 2.7.1 backend — deployment pending

This package contains implementation for online coins, gifts, all-time rankings,
inbox/push notifications, blocks and admin review. It is not deployed by saving
this ZIP. No billing setting, Firebase project or production data was changed.

## Prerequisites and deployment

1. Use the Firebase project associated with app/google-services.json; enable
   Firebase Authentication and Cloud Firestore (Native mode).
2. Cloud Functions deployment requires an appropriate billing-enabled project.
   Review costs before enabling billing. This change does NOT promise a free
   hosted backend. Local test OTP still works without these online services.
3. In backend/functions, use Node 22 and run `npm ci` and `npm test`.
4. From backend, use an authenticated Firebase CLI and your actual project ID:
   `firebase deploy --project YOUR_PROJECT_ID --only functions,firestore`.
   The functions use us-central1; the Android client uses the same region.
5. On an existing project, review and merge firestore.rules/indexes with its
   current policy first. Do not blindly replace unrelated collection rules.
6. Build the Android app with the existing GitHub Actions workflow or Gradle
   8.10.2, JDK 17 and Android SDK 35. Install on two phones and sign in with real
   Google/phone accounts. A local OTP 123456 session is intentionally ineligible.
7. An authorized owner can use Application Default Credentials with
   `node set-admin.js FIREBASE_UID grant` (or revoke). No client controls this
   claim. Sign out/in after a claim change; revoked token access can persist
   until a previously issued ID token expires. Do not include admin keys in APK.

## What is implemented

- Accounts start at zero cloud coins; claim 100 virtual coins per UTC day once.
  No local wallet, mission or game rewards are imported into the online wallet.
- Gift prices come from the server catalog. Atomic transactions debit balance,
  update both users' counters, write ledger records and recipient inbox entries.
- Client persists each gift request ID before sending. Network retries reuse it;
  server rejects an ID reused with different contents. A pending ambiguous
  request remains until confirmed. Definitive validation failures can be fixed.
- Gifts create receiver charm points, NOT spendable coins or money.
- Top 50 non-suspended accounts ranked by all-time sent coins / received charm.
- Ledger and inbox show the latest 50 records. Refresh by reopening the page.
- Blocks prevent online gifts in BOTH directions. Block list shows first 100.
  They do not claim to enforce a future voice/chat backend. Existing local demo
  conversation checks local blocked names.
- Reports retain target, reason, reporter, status and time; 10 reports per UTC
  day. Same request ID is idempotent. Admin can dismiss, warn or suspend a user.
  Room reports can be reviewed/dismissed; live room enforcement is not built.
- Admin decisions write an immutable client-inaccessible audit record. Account
  suspension denies online feature calls; it is not a Firebase Auth deletion.
- Server-created inbox entries trigger generic data-only push. Device token has
  one owner, refreshed on entry to online screens/token rotation. Push is opt-in;
  Android 13+ asks notification permission. The receiving app verifies UID and
  current preference before display. Logout/account switches suppress old-user
  messages. Durable inbox remains available if push fails.
- Firestore direct client read/write is denied; validated callable functions
  authenticate users. No client can mint coins, edit rankings or grant admin.

## Verification and remaining limitations

Automated callable-handler tests use an in-memory transaction double. They check
replay safety, concurrent spend, daily reward deduplication, blocking, invalid
input, quotas, authorization and moderation. They are NOT Firestore emulator,
FCM delivery, Android compilation or device tests.

Before live release, run Firebase Emulator tests plus two-device QA: switch
accounts, deny/allow notifications, retry gifts after lost connectivity, test
suspension and report review. Configure App Check/rate limits and abuse monitoring
before a public launch; this package does not claim complete anti-abuse coverage.
A trusted server for online games/missions, online chat/voice, real-money payments,
weekly rankings and bulk moderation are outside this update.

References:
https://firebase.google.com/docs/functions/callable
https://firebase.google.com/docs/firestore/manage-data/transactions
https://firebase.google.com/docs/cloud-messaging/android/get-started
