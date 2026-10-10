# KING Plus — Mobile OTP + Real Diamond Recharge WITHOUT $300 Google Cloud Billing

Status: **source and automated tests ready, backend NOT deployed, live payments OFF**.
The user has no computer, so use Cloudflare/Razorpay/MSG91 mobile browsers plus GitHub Actions to build Android.

## What the $300 means

Google Cloud's $300 is an **optional introductory credit**, not a required $300 payment.
Firebase Phone Authentication SMS is a **Blaze-only** feature and charges per sent SMS.
This independent provider path avoids Firebase SMS and Firebase Cloud Functions.

## Alternate architecture

- **Firebase Authentication on Spark:** keep the existing Firebase project `king-plus-2f365`. The Android app authenticates using `FirebaseAuth.signInWithCustomToken(jwt)`; JWT is signed only by the trusted server after MSG91 verifies the genuine mobile OTP.
- **Cloudflare Workers Free + D1:** `worker.mjs` + `schema.sql`. Server never trusts a phone UID, client-side wallet amounts, payment screenshot or offline TEST coins.
- **Cloudflare Turnstile CAPTCHA:** mandatory for every OTP send and per-IP/phone rate limits (5 messages/IP/hour, 1 per number/minute, max 5 OTP verification attempts).
- **MSG91 approved SMS template:** pays per actual SMS; India DLT registration and template/sender approval may have upfront charges. Do **not** assume SMS is completely free.
- **Razorpay merchant (India):** KYC / approved merchant account, pricing per successful payment; no need for $300 Google Cloud credit. External checkout via Razorpay is suitable only when the app distribution method and the merchant's terms permit it.
- **D1 atomic wallet:** signed `payment.captured` webhook must match a server-created `order_id`, amount and INR; a D1 trigger adds verified Diamonds and lifetime recharge totals **exactly once**. Gifts cannot be cashed out; recipients get decorative recognition, not a bank transfer.

## API routes

`POST /otp/request`: JSON `{"phone":"+919876543210","turnstileToken":"..."}`. Requires valid Cloudflare CAPTCHA + configured SMS provider. Returns a generic sent confirmation, **never** an OTP or password.

`POST /otp/verify`: JSON `{"phone":"+919876543210","otp":"123456"}`. Only MSG91's genuine successful verification permits the backend to issue a signed Firebase custom auth token. The example digits are not accepted by a real provider unless truly issued. The Java Android app then signs into Firebase using `signInWithCustomToken()`.

`GET /wallet`: `Authorization: Bearer <Firebase ID token>`; verifies Google signing key / issuer / audience and returns trusted Diamond balance, recharge total and VIP level.

`POST /recharge/order`: Firebase ID token, JSON `{"sku":"diamonds_600"}`. **Disabled by default.** Server creates an actual Razorpay order with amount from *merchant-configured* SKU prices, not prices supplied by the phone. Only an approved merchant account can enable this.

`POST /razorpay/webhook`: Raw webhook body and `X-Razorpay-Signature`. Uses constant-time HMAC-SHA256 comparison, matching captured status/order/amount/INR. On one atomic D1 order-state change, the trigger credits wallet and ledger. Duplicate webhooks credit zero extra Diamonds.

## Prerequisites before OTP can truly work

1. Create a Free Cloudflare account (Workers + D1). Create Worker and D1 database. Deploy the code from this folder to a `*.workers.dev` HTTPS address. Bind the database as `DB`, apply `schema.sql`.
2. Register an MSG91/approved OTP provider, including India DLT entity, sender ID and approved template when required. There may be unavoidable registration and per-SMS costs.
3. Create a Cloudflare Turnstile widget for the HTTPS Worker domain. Register `TURNSTILE_SECRET` as a Worker secret; its **site key** is a public field in the Android Login screen.
4. Store secrets securely in Cloudflare Worker **Secrets**, never in GitHub source or chat: `MSG91_AUTHKEY`, `MSG91_OTP_TEMPLATE_ID`, `PHONE_UID_SECRET` (random high-entropy string), `FIREBASE_SA_EMAIL`, `FIREBASE_PRIVATE_KEY` (PKCS8 PEM), `TURNSTILE_SECRET`.
5. Android v9.7.5 contains a real Mobile OTP screen. The owner enters the deployed HTTPS Worker URL and Turnstile site key. If the backend/keys are not configured, SMS sending fails visibly; the app does not create a fake logged-in user.

**IMPORTANT ID note:** A Firebase account signed in with a Google UID and a phone-custom-token UID are two different identities unless *secure account linking* is implemented. Do NOT automatically merge data or transfer balances merely because names or phone numbers look similar.

## Prerequisites before REAL recharge can truly work

1. Decide whether KING Plus is distributed directly as an APK or by the Google Play Store. For **Play-distributed digital currency**, Google Play Payments policy generally requires Play Billing or participation in an eligible Indian alternative-billing program with additional terms/reporting; do not secretly add Razorpay to a Play app.
2. Complete Razorpay merchant onboarding/KYC and permitted payment-method setup. Set Worker secrets `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET`, `RAZORPAY_WEBHOOK_SECRET`.
3. Choose actual INR pack prices as a server secret `RECHARGE_PRICES_PAISE_JSON`, e.g. `{"diamonds_100":<merchant_price_paise>,...}`. **No prices are invented or committed to source**.
4. Before production: implement/check refund and dispute reversals, wallet-to-Firebase profile sync or switch UI to `/wallet`, paid Gift debit and real merchant Checkout, anti-fraud controls, transaction receipts/tax/support, and account-linking/migration.
5. Only then **explicitly authorize** changing `PAYMENTS_ENABLED` from `false` to `true`, perform test payments, and update the Android Recharge UI. **Do not activate current code merely because it compiles.** The last Android Play Billing screen is deliberately disabled; the Razorpay path is an alternative backend pending client wiring.

## Phone acceptance tests

- One phone requests SMS OTP and logs in with correct 6-digit OTP; incorrect code rejects, 6th attempt rejects, 2nd phone can sign into the SAME Firebase UID with the same verified number.
- Google login continues to work and does not silently overwrite a user's earlier Google profile.
- Duplicate provider send or OTP verification rate-limit returns clear errors and does not mint extra accounts.
- A signature-invalid/payment-pending/price-mismatch webhook credits **0** Diamonds; one captured verified webhook credits exactly once; a duplicate webhook credits **0 extra**.
- A user's `rechargeTotal` determines VIP. Sending Gifts never changes VIP. Refunds require production-safe reversal handling **before activation**.

## Verified official documents

- Cloud trial: https://cloud.google.com/signup-faqs
- Firebase SMS pricing and limits: https://firebase.google.com/docs/auth/limits
- Custom Firebase auth: https://firebase.google.com/docs/auth/admin/create-custom-tokens
- Cloudflare Workers Free (100k requests/day): https://www.cloudflare.com/en-in/plans/developer-platform-pricing/
- Cloudflare D1 limits: https://developers.cloudflare.com/d1/platform/pricing/
- MSG91 OTP API: https://docs.msg91.com/otp
- MSG91 India DLT requirements: https://msg91.com/help/dlt-registration-in-india/dlt-faqs
- Razorpay standard pricing: https://razorpay.com/pricing/
- Razorpay webhook security: https://github.com/razorpay/markdown-docs/blob/master/webhooks/validate-test.md
- Google Play India alternative billing: https://support.google.com/googleplay/android-developer/answer/13306652
