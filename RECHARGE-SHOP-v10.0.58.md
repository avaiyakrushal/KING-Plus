# KING Plus v10.0.58 – Recharge Shop usability and safety

Continues the exact signed v10.0.57 source. Only UI/navigation/verification logic is changed; unrelated social, party and game screens remain untouched.

## Behavior
- Diamonds and Crystal tabs have an accurate selected underline. Crystal remains not activated; preview packs never charge.
- UPI, Card, Google Pay and Agent are separate accordion sections. UPI pack previews are disabled until server provides a verified merchant catalog, then only server priced packs can be bought.
- Merchandised Diamond rows share a readable pack layout; no third-party logos or copied artwork.
- Recharge Agreement and entered India mobile number remain intact when switching methods and during Activity restoration. Agreement still requires explicit checking.
- Recharge history can inspect and re-verify a previously created order even if sales/catalog are currently OFF, so payments made before a temporary outage are not locked out. Credited receipts cannot start new checkout.
- Cashfree Payment Links must use https, the approved gateway hostname, /links/ path and no embedded credentials.
- DEMO checkout remains ₹0 and does not credit wallet, VIP or Crystal. Firebase and merchant still decide any real credit.

## Live merchant blockers
- Cashfree production merchant KYC/bank approval, server-side API keys, approved INR catalog, deployed Firebase Cloud Functions / rules / indexes, refunds/chargeback handling, live end-to-end verified payment, and any applicable Google Play billing policy.
- Card and Google Pay only become payment options after gateway support, merchant approval and policy compliance are verified. Agent requests are not payment receipts. Crystal has no wallet or purchasing backend.

Build is a signed *TEST* artifact, not a live-money release.
