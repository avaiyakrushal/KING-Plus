# KING Plus v8.6.6 — Werewolf, Spy and Draw & Guess

Based on v8.6.5.

- Adds synchronized **Werewolf** to Room Multiplayer. Requires at least 4 ready players. One non-host player receives the Werewolf role privately; other ready players receive Villager. Players discuss in the existing Party Room voice/chat, vote or change vote, and host/co-host reveals the result.
- Adds synchronized **Spy Game**. Requires at least 3 ready players. One non-host player privately receives Spy; the others privately receive the shared secret location. Players discuss and vote, then the host/co-host reveals the result.
- Private Werewolf/Spy role cards are delivered through the existing per-recipient room-invite documents, so no new Firestore rule family is required.
- Adds synchronized **Draw & Guess**. Host/co-host is the drawer, sees the secret word, draws on a shared canvas, and the canvas syncs through the existing moderator-writable game state. Other room players submit guesses and the final result reveals the word and correct guessers.
- Existing v8.6.5 Memory Duel + Domino Match and all earlier synchronized room games remain available.
- Existing Party-room Online Ludo, Gift Shop, Live 50 effects, message/keyboard layout fixes, TEST OTP and no-billing behavior remain unchanged.

Physical multi-phone testing is still recommended for realtime latency and role delivery.