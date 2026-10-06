# KING Plus v9.4.0 Full Parity Overhaul — internal status

Rule: no APK is published from this branch until the parity checklist is reviewed as a whole.

## Completed source-level parity passes
- Stable Gmail identity / cross-device profile restore from v9.3.1
- Party Room opens UI before membership sync completes
- Party lobby: original category room covers replace giant initial placeholders
- Party Room: seat frames, crowd/seat counts, chat composer, themes, Gift/Emoji realtime flows retained
- Gift Shop: original illustrated KING gift art, equal card grid, categories, quantity/send flow
- Live Emoji/Stickers: compact equal grid + VIP-exclusive KING pack treatment
- Game Home: illustrated original game cards
- Dedicated KING Ludo lobby: Online / 2VS2 / Chat Room / Championship / Offline
- Discover: 2-column profile cards + Hot/New/Nearby/Follow presentation
- Messages: Recent friends in room + system rows + real chat threads
- Drawer: Square, Privilege Pack/Shop, VIP, Noble, Family, User Level, Wallet, Invite, Settings, Help, Rules
- VIP: dedicated premium screen, tier carousel, progression card, reward pack and privilege grid
- KTV / PK / Family / Rank: expanded animated presentation panels and existing realtime controls
- Login: original premium KING backdrop and consistent sign-in hierarchy
- Wallet: dedicated Diamond/Crystal visual structure while keeping no billing

## Still requires visual/regression review before APK
- Full Party Room pass across Classic/KTV/PK/Love/Game/Royal/Galaxy/Neon/Festival/Ice themes
- Create Party spacing/cover/seat-preview pass
- Gift/Emoji sheets at multiple Android screen sizes
- Ludo/game screen transitions, loading and result-state consistency
- Public profile / Me / relationship / collection detail screens
- KTV queue/stage empty/loading/error states
- PK active/result/history empty/loading/error states
- Family create/join/level/treasury detail states
- Rank top-3/pagination/error states
- Settings/help/rules/noble/privilege detail pages
- Final typography, spacing, bottom-nav and Android insets pass
- Physical multi-device verification still depends on live Firebase rules/IAM

## External blockers
- Firebase production Security Rules deployment IAM 403
- Facebook provider requires Meta/Firebase credentials
- WhatsApp OTP requires approved provider/backend

Copyright boundary: KING Plus uses independently implemented layouts and original generated/drawn visuals. No Bolo Hi proprietary source, gift art, icons, animation packages or Unity content is shipped.
