# KING Plus v10.0.49 — verified UPI recharge without upfront gateway setup charges

The app preserves Google Play Billing as OFF (`LIVE_RECHARGE=false`).
Cashfree is an **optional alternative** for appropriately distributed APKs or a
Google Play billing-alternative program that meets Play's rules. Do not enable
an external in-app purchase link in a Play-distributed app except where a
proper program and policy allow it. No merchant secret exists in the APK.

## Merchant actions required before *any* real payments can be accepted

1. Apply for a **Cashfree merchant account**, complete **KYC and bank/settlement verification**, and read Cashfree's current pricing and eligibility. Cashfree lists zero setup fee, but platform/transaction costs may apply and promotional offers have conditions. The customer always pays for the recharge; zero setup is **not** free Diamonds.
2. In a Firebase project you control, set two *Secret Manager* secrets:
   `firebase functions:secrets:set KING_CASHFREE_CLIENT_ID` and
   `firebase functions:secrets:set KING_CASHFREE_CLIENT_SECRET`.
   Never put these secrets in GitHub, application code or screenshots.
3. Configure `KING_CASHFREE_MODE=sandbox` first in Firebase Functions runtime
   environment and deploy **only** the listed Cashfree callable functions and
   vetted Firestore rules. The default is OFF. Move to `production` only after
   merchant activation, rules, payment/refund tests and legal/store policy checks.
4. Using Admin SDK/server-side writes only, create explicit approved INR prices:
   `cashfree_recharge_catalog/king_coins_100` etc. Each document must have
   `{ active: true, currency: "INR", amountPaise: INTEGER }`. **Do not invent
   prices** and never allow client writes to the catalog. The approved merchant
   must set the real prices. The three SKUs map to 100/600/1300 Diamonds.
5. When live merchant setup is confirmed, deploy Firebase Functions `v3.js`:
   `getCashfreeRechargeCatalog`, `createCashfreeRechargeLink`,
   `confirmCashfreeRecharge`. They use server-side gateway APIs. The app calls
   the server and opens the authentic HTTPS payment page in the browser.
6. Payment status is always rechecked with Cashfree's server API. Only an exact
   completed INR payment may credit the wallet. A Firestore transaction creates
   a unique receipt, ledger entry and increases recharge-only VIP totals.
   Repeated verification is idempotent. Users can manually press **Check Payment**
   and the app also checks when it returns to Recharge after payment.

## Release blockers (must be verified externally)

- Merchant KYC and account activation; accepted category of digital credits;
  payment provider production credentials; agreed payment fees and pricing.
- Firebase Cloud Functions deployment may need a billing-enabled Firebase
  project and may incur infrastructure charges above applicable free quotas.
- Verified refunds, disputed/chargeback payments and reconciliation require
  operational monitoring before a production launch. Do **not** label this
  TEST implementation production-ready without these checks.
- Play-distributed apps selling digital goods must comply with Google's
  billing rules/program enrollment for India. Direct APK distribution may
  have different payment-policy requirements.

## Tests

`node --test functions/cashfree-recharge-service.test.js`

The default configuration intentionally refuses payment and never credits
Diamonds without a properly activated merchant and successful server response.
