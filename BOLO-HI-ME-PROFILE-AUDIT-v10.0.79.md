# Bolo Hi Me/Profile → KING Plus route coverage (v10.0.79)

This change consolidates the Bolo Hi Me drawer shortcuts and the existing KING Plus Me/Profile pages in one source update. KING Plus branding and existing destination screens are retained.

The follow-up route audit also separates local report history by signed-in KING account, routes Reports & Safety to the report history screen instead of the blocked-user list, and removes stale/claimed XP indicators from activity shortcuts.

## Drawer shortcuts

| Bolo Hi / Me option | KING Plus destination |
| --- | --- |
| Square | Community Moments / Square |
| Coin Mall | Secure KING Plus recharge packages |
| Data Center | Account, social, gifts, wallet, rooms, levels and ranking destinations |
| Privilege Pack / Shop | Existing cosmetics and wardrobe flows |
| VIP / Noble / User Level | Existing verified VIP, Noble and level screens |
| Family / CP | Existing Family and Relationship community flows |
| Wallet / Web Diamond | Existing secure wallet/recharge screen |
| Share Gift / Gift Wall | Existing Gift Center and Gift Wall |
| Social Media | Existing Square, People Search, Friends/Followers and Messages routes |
| Invite Friends | Shares the issued KING ID |
| Settings | Existing account, privacy, notification and network submenus |
| Help / Rules | Existing help topics and community/safety policies |
| All Me Options | Expanded profile menu with the routes above plus visitors, rankings, tasks, events, and agency tools |

## Verification boundary

The source routing and menu presence are checked by `tools/me-profile-parity-v10079/check_source.py`. This is a source-level route audit; it does not claim pixel-perfect Bolo Hi UI parity or certify every backend integration/device flow. Coin Mall and Web Diamond intentionally enter the existing secure recharge flow; no local balance or payment entitlement is fabricated. Task completion/rewards still require a verified server task service; this TEST app does not grant XP for opening shortcuts. Build the Android app and verify backend-dependent routes on a signed-in test device before calling the APK release-ready.
