# KING Plus v10.0.70 — Non-game implementation gates

Base: work/king-v10068-live-profiles-history. Games are explicitly out of scope: do not modify game code, rules, assets, balances or game APIs.

## Priority / acceptance checks
1. Party Room: verify join/leave lifecycle, reconnect, microphone seat permissions, host kick/mute, stale chat clearing, background return, and no crash on null crowd capacity. Two physical devices required for voice validation.
2. Me: authenticated profile edits persist after reinstall; follower/following and VIP/wallet values originate from server; avoid optimistic financial credits.
3. Home and Discover: real-time follow/unfollow and live room list, user search, pagination and stale-list handling; no fake local counts.
4. Messages: direct-message authorization, real-time listener cleanup, unread counts, duplicate prevention, push and offline retry.
5. Gifts: callable/server-only wallet debit/credit, idempotent request IDs, atomic ledger and receiver credit, history and animation after confirmed success.
6. Recharge/Agency: server-verified purchase before credit; cancel/pending/refund must not credit; agent roles server enforced, transaction audit.
7. Auth and notifications: Phone/Google provider configuration and two-account push tests.

## External blockers already documented
Production RTC provider/token service, Firebase production deployment and providers, Play billing products/API, signing secrets. See docs/PRODUCTION_LAUNCH_CHECKLIST.md.

## Release rule
No final APK claim until Gradle CI passes and two-device smoke tests pass. Existing games unchanged.
