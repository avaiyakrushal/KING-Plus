# KING Plus Firestore outage — confirmed root cause (2026-10-11)

## Evidence from the user Android app
- Installed KING Plus v9.8.6 Firebase test reports validated internet, Firebase signed in, refreshed Firebase Auth token, and Firebase project `king-plus-2f365`.
- Firestore server-only Room document read fails as UNAVAILABLE. The document is present only in the local cache. Never accept cached membership as a live verified join.

## Independent remote checks (no Firebase writes)
1. Admin SDK Firestore collection read, via GitHub run 38098448515, returned `RESULT: RESOURCE_NOT_FOUND`: https://github.com/avaiyakrushal/KING-Plus/actions/runs/38098448515
2. Separate authenticated Firestore database metadata GET, via GitHub run 38098524414, returned `RESULT: DEFAULT_DATABASE_NOT_FOUND`: https://github.com/avaiyakrushal/KING-Plus/actions/runs/38098524414
3. Firebase Rules deployment via GitHub run 37874489494 had succeeded previously, but publishing rules alone is not proof a `(default)` Cloud Firestore data database is provisioned.

**Diagnosis:** `king-plus-2f365` lacks the expected `(default)` Firestore data database (or it is not discoverable by this authenticated metadata GET). The mobile Firebase client connects to `(default)` by default. This must be fixed in the Firebase project; another client APK build cannot create a missing server database.

## Required administrative repair — do not run without owner approval
1. In Firebase Console open **KING Plus / king-plus-2f365** → **Build / Firestore Database**.
2. Check whether an alternate named database exists; do **not** delete it, overwrite anything, or mistakenly create an extra paid database.
3. If the first database does not exist, create the **Standard edition** database with ID **(default)** and a region chosen/approved by the owner. Region cannot be casually changed afterwards. The first Firestore database has a no-cost tier; avoid upgrading to Blaze or activating Google Cloud Billing.
4. Choose restrictive/production starting rules, then deploy the existing reviewed `firestore.rules` for this project. Do NOT leave database-wide test-mode allow-all access enabled.
5. Rerun both read-only GitHub audits: require `DEFAULT_DATABASE_EXISTS` and `ADMIN_SERVER_READ_OK`.
6. On phone, KING Plus Settings → Help & Feedback → Test Firebase / Party Room connection **without any Room Code** first. Server profile read must be reachable.
7. Create a **new Party Room**, because a cached pre-database Room is not necessarily present in the new server database. On two different Google/Firebase accounts, verify real server-only Room, members, microphone, seats and chat.
8. If deployed permissions still fail, examine Firestore Rules access responses and Auth UID; never trust offline cache as proof of authorization.

## Non-goals
- No billing, SMS OTP, paid recharge, member writes, database creation, credential exposure or bypass of private Room permissions in either audit workflow.
- Do not keep publishing diagnostic APK versions until the backend database exists.
