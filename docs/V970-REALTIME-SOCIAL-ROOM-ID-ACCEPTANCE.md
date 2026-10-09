# KING Plus v9.7.0 — Realtime Social, Followers, User IDs, Room IDs

## Verified gaps from v9.6.9 code
- `SocialActivity.loadFollowing/loadFollowers/loadFriends` used Firestore `.get()` (one-time fetch). A follower created from another phone did not update the currently opened screen until it was reopened/refreshed.
- Search/Discover/profile-card requests did not verify that their Firestore response still belonged to the selected screen/query; delayed results could overwrite newer UI.
- Entering Social republished old phone-local `displayName`, `bio`, `xp/level` on every open, potentially undoing later cloud changes from another phone even though the document used `SetOptions.merge()`. Merge by itself doesn't prevent overwriting the keys in its patch.
- `SocialActivity.loadSocialPhoto940` started an unlimited thread for each visible person and decoded unbounded full-resolution photos, risking high memory use on crowded directories.
- Party room fallback creation `retryRoomCreateLegacy921` omitted `joinCode`, while Share Room continued advertising a six-digit code. Other phones querying `whereEqualTo('joinCode',code)` could not find such legacy fallback rooms.

## Implemented
1. Two Firestore snapshot listeners follow the signed-in UID (outgoing and incoming directed Follow edges), update Following/Followers/Friends immediately, and deduplicate by Firebase UID. Unsubscribe on stop/destroy. List length capped to 60 cards to bound UI and image load.
2. Generation-based async protection on name, KING ID, exact Firebase UID, Discover and each member's profile-card fetch; older results are discarded once user changes tab/account/search.
3. Existing cloud Profile is read before Social edits it. Opening Social now backfills only missing identity/profile fields and a canonical six-digit KING ID. New profile creation still includes user defaults; previously edited name/photo/bio/level from another phone remain unchanged.
4. Follow/Unfollow is one Firestore transaction against the canonical `followerUID__targetUID` document. Double-taps are prevented while the transaction is pending.
5. Image loading uses 2 workers, max 192-pixel RGB565 thumbnails and a 4 MiB cache, with cleanup on Activity destruction and Android memory trim.
6. Legacy Party Room creation now always stores its shown `joinCode`. When the host opens an older room lacking the field, that host may backfill it; other viewers cannot tamper with room code.
7. Retains v9.6.9 memory, v9.6.8 Mic/Seat, v9.6.7 Party presence, v9.6.6 crash, and v9.6.3 Ludo code.

## Phone acceptance (three different Firebase/Google accounts)
1. Install **the same v9.7.0 APK** on A, B and C without uninstalling if app signature allows. Sign in to **different** Google accounts; each device's `My verified account UID` must differ.
2. On A search B using six-digit KING ID. Search by the full verified Firebase UID; both should show the same profile. Change search term quickly during a slow network: older search results should **not** replace newer ones.
3. Keep A's `Followers` page open. B follows A using Social or Public Profile. Without reopening, A should show B; B's `Following` should show A. After A follows B back, both `Friends` lists should include one another.
4. B unfollows A; the lists should update without duplicated cards, and A's friend count should drop correspondingly.
5. A changes its profile name on one phone. Opening Social on a different phone still logged into that *same* A account must not revert the newer name/bio/level in Firebase.
6. A creates public Party Room, shares six-digit room code and full room invitation. B/C join with each form. Host and Join fields should refer to the same actual Firebase room ID. Verify Room and User IDs are distinct concepts.
7. Revisit older Party Room created by host and lacking `joinCode`; when the original host opens it, the code should be backfilled. Verify another user can then search its shown code. If multiple rooms share a six-digit alias, choose the correct host; the alias is **not guaranteed globally unique**.
8. In Social Discover scroll through 30 profiles for several minutes and monitor RAM/crashes. Larger image downloads should no longer cause one full-size decoding thread per profile.
9. Confirm existing Party Mic/Seat, Ludo, gifts and live reactions are not broken.

## Scope and limitations
- Changes Android clients only; no new Firebase Rules deployment.
- No automated functional test was run on physical user phones. CI unit tests and Gradle build show the source is buildable, **not** that cross-device behavior is fully verified.
- The 6-digit KING ID and Room Code are friendly aliases generated from Firebase IDs; neither is mathematically guaranteed globally unique. The full Firebase UID and Room ID/invite are the authoritative identifiers.
