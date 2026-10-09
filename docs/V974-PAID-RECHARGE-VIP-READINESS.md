# KING Plus v9.7.4: Purchasable Diamonds → Gifts, Recharge-only VIP

## User's requested economic model
- Another KING Plus user chooses a Diamond pack and pays via the store's approved checkout.
- Only a verified, completed purchase is credited to **that authenticated account**; no manual Client Firestore credits or screenshot-based approval.
- The purchased Diamonds are available in that account's server wallet and can be spent on non-cash-out in-app Gifts.
- **VIP Level increases when the user recharges Diamonds**. Sending Gifts spends Diamonds but **must never grant additional VIP points**.
- Receiver earns decorative Gift recognition/score, not cash, transferable money or withdrawal rights.
- FREE Gifts/Emoji, Party, Chat, Family and Ludo remain independently available.

## Existing server implementation corrected
The repository already had `functions/v3.js` with `verifyPlayPurchase` and an idempotent gift handler, but previously granted VIP on **gift spending**, not recharges. Version 9.7.4 changes the backend to:
1. Whitelist exactly `king_coins_100`, `king_coins_600`, `king_coins_1300`; prices are set only in Google Play Console, not by any Android code.
2. Verify `purchaseState == PURCHASED`, Google Play purchase token and mandatory SHA-256 Android obfuscated UID binding using Android Publisher API.
3. Transactionally credit the server wallet exactly once per token; atomically write receipt, ledger, Diamonds and recharge-only VIP fields.
4. On Gift, debit sender's real Diamond wallet, never increase VIP; receiver obtains `giftScore` (not transferable Diamonds).
5. Preserve the server receipt for duplicate/retry handling; purchases in `PENDING` state don't grant Diamonds.
6. Consume/acknowledge after successful server processing. Any refund, void/reversal and insufficient-money handling still require production acceptance checks before real launch.

## Android v9.7.4
- Adds `KingRecharge974Activity` with three official Play consumable SKUs, official formatted price from Google Play, per-UID secure receipt verification and Firestore live wallet/VIP display.
- `MainActivity` Wallet/Recharge Center routes to that screen instead of simulated ₹ purchase buttons.
- `KingVipVisualActivity` reads recharge-only VIP from the verified wallet rather than locally awarded TEST coins.
- **`KingRecharge974Activity.LIVE_RECHARGE=false` intentionally disables every checkout** until store products and the secured server endpoint are configured and tested. THIS APK DOES NOT CHARGE ANYONE.
- The backend changes were **committed but NOT deployed** to Google Cloud; the Firebase Spark project has no enabled billable Cloud Functions backend.

## Activation requirements; DO NOT create charges automatically
1. The developer needs an approved Google Play Console developer/merchant account with payout and tax information. Product IDs must match exactly and the store must configure local currency prices.
2. Deploy trusted Firebase Cloud Functions `verifyPlayPurchase` and `sendGift` (currently requires the Blaze plan / linked Cloud Billing account) **or** another properly secured production server; never validate a paid receipt using client-only Firestore.
3. Grant the verification service account the minimum required Android Publisher API permissions.
4. Install via Google Play internal testing for real checkout; a sideloaded GitHub debug APK alone is NOT sufficient for Play purchase validation.
5. Run Google Play license/test purchases, including pending, cancelled, concurrent duplicate tokens, switching accounts, a second phone restoring purchases, fraud attempts and refunds.
6. Audit the paid Gift catalog and fixed server-side prices; client-reported gift costs must NEVER be trusted. Define and disclose that the currency/receiver Gift scores have **no cash withdrawal or transferable monetary value**.
7. Only after every acceptance check and explicit developer approval: flip `LIVE_RECHARGE=true`, compile a new signed Play release, test and publish. Do **not** do this under the user's current no-billing constraint.

## Five example acceptance scenarios
- A buys a verified 600-Diamond Play SKU: server balance +600, `rechargeDiamondsTotal` +600, VIP recalculated from recharge thresholds.
- A then Gifts 50 Diamonds: server balance -50, VIP unchanged, recipient receives nonredeemable Gift score.
- Same purchase token is submitted again: 0 extra Diamonds, 0 extra VIP.
- User switches account and replays previous account token: denied, 0 Diamond credit.
- User starts but does not finish payment: `PENDING` → 0 Diamonds until Play verifies it as PURCHASED.

## Policy/technical sources
- Google Play Payments policy: https://support.google.com/googleplay/android-developer/answer/9858738
- Play Billing integration and verification: https://developer.android.com/google/play/billing/integrate
- Firebase Spark vs Blaze: https://firebase.google.com/docs/projects/billing/firebase-pricing-plans
