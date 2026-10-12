# KING Plus v9.9.30 — Bolo Hi video reference audit (2026-10-12)

Reference: user-supplied screen recording `1000011619.mp4`, ~6:31, 720×1600. All listed functions are observations of the recording, **not permissions to use Bolo Hi proprietary code, graphics, or trademarks**. Implement independently with KING Plus branding.

| Timestamp | Reference screen / observable behavior | KING v9.9.29 status | Next acceptance gate |
|---|---|---|---|
| 00:05–00:20 | seasonal promo, Home Hot / Discover / Date / Music / Game, room cards | Some home/discover/games exist; visual and catalogue parity unverified | All tabs navigate and real live room cards refresh |
| 00:25–00:40 | Discover hosts and game masters; inbox messages, friend menu | Social/discovery/inbox present; not screen-matched | Real Firebase accounts, messages, add friend, channel CRUD tested on two phones |
| 00:50–01:35 | Followers/Friends, profile header, About Me, Gift Wall, Medals, Influence, channels | Public profile and collection present but not complete visual/functional equivalence | Full saved profile, owner-only edits and real received gifts |
| 01:45–03:30 | Square, Privilege Pack/Shop, VIP, Noble Center, Family, Wallet, settings; profile frame edit | Some routes and wardrobe exist, some panels partial | Actual authenticated purchases and persistent equip metadata |
| 03:45–04:30 | Avatar frame, entrance effects, chat bubble, stage, decorations store | Cosmetics/gallery exist with limited original options | Live preview plus proof of ownership; no decorative control claiming purchase |
| 04:35–05:30 | Swipe/select successive VIP0 / VIP1 / VIP5 / VIP10 / VIP15 / VIP21 / VIP27 / VIP33 / VIP38 / VIP43 / VIP45 cards | v9.9.29 VIP chips are static; server-verified current VIP already separate from local XP | **This PR** makes tiers selectable with locked previews; never falsely unlocks |
| 05:35–06:00 | Level tasks: daily/weekly, progress, rewards | Task buttons incorrectly granted local XP on tap, without proof, once forever | **This PR** removes unverified reward minting; real event-backed award is a remaining gate |
| 06:05–06:15 | Crystal / Diamonds wallet, UPI / Card / GPay recharge navigation | UI available; payment receipts are in draft PR #30; no live billing | Play purchase, consumption retry, refunds, accounting, consumer safety |
| 06:20–06:31 | Settings notification/privacy controls | Settings partially implemented | Android 16 notifications and account privacy tested |

## This change

- Rebuild only the verified v9.9.29 full release source plus **PR #30 purchase verifier overlay**. Never mistake stale repository-root Android source for full release.
- Interactive VIP tier preview; current tier and recharge points come from **verified backend wallet**, never the user's local XP store. Locked preview is not entitlement.
- Task buttons navigate to relevant activities and no longer silently award XP for merely tapping; legacy already-claimed flags remain unchanged.
- Maintain billing OFF, no live wallet mutation, no Firestore rule deploy.
- Android `versionCode 209`, same debug signer for install-over compatibility; compiled artifact must be marked **TEST**, not Final.

## Mandatory blockers before 100% Final

1. Complete reference-screen and practical feature walkthrough (this video does not show Party room moderator controls or all games).
2. Event-backed daily/weekly XP task credit; server-verified VIP entitlement where paid perks affect backend authorization.
3. Full Bolo Hi-like *independently developed* 3D controls, stage animations, live emoji, gifts and every selected game with actual multiplayer rules; not just labels.
4. Two physical Android phones: voice/video consent, seat roles, admin/mic locks, reliable Party join/leave, crash/ANR-free navigation.
5. Verified Indian SMS OTP received and used; Facebook provider configured and tested.
6. Google Play paid-recharge production gate: Play access, legitimate test wallet, refund/RTDN reconciliation, consumption recovery, purchase replay and ledger audit.
7. Production backend deploy under authorized credentials and smoke/rollback plan.

### On-phone acceptance for this TEST candidate
1. Install over v9.9.29, check same package/signature and versionCode 209, no loss of data.
2. Open VIP: swipe chips VIP0/VIP1/VIP5/.../VIP45; tap each and verify title changes; locked tier says preview only.
3. With Firestore wallet unavailable, no paid VIP is shown as unlocked.
4. Open Tasks; tap Join/Chat/Gift/Social. Navigation opens relevant functional screens; XP must **not** increase merely on the tap.
5. Recheck existing 3D Party, 12 live seats, game screens, gifts, frames, login and recharge remains disabled.
