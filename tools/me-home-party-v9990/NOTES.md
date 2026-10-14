# v9.9.90 TEST — integrated Me / Home / Party saved rooms and room entry

- New shared My Party Rooms screen for Me and Home/Party: live Favorites/Recent, favorite toggles, offline-state label and opening real rooms.
- Saved rooms live on the owner's private users document under savedPartyRooms9990. Transactional edits preserve profile/family fields and concurrent phone updates. No new security rules or deployment is required relative to the supplied rules. Production access still requires validation.
- 60 favorites and 40 recent visits retained. Favorite-only changes do not create fake visits. Existing account-scoped local history is imported only if cloud history is absent; unowned pre-v9.9.89 history is not imported.
- Moments room broadcasts and Community search now pass the correct verified room identifier.
- Deep Party menus reuse the existing Party Activity and requested Music/Admin/Theme/etc. panels open after membership is established, including new intents for the same room.
- Me Status/Moments now uses the shared live feed instead of local-only posts. Live feed updates preserve the composer; Like/unlike uses transactions and displays its actual result. Old local post data remains stored.
- Previous v9.9.89 fixes are preserved. No paid credits, VIP grants or simulated recharge introduced.

CI: pure Java data/retention regressions, Firebase emulator with two clients sharing one account plus a denied second user/guest, concurrent transactions and live listener, full Android compile and same APK signer check.

Not Final: Family Rewards/treasury, true paid recharge/VIP services, production configuration, full game/3D parity and physical two-phone voice/video/gifting acceptance remain incomplete or unverified. Emulator clients do not verify physical phones or audio.
