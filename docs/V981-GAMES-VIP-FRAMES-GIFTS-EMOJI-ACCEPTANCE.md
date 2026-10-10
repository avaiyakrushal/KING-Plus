# KING Plus v9.8.1 — Games, VIP, Profile Frames, Gift, Emoji

## What this changes compared with last successfully installed v9.7.9

### 1. Actual playable Games from the main Game page
The previous Game tab launched `PartyActivity` when a user tapped **any** non-Ludo title. No interactive game started. The v9.8.0 patch connects 18 advertised game IDs to interactive `GamePlayActivity` modes (solo/bot/pass-phone) and keeps Ludo's dedicated online/offline Lobby:

- Ludo Online/Offline, Werewolf deduction, Draw & Guess pad, Bingo 5×5, Domino Match
- Spy clues, Sheep Fight training/battle, Crazy Zoo collection, Memory pairs
- Rock Paper Scissors, Dice Duel, Lucky Wheel, Number Guess, Coin Toss
- Reaction Tap, High/Low card, Tic-Tac-Toe, free Lucky Slot (no cash prizes).

**Online Party** has a separate button for existing Firebase multiplayer rounds: RPS, Dice, Coin, Wheel, Number Pick and Bingo. Ludo has its own separate 2-player/4-player online code. **Other game modes listed above are actual solo/pass-phone games, NOT claimed as full realtime multiplayer**. Werewolf/Spy/Draw & Guess secret-role synchronous online protocols would require additional work and game-specific anti-cheat rules.

### 2. Genuine VIP progress is not TEST points
The previous `MainActivity.vipPage()` displayed local `LevelSystem` VIP, including misleading TEST progression through offline gift activity. New VIP opens `KingVipVisualActivity` backed by `KingBackend976.fetchWallet` and a server-authenticated `/wallet` response. The VIP info now states VIP grows only by verified **Diamond Recharge**, not free Gift sends. Normal XP remains separate from paid VIP. Public profile/Party member sync no longer republishes client-local TEST VIP as paid entitlement.

**Paid Diamond Recharge, paid Gift charges and unlockable premium VIP remain gated/off until the owner configures a trusted server and legally compliant payment channel.** No billing/charges are activated by this APK.

### 3. New functional avatar Frame & Entrance Effect gallery
Open Profile → Backpack → Choose Frame/Entrance Effect. The gallery shows original colored Frame previews or entrance effect icons; level/free conditions are shown. Levels 5 and 10 can unlock free Music/Heart frames, level 3 unlocks Welcome Sparkle. Premium frames and paid effects remain locked until genuine verified VIP. Selecting an unlocked frame writes `public_profiles/<auth UID>` and acknowledges Firebase success before replacing device-local selection. If offline, logged-in user retains their prior selection and sees a retry error; guests get a local-only preview.

On another phone, reopen Profile/Party to read the updated equipped Frame. The Family/Party member frames are otherwise preserved.

### 4. Real FREE Gifts; no false local Diamond debit
The old `MainActivity` demo room and Gift Catalog included false local Diamond deductions and pretend user/recipient names. Gift Center now offers links to real Party or Private Chat where the sender/recipient and Firebase Gift events are authoritative. FREE Gifts remain zero value and do not grant paid VIP.

### 5. Realtime Live Emoji
Existing 50 original Live Emoji previews, reference effects, stickers, and drawable animation classes are retained. The Room event listener now captures both room ID, Firebase UID and generation: delayed callbacks from an abandoned Party visit cannot play Emoji in the user's new room. The catalog clearly labels free original effects instead of fake VIP-exclusive stickers. The existing live room Firestore event write and other-phone listener remain, with no new Cloud Function or billing dependency.

## Acceptance checks on Android phone (must still run)
1. Install v9.8.1 over a previous KING Plus test APK. Verify version name, keep data. Tap **every** visible Game tile and use at least two controls in that game's actual view; Back works.
2. Ludo: test Create 1v1, Join Code using a separate Google account on a second phone; roll dice and see turn transitions on both phones. Test 4P separately; untested online paths must not be represented as verified.
3. Party Room: two or three different Firebase UIDs join; host starts each of the six known multiplayer rounds from Room Games; members Ready, move, see results, rematch.
4. On phone A equip Music Frame at normal level 5. Open Profile/Party on phone B logged into the same account: see identical Frame. Attempt Crown Frame without verified recharge: locked. Login as unrelated account: should not inherit Frame.
5. Open VIP: Device-only/test coins or free gift sending must NOT promote paid VIP. Only genuine backend Recharge (when deployed) can change it.
6. Party A sends B a FREE gift: B sees sender/recipient and matching animation without Diamond deduction or fake VIP promotion. For real-money Gifts the feature is intentionally off.
7. Send Live Emoji from A while B stays in same room; B sees animation. A leaves and joins a different room; old room's delayed events do not flash into the new room.
8. Leave/join repeatedly on low-memory vivo Android 16; report any native Room crash including timestamp and version.

## Release honesty
Successful `assembleDebug` + Java tests prove an APK compiled and basic code invariants hold; they do not prove the app is Bolo Hi-identical or that every realtime mode was tested on physical devices. Never reuse Bolo Hi proprietary assets/source.
