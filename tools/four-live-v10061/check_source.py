#!/usr/bin/env python3
"""Source-level regression gates for KING Plus four-page v10.0.61.
Real device & live service tests remain separate acceptance criteria.
"""
from pathlib import Path
root=Path(__file__).resolve().parents[2]
app=root/'app/src/main/java/com/kingplus/social'
read=lambda filename:(app/filename).read_text()
discover=read('DiscoverActivity.java')
inbox=read('InboxActivity.java')
me=read('MainActivity.java')
home=read('CommunityHubActivity.java')
gradle=(root/'app/build.gradle').read_text()
checks={
 'app version increments and preserves package':'versionCode 377' in gradle and "com.kingplus.social" in gradle,
 'Discover grid follow button is real':'bindFollow(uid,follow);' in discover,
 'Discover card fits live grid height':'dp(98)' in discover and 'dp(246)' in discover and 'dp(282)' not in discover,
 'Discover follow live owner query':'whereEqualTo("followerUid",owner)' in discover,
 'Discover one follow subscription':'followListener10061=db.collection' in discover,
 'Discover follow transaction atomic':'db.runTransaction(transaction->' in discover,
 'Discover previous edge state verified':'transaction.get(ref)' in discover,
 'Discover forbids editing someone else edge':'Follow ID/account mismatch' in discover,
 'Discover uses correct document ID':'followId(owner,target)' in discover,
 'Discover blocks repeated tap':'!pendingFollow10061.add(uid)' in discover,
 'Discover follows server timestamps':'FieldValue.serverTimestamp()' in discover,
 'Discover rebinds account on resume':'startFollowListener10061();' in discover,
 'Discover cleans up listeners':'@Override protected void onStop(){stopFollowListener10061()' in discover,
 'Discover denies self follow':'uid.equals(signed.getUid())' in discover,
 'Messages do not mask valid peer threads':inbox.index('if(doc.getString("lastMessage")==null') < inbox.index('if(!seen.add(peerUid))'),
 'Messages support UID search':'thread10029.peerUid.toLowerCase(Locale.ROOT)' in inbox,
 'Messages search preserves existing term':'q.setText(activeSearch10029)' in inbox,
 'Messages listener is live':'whereArrayContains("members", sessionUid10023)' in inbox,
 'Messages listener account generation':'callbackGeneration10030!=inboxListenerGeneration10030' in inbox,
 'Me does not display fake zero followers':'profileFollowersNumber.setText("—")' in me,
 'Me does not display fake zero balance':'profileTopBalance.setText("💎 —")' in me,
 'Me removes account listeners':'stopProfileFollowUpdates965();stopWallet9989();' in me,
 'Home refresh from actual live listener':'button("↻ Refresh Live Home Moments",()->loadMoments())' in home,
 'Home Moments subscribes Firestore':'momentsListener9990=db.collection("moments")' in home,
 'Home requires auth session':'KingSavedRoomsCloud9990.account(account)' in home,
 'Recharge real-money UI remains OFF':'LIVE_RECHARGE = false' in read('KingRecharge974Activity.java'),
}
for name,success in checks.items():
 print(('PASS' if success else 'FAIL'),name)
assert all(checks.values()),f'{sum(not v for v in checks.values())} failed'
print('Passed',len(checks),'source regression checks')
