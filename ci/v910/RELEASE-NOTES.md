# KING Plus v9.1.0 — Final Completion Stage

This stage continues the user's 1–12 checklist on top of v9.0.0.

- Mobile login now exposes REAL Firebase SMS OTP as well as FREE TEST OTP 123456.
- WhatsApp OTP is clearly separated because it requires a verified WhatsApp Business provider/API; no fake WhatsApp OTP is created.
- Facebook login status now reports the real external setup requirement instead of pretending a local login worked.
- Party voice now requests microphone permission before entering the shared Jitsi room; compatibility fallback counts as a successful voice launch.
- Live Emoji Firestore batches now surface sync success/failure through the Party online-state chip.
- Gift event + message writes are now one Firestore batch so other phones do not receive only half of a gift event.
- Multiplayer game entry reports the real current room-member count after membership self-heal.
- KTV queue requests are consumed when the host starts them, so already-started songs do not keep reappearing in the queue.
- PK gets an explicit End + Result action which writes shared final red/blue scores and a PK result event.
- Match Center now supports name/KING-ID search and Level/VIP filters.
- Crash reports include version and Android/device metadata.
- Existing v9.0.0 heartbeat/rejoin, Room Code/full ID join, profile sync, realtime game state, visual parity, chat/keyboard fixes and no-billing behavior remain.

External blockers that code alone cannot remove:
1. Firebase Firestore rules deployment is still blocked by Google IAM/service-account permission.
2. Real Facebook login requires Meta App ID/App Secret and Firebase Facebook provider configuration.
3. WhatsApp OTP requires a verified WhatsApp Business messaging provider and server credentials.
