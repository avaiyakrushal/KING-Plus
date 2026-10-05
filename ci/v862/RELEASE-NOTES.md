# KING Plus v8.6.2

Based on the successful v8.6.1 Party UI build.

Party Room game integration stage:
- Online Ludo now launches with a stable 6-character match code derived from the current Party Room instead of opening as an unrelated standalone lobby.
- Host/co-host can create the Party Room's Ludo match the first time it is opened.
- Other room members opening Online Ludo are routed to the same Party Room match and can join while the match is in the lobby phase.
- Party Ludo match documents store the originating Party Room id/name so an unrelated match cannot silently reuse the same code.
- Existing 2/4-player realtime Ludo Ready/Start/turn/move/winner/rematch behavior is preserved.
- v8.6.1 fixed bottom composer, keyboard dismissal, gift/emoji sheet sizing and safe emoji rendering are preserved.
- v8.6.0 64-gift catalog, Live 50 effects, local playable game engines, TEST OTP and no-billing mode are preserved.

Cross-device Party Ludo still depends on deployed Firebase rules/permissions and should be verified on two signed-in phones after installation.
