# KING Plus — UX-first correction to match the Bolo Hi-style experience (2026-10-11)

## User's reported blocker
The current app still **does not feel like Bolo Hi from login to last option**, despite many functional buttons. Users must open extra tools/popups, go back, and navigate across many separate screens instead of performing common room actions naturally.

This is the highest-priority UX integration problem, not simply a missing-feature list. **Never equate number of screens/options with finished experience.**

## Verified root causes from KING integrated source
- `PartyActivity.roomMenu()` builds more than 50 items in a four-column vertically scrolling bottom dialog. The class contains ~193 AlertDialog.Builder occurrences (source occurrences, not necessarily 193 on-screen popups). Too many important actions hidden under More tools.
- `MainActivity.home()` calls `openPartyActivity()`; other app areas are implemented as independent Android Activities or reset their own view via `setContentView`. User back navigation and visual identity lack a single coherent shell.
- `SocialActivity.render()` originally exposed visible debug Firebase UID, an onboarding/test banner and Test Connection next to normal friend features.
- All five preceding feature areas compile, but available APKs have **not** passed two-device UX tests or screenshot-by-screenshot visual parity.

## Stage UX 1 — In progress (compile gate required)
- Preserve original `roomMenuAll()` for every existing option; replace top-level `roomMenu()` with a compact 12-action sheet: Mic, Members, Games, Live Emoji, Gifts, Private Chat, Multi Video, 3D World, Find User, Admin/Settings, Share and All Tools.
- Each primary tile dismisses the sheet and routes via existing original room action dispatcher on one tap. Avoid launch of duplicated Party Activities or resetting voice connection.
- Keep diagnostics accessible, but hide technical debug account UID and long testing instructions from the default People/Discover UI.
- Build stage with the original Firestore permissions, video rooms, Party seats, Ludo, gifts and VIP wallet locks preserved.
- See CI: https://github.com/avaiyakrushal/KING-Plus/actions/runs/38134220079 . DO NOT publish a final APK based solely on compilation.

## Full beginning-to-end redesign acceptance map (future stages, not complete)
| Screen | The experienced interaction expected | Required acceptance |
|---|---|---|
| Login/Welcome | Original KING-branded high-quality screen; clear Google/Facebook/phone buttons, Terms links | All authentic providers work, no fake OTP and no crash on entry |
| Home discovery | Visually rich live room cards, Hot/Music/Game/Video tabs and one-tap Join | Accurate live counts and categories, stable scroll and search |
| Party room main | Consistent room title/photo, Mic seats/host badges, bottom dock Chat/Gift/Emoji/Games/More | One tap per core action; background voice stays connected |
| Mic/Seat management | Tap seat, request seat, release/mute or invite/kick via role-safe sheet | Both phones update live; mic never triggers duplicate calls |
| Gift/Emoji shop | Fast category tab, preview/send to selected receiver, animation without losing room | Receiver sees event, sender sees own feedback; secure balances |
| Multiplayer games | Enter from Party overlay, games truly playable, return to SAME voice room | Turn state, winner, reconnect and memory tested on two devices |
| Friends/Inbox | Search KING ID, Follow Back, Profile, Chat, Invite within obvious tab bar | No Firebase debugging clutter; real two-account SERVER acceptance |
| Profile/VIP/Family | Consistent tabs and sheets, legitimate level/treasury and media gallery | Privacy, state persistence, backend entitlements and rollback |
| 3D/Video | Open 3D world/avatar and video in room with clear exit/return actions | Actual two-device RTC, permission handling, performance and safe private calls |
| Back behavior everywhere | Dismiss open sheet → previous tab within same section → Party/Home; never duplicate screen stacks | Verify physical/system gesture and top-left close, 10 repeats each |
| Error/Empty states | Same design, actionable Retry/Sign in, no technical stack traces on screen | Offline/slow/denied permissions do not crash or falsely claim success |
| Release | Original KING look, Bolo-Hi-inspired interactions, no copied brand art/code | Reference videos/screenshots side by side plus real device gate |

## Explicit release blocker
This is a multi-screen design/navigation project, not solved by adding more buttons or code-only features. Original polished UI assets + actual interaction tests of the reference app are still needed. Paid VIP/recharge, real OTP providers, native avatar customization and private video backend remain blocked by verified service configurations. No promise of instant 100% parity or final APK now.
