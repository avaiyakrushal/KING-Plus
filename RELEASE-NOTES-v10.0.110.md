# KING Plus 10.0.110 — Home room paging

- Home live-room categories now keep a realtime Firestore listener for the visible room page and release it when leaving Home or refreshing.
- Hot/category room results and Popular room results support Previous/More paging; Popular ranks public room presence from server counts.
- Party Room now reads RTC readiness from the authenticated backend config action instead of a compile-time Android flag. The server reports ready only when both activation flags and provider credentials are present.
- Firebase Firestore rules were not changed in this release.
- Android CI builds a debug APK. It is not a production-signed release.
- Live voice still requires a configured Cloudflare account, server secrets, Functions deployment and operator billing verification. No two-physical-device voice test was available in this environment.
