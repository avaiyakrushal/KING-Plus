# KING Plus 10.0.67 — Home, Me and Party corrections TEST

Reference actually read: uploaded BoloHi 5.30.5 arm64 APK,
SHA256 `6b9184ce120607c21f9ed9259ca207e65971e737393f0b114277ce73bff0caf2`.
Its ZIP contained 13,523 entries. Binary Android layouts and resource names/string values
were decoded for static comparison. The APK was not installed or run: this environment
has no Android device/emulator. No proprietary BoloHi code, graphics or audio was copied.

| Reference evidence | KING Plus finding and change |
|---|---|
| `layout_me_profile_header`: 90dp avatar, name, ID, followers/following/friends, family entry | Existing identity/stat/family routes retained; server photo and editable fields now update Me live. |
| Me header and `me_list_item_wallet_balance_layout`: separate diamond and crystal values | KING displayed the same coin balance twice. The second value now shows existing server giftScore, explicitly nonredeemable received-gift credit, with gift-history entry. This does not invent a new spendable currency. |
| `layout_home_main_page_new_5_7`: scrolling list/status container | KING category matching happened after a limited global query and had a 300-room ceiling. Category selection now queries aliases first; bounded 50-record cursor pages support Previous/Next without a 300-room cutoff. Search applies within each category page; full text search across all rooms still needs a search backend. |
| `channel_voice_room_page_channel` and `layout_channel_bottom_channel_v2`: seat, chat and anchored input holders | KING duplicate Games strip consumed chat space. Games moved into the existing one-tap action shelf. Keyboard hides seat/badge/action sections while preserving the same room and media session. Room badges scroll horizontally on narrow phones. |
| `voice_room_stage_channel`: four-column seat grid, nicknames and mic state | Existing 8/12-seat four-column layout and 10-seat layout retained with consent, moderation and server callbacks. |

Additional corrections: profile refresh now creates a missing profile transactionally and
does not overwrite an existing remote profile with stale phone preferences; explicit Edit
Profile remains the write path. Photo upload no longer claims success or discards unsaved
text edits before acknowledgment. A worker bounds source bytes, decodes a thumbnail of at
most 640px, handles EXIF orientation and uploads JPEG with metadata; public/private photo
records commit together before success is shown. Remote Me photos are HTTPS-only and
bounded to 3 MB/640px decode. Palette selection handles Integer.MIN_VALUE hashes.

## Validation and remaining work

Automated checks include Node regressions, pure-Java policies, a real Firestore emulator
category/cursor/live-photo/atomic-write suite, previous gifts/mic/join-leave tests and full
Android compilation/signature comparison. Native image decoding, keyboard geometry,
large font settings and real two-phone behavior still require Android device QA.

This is NOT exact visual or complete functional parity. Dynamic/remote BoloHi screens,
its private backend, full 3D avatar/world behavior and every game cannot be validated by
resource extraction. Live authenticated voice/video, paid gifts/recharge/VIP production
activation, provider credentials/hosting, original animated assets and broad two-phone
acceptance remain outstanding. Cloudflare stays OFF; the user's dashboard required a
payment method and no strict zero-cost spending cap is verified. No paid service or
production deployment was enabled by this change.
