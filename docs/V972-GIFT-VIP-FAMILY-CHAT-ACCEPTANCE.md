# KING Plus v9.7.2 — Final two roadmap sections: Live Emoji/Gifts, VIP/Family/Chat/Profile

## Known successful build and scope
- v9.7.1 [successful APK workflow](https://github.com/avaiyakrushal/KING-Plus/actions/runs/37951725170): server-confirmed Party Gift events, cross-phone Live Emoji broadcast, no fake TEST-coin debit when a real server transfer fails, animations bounded, and no VIP rewards from local-only Party previews.
- v9.7.2 is built on top of the complete successful v9.7.1 source. It implements verified private Chat gifts, Family member realtime synchronization, authentic UID peer lookup, and VIP secure-wallet visibility.
- v9.7.1 and v9.7.2 changes preserve the previous v9.7.0 Follow/User ID/Room ID and v9.6.9 memory and Mic/Party fixes.

## Verified defects corrected
1. **Party Gift:** Prior implementation generated gift events and awarded VIP even if Cloud Functions transfer failed; v9.7.1 fails closed. Local TEST coin previews no longer count as actual gifts.
2. **Live Emoji:** Real-time Firestore events previously displayed locally even when writes failed. v9.7.1 validates live membership, rate limits, awaits a confirmed Firestore batch and handles errors clearly.
3. **Private Chat Gift:** Earlier implementation debited *local* TEST coin preferences and awarded VIP before any real backend success. v9.7.2 invokes `CloudBackend.sendGift` and sends a chat Gift message only after secure backend confirmation. Local-only preview earns no VIP points.
4. **Family:** Firebase Family messages already had a real-time listener, but Family members list only loaded once; v9.7.2 adds a member-list listener, safe unsubscribe and duplicate-send protection.
5. **New Chat:** The old dialog permitted typing an arbitrary name and an optional UID, creating misleading local-only chats. v9.7.2 looks up an authentic Firebase profile by six-digit KING ID or full verified Firebase UID and handles alias collisions.
6. **VIP:** Previously the VIP screen showed only local-device points. v9.7.2 reads secure `wallets/<uid>.vipPoints` if available and computes thresholds, otherwise clearly labels LOCAL VIP PREVIEW. It does not claim local/test balances are a paid entitlement.
7. **VIP cloud progression:** `functions/v3.js` now contains an idempotent, server-authoritative VIP reward credit in the same wallet transaction as a real gift. VIP thresholds are unit-tested in `functions/king-vip-level.test.js`.

## Backend deployment blocker (DO NOT represent as live)
GitHub Actions [verified gift Functions deploy](https://github.com/avaiyakrushal/KING-Plus/actions/runs/37952910928) **failed** with:
`HTTP 403 Permission denied to get service [artifactregistry.googleapis.com]` at `serviceusage.googleapis.com`.
The GitHub service account needs IAM rights for **Service Usage services.get** (e.g., **Service Usage Viewer**) and likely additional Cloud Functions/Cloud Build/Artifact Registry deployment permissions. It currently has Firebase Rules Admin, which is not enough. Do NOT request/upload/share its private key.
Until a successful Functions deploy, **new server VIP points awarding is not live**, though the APK can read the field if a trusted backend populates it. Existing production `sendGift` behavior and billing may be different from the latest repository code.

## Phone acceptance (must be done, not assumed)
1. Install the identical v9.7.2 APK on three phones and sign in with **three different Google/Firebase accounts**.
2. Open Party, send Live Emoji from A to B/C. All three should see the same event (not only the local sender), and an offline failure must display an error, not a fake sent state.
3. From A, select a gift for B with real server wallet credit. Only a confirmed server transaction may debit A and credit B; cross-phone GiftBurst should show once and appear in history. If insufficient server coins, nothing real is sent. Check Wallet ledger.
4. When in an offline/test party, Gift is clearly a preview and does not increase real VIP or gift history. When offline, do not claim a secure payment.
5. Open Private Chat on A with B by exact Firebase UID/6-digit KING ID; ensure correct B display name. Gift can only be sent using CloudBackend confirmation; any backend rejection leaves the local TEST coins unchanged.
6. Create a Family on A; B/C join with the six-character Family code. Leave B's Family Members and Family Chat open. When C joins/leaves, A/B should see the member list update without reopening. Two users send Chat messages; they appear on both phones without duplication.
7. VIP display shows **LOCAL VIP PREVIEW ONLY** if `wallets/<uid>.vipPoints` isn't populated by a verified backend. A real VIP increase requires the functions deployment to succeed and wallet transaction tests to pass.
8. Switch rooms and repeat Gift/Emoji sending for 15 minutes on a lower-memory Android phone. Report any new exit reason or stack trace; source tests do not establish real-device stability.
9. Check prior User ID/Follow, Mic and Seat, other room tools, Ludo and calls for regressions.
10. The remaining multiplayer Games roadmap line is a *separate unfinished scope*; two feature sections do not equal total KING Plus completion.

No third-party proprietary Bolo Hi code or artwork is included.
