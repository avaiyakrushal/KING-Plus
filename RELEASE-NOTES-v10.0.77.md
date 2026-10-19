# KING Plus v10.0.77 — Party reconnect choice (TEST)

- Party no longer exits immediately on the first server snapshot that shows the current member missing. It waits briefly, re-reads membership and the room from Firestore SERVER, verifies ban state, then offers Rejoin or Leave. Closed rooms and active bans still exit safely. Failed membership reads keep the Party screen open with a reconnect status.
- Keeps v10.0.76 Discover Hot server-ranked pagination, v10.0.75 follow notification names and Android 16 URI crash handling.
- Version code 356; version name `10.0.77-party-rejoin-choice`.

Full source regression checks and Android CI build are required before APK delivery. Two-device Party acceptance, voice/video, production payments and every game round still require live services/devices.
