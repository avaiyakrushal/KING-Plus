# KING Plus v10.0.18 — Combined Me / Home / Party release gates

This is ONE Android TEST APK from the complete signed v10.0.17 source. It does not reset any approved UI, Firebase collection or signing identity.

## Me Page
- Followers opens live Followers list; Following opens live Following list; Friends opens mutual Friends (not Discover in all three cases).
- Returning from a hosted Party to Me refreshes that account's hosted rooms, including pagination, without stale owner callbacks.
- Existing real profile visitors, cloud gallery, VIP, wallet and profile statistics are preserved.

## Home Page
- Moments beyond the previous 320-post hard cap load from Firestore cursor pages of 80, with topic filtering, account/tab guards and retry.
- In-flight Like results cannot update a different signed-in account or an unrelated section.
- Topic counters are explicitly labeled a recent-100 sample, not an all-time count.
- Previous live Moments, CP relationship full pagination/atomic approval/decline and family/visitors remain intact.

## Party Room
- Host co-host removal checks room, actor, current host role and that the same role still exists, within a Firestore transaction.
- Gift ranking uses verified sender UIDs, not colliding display names, and ignores free/zero-value gift events.
- Recent ranking explicitly says it samples up to 250 events; it does not claim all-time totals.
- Gift history has server-cursor pages of 100 room events, including retry and room/account safety.
- Previous Loop Mic, host moderation, synced games, mic-seat transactions, reactions and gifting paths remain in place.

## Automated gates
- Exact prior signed source plus SHA-256 checked batch patch, versionCode 297.
- Source assertions, Java policy test and two-user Firestore emulator permissions.
- Full Android assembleDebug and matching signing-certificate fingerprint.
- Source ZIP and signed TEST APK artifact.

## Not automatically verified
- Real two-phone RTC microphone/speaker/latency and voice reconnect behavior.
- All original Bolo Hi games, graphics, VIP animations and per-gift sounds vs user video, with side-by-side acceptance.
- Deployed production Firebase rules/Functions, real phone/Facebook OTP, external billing/recharge and account/provider permissions.
- Do not label this build as production Final until physical acceptance succeeds.
