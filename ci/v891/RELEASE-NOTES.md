# KING Plus v8.9.1 — Multi-device Party Room Fix

Focused on the real two-phone Party Room problem.

- Public Party rooms now get a stable 6-digit joinCode at creation time.
- Shared room text now contains both the 6-digit code and a full KINGROOM:<Firestore-document-id> token.
- Added Join by Room ID / Code to Party actions and the empty lobby.
- Direct full-ID lookup avoids depending only on the first 30 lobby results.
- Code lookup falls back across recent rooms and also accepts legacy hash codes, room names and full document IDs.
- A cloud Party screen opens only after the current user's member document is successfully written. This prevents a false “joined” UI when Firestore actually denied the join.
- If two phones use the same Firebase/Google account, the app warns that both devices represent the same KING user; use different Google accounts for two separate people.
- Join failures now show the actual Firebase error and explain permission-denied/backend-rule failures.
- Room search also matches room codes and full IDs.
- Firestore source rules now permit validated public or invite-only room creation with a 6-digit joinCode.

Important backend blocker:
The latest Firestore rules still need to be deployed to Firebase project king-plus-2f365. Previous deployment attempts failed because the configured service account lacks Security Rules deployment permission.
