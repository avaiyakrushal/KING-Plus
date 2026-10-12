# Receipt integrity follow-up

This is a backend change, not a final APK or a live-payment activation.

## Problem and change

The verified v9.9.29 release ZIP carries an older `functions/v3.js` than the
repository root. Its purchase verifier accepts missing account binding, coerces
null/false purchase states to PURCHASED, lacks quantity and consumed-receipt
checks, and returns an old balance on duplicate requests. It also does not
isolate notification failure from committed credit. Root-only edits would not
reach the reconstructed release.

`apply.py FULL_RELEASE_DIRECTORY` replaces only the purchase callable and copies
its three required modules. Existing moderation, notifications, game code,
Firestore rules and Android assets are preserved. The same callable is installed
in the repository root. Subsequent builds/deployments must apply this overlay to
the verified release source; do not deploy the old root project as the full app.

The shared service rejects malformed states, unbound/wrong-account tokens,
conflicting products, unsupported quantity, already-refunded purchases, and
consumed tokens without a stored receipt. Wallet, receipt, recharge VIP and ledger
are committed together. Duplicate retries use the current wallet balance. The
transaction returns its committed result instead of leaking state from aborted
attempts. Purchase tokens and Google error payloads are not logged. Consumption
failure is returned as `consumptionPending` and may be retried with the same token.

Validation: 35 receipt-service cases in unit mode and the same cases against real
Firestore emulator transactions in CI, plus the existing wallet/VIP/catalog tests.
Google Play itself is stubbed; these tests do not charge money or prove live
provider configuration. Emulator Admin SDK tests exercise transaction behavior,
not client Firestore authorization.

## Remaining requirements for paid production

- Non-standard Play purchases (license-test, promo, rewarded) are rejected from
  paid wallets. Add a separately deployed staging wallet for license testing;
  do not weaken the paid wallet's guard to make license-test purchases pass.
- Implement authenticated Play RTDN/Voided Purchases reconciliation and policy
  for reversing already-spent/refunded balances. Checking `refundableQuantity`
  on verification is not a complete refund lifecycle.
- Add durable server consumption retry and Android purchase recovery. A returned
  `consumptionPending` flag alone is not a background retry service.
- Verify active Play products, package/signing, service account purchase access,
  client account binding and internal-track purchase/cancel/pending flows.
- Gift paths in the historical release backend need their own catalog/wallet
  audit before real Diamonds are enabled. This overlay fixes purchase verification
  only; it does not silently replace the rest of that backend.
- Keep `LIVE_RECHARGE = false` and production billing disabled until those pass.

## Other final-app requirements, verified 12 October 2026

Existing GitHub runs show Firestore rule deployment succeeded (38244539556), the
v9.9.29 signer SHA-256 was registered (38251155099), and Phone provider plus India
SMS allowlist are enabled (38251360323). These are previous completed operations,
not changes made by this receipt patch. SMS delivery still needs real-device
acceptance. Facebook provider was disabled in run 38244808277.

Full reference game/3D acceptance, controlled authenticated RTC, two physical
phone audio/video tests, Facebook provider configuration and verified paid
Recharge are not completed by this PR. Do not label it Bolo Hi parity or Final.

API contracts used:
- https://developers.google.com/android-publisher/api-ref/rest/v3/purchases.products
- https://developer.android.com/google/play/billing/security
