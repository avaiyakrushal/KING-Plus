# KING Plus v8.6.4 — synchronized Tic Tac Toe

Based on v8.6.3 synchronized Party mini-games.

This stage adds a real shared-state Party Room Tic Tac Toe flow using the existing Firebase room game collections and current deployed rules shape:

- Host/co-host starts Tic Tac Toe after at least two room members tap Ready.
- The first two ready players are assigned X and O.
- Both phones see the same 3x3 board and current turn.
- Only the player whose turn it is can submit a square.
- Player moves are written through the existing `game_moves` path; a room moderator atomically validates and applies them to the shared `game_state` board.
- Win/draw detection, shared result and rematch are handled in the room state.
- Spectators can watch the same live board without being able to move.

All v8.6.3 synchronized games, v8.6.2 Party Ludo, v8.6.1 Party UI fixes, gifts, Live 50 effects, no-billing mode and TEST OTP behavior are preserved.

Physical two-phone runtime testing is still required to verify latency and Firebase permissions on the deployed project.
