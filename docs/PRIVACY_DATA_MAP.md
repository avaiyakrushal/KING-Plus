# KING Plus — Production Privacy & Data Flow Map

Use this document to prepare the public Privacy Policy and Google Play Data Safety answers. It must be reviewed again after final provider/account setup.

## Authentication
Provider: Firebase Authentication.
Potential data: Firebase user ID, display name, email address for Google sign-in, phone number for phone authentication, provider identifiers.
Purpose: account creation, sign-in, fraud/security and account linking.

## Profiles and social graph
Provider/storage: Cloud Firestore.
Potential data: display name, profile metadata, follows, room ownership/membership metadata and user-generated social records.
Purpose: social features, room discovery and account experience.

## Messages and rooms
Provider/storage: Cloud Firestore.
Potential data: room names, room messages, direct messages, sender/recipient user IDs and timestamps.
Purpose: real-time communication.

## Push notifications
Provider: Firebase Cloud Messaging.
Potential data: FCM registration token, user ID association and notification payload metadata.
Purpose: room invites, messages, follows, gifts and moderation notices.

## Wallet, gifts and recharge
Provider/storage: Cloud Functions, Firestore, Google Play Billing / Android Publisher API.
Potential data: user ID, server coin balance, gift ledger, Play product ID, purchase token hash, order identifier when supplied by Google Play and verification timestamps.
Purpose: virtual currency accounting, fraud prevention, purchase verification and support/audit.
The app must never store payment card details itself.

## Safety and moderation
Provider/storage: Cloud Firestore / Cloud Functions.
Potential data: report reason, reporter user ID, reported target identifier, moderation status, ban/restriction state, admin audit metadata.
Purpose: abuse prevention, user safety and policy enforcement.

## Voice / RTC
Current test path: web/Jitsi bridge.
Production provider: not finalized.
Potential data depends on final RTC provider and may include room identifier, participant user ID, IP/device/network metadata and live audio transport data.
Purpose: live voice communication.
Before launch, replace this section with the exact provider, retention behavior, regions and privacy terms.

## Device/app diagnostics
Current repository does not claim a production analytics/crash provider beyond Firebase services already listed. If Crashlytics, Analytics, ads or attribution SDKs are added later, this map and the Play Data Safety form must be updated before release.

## Security principles implemented in source
- Production wallet writes are denied to normal Firestore clients.
- Wallet/gift changes go through trusted backend functions.
- Play purchases are verified server-side before coin credit.
- Duplicate gift/purchase operations use idempotent server records.
- Restricted/banned users are blocked from creating rooms, room messages, direct-message writes and follow writes after v3.0.1 hardening.
- Moderation queues and wallet operations are not writable by normal clients.

## Public Privacy Policy must additionally state
- Legal entity/controller name and support contact.
- Retention/deletion rules for accounts, messages, reports and purchase records.
- How users request account/data deletion.
- Final list of processors/providers and links to their privacy information.
- Countries/regions where data may be processed.
- Child/age eligibility and safety rules.
- Security limitations and incident contact.

Do not publish this internal mapping as the final Privacy Policy without filling the missing legal/account-specific details.
