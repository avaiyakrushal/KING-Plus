from pathlib import Path
import sys
root=Path(sys.argv[1]);pkg=root/'app/src/main/java/com/kingplus/social'
me=(pkg/'MainActivity.java').read_text();inbox=(pkg/'InboxActivity.java').read_text()
home=(pkg/'CommunityHubActivity.java').read_text();party=(pkg/'PartyActivity.java').read_text();rules=(root/'firestore.rules').read_text()
checks=[
('Me opens interactive Inbox notifications', 'inbox10010.putExtra("openNotifications10010",true)' in me),
('No local-only notification button', 'button("Mark all as read",CARD' not in me),
('Inbox routes to cloud feed', 'getBooleanExtra("openNotifications10010",false)' in inbox),
('Inbox server query', 'get(com.google.firebase.firestore.Source.SERVER)' in inbox),
('Inbox auth callback guard', 'noticeAccountCurrent10010(noticeAccount10010)' in inbox),
('Family join membership batch', 'join10010.set(membership9986,member956)' in home),
('Family join account link batch', 'join10010.set(db.collection("users").document(joiningAccount10010)' in home),
('Family leave batch delete', 'batch10010.delete(familyRef.collection("members")' in home),
('Family leave account unlink', 'batch10010.set(db.collection("users").document(leavingUid10010)' in home),
('No premature local leave cleanup', 'leaveJoinedFamily10010(code,r)' in home),
('Party live server guard', 'sameGuessRound(current.exists()' in party),
('Party scoped room-bound guess', 'currentVoteRoom10001(guessRoom10010,guessUid10010,guessBinding10010)' in party),
('Party 1-9 validation', 'if(guess10010<1||guess10010>9)' in party),
('Family owner atomic rules', 'existsAfter(/databases/$(database)/documents/families/$(familyCode))' in rules),
('Family owner getAfter rules', 'getAfter(/databases/$(database)/documents/families/$(familyCode)).data.ownerUid' in rules),
('Build code 289', 'versionCode 289;' in (root/'app/build.gradle').read_text()),
]
for name,valid in checks:
    if not valid:raise AssertionError(name)
print('PASS v10.0.10 source regression: %d checks'%len(checks))
