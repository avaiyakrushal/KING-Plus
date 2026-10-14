# KING Plus v10.0.10 TEST release

Based on successfully compiled v10.0.9; retains previous UI and flows.

- Me Notifications opens the existing server-backed, interactive Notifications feed in InboxActivity, rather than the local-only placeholder with inert buttons.
- Home Family join/leave now write membership and user Family link together in one Firestore batch. Preferences update after commit. The owner-member creation rules now use the post-commit Family document (existsAfter/getAfter); production rules must be separately reviewed and deployed.
- Party Guess It validates room membership, numeric input 1-9, and the exact active server round before an in-room guess event.
- Guess It answer remains readable in the client-accessible game document. These checks prevent stale UI submissions but **do not prevent client-side cheating**. No prizes or trusted scores should rely on this answer.
- Automatic source assertions and pure Java tests are included. CI checks Android compile and APK signing.

**Not final/production**: Two-device RTC verification, provider sign-in, deploy of Firestore rules, real money recharge, complete 3D and Bolo Hi feature parity remain unverified.
