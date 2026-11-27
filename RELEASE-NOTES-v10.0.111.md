# KING Plus 10.0.111 — Server-driven Party voice readiness

- Home live-room categories use a realtime Firestore listener for the visible page and release it on refresh/navigation.
- Home category rooms page in groups of 8; Popular pages 24-room server-ranked windows.
- Party Room checks the authenticated backend voice config before allowing Cloudflare SFU voice. The server marks it ready only when activation flags and both provider credentials are present; no provider secret is returned to Android.
- Cloudflare voice remains off until the account is configured, Firebase Functions and moderation triggers are deployed, and an operator verifies provider billing. The zero-cost flag is not a spend cap.
- Backend tests cover absent credentials and secret non-disclosure. Two-physical-phone audio/reconnect acceptance remains unverified.
- CI output is a debug APK, not a production-signed release.
