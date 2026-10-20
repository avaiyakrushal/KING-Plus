# KING Plus v10.0.36 — combined recovery changes (TEST)

Baseline: user-supplied v10.0.35 source. Version code 315.

- Me: gender selection works; profile edits are confirmed by Firestore before local success; rejected writes retain editable fields and allow retry; account-change callbacks cannot overwrite another profile.
- Home/shared Moments: cached and pending snapshots have visible labels; metadata-only confirmation changes refresh the feed.
- Party: invalid document paths rejected before join state changes; lobby cache labels refresh on metadata changes; room string metadata tolerates wrong types.
- Game: Reaction Tap cancels the pending GO signal and invalidates interrupted timing on pause. Retry starts a fresh round.
- Discover: followed profiles fetched from server; partial child-task failures show an explicit retry rather than a complete-looking empty result; malformed target IDs skipped.
- Messages: malformed IDs rejected; room invitations remain unread on lookup failure or closed rooms; retry is offered; read acknowledgement happens only after navigation; metadata-only inbox updates refresh cached status.

This is not a full Bolo Hi implementation or final production acceptance. Remaining unverified gates include two-phone voice/video and games, production rules/provider configuration, real OTP/Facebook, secure recharge, and complete 3D/game parity. See existing integrated 16-gate matrix. CI compilation and emulator tests do not prove physical-device acceptance.
