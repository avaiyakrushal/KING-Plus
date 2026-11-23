# KING Plus Production RTC Setup

The existing `VoiceWebActivity` is an experimental Jitsi-based test path. Do not treat it as the public production voice stack.

## Current KING Plus Cloudflare wiring
The repository contains a Cloudflare SFU broker and Android room client, but activation is intentionally opt-in. The checked-in defaults leave both backend flags off and the Android build flag off:

- Firebase Functions booleans: `CLOUDFLARE_VOICE_ENABLED` and `CLOUDFLARE_ZERO_COST_VERIFIED`.
- Firebase Functions secrets: `CLOUDFLARE_SFU_APP_ID` and `CLOUDFLARE_SFU_APP_SECRET`.
- Android Gradle property: `-PkingPartyCloudflareVoice=true` for a build that is meant to use the Cloudflare client.

The Android property does not configure the provider. Production voice requires the server flags and secrets to be provisioned, Functions and the moderation triggers to be deployed, and a matching Android build. The `CLOUDFLARE_ZERO_COST_VERIFIED` flag is an operator assertion, not an enforced spend cap or a guarantee of zero charges. Keep it false until the provider account's usage and billing terms have been checked. With the default Android build, private/password rooms do not get a fake voice connection; the UI reports that a voice service is required.

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
