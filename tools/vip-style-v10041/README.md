# KING Plus VIP Identity Shop — v10.0.41

A separate **VIP 6-digit number** (`VIP888888`, not a replacement for the free KING ID)
and one of six purchasable **VIP name colors** are supported. Public grants and
number reservations are Firebase Admin-only, wallet deductions are atomic and
idempotent, and purchases use only existing verified server Diamonds.

## Launch gate (currently OFF)

1. Deploy new Firestore rules and `buyVipIdentity` callable from `functions/v3.js`;
   set up verified paid recharge before enabling purchases.
2. Owner/admin seeds `king_vip_style_catalog/current` using trusted Admin SDK with
   `enabled: false`, `paymentMode: 'verified_diamonds'`,
   `vipNumberDiamonds: <owner-approved integer price>`,
   `nameColorDiamonds: <owner-approved integer price>`.
3. After live payment setup and provider compliance, change `enabled` to `true`
   through a trusted administrator operation. Do not allow client writes.
4. Verify two distinct authenticated devices, real purchased Diamonds, competing
   claims for the same ID, replayed requests, anti-fraud, app reinstall and account change.

The current app has `KingRecharge974Activity.LIVE_RECHARGE=false`.
**No live rupee collection, paid recharge or production purchase is claimed.**
Google Play distribution of these digital goods ordinarily requires compliant
Google Play Billing or enrollment in an applicable alternative billing program.

Owned VIP name colors are recorded server-side and the most recently purchased
color is active; changing back to an earlier color can be added without payment
in a subsequent update. VIP number stays tied to the owner and cannot be resold.
