# KING Plus v10.0.50 — Real-money recharge activation gates

This is a signed TEST build until merchant activates it. Code can be tested without charging customers.

1. Cashfree merchant completes KYC and links a verified payout bank account. Do not share API secrets in chat, GitHub or Android.
2. Set Firebase Secrets `KING_CASHFREE_CLIENT_ID` and `KING_CASHFREE_CLIENT_SECRET` using secure authenticated CLI.
3. Supply `KING_CASHFREE_MODE=sandbox` and `KING_CASHFREE_ENABLED=true` for sandbox; test create, paid verification, idempotence, timeout, and order history on real devices.
4. Production requires `KING_CASHFREE_MODE=production`, `KING_CASHFREE_ENABLED=true`, and `KING_CASHFREE_BILLING_APPROVED=true`; production API credentials, explicit merchant consent, Play-compliant alternative billing enrollment where applicable, fraud/refund/support processes and published user terms are prerequisites.
5. Server-admin merchant must create `cashfree_recharge_catalog` with SKU documents (`king_coins_100`, `king_coins_600`, `king_coins_1300`), data `{active:true,currency:"INR",amountPaise:...}`. Never invent prices. Never allow app clients to edit these documents.
6. Deploy Firestore rules and Firebase Cloud Functions, and test at least one merchant-authorized sandbox payment. No client-side crediting.
7. Customer can retry `Check Payment` and `Recover Pending Order` after reinstall. Even an ambiguous create response keeps a server order for recovery; wallet credit remains transactionally one-per-receipt.
8. Plan payment-support reconciliation and refund/chargeback investigations; wallet balances may require administrative adjustment following a documented, audited policy.
9. Do not direct Google Play-distributed users to external payment for digital Diamonds unless the account/app is eligible and enrolled in an applicable billing program, and the required reporting and other policies have been implemented.

**This release does not enable real production payments without merchant-owned KYC/credentials and explicit flags.**
