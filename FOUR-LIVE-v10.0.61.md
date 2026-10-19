# KING Plus v10.0.61 Four Live Reliability — TEST only

- Discover: working Follow/Unfollow button on public-profile cards and recommendations, one Firestore live subscription per account, atomic document transaction, in-flight prevention, account/session lifecycle safety.
- Messages: valid thread precedes duplicate suppression; search includes peer UID; retain search term when refining; existing realtime unread, inbox, send, media, notification flows untouched.
- Me: do not falsely report zero followers, friends or wallet when signed out / backend unavailable; stop stale listeners.
- Home: manual refresh of the existing live Moments feed, preserving subscription and cross-device guards.
- Recharge and all other modules preserved from v10.0.60; real money is OFF.

CI static, Node/VIP, Firestore emulator, APK signing continuity tests required. Physical two-phone acceptance still pending, so not production approved.
