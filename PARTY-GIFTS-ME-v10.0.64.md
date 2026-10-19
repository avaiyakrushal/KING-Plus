# KING Plus v10.0.64 — Party, Gifts and Me integration

Base: exact signed v10.0.61 source from Actions run 39016797671, plus supplied v10.0.63 Party lifecycle patch. Version code 343. Uses the existing stable TEST signing identity.

Changes:
- Preserve Jitsi voice, atomic mic seats, host controls and existing join/leave logic. Include v10.0.63 revoke-consent/cancel-reconnect cleanup even when a finishing room ID has already been cleared.
- Paid room gifts use one server transaction for catalog pricing, sender debit, receiver gift-score credit, immutable history, room event and chat. Server reads room membership, bans and both block directions inside the transaction. Receiver credit is non-redeemable gift recognition, per existing recharge-policy.js; gifts never increase VIP or mint spendable Diamonds.
- Durable per-account request ID and a pending-gift retry action protect against lost responses and repeated taps. No success message or sender animation before server confirmation. Free backpack/voice gifts remain separate.
- Gift Center shares the exact 64-item server catalog, supports quantities 1/3/9, removes fake local sending and uses live wallet/history listeners with account/lifecycle guards and older-history pagination.
- Me identity, Party mic seats, host action buttons and gift header are inflated from XML. Me name, wallet, receiver score and recharge-based VIP reflect live server records. Existing follower/following/friends listeners and profile editing are retained. Wallet history now opens real gift history.
- Extend recipient-only playback receipt rule to server-created paid events. Client creation of paid events and all wallet/ledger writes remain denied.

Validation is recorded in CI. Local backend tests use an injected transaction harness; CI additionally runs the real Firestore emulator, previous room/mic tests and Android compilation/signature validation.

Completion limits: successful compilation/emulator tests are not proof of live two-phone voice/animation. Backend functions/rules/index deployment must succeed before new paid room gifts work in the installed app. Existing merchant/recharge OFF settings are preserved. This is a TEST build, not a fully accepted final production release. No real two-device QA has been performed by this environment.
