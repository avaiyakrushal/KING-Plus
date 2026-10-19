# KING Plus v10.0.69 vs Bolo Hi 5.30.5 — source review

Reference: the supplied Bolo Hi 5.30.5 arm64 APK, SHA256
`6b9184ce120607c21f9ed9259ca207e65971e737393f0b114277ce73bff0caf2`.
Its ZIP has 13,523 entries. This is a static APK/resource and KING source review;
neither app was installed or run on a phone in this environment.

| Area | Bolo Hi reference | KING Plus v10.0.69 source | Remaining gap |
|---|---|---|---|
| Me / profile | Profile header, ID, avatar, follower/following/friend counts, family entry, separate diamond and crystal values | Live profile/photo refresh, editable identity, family routes, distinct wallet and nonredeemable gift-score display | Two-account live profile/family acceptance; production VIP and wallet entitlements |
| Home / rooms | Scrolling live-room list and category navigation | Category aliases, live room listeners, 50-room cursor pages, previous/next controls, cover images, room ID join | Text search is limited to the loaded page; global room search needs a deployed search index/backend |
| Party Room | Four-column mic-seat stage, room chat and anchored composer | 8/10/12-seat layouts, host/moderation controls, live emoji/gift paths, room lifecycle cleanup, keyboard-aware controls | Authenticated production voice/video and real two-phone visual/audio testing |
| Discover | Public profile and social discovery surfaces | Hot/New/Nearby/Follow tabs, live follow listener, transaction-based follow changes, in-flight duplicate guard | Live cross-account follow and notification acceptance remains unverified |
| Messages | Direct chat and message history | Live thread/message listeners, bounded history paging, foreground/server-confirmed read receipts | Two-phone send/read/notification acceptance and production notification delivery |
| Games | Multiple live game modes | Existing game code and rules remain in this source | Not changed in this pass, per the existing instruction to leave Game code untouched |

## Work advanced in this review

- Updated three source regression checks so they accept the current v10.0.69 code
  and verify Party exit cleanup, Discover follow deduplication, and live Messages
  read guards.
- All three checks pass: `four-pages-v10048`, `party-gifts-me-v10064`, and
  `home-me-party-v10067`.
- No app Java/XML behavior changed in this pass. The changes are test coverage and
  this comparison record.

## Release gates still open

- Real Google/Facebook/SMS provider setup and fresh-account login tests.
- Authenticated voice/video provider setup and two-device testing.
- Production gift/recharge/VIP activation and server receipt validation.
- Firebase rules/functions deployment and post-deploy verification.
- Full Bolo Hi UI/function parity acceptance on physical Android devices.
- Global room search backend/index and migration for existing room records.

The package remains TEST source. Automated source checks do not establish live
provider behavior, production deployment, complete visual parity, or two-phone
acceptance.
