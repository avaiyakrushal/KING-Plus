# KING Plus v8.1 Deep Parity Audit

Reference analyzed: user-provided Bolo Hi 5.30.5 arm64 APK and three user-provided walkthrough videos.

## Reference scale
- 13,523 APK entries
- 3,955 layout resources
- 3,489 drawable resources
- 1,083 game-related resources
- 427 party-related resource hits
- 234 gift-related resource hits
- 286 family-related resource hits
- 11 DEX files
- 86 native library files

## High-level parity map
1. Login/account: login surface, phone/Google, account/security/history/device flows.
2. Home/Party: room discovery, create/join/private/password, 8/10/12 seats, room categories, Party Master/Data, admin, members, rankings, notices.
3. Play Center: room games, templates, events, room tools, vote/truth-dare/counter/wheel/battle.
4. Gift: recipient, quantity, categories, quick gift, VIP gift, gift wall/wish/lucky/bag/history/ranking.
5. Games: local playable catalog plus realtime room rounds and Ready/Start/Result/Rematch.
6. KTV/Music: song request, queue, radio mic, room music/audio controls.
7. Voice/Video: native media session plus compatibility fallback, multi-video entry.
8. Social: Search, Moments/Square, followers/friends, Messages, Visitors, Family and Relationship/CP.
9. Profile: edit, gallery, level/achievements, medals/influence, gifts, joined channels, cosmetics.
10. VIP/Noble: VIP progression, badges, frames, entrances, seat identity and gift unlocks.
11. Wallet/Shop: test/no-billing wallet, inventory, privilege pack/shop and history.
12. Settings/Safety: account, privacy, notifications, network/media, block/report, help/rules.

## v8.1 changes
- Adds a persistent deep-navigation activity so nested option -> sub-option -> detail pages use one predictable stack instead of unrelated dialogs.
- Extends VIP progression from 12 to 50 while preserving early thresholds.
- Adds VIP 0-50 preview bands and Noble derivation.
- Adds deeper Privilege Pack/Shop, Wallet, Settings, Help, Rules, Party Play Center, Gift Center, Music/KTV, Voice/Video, Tasks/Events, Profile/Level/Medals, Social and Discover paths.
- Replaces the Party room overflow menu with a full scrollable drawer-like panel and routes games into the deep flow.
- Expands Firebase realtime room games from RPS/Dice/Number to RPS/Dice/Number/Coin/Wheel/Bingo.

## Boundary
KING Plus uses its own implementation, Firebase data model, assets and native/open SDKs. Proprietary Bolo Hi code, private server APIs, paid inventory, licensed game binaries and copyrighted art are not embedded or copied.
