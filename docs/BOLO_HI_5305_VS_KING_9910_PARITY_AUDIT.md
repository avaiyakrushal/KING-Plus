# KING Plus → Bolo Hi 5.30.5: End-to-end Parity Audit (11 Oct 2026)

## Goal
Deliver a cohesive KING Plus-branded experience that follows the complete Bolo Hi interaction journey: onboarding → discover → party creation/join → occupied seats/mics → tools/moderation/games → gifts/emoji/VIP → social/profile/family → exit/re-entry. Do not substitute decorative buttons or fake data for working features. Use original KING Plus artwork/code and authorized services rather than redistributing proprietary Bolo Hi code/assets.

## Inputs inspected
- Original Bolo Hi Android APK: `com.live.hey` 5.30.5, supplied by user, 186,863,101 bytes.
- Current KING Plus v9.9.10 Stage15 TEST APK, 90,038,220 bytes.
- Matching v9.9.10 Stage15 source ZIP and active branch `work/king-plus-complete-five-sections`.
- Method: direct APK ZIP entry inventory, names of Android XML resources, bundled game assets and native libraries, plus targeted review of KING Plus Java sources. **No live two-device walkthrough or proof of Bolo Hi back-end operations yet.** An APK's presence of resources does not prove a feature presently works.

## Direct APK comparison
| Measure | Bolo Hi | KING Plus |
|---|---:|---:|
| Total APK entries | 13,523 | 1,987 |
| res/layout files | 4,065 | 168 |
| res/drawable files | 8,680 | 1,093 |
| assets files | 125 | 69 |
| ARM64 shared native libraries | 43 | 21 |
| DEX files | 11 | 7 |

These are structure counts, **not** percentages of feature parity or quality. The applications use different app stacks.

Bolo Hi's APK visibly includes:
- Login/home: `layout_login_select_page.xml`, `layout_home_main_page_new.xml`.
- Voice/party: `channel_voice_room_page_channel.xml`, `channeel_party3d_page_channel.xml`, `channel_page_micup.xml`, seat/invite/lock-room layouts.
- Games: `channel_room_chess_game_page.xml`, `channel_room_interactive_game_page.xml`, bundled game packages, Unity/Cocos libraries.
- Gift: `gift_menu_container.xml`, `gift_menu_item.xml`, gift wall layouts.
- Social/profiles: `dialog_bottom_profile_card_channel.xml`, family, follow, ranking, group/channel, friends layouts.
- KTV/video: `channel_page_ktv.xml`, multi-video room layouts, audio libraries.
- Dress/levels/recharge: wardrobe, mall, level, VIP seats, rechargeable virtual-currency UI resources.

KING Plus visibly includes custom Java activities such as `PartyActivity`, `KingNative3DRoom`, `OnlineLudoActivity`, `KingGiftCenterActivity`, `KingWardrobeActivity`, `KingPhoneOtp975Activity`, and Jitsi integration.

## High-priority technical gaps discovered from KING Plus source
1. **UI is not a unified Bolo-style screen system**: `MainActivity.java` constructs many screens directly using programmatically created `TextView`, `LinearLayout`, emoji button labels and text, rather than rich layouts/artwork and common components. Prioritize shared visual design and genuine navigation first.
2. **MainActivity legacy room fallback can show synthetic counts**: around the `room(String name)` method the room count is computed from `28 + abs(name.hashCode() % 220)`. Replace with authenticated live room presence, or show an honest loading/unknown state. **Do not claim that every current PartyActivity screen uses synthetic counts**; `PartyActivity` includes its own real-data path.
3. **3D is not fully equivalent**: `KingNative3DRoom.java` describes a first-stage geometric GLES room and live seats; it explicitly is not a fully rigged social avatar world. Bolo APK includes Unity/3D assets and libraries. Audit actual avatar, seating, gestures, motion, performance.
4. **Gift/VIP is partly preview**: `PartyActivity` includes local emoji/gift previews and explicitly disabled VIP payments/unlocks. Production wallet/recharge, secure ledger, authorized billing, animations, sounds and cross-device receipt must be verified independently.
5. **Games are not equivalent just because names/buttons exist**: real synchronized matches need both phones to see the same game state, valid turns, results, exits/reconnect and no fake awards.
6. **Voice/video**: KING integrates Jitsi and its own mic-state functions; Bolo bundles specialized real-time/native media libraries. Compare seat assignment, on-mic/off-mic semantics, permissions, room reconnect, video entry and multi-device latencies with physical phones.
7. **Social and room moderation**: profile card, invite, kick, mute, admins, host transfer, lock, block, following, family, and group/channel operations must persist and be enforced server-side, not only visually.

## Page-by-page implementation and acceptance checklist
### P0 — Trust, stability, and core visual parity (first)
- [ ] Crash-free cold start, login, Home, Party, Back/Leave, reopen; no hidden fallback that masks failures.
- [ ] KING-branded login layout matched to reference spacing, hierarchy, buttons and transitions.
- [ ] Home Party/Discover/Inbox/Me entry layout, room list card, selected-state, and floating overlays consistently redesigned.
- [ ] Party core: room background, header, live seats and occupant avatars, footer toolbar and submenus with consistent responsive sizes.
- [ ] No fabricated participant counts/occupants in **any** active path.

### P1 — Fully functional Party Room
- [ ] Create/join public/private/locked room by real room ID; host/invite link opens correct target.
- [ ] Cross-device mic/seat occupancy + mute states; a mic toggle never accidentally initiates a phone call.
- [ ] Tap seated user's DP to see real public profile, Level, Charisma, Noble/VIP and actions.
- [ ] Host/admin/promote/demote/kick/mute/lock/block works for appropriate roles; other phone immediately updates.
- [ ] Notice, room setting panels, games, gifts, emoji, menu navigation and Back flows are complete.

### P2 — Social, profile and economy
- [ ] Search/follow/friends/chat/invite, Family/Group, notifications and profile with server persistence.
- [ ] Distinct gift animation, synced recipient, correct individual sound and gift history.
- [ ] VIP/Noble/level visibility and rules. Real-money recharge only when secure provider/billing and server verification are available; do not credit coins or premium status locally.

### P3 — Games, KTV and 3D world
- [ ] Ludo and all advertised room games actually playable, server synchronized and reconnection-safe.
- [ ] KTV/music/video feature check; use legally authorized audio content/services.
- [ ] Full 3D environment with KING-original models, avatar dressing, seat/world movement and support for lower-memory phones.

### P4 — Complete release acceptance
- [ ] Two phones on separate networks, real accounts: room create/join + communication + gifting + games + social + host controls + crashes.
- [ ] Capture side-by-side phone recordings for Bolo Hi and KING Plus with the same user journey and mark all visual/function differences.
- [ ] Every tap leads to a real working screen, error state or clear disabled notice; no fake success or pretend users.
- [ ] Signed APK upgrades the previous install safely, with data preserved. Provide working APK + source ZIP only after successful build and acceptance.

## Important limits
This report establishes **APK-structure and KING-source parity gaps**. It does **not** certify that the Bolo Hi app can currently connect to its old servers, nor that all listed layout screens were navigated live, nor that KING Plus is now equivalent. That requires a running reference build and real-device, multi-account testing. No new APK was built by this audit.

## Engineering recommendation
Begin with P0 login → home → Party Room UI/navigation and stability. Stop adding disconnected feature labels to the UI. Define a shared design system and route contracts, then implement real room data, multiplayer and monetization in order, with user-visible acceptance on each screen. Maintain KING Plus own identity/artwork and use reference behavior/design as the target.
