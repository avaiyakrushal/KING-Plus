# KING Plus v8.6.3 — synchronized Party mini-games

Based on v8.6.2 Party Ludo integration.

This stage expands the existing Firebase-backed Party Room synchronized round engine with five additional playable modes:

- 🎰 Slot Clash — each player spins once; best reel score wins.
- ⚡ Reaction Tap — each player taps once; the fastest measured reaction wins.
- 🃏 High / Low — players pick High or Low and the shared room draw decides the winners.
- 🐑 Sheep Race — each player runs once; highest race score wins.
- 🦁 Crazy Zoo — each player releases one animal; highest power wins.

These modes use the same Party room ready list, host/co-host round start, one locked move per player, shared result, rematch and room event flow already used by RPS/Dice/Number/Coin/Wheel/Bingo. No billing was added and TEST OTP remains unchanged.

Local playable cards remain available for the full game catalog. More complex turn-based/social games such as Tic Tac Toe, Memory, Werewolf, Spy, Draw & Guess and Domino still need dedicated shared-state engines for true cross-phone synchronized play.
