# KING Plus v9.4.2 — implementation checklist

Base: v9.4.1 stability (do not regress navigation or login).
Reference: Bolo Hi user-provided APK; independently implement features. No proprietary assets/code reuse.

## Required gates (in order)
- [ ] Capture reproducible crash logs on Android 13–16 for Home, Party, Games, Inbox, Profile; fix root cause rather than relying only on Throwable guards.
- [ ] Validate login expiry, Firebase permission failures, microphone denial, room disconnect/reconnect, and back navigation.
- [ ] Party: verify host/co-host permissions, seat invite/kick/ban, locked/private rooms, microphone toggling without starting a call.
- [ ] Room games: real multiplayer synchronized state, valid turn rules, finish/reconnect/rematch for Ludo; clearly label any other games not yet playable.
- [ ] Gifts/live emoji: room-wide events, animation lifecycle, deduplication, accessibility and low-memory fallback.
- [ ] KTV and PK: singer queues, state transitions, timers, results and reconnect.
- [ ] Social: real followers, profiles, chat, Family, VIP, Rank, Collection/Wardrobe and proper empty/loading/error screens.
- [ ] UI parity: original KING Plus art, responsive insets, text clipping, animations, dialogs and navigation.
- [ ] Run release smoke tests with two accounts and two devices; compare screen recordings before marking complete.

Acceptance: Gradle build + static checks are insufficient. No claim of crash-free or full Bolo Hi parity until device tests pass.
