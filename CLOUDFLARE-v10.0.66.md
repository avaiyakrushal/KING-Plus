# KING Plus 10.0.66 — Cloudflare preparation TEST

This is a development build, not activated production voice. The existing Jitsi path remains the default.

Implemented: replaceable Party voice boundary; local-only WebView audio WebRTC endpoint;
authenticated Firebase callable signaling for raw Cloudflare Realtime SFU; server-only credentials;
room-visit ownership; mic-seat and host-mute checks; serialized negotiations; per-track errors;
server moderation cleanup; audio-only publish; explicit native mic consent; actual connection callbacks;
leave/auth/lifecycle cleanup. No card, account, billing, API or hosting plan was enabled.

## Activation blockers

1. No configured Cloudflare SFU App ID/App Secret or connected Cloudflare management account.
2. No verified strict zero-cost cutoff. The 1,000 GB allowance is followed by paid overage;
   manual flags below are a gate, NOT an implemented usage meter or a financial spending cap.
3. Existing Firebase Functions deployment is blocked by Cloud Build / Artifact Registry permissions.
   This broker and its Firestore triggers have their own hosting and database costs. They must not
   be deployed as a supposedly free solution. An alternative free backend may be needed.
4. No physical two-phone or 100–150-concurrent-user test. Client discovery currently polls every
   10 seconds and is a development implementation, not a verified 10,000-user free hosting design.
5. No TURN relay credentials yet; networks that cannot reach the SFU directly require relay testing.
6. One active connection per user/room/visit. Unknown provider responses are not blindly retried.
   Crash leftovers and permanently abandoned operation locks need operator reconciliation before rollout.
   Sessions expire for new operations after one hour; this is not a guarantee that existing egress stops.

Keep Android `BuildConfig.CLOUDFLARE_VOICE_ENABLED=false`, server `CLOUDFLARE_VOICE_ENABLED=false`
and `CLOUDFLARE_ZERO_COST_VERIFIED=false` until these are resolved. Never paste secrets into a chat,
Android resources, APK, JavaScript, Firestore client-readable documents, or repository.

Provider documentation checked 2026-10-17:
- https://developers.cloudflare.com/realtime/sfu/platform/pricing/
- https://developers.cloudflare.com/realtime/sfu/api/
- https://developers.cloudflare.com/realtime/sfu/get-started/connection-patterns/

## Validation

Run `node --test functions/*.test.js tools/cloudflare-v10066/client.test.js` and
the real Firestore emulator test `tests/firestore/cloudflare-voice-v10066.test.mjs`.
The provider HTTP API is mocked in automated tests. These tests do not prove live voice,
free billing behavior, mobile playback, network traversal or production load capacity.
