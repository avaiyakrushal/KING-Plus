# KING Plus v2.7.1 Feature Status

## Works in the current test APK
- Free mobile login test mode: no SMS, no Firebase billing, fixed test OTP `123456`
- Mobile number validation and local login session
- Clickable Terms & Privacy and Trouble logging in help pages in the generated APK
- Friendlier Google error-10 explanation in the generated APK
- Firebase Google sign-in wiring remains present for later production setup
- Party/Home category UI and custom room creation
- 12-seat voice-room UI, microphone level meter, local seat/admin/mute controls
- Local room chat history, gifts, wallet/coins, transactions
- Messages, profiles, follows/friends, local direct-message history
- Moments with local text/photo posts
- Profile photo/details, block/report/privacy settings
- Game Center local mini-game outcomes/rewards
- Daily check-in streak, missions, XP/levels
- Family create/join/share code and agency prototype

## Requires external setup or backend for production
- Production SMS OTP (Firebase Phone Auth billing/provider setup)
- Google login: register the GitHub-built APK signing SHA-1 in Firebase/Google OAuth
- Facebook/Meta sign-in credentials and Firebase provider configuration
- Real multi-user low-latency voice (Agora/Zego/WebRTC or equivalent)
- Real-time multi-device rooms/chat/presence
- Server-authoritative coins, gifts, rankings, missions and anti-cheat
- Push notifications
- Real-money recharge/payouts and store compliance
- Production moderation/report review tools
- Cloud data storage, security rules, backups and admin APIs

The current APK is a test/prototype build. Local-only features are intentionally not presented as live cloud services.
