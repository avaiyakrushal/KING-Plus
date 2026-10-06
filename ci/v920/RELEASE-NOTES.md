# KING Plus v9.2.0 — Final Validation

This stage converts the remaining production verification work into an in-app two-phone test center.

- Adds Production Test Center inside live Party Rooms.
- Shows Firebase project, room document, signed-in user, member count, online members seen in the last 60 seconds, unique device count, occupied seats and voice-presence count.
- Reads recent chat, gift, live-emoji, multiplayer-ready and shared game-state data.
- Adds a two-phone realtime ping. A second KING Plus phone in the same Party Room automatically writes a sync acknowledgement; the first phone shows whether a second device actually received room events.
- Adds zero-cost Test Gift and Test Emoji room events for visual cross-device verification without spending wallet coins.
- Production Test Center can open the exact same Room-ID voice conference and Room Games.
- Party Tools and More menu include Production Test.
- Existing v9.1.0 real SMS OTP option, crash diagnostics, KTV/PK history, stale-member cleanup and profile search remain.

External configuration is still required for:
- Google Cloud/Firebase IAM permissions needed to deploy current Firestore Security Rules.
- Facebook provider credentials.
- WhatsApp OTP provider/backend.
- Production signing key / Play Console credentials for a store release.
