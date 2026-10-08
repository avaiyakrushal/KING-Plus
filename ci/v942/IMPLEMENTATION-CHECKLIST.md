# KING Plus v9.4.2 — implementation checklist

Base: v9.4.1 stability (do not regress navigation or login).
Reference: Bolo Hi user-provided APK; independently implement features. No proprietary assets/code reuse.

## Code-level work completed in v9.4.2
- [x] Stability: root navigation guards from v9.4.1 retained; v9.4.2 adds explicit microphone permission gating.
- [x] Reconnect: active Party rooms now rebind realtime listeners, heartbeat and inline audio mute state when connectivity returns.
- [x] Party: existing host/co-host, seat request/invite, kick/ban, locked/private room and in-room mic flows retained.
- [x] Games: exposed non-Ludo games stay on synchronized Party/RoomGame routing; Ludo keeps the dedicated realtime flow.
- [x] Gift/Emoji: realtime dedupe retained and low-memory animation pressure guard added.
- [x] KTV/PK: synchronized room-state flows retained; network failures now expose retryable states.
- [x] Social/Profile/VIP/Family: existing real profile/social routes retained with Discover loading/offline/retry behavior.
- [x] UI: adjustResize applied on major interactive screens to reduce keyboard/content clipping.
- [x] Build gates: debug compile, route audit and Android lint complete successfully in run 37710317465.

## Runtime/device validation still required before calling it final
- [ ] Capture reproducible crash logs on Android 13–16 for Home, Party, Games, Inbox and Profile.
- [ ] Validate login expiry and Firebase permission failures on a real signed-in account.
- [ ] Verify host/co-host moderation and private/password rooms with two accounts.
- [ ] Verify Ludo turn rules, finish/reconnect/rematch on two devices; verify every other exposed room game end-to-end.
- [ ] Verify Gift/Live Emoji animation timing on low-memory and normal devices.
- [ ] Verify KTV singer queue and PK timer/result/reconnect simultaneously on two devices.
- [ ] Verify followers/profile/chat/Family/VIP/Rank/Collection/Wardrobe empty/loading/error states against live data.
- [ ] Final typography/spacing/insets visual comparison from screen recordings.
- [ ] Run final two-account/two-device smoke test before marking Bolo-reference parity complete.

Acceptance: passing Gradle/static/lint checks is necessary but not sufficient. Do not claim crash-free or full parity until the runtime/device gates pass.
