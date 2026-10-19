# KING Plus ↔ Bolo Hi Parity Matrix — v9.4.0

Reference used:
- User-provided Bolo Hi APK: com.live.hey 5.30.5 arm64-v8a
- User-provided screen recording (Party, Gifts, Emoji, Ludo, drawer, VIP)
- Current KING Plus v9.3.1 source

Static package gap:
- Bolo: ~13,523 files, ~4,065 layouts, ~8,680 drawable resources, ~3,651 PNG, ~1,407 WebP, 13 SVGA
- KING: ~1,987 files, ~168 layouts, ~1,093 drawable resources, ~654 PNG, ~67 WebP, 6 SVGA
This gap is visual/state depth, not a target file-count. Do not pad resources.

Parity workstreams:
1. Login/account identity — stable Google identity, phone OTP, same account on multiple devices.
2. Party lobby — Hot/Event/Date/Music/Game tabs, dense 2-column room cards, search/fire controls.
3. Create Party — cover, public/private, seat mode, room name/type, seat preview, yellow Start Room.
4. Party room — header, room badges, dense seat grid, member strip, safety notice, chat composer, tools.
5. Gift Shop — weekly card header, recipient strip, categories, equal 4-column cards, quantity row, send.
6. Live Emoji/Stickers — bottom sheet, 5 tabs, equal grids, room-wide animation events.
7. Ludo — dedicated Ludo Lord lobby with item mode, online, 2v2, chat room, championship, offline entry.
8. VIP — premium tier card, progress, reward strip, privilege tiles, dark tier-specific visual treatment.
9. Me/Profile — top wallet pills, avatar/ID/VIP/level, social counts, Family Square, quick tools, records.
10. Drawer — Square, Privilege Pack/Shop, VIP, Noble, Family, User Level, Wallet, Invite, Settings, Help, Rules.
11. KTV — queue/stage/current singer/history and dedicated visual stage.
12. PK — red/blue battle layout, timer, contribution score, result/history and visual effects.
13. Family — family home, level, treasury, members, activity/check-in/chat/voice.
14. Rank/Discover — contribution/charisma/game/event ranking cards.
15. Games — room-linked multiplayer and dedicated presentation pages, not generic dialogs.
16. Profile/public identity — canonical name/photo/frame/level/VIP across devices.
17. Stability/reconnect — room membership self-heal, stale cleanup, diagnostics.
18. Final visual pass — spacing, typography, card radii, sheets, navigation, empty/loading/error states.

Copyright boundary:
- Recreate layout patterns/behavior independently under KING Plus branding.
- Do not extract or ship Bolo Hi proprietary source, icons, gift art, animations or Unity data.
