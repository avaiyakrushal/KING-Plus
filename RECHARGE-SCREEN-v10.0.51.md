# KING Plus v10.0.51 — Recharge UX compatibility and safety

Reference screenshots showed Diamonds/Crystal tabs, wallet balances, UPI, Agent, Card,
Google Pay, package rows and transaction history. This update adds those native controls
without reusing branded images/artwork from another app.

- Diamonds show only a SERVER-confirmed wallet balance; a non-confirmed balance is `—`.
- Crystal tab is an explicit **preview** because no verified Crystal wallet/recharge service exists.
- Six sample tiers shown under disabled UPI are **visual examples, not prices or purchasable SKUs**.
- When the verified Cashfree catalog is enabled, real purchasable tiers come only from the
  server, and the visual preview list is hidden. Agreement must be accepted before checkout.
- Card and Google Pay are informational entries until merchant checkout supports them.
  There is no direct card payment collection from the Android app.
- Agent entry opens the actual agency portal with existing server-side authorization.
- Recharge history is read using the signed-in user's secured callable backend.
- `LIVE_RECHARGE` remains false, no client-side wallet credits or fake Crystal currency.

Actual paid recharge requires merchant KYC, production secrets, approved prices,
Firebase functions/rules deployment, payments and refunds setup, and applicable app-store
billing policies. No physical-device or production payment acceptance tests are claimed.
