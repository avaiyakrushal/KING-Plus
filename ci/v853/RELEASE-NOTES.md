# KING Plus v8.5.3

Based on v8.5.2, preserves Ludo board and original emoji assets.

Fixed Room Multiplayer activity not attaching its content view (blank screen). Existing six room mini-games: RPS, Dice Duel, Pick Number 1–30, Coin Pick, Lucky Wheel, and Number Pick 1–9. Number Pick is labelled accurately; it is not full multiplayer Bingo.

Require two ready players to start and two submitted moves to finish. Atomic cleanup/start, transaction-locked single submissions, stale-round guards, rematch readiness checks, immediate control refresh, and nine number choices arranged in three rows. Draw results generated at finish and stored once; this is casual no-stakes play with host/client trust, not server-authoritative competitive play. Existing rules permit reading RPS moves and deleting one's move; UI locking is not cheat-proof.

Local games: scrolling avoids clipped controls; Memory waits for comparison; Bingo only marks actually called numbers, draws without repeats and recognizes diagonals; Reaction Tap cancels early starts and supports retries; Guess enforces 1–20 and locks after success. All Ludo entries in GamePlay now open the real Online Ludo screen.

Known incomplete scope: Sheep Fight, Werewolf, Spy, Draw & Guess, full Bingo, Domino, Memory and other local-only activities have not been converted to full room multiplayer. No claim of complete Bolo Hi parity. APK compilation is CI-verified; device/runtime and two-phone end-to-end tests remain outstanding. Existing Firebase Rules deployment is blocked by HTTP 403 and was not retried or bypassed in this build. No billing or IAM changes.
