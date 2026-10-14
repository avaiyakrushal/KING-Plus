# KING Plus v10.0.19 — One combined Me / Home / Party batch

Starting point: **signed and CI-tested v10.0.18 source (versionCode 297)**. Release target versionCode 298. All previous UI and functionality is retained.

## Me Page
- Profile Visitors display actual Firestore history in pages of 50.
- A Next 50 control fetches older visits using a document cursor rather than silently truncating.
- Each page and profile-opening callback is bound to the exact authenticated account and Me screen generation.
- Existing social Followers / Following / Friends, hosted-room pagination, profile data and Me wallet are preserved.

## Home Page
- A Moment owner can delete an existing Moment only if the current signed-in user matches the same UID and the server Moment still belongs to that UID. A Firestore transaction prevents deleting a changed or already deleted item.
- Older Moments paging now consumes its Load More view before requesting the next cursor, avoiding repeated taps that append the same page.
- Topic filtering, CP and relationship workflows, real Moments and likes retained.

## Party Room
- Room Recent Activity supports server pagination of 80 events per page, with Refresh, Retry and strict room/account matching.
- Unban checks the same still-active ban and its timestamp/reason inside a Firestore transaction, avoiding deleting a changed ban opened in a stale dialog.
- Seat Lock / Unlock verifies same room/account/live moderator, compares live lock status in a transaction, and fails safely rather than overriding a changed seat lock.
- Member Gift Wall and medals explicitly label the recent-event sample, instead of presenting truncated totals as all-time counts.
- Earlier Loop Mic/host/cohost/seat transactions, game & gifting UIs, event history, Me and Home all retained.

## Automated gates
- Exact source artifact recovery and SHA-256 validated batch patch.
- Comprehensive three-page source regressions plus existing Java policy checks and two-account Firebase emulator rules/security tests.
- Android full assembleDebug, versionCode 298 and APK signing identity same as prior installed versions.
- One APK + one complete source ZIP.

## Production acceptance still needed
1. Two physical phones (different users) in the same room for speaker and mic audio, reconnect and permissions.
2. Games, animations, gift sounds and gifting ledger behavior against the user's Bolo Hi reference footage.
3. Deployed production Firestore rules, login providers including real OTP, Facebook and external recharge/payment checks.
4. Full feature parity audit of Me, Home and Party Room on actual devices. These are NOT automatically proven by compilation.

Do not merge to production based solely on these TEST gates.
