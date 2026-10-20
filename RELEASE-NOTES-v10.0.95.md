# KING Plus v10.0.95 — Owner Console (TEST)

- Added a Me/Profile Owner Console entry shown only when the signed-in Firebase ID token has the trusted `admin=true` claim.
- Added server-backed app-wide announcement and maintenance notice settings with strict input validation.
- Added an owner operations overview for account/profile, live room, report, Agency and Agent counts.
- The signed-in app reads only the public notice fields; owner metadata remains server-side.
- Existing report moderation and bounded wallet adjustment controls remain claim-checked.
- No private messages, credentials, or owner-only data are exposed to ordinary users.
- Backend Cloud Functions must be deployed before these remote controls are available in a live app.
