# KING Plus Production RTC Setup

The existing `VoiceWebActivity` is an experimental Jitsi-based test path. Do not treat it as the public production voice stack.

## Production requirements
A production RTC provider must support:
- Android audio rooms with low latency.
- Server-issued, short-lived room tokens.
- Host/speaker/listener roles.
- Mute, remove, block and room lock controls.
- Reconnect after network changes.
- Abuse prevention and rate limits.
- Region selection and capacity monitoring.
- No provider secret embedded in the Android APK.

## Recommended architecture
1. Android asks the KING Plus backend for a room token.
2. Backend verifies Firebase Auth, ban status and room permissions.
3. Backend signs/requests a short-lived RTC token using provider credentials stored only on the server.
4. Android joins the provider room with the short-lived token.
5. Room moderation actions are authorized on the backend and mirrored to the RTC provider.
6. Provider webhooks, when available, are verified server-side for join/leave and abuse/audit events.

## Provider choices
LiveKit, Agora, Zego or a controlled WebRTC/Jitsi deployment can fit this architecture. The final provider must be selected based on account availability, India-region latency, pricing, moderation controls and token support.

## Launch test
Before public release, verify on two physical devices:
- both users can join and hear each other;
- mute/unmute is reflected correctly;
- host can remove a participant;
- banned user cannot obtain a new token;
- token expiry/reconnect works;
- app survives Bluetooth/headset changes, phone calls, background/foreground and network switching.
