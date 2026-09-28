# KING Plus v2.9.0 Feature Status

## Work added for items 1–6
1. **Real login path**
   - v2.9 build stops replacing mobile login with the fixed test OTP flow.
   - Firebase Phone Auth real SMS OTP path from the app source is used again.
   - Google Firebase Auth wiring remains in place.
   - Google sign-in still needs the stable APK SHA-1 registered in Firebase/Google OAuth.
   - Facebook still needs Meta App credentials/provider configuration before it can be genuinely enabled.

2. **Real-time rooms & chat**
   - Added `LiveCloudActivity` backed by Firestore snapshot listeners.
   - Signed-in users can create cloud rooms and exchange room messages across devices.
   - Added Firestore rules for authenticated live-room reads and protected room/message writes.

3. **Real voice**
   - Added an experimental real multi-user audio room path using an in-app WebView connected to Jitsi Meet.
   - Microphone access is restricted to audio capture; video starts muted/audio-only.
   - This is suitable for live testing but should be replaced by a dedicated production RTC provider (LiveKit/Agora/Zego/WebRTC infrastructure) before store release.

4. **Server-authoritative wallet & gifts**
   - Added callable Cloud Functions client bridge.
   - Added backend `sendGift` transaction that debits sender and credits receiver on trusted server code.
   - Client writes to production `/wallets` and `/wallet_ledger` remain denied by Firestore rules.
   - Admin wallet adjustment is protected by Firebase custom claim `admin=true` and audited in the ledger.

5. **Real push backend**
   - Existing FCM device token registration is used.
   - Added callable room-invite push backend.
   - Added Firestore room-message trigger that notifies the room owner of new messages.
   - In-app notification records are stored server-side as well.

6. **Admin dashboard & moderation**
   - Added Android admin dashboard that verifies `admin=true` custom claim.
   - Admin can review/resolve/dismiss reports, ban users, and perform audited wallet adjustments through trusted functions.
   - Normal app clients cannot read moderation queues or write production wallets.

## External setup still required before all six are production-live
- Register the stable GitHub APK signing SHA-1 in Firebase/Google OAuth for Google sign-in.
- Enable/configure Firebase Phone Authentication and SMS billing/provider requirements for production OTP.
- Add Meta/Facebook App ID, secret and Firebase Facebook provider setup for Facebook login.
- Deploy `firestore.rules` and the `functions/` backend to the Firebase project.
- Set the desired account custom claim `admin=true` through a trusted admin process before using the admin dashboard.
- Replace the public Jitsi test voice path with a controlled production RTC provider for scale, abuse controls, tokens and reliability.

The Android APK can build without deploying the backend, but Firestore live rooms, server gifts, push sends and privileged moderation only become fully functional after the corresponding Firebase services/rules/functions are deployed.
