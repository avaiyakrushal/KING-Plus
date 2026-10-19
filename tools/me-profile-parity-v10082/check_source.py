from pathlib import Path

root=Path(__file__).resolve().parents[2]
gradle=(root/'app/build.gradle').read_text()
chat=(root/'app/src/main/java/com/kingplus/social/ChatActivity.java').read_text()
privacy=(root/'app/src/main/java/com/kingplus/social/KingMePrivacy9984Activity.java').read_text()
rules=(root/'firestore.rules').read_text()

assert 'versionCode 361' in gradle
assert '10.0.82-chat-block-sync' in gradle
assert 'private ListenerRegistration threadListener,peerProfileLive10068,ownBlocksLive10082;' in chat
assert 'startOwnBlockSync10082();' in chat
assert 'whereEqualTo("ownerUid",owner)' in chat
assert 'Source.SERVER' in chat
assert 'private void setCloudBlock10082(boolean block)' in chat
assert 'ref.set(data)' in chat and 'ref.delete()' in chat
assert 'private boolean isBlocked()' in chat
assert 'cloudBlockedUids10082.contains(peerUid)' in chat
assert 'if(isBlocked()){' in chat and 'Unblock this account before messaging' in chat
assert 'db.collection("blocks").whereEqualTo("ownerUid",owner).limit(100)' in privacy
assert 'allow create: if activeUser()' in rules
assert 'allow delete: if signedIn()' in rules
assert "exists(/databases/$(database)/documents/blocks/$(request.auth.uid + '_' + request.resource.data.recipientUid))" in rules
assert "exists(/databases/$(database)/documents/blocks/$(request.resource.data.recipientUid + '_' + request.auth.uid))" in rules

print('PASS Chat block/unblock writes the account Block List; live account block state gates future message actions')
