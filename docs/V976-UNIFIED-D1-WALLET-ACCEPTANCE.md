# KING Plus v9.7.6 — One verified Cloudflare Wallet

## Why this change matters
The app's v9.7.5 phone authentication was prepared for Cloudflare Workers + SMS OTP on Firebase Spark, while the previous Recharge/VIP Android screens still read a *different* Firestore Wallet collection used by the Firebase Cloud Functions / Play Billing prototype. If a verified Razorpay recharge eventually credited Cloudflare's D1 Wallet, the Android VIP/Recharge screens would not have shown that updated Diamond balance.

## v9.7.6 fixes
- `KingBackend976` retrieves a Firebase **ID token** for the currently signed-in Google or phone-custom-token account and queries the authenticated **GET /wallet** endpoint. The Worker verifies Google signature, issuer, project audience and expiry, and only returns the wallet owned by that UID.
- The Main Activity Recharge option now launches `KingWallet976Activity`. Diamond balance and recharge-based VIP are displayed from the exact same authenticated Cloudflare D1 Wallet.
- VIP screen also reads `KingBackend976.fetchWallet()`, not Firestore `wallets`, and shows unknown/unavailable when the backend has not yet been deployed. The app never fabricates paid Diamonds or interprets an unavailable wallet as a real zero balance.
- Android OTP is restricted to HTTPS `kingplus-lowcost.<account>.workers.dev`. No HTTP, arbitrary hostname, port, credentials, query or redirect is accepted. JSON responses are capped to 16KiB and cannot be redirected to leak OTPs or Firebase custom tokens.
- Keep Gmail/Firebase Google login, Mobile OTP UI, Party, FREE Gifts, Realtime Follow, Family, Chat, VIP screen, and Online Ludo from the successful v9.7.5 baseline.
- **Payments remain OFF**. The old Android Google Play Billing prototype remains disabled (`LIVE_RECHARGE=false`). The separate Razorpay backend remains in `PAYMENTS_ENABLED=false` and no merchant charges are activated or simulated.

## Phone acceptance steps
1. Install v9.7.6 APK over v9.7.5 when signatures match. Existing KING Plus application data should remain; do not uninstall unless a signature issue requires it.
2. Confirm Google login, existing Party Rooms, Mic/Seat and Free Gifts still work.
3. Open Recharge/Wallet while Cloudflare is **not** deployed: it should say server is not configured and display `Diamonds: unavailable (not zero)`, not falsely claim a purchased zero balance.
4. On a separate test Cloudflare Worker with D1 and correct Firebase JWT-verification setup, log in using two different Google accounts. Open Wallet on A/B. The Wallet API must return balances bound to their own Firebase UIDs.
5. Change accounts without clearing app data and refresh Wallet; never show account A's returned balances to B (async UID callbacks must be rejected).
6. Open VIP: it must show only the returned `rechargeTotal` and VIP, not TEST coin balance or local Gift counters.
7. In Phone OTP enter `http://`, a malicious domain, localhost or wrong Cloudflare worker name. Login cannot send OTP to such hosts. A correctly configured `https://kingplus-lowcost.<account>.workers.dev` origin may proceed only after Turnstile and SMS credentials are provisioned.
8. Try a cancelled/pending payment: no Diamonds are credited. Live purchases are deliberately disabled in this APK.

## Important remaining tasks before real money/OTP can run
- User must own/configure Cloudflare Worker + D1 and provider secrets. Real Indian SMS generally costs per OTP, and approved sender / DLT or an equivalent lawful provider flow may require registration.
- A real merchant account (KYC), store-distribution compliance, secure checkout client, refunds/chargebacks, and controlled release are still required. The source contains signed captured-payment webhook and idempotent D1 ledger, **but has not been deployed to a live merchant**.
- A public-release APK should pin the exact owner-controlled Worker domain; the current generic Worker-name rule is an improvement over arbitrary URLs but is **not** as strong as domain pinning to one owner.
- Full acceptance requires real accounts and two Android phones. Passing Gradle is not a claim that SMS delivery or recharge works before provider connection.

## Source and build
- Android branch: `work/reference-completion`
- CI: https://github.com/avaiyakrushal/KING-Plus/actions/runs/38011230656
- Low-cost Worker: `edge/kingplus-lowcost/`
