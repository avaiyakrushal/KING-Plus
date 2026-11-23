# KING Plus 10.0.110 — Home room paging

- Home live-room categories now keep a realtime Firestore listener for the visible room page and release it when leaving Home or refreshing.
- Hot/category room results and Popular room results support Previous/More paging; Popular ranks public room presence from server counts.
- Existing Party Room behavior and Firestore rules were not changed in this release.
- Android CI builds a debug APK. It is not a production-signed release.
- Live voice still requires the Cloudflare account, server secrets, zero-cost configuration verification and Android build flag. No two-physical-device voice test was available in this environment.
