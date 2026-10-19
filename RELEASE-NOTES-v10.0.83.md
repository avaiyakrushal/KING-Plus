# KING Plus v10.0.83 Profile Progress Integrity TEST

## Me / Profile progress

- Level and XP are clearly labeled as device-only TEST previews, not synced or server-verified account activity.
- Free gift activity no longer publishes XP or level to the public profile.
- Local progress APIs always return VIP 0; old device-stored VIP point balances are ignored, and gifts cannot create VIP points.
- Me medals remain locked until verified activity tracking or verified Wallet entitlement exists.
- Updated Data Center and Safety copy to distinguish account-scoped report receipts and device-only preview data.

## Verification boundary

Source regression checks require these invariants and the workflow compiles and signs the TEST APK. This does not create a production progression backend or grant real rewards. Bolo Hi parity remains incomplete.
