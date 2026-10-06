# KING Plus v9.0.0 — Online Multi-User Completion

This stage addresses the user's remaining 1–8 checklist as far as client/source work can go.

1. Firebase backend readiness
- Adds in-room Online Diagnostics showing Firebase project, room, membership, message-read state and member count.
- Join/backend failures show the actual Firestore error.
- A separate deploy workflow is provided for the latest Firestore rules. Google IAM permission is still required on the configured service account.

2. Real two-phone Party Room
- Adds 20-second Firestore presence heartbeat and automatic membership self-heal/rejoin.
- Member documents refresh current name, profile photo, level/VIP, device id, app version and lastSeenAt.
- Existing Room Code/full KINGROOM ID direct join remains.

3. Real voice
- Tapping the Party mic while seated now enters the same Jitsi audio conference derived from the Firestore Room ID.
- Mic/voice presence is mirrored to Firestore; returning from the voice conference marks the seat mic off.
- Host Mute All now also writes micOff to occupied Firestore seats. (Remote Jitsi moderation still depends on the Jitsi service/UI.)

4. Gifts and Live Emoji
- Existing Firestore event listeners continue to animate gifts/emoji on other phones.
- In this no-billing build, if the backend wallet function is unavailable, a TEST-coin room gift can still be published through the room Firestore event/message flow so other room members can see it.

5. Multiplayer games
- RoomGameActivity self-heals the current user's room membership before ready/game listeners attach, reducing permission failures caused by stale/missing member docs.
- Existing synchronized games are preserved.

6. KTV / PK / Family / Rank
- Existing realtime Firestore room_settings/events/member based flows are preserved and benefit from the stronger room membership/presence layer.

7. Profile/account sync
- Editing nickname now also updates FirebaseAuth displayName.
- Party heartbeat refreshes current name/photo/progression to the room member document.

8. Final room cleanup
- Adds a visible Online status chip in Party Room with one-tap diagnostics.
- Existing v8.8.1 chat/keyboard/equal gift-card fixes and v8.9.0 visual parity remain.

Known external blocker:
Previous Security Rules deploys failed with 'The caller does not have permission'. The service account needs Google Cloud/Firebase IAM permission such as Firebase Rules Admin (roles/firebaserules.admin) before the latest rules can be released to production.
