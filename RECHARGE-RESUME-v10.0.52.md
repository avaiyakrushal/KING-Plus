# KING Plus v10.0.52 — Recover & resume original Cashfree UPI checkout

This version extends v10.0.51 signed TEST source; not live real-money payments.

- A customer can reopen the SAME Cashfree Payment Link from Recharge History or Continue Previous Payment after a browser interruption/reinstall. A new order is never created by the resume endpoint.
- `resumeCashfreeRecharge` requires permanent signed-in account, disabled-by-default merchant setup, order ownership, original SKU/amount/quantity and server-fetched payment-link verification.
- Paid provider response returns only `paid` (and the app triggers receipt confirmation); no link URL is offered for a paid, cancelled or altered order.
- History orders are queried newest-first before the 50-document query bound and 20-entry display bound. A Firestore composite index on `uid ASC, createdAt DESC` is required and included in `firestore.indexes.json`. Deploy indexes with the project alongside rules and functions before production rollout.
- Checkout URLs are allowed only over HTTPS and the expected Cashfree hosts. The real charging UI remains disabled until merchant KYC, production API credentials, catalog, Firebase indexes/rules/functions deployment and applicable Google Play alternative billing compliance are complete.
- Additional Node regression tests cover retry/reopen, account isolation, changed price or URL, failed gateway, successful payment with no checkout URL and recent-first history.
- For refunds/chargebacks, use a reviewed support reconciliation process. This code intentionally does not automatically debit a user's virtual currency or assume a payment cannot later be reversed.
