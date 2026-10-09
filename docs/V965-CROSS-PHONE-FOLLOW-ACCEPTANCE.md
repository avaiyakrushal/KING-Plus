# KING Plus v9.6.5 — Real-time Follow and canonical KING ID

## Confirmed source bugs fixed
- `KingPublicProfileActivity` previously stored follow edges as `followerUid_targetUid`, unlike Social and Party which stored `followerUid__targetUid`. Different views therefore read/write different Firestore documents. This release standardizes all three to the double-underscore form using `KingSocialIdentity965.followId`.
- Public Profile's six-digit `publicId` used a different hash/number range than Main Profile and Social. All three now share the SAME display ID function. **This is only an alias; Firebase authentication UID remains the authoritative identity.** Existing Firestore `public_profiles.publicId` entries from Main remain unchanged.
- On viewing someone's public profile, legacy single-underscore Follow documents owned by the currently signed-in user are migrated safely: verify `followerUid` and `targetUid`, write canonical document if absent, then delete only that old legacy document after a successful canonical write. Existing followed relationships are preserved.
- Your own Profile and someone else's Public Profile now subscribe to the `follows` collection, showing Followers / Following / Friends counts live, deduplicated by actual Firebase UID, instead of loading once.
- Unsubscribe on Activity pause/stop/destroy to prevent background Firestore listener leaks. Social lists refresh after Follow/Unfollow and after returning from another profile.
- Party warns that two devices logged into the same Google/Firebase account represent **one** member UID, not two members.

## Two-phone acceptance tests
1. Install the same v9.6.5 debug APK on A and B, each using a different Firebase-authenticated Google account; do not clear app data by uninstalling if avoidable.
2. On both phones open Social/Discover > **My verified account UID**. The exact UID must be different. If identical, both phones are signed into the same account and cannot appear as two unique people in a room.
3. From A search B by six-digit KING ID and separately by B's full Verified UID. The same B profile must open for both searches. Verify the KING ID displayed on B's Me screen matches B's Public Profile.
4. A opens B's Public Profile and taps Follow. B's **Followers** increases without leaving the profile. A's **Following** increases without reopening its profile. Reciprocal B follows A -> Friends counts increase.
5. From Social Discover on A, follow/unfollow B. Open B's Public Profile: it must show the same followed/unfollowed state as Social.
6. If A used an older version to follow B from B's Public Profile, simply revisit B's Public Profile while online. The old follow record is migrated to the canonical format. After sync, the relationship must remain followed and must not double-count.
7. Repeat follow/unfollow on third signed-in account. Switch away/back 10 times; check there are no accumulated Firebase listeners or memory-related crashes.
8. Create a public Party room on A; on B paste its full Room invite or 6-digit code (as fixed in v9.6.4). Both users must show as separate members and share messages; mic tests require explicit consent.
9. Verify App never silently claims its 6-digit KING ID is globally unique. The real Firebase UID is the unambiguous search option.

## Limitations
Build/tests do not prove the app behaves correctly on the user's hardware. Prior `Low memory kill` for v9.6.2 remains unconfirmed fixed until a 10–15 minute Party test on lower-memory phones. Native voice-engine problems may be separate.
No Firebase rule or Cloud IAM changes required for this release; existing deployed rules remain.
