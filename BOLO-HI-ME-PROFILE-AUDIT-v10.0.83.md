# Bolo Hi Me/Profile → KING Plus parity audit (v10.0.83)

This update addresses a data integrity gap in KING Plus Me/Profile progress screens.

| Flow | v10.0.83 behavior |
| --- | --- |
| Me → Level & Achievements | Device-only activity is labeled TEST and explicitly marked as not synced or server verified |
| Free gift progress | May update only the local TEST preview; it does not publish XP or level to public profile |
| VIP | Device-local VIP counters are ignored; progress API returns VIP 0 until the verified Wallet supplies account entitlement |
| Me → Medals | Unverified activity and VIP medals stay locked |
| Data Center / Safety | Copy identifies the account-scoped server report receipts and distinguishes preview data |

This prevents test state from appearing as earned account progression. Secure progression, verified activity receipts, and full Bolo Hi parity remain unfinished.
