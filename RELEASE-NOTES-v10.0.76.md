# KING Plus v10.0.76 — Discover Hot ranked paging (TEST)

- Discover Hot now requests profiles in descending server level order and uses that same order for every cursor page. The old behavior sorted each 60-profile page locally, so higher-ranked profiles on a later page could appear after lower-ranked profiles on the first page.
- Keeps v10.0.75 Social/Discover follower-name notifications, Party presence cleanup and Android 16 image URI crash handling.
- Version code 355; version name `10.0.76-discover-hot-pages`.

Full source regression checks and Android CI build are required before APK delivery. Two-device social and Party acceptance, voice/video, production payments and every game round still require live services/devices.
