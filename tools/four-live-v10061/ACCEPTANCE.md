# KING Plus v10.0.61 — Four Live Pages Acceptance

Changes: Discover live Follow and Unfollow through Firestore transactions/listeners, Home/Party Follow session guards, Me server-only profile counters and wallet unknown values while signed out, Messages safe peer identities and Inbox UID search/thread filtering. Real paid Recharge remains disabled.

Before claiming full parity: test on two real phones under separate Firebase accounts. Follow/Unfollow from Discover and Home must sync. Me followers/following/friends counters and verified wallet must update. Messages must send, receive, mark read, search UID and never cross account boundaries. Check Firebase production rules, large text and Android 16.

Build: signed v10.0.60 source + tools/four-live-v10061/patch.gz.b64; patch SHA-256 49aaae367443a533cb16c0ec942871fc624162c65840c50103cf3a3c36abdf70. Run 19 code checks, Firestore Follow/DM/Me cross-device emulator tests, Android compile, versionCode=340, signing fingerprint continuity and APK structure comparison. Do not call full production-ready without actual mobile acceptance.
