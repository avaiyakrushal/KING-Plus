# KING Plus v2.8.0 Feature Status

## Added in v2.8.0
- Cloud profile sync foundation using Firebase Firestore for real Firebase-authenticated users.
- TEST wallet cloud snapshot kept in a separate non-authoritative Firestore collection.
- Firebase Cloud Messaging client wiring, Android notification channel, runtime notification permission and device-token registration.
- Cloud moderation report submission for signed-in Firebase users plus local report audit history.
- Moderation & Safety center with block/report status and privacy controls.
- Recharge Center foundation with TEST coin packs and receipts. It never charges real money.
- Starter Firestore security rules separating profile/device data, test wallet snapshots, reports and locked production wallet paths.

## Already available in the test APK
- Free mobile login test mode: no SMS, no Firebase billing, fixed test OTP `123456`.
- Party/Home category UI and custom room creation.
- 12-seat voice-room UI, microphone level meter, local seat/admin/mute controls.
- Local room chat history, gifts, wallet/coins, transactions.
- Messages, profiles, follows/friends, local direct-message history.
- Moments with local text/photo posts.
- Profile photo/details, block/report/privacy settings.
- Game Center local mini-game outcomes/rewards.
- Daily check-in streak, missions, XP/levels.
- Family create/join/share code and agency prototype.

## Still requires external production setup/backend
- Real SMS OTP billing/provider setup and final Google/Facebook authentication configuration.
- Real multi-user low-latency voice (Agora/Zego/WebRTC or equivalent).
- Real-time multi-device rooms/chat/presence.
- Server-authoritative coins, gifts, rankings, missions and anti-cheat.
- Sending real FCM pushes from a trusted server/Cloud Functions.
- Production moderation dashboard with privileged admin roles.
- Real-money recharge/payout. This requires Play Billing/store product configuration, trusted backend receipt verification, server-side balance crediting and store/legal compliance.

The v2.8.0 APK remains a test/prototype build. The recharge simulator displays sample prices but never charges money.
