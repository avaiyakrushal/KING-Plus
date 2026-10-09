# KING Plus v9.6.4 — Party memory and cross-phone identity acceptance

## Evidence triggering this fix (Android diagnostic, user screenshot)
- User reported `Android exit: Low memory kill` while using **v9.6.2-ludo-turn-guards**.
- Last screen: `PartyActivity/party-render`; Java heap 16 MB, RSS 385,344 KB.
- This is Android OS process termination due to memory pressure, **not** evidence of a virus scan problem. Android RSS counts memory outside the Java heap, so the exact native/library culprit remains unproven.
- Multiple phones could not enter the same Party, find one another by shown short IDs or follow reliably.

## Verified v9.6.3 code root causes and mitigations
1. Lobby opens a persistent members snapshot listener for **every** room card. v9.6.4 uses bounded one-time reads instead.
2. Party caches 24 arbitrarily large bitmaps and decodes each photo at only half resolution in an unbounded number of threads. v9.6.4 caps cache by byte size (6 MiB), downloads max 256-pixel RGB565 thumbnails with just two workers, and frees the executor on destroy.
3. Joining a public room renders the full Party scene immediately and renders it again after successful membership write. v9.6.4 displays a cheap connecting view first and renders the full scene only after the membership write succeeds.
4. Shown 6-digit room aliases derive from `hashCode() % 900000` and are **not unique Firebase room IDs**. v9.6.4 looks up `joinCode` remotely, filters closed rooms, asks the user to choose on collisions, and supports full invite links.
5. Shown 6-digit user aliases also derive from `hashCode() % 900000`; old numeric profile search fetched **all** public profiles to compute hashes locally, leading to unnecessary memory/network use. v9.6.4 queries `whereEqualTo("publicId",alias).limit(15)`; additionally accepts exact verified Firebase UID search. Profile navigation takes authoritative document ID, not possibly stale payload `uid`. Social Discover shows "My verified account UID" with clipboard copy.
6. Cross-phone account checks: each person must be signed into a different Firebase account to appear as a distinct member; multiple devices sharing **the same Firebase UID** intentionally occupy the same member ID.

## Acceptance checklist on phones
1. Update to v9.6.4 (or install safely after backing up if Android detects a signature conflict).
2. Device A: open Social/Discover, tap **My verified account UID** to copy. Device B must show a different UID if it's a distinct participant.
3. Device B: search Person A by 6-digit KING ID; if any ambiguity or missing alias, paste their full verified UID instead. Verify correct name/photo, tap Follow and confirm the count on both devices.
4. Device A: create a **public** Party room, copy the actual full invite link and note the displayed 6-digit room code.
5. Device B: Party > Join by link / Room ID and paste the exact invite; repeat using the 6-digit number. If number matches multiple rooms, choose correct room and host rather than autojoining an arbitrary room.
6. Leave and rejoin from Device B at least 10 times; repeat with a third Google account, and with mic off/on. Record any Android crash report and the exact version.
7. If the room is **private**, the existing password/host invitation/access rules still apply. Do not bypass Firebase room membership authorization.
8. Measure Party Room stability on a low-memory phone for at least 10–15 minutes. An APK compile pass is **not** proof of runtime stability or multiplayer success.

## Scope of release
- Modifies Android source only; reuses existing Firestore `public_profiles`, `follows`, `live_rooms.joinCode`, `live_rooms.members`, `roomAccess()`. No new Firestore deploy required for these fixes.
- v9.6.2 tightening of Firebase rules was successfully released on 2026-10-09 (GitHub Actions run 37874489494, attempt 3).
- Previous Jitsi voice, Ludo, live emoji, and crash diagnostics features are preserved. No code or assets from the Bolo Hi APK were copied.
