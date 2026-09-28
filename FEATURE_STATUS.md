# Current 2.7.1 status

Online economy, inbox/FCM and report moderation are implemented in source,
not yet deployed or tested on devices. See backend/SETUP.md for exact scope.
The local-only list below describes the earlier prototype.

# KING Plus v2.5 Feature Status

## Works locally in this source
- Firebase phone OTP flow wiring and Firebase Google sign-in wiring
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
- Real multi-user low-latency voice (Agora/Zego/WebRTC or equivalent)
- Real-time multi-device rooms/chat/presence
- Server-authoritative coins, gifts, rankings, missions and anti-cheat
- Push notifications
- Real-money recharge/payouts and store compliance
- Production moderation/report review tools
- Facebook/Meta sign-in credentials and Firebase provider configuration
- Cloud data storage, security rules, backups and admin APIs

This split is intentional: v2.5 improves the full app prototype without pretending local-only features are already online services.
