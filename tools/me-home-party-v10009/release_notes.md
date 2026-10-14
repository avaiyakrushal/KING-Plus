# KING Plus v10.0.9 – Me / Home / Party TEST

Incremental fixes from the successfully built v10.0.8 TEST source. No changes to unrelated screens.

- **Me**: clear stale Followers/Following/Friends counts on listener restart; invalidate cached follower states when Firestore returns an error.
- **Home Family**: atomic Firestore transaction creates the Family and owner membership together; retries only on a real Family-code collision. A failed membership write can no longer delete somebody else's Family created concurrently. Async result stays bound to the original tab/account.
- **Party Room**: game start now requires an active joined moderator, a ready room snapshot, and a Firestore compare-and-set based on the host's observed round. An old dialog cannot replace another moderator's newer active game. Monotonic round IDs and allowlisted extras prevent malformed game metadata.

Local pure-Java guard tests and source checks included. **TEST only**, not production rollout; Firebase rules, real dual-phone RTC/mic/voice, live gifting/paid recharge and 3D/game parity remain unverified.
