#!/usr/bin/env python3
"""KING Plus v9.9.8: real friends only, no misleading local demo directory.

Based on v9.9.7 signed source. This is ONE incremental fix in the five-section
plan, not a claim of complete Bolo Hi parity. No auth bypass or rules changes.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1]);social=root/'app/src/main/java/com/kingplus/social/SocialActivity.java'
gradle=root/'app/build.gradle'
s=social.read_text()
checks=[
('''if (!cloudReady()) { showLocalDemo(); return; }''',
 '''if (!cloudReady()) {
            setLoading("Sign in with Google/Firebase to search real KING Plus accounts. Offline/demo users are hidden.");
            return;
        }''',1,'Search never shows fake local users without a signed-in Firebase account'),
('''if(!currentAccount970()){showLocalDemo();return;}''',
 '''if(!currentAccount970()){
            setLoading("Sign in with your own Firebase account to see real friends. Demo profiles are disabled.");
            return;
        }''',4,'Friends, Following, Followers and Discover show only real Firestore accounts'),
('''db.collection("public_profiles").limit(30).get()''',
 '''db.collection("public_profiles").limit(30)
            .get(com.google.firebase.firestore.Source.SERVER)''',1,'Discover reads SERVER, not stale offline cache'),
('''db.collection("public_profiles").document(uid).get()''',
 '''db.collection("public_profiles").document(uid)
            .get(com.google.firebase.firestore.Source.SERVER)''',1,'Follower profiles resolve on SERVER'),
('''db.collection("follows").document(followId(uid,myUid970)).get()''',
 '''db.collection("follows").document(followId(uid,myUid970))
                    .get(com.google.firebase.firestore.Source.SERVER)''',1,'Follow Back confirmation checks actual SERVER'),
]
for old,new,want,label in checks:
 count=s.count(old)
 if count!=want:
  raise SystemExit(f'FAILED {label}, expected {want}, found {count}')
 s=s.replace(old,new)
 print('PASS',label)
social.write_text(s)
g=gradle.read_text()
old="versionCode 186; versionName '9.9.7-room-admin-user-profile-live-dice'"
new="versionCode 187; versionName '9.9.8-real-friends-server-sync'"
assert g.count(old)==1
gradle.write_text(g.replace(old,new,1))
print('DONE: v9.9.8 real cross-account Friends and Follow status only; Party/Gifts/Games unmodified')
