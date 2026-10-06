# KING Plus v9.3.1 — Party Open + Stable Gmail Identity

Fixes the two issues reported from the user's real screenshots.

Party Room:
- Public/owner Party UI opens immediately after tapping a room card, while membership sync continues in the background.
- A blocked member write no longer makes the tap look like nothing happened; the room UI opens and Online status shows the join/sync state.
- Same Gmail/Firebase account on another phone no longer stops room opening with a blocking confirmation dialog.
- Same Gmail still represents one KING account/UID; different people should use different Google accounts.
- Room owner photo now prefers the KING cloud profile photo instead of reverting to the Google avatar.
- Member app version is 9.3.1.

Google account identity:
- public_profiles/{uid} is now the canonical KING identity for returning Google users.
- Returning login restores KING displayName, photoUrl, bio, tags, hometown, birthday and cosmetics before syncing.
- Google display name/photo no longer overwrite an existing custom KING name/photo on every login.
- Switching to a different Firebase UID clears device-local identity fields so one account's photo/name does not leak into another account.
- FirebaseAuth profile displayName/photo is aligned to the canonical KING cloud profile.
- Me/Profile screen can display the cloud/Google photo even when the original local content URI only existed on another phone.

External note:
If Firestore membership/chat/game writes show PERMISSION_DENIED, Firebase production Security Rules/IAM still must be deployed. v9.3.1 makes the room itself visibly open so the exact backend state can be diagnosed instead of failing silently.
