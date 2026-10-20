# KING Plus v10.0.96 — Me Profile Progress (TEST)

- Replaced the Me page's device-only level badge with a Firebase account level derived from server-confirmed Daily Check-in XP.
- Added an account-synced Level page with verified XP, check-in streak, total check-ins and next-level progress.
- Replaced placeholder medals with check-in milestones based only on server-confirmed account claims.
- Notification settings now filter message, follow, room-invite and gift notifications through Cloud Functions.
- The saved Party invitation permission is checked by the trusted Cloud Function before sending an invite.
- Removed nonfunctional local-only Remember Login, auto-reconnect, low-data and visual-effect switches from Me settings.
- Existing privacy notes continue to identify discovery/presence/display preferences that are not enforced on every legacy surface.
- Cloud Function deployment is required for live server behavior.
