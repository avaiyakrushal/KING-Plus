#!/usr/bin/env python3
"""KING Plus v9.8.7: reduce FIRST host join latency after newly created room.

Base verified v9.8.6 APK source (not old tracked repo app).
New-room SERVER creation has already completed before finishRoomCreate921 fires.
Safely skip the redundant initial member SERVER read only for this new host.
The final member .set() is still server-acknowledged before rendering the Party.
Never optimize guests, old/reopened/private Room joins or modify backend rules.
"""
from pathlib import Path
import shutil,sys
root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
file=pkg/'PartyActivity.java'
s=file.read_text()

def once(old,new,label):
    global s
    count=s.count(old)
    if count!=1:
        raise SystemExit(f"{label}: expected 1 marker, found {count}: {old[:150]!r}")
    s=s.replace(old,new,1)
    print("PASS",label)

shutil.copy2(Path(__file__).with_name("KingFreshHostRoom991.java"),pkg/"KingFreshHostRoom991.java")
once('    private void createCloudRoom(String name,String category,int seats,boolean priv){',
'''    // Set ONLY in successful new-room Firestore .set() callback, consumed once.
    // A normal room re-entry cannot use this newly-created-room shortcut.
    private String newlyCreatedServerRoom991;
    private void createCloudRoom(String name,String category,int seats,boolean priv){''',
'Add one-time server-acknowledged room creation proof')

once('openCloudRoom(ref.getId(),name,user.getUid(),safeName(),priv,false);',
     'newlyCreatedServerRoom991=ref.getId();openCloudRoom(ref.getId(),name,user.getUid(),safeName(),priv,false);',
'Mark only successfully created room for one host join')

once('''        final int requestToken966=++roomJoinGeneration966;
        startPartyJoinTimeout977(requestedRoom966,requestedUid966,requestToken966);''',
'''        final int requestToken966=++roomJoinGeneration966;
        // A success callback from creation is a SERVER write acknowledgement.
        // Reuse it only once, for the matching signed-in host and same room.
        final boolean newlyCreatedHost991=KingFreshHostRoom991.canWriteWithoutPreRead(
            newlyCreatedServerRoom991,requestedRoom966,
            ownerUid,requestedUid966,isOwner());
        newlyCreatedServerRoom991=null;
        startPartyJoinTimeout977(requestedRoom966,requestedUid966,requestToken966);''',
'One-time host validation bound to room/user/owner before optimized member write')

once('''        };
        ref.get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(oldDoc->{''',
'''        };
        if(newlyCreatedHost991){
            // Skip only the pre-read after server-confirmed CREATE. Firestore still
            // validates member write, room access, bans and signed-in identity.
            write.run();
            return;
        }
        ref.get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(oldDoc->{''',
'Skip one redundant round trip for host of a JUST CREATED Room, not guests or re-entry')

once('''    private void finishMemberJoin921(){
        if(isFinishing()||isDestroyed()||roomId==null||!cloudRoom)return;''',
'''    private void finishMemberJoin921(){
        if(isFinishing()||isDestroyed()||roomId==null||!cloudRoom||memberSeen)return;''',
'Prevent multiple join events if duplicate member-write callbacks complete in one visit')

file.write_text(s)
g=root/'app/build.gradle'
old="versionCode 177; versionName '9.8.6-firestore-server-diagnosis'"
new="versionCode 178; versionName '9.8.7-faster-new-host-party-join'"
text=g.read_text()
if text.count(old)!=1:raise SystemExit('Base must be validated v9.8.6 Android source')
g.write_text(text.replace(old,new,1))
print('PASS v9.8.7 new-host server read shortcut only; final Firestore membership still required')
