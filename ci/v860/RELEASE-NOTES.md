# KING Plus v8.6.0 — Party parity stage

- Gift Shop now opens on an **All** tab with **64 visible gifts**, plus Relationship, Activity, Classic, Flying, Fame, Privilege, Filters and Parcel categories.
- Existing gift sending, room gift events, Gift Wall, rankings, quantities and TEST/no-billing behavior are preserved.
- Live Emoji first page now contains **50 original animated live reactions/effects**, including Tiger, Lion, Dragon, Crown, Diamond, Fireworks, Rocket, Heart Rain, Galaxy and more. The bundled 6 SVGA effects and 67 sticker assets remain available on separate tabs.
- Party chat now dismisses the Android keyboard after a valid message is sent/queued.
- The Game page exposes the full playable catalog instead of hiding many games behind Search.
- Room Multiplayer now includes direct launch controls for all local playable engines: Tic Tac Toe, RPS, Dice, Guess, Slot, Coin, Memory, Reaction Tap, High/Low, Wheel, Sheep Fight, Werewolf, Spy, Draw & Guess, Bingo, Crazy Zoo and Domino, plus Online Ludo.
- Existing synchronized room rounds (Ready/Start/Move/Finish/Rematch) remain for RPS, Dice Duel, Pick Number, Coin Pick, Lucky Wheel and Number Pick/Bingo mode.
- No billing was added. Existing TEST OTP/no-billing flow is unchanged.

## Validation target

Build with Java 17 / Gradle 8.9. Verify Party Gift Shop, all Live 50 effects, message keyboard dismissal, all Game cards, Room Games launcher and Online Ludo. Firebase-backed room actions still require the project rules/permissions to be deployed successfully for cross-device testing.
