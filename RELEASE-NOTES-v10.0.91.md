# KING Plus v10.0.91 — Owner VIP and Hunt (TEST)

- Adds a server-verified Super Admin path to reserve VIP number `000001`. The normal VIP number shop remains paid only through existing verified server transactions.
- Shows the Wallet infinity marker only when Firebase ID token custom claims confirm `admin=true`. It does not mint or alter the finite wallet balance.
- Trusted Super Admin gift sends may use unlimited sending entitlement without debiting the sender’s finite wallet; recipient gift recognition remains nonredeemable. Other accounts keep the existing verified-balance checks.
- Adds a free local Hunt practice game. Hunt stores no currency, spends no Diamonds, and awards nothing redeemable.
- Keeps Agency entry points restricted to server-authorized Super Admin, active Agency, or active Agent accounts.

This is a TEST build. The Firebase callable and Admin custom claim require deployment/configuration in the owner-controlled Firebase project before the online entitlement can work.
