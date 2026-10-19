# KING Plus v10.0.60: non-money Recharge UI receipt completion

From signed v10.0.59, build unsigned-source delta only; no merchant keys or production deployment.

- DEMO History stores structured local-only receipts by Firebase UID, each with a DEMO-ID, time, method, currency, quantity, and zero actual charge / zero wallet credit.
- Filter by currency and method; open individual DEMO receipts. Older plain-text DEMO records remain viewable.
- Explicitly clear current account's local DEMO history; never delete server payment records or alter wallet balance.
- Crystal header correctly opens local DEMO History rather than falsely representing Diamond payment orders as Crystal payments.
- Diamond payment history and server Check Payment stay separate from local DEMO receipts.
- Selected sample pack remains visible near the top and preserves method/tab state on rotation.

**Critical:** LIVE_RECHARGE and LIVE_CASHFREE_RECHARGE remain `false`. Real Diamonds, Crystal, VIP, Beans, actual UPI transactions, and production billing are not enabled.
