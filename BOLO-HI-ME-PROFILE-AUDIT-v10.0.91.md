# Me/Profile and owner-only access audit — v10.0.91

- Agency entry points are still gated by trusted server status and are not displayed to ordinary users.
- VIP `000001` reservation requires the Firebase Super Admin custom claim at the callable boundary; no client-supplied UID grants ownership.
- The wallet infinity label is rendered only from the verified Firebase token claim. No client writes currency balances.
- Owner unlimited gifting skips only the sender debit in the trusted server function. All recipient, ban, block, profile, room-membership and idempotency checks remain enforced; recipient score is nonredeemable.
- Hunt is local practice and does not access the wallet.
- Validation: owner/Hunt source checker, 20 Node gift-service tests, full Android CI compile/package/signature/native library checks.
