#!/usr/bin/env python3
"""KING Plus v9.6.6: fix the Android Party room join NPE at PartyActivity.java:682.

Verified on actual v9.6.5 source:
  ref.get().addOnFailureListener(e->checkCrowdCapacity930(write))
  -> renderLobby sets roomId=null
  -> checkCrowdCapacity930 called after room exit
  -> db.collection("live_rooms").document(roomId) throws IllegalArgumentException/NPE.

Preserve full room identity and a generation token across every async operation.
Do not accidentally join another room or create a ghost member after leaving.
"""
from pathlib import Path
import sys,shutil

root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
party=pkg/'PartyActivity.java'
src=party.read_text()
shutil.copy2(Path(__file__).with_name('KingPartyJoin966.java'),pkg/'KingPartyJoin966.java')

def replace_once(old,new,label):
    global src
    n=src.count(old)
    if n!=1:raise SystemExit(f'{label}: expected 1 source marker, found {n}: {old[:135]!r}')
    src=src.replace(old,new,1)
    print('PASS',label)

begin='    private void joinMemberThenOpen891(){'
end='    private Map<String,Object> minimalMember921(){'
start=src.find(begin);stop=src.find(end,start)
if start<0 or stop<0 or src.count(begin)!=1 or src.count(end)!=1:
    raise SystemExit('Party join/capacity source section not found')
replacement=r'''    // Each join has its own Firestore room reference and generation. A listener
    // from a previous room must NEVER act on mutable roomId or user.
    private int roomJoinGeneration966;
    private boolean isCurrentPartyJoin966(String requestedRoom966,String requestedUid966,int token966){
        return db!=null && KingPartyJoin966.current(
            requestedRoom966,requestedUid966,token966,
            roomId,user==null?null:user.getUid(),roomJoinGeneration966,
            cloudRoom,!isFinishing()&&!isDestroyed());
    }
    private void joinMemberThenOpen891(){
        if(user==null||db==null||roomId==null||roomId.trim().isEmpty()||!cloudRoom){
            toast("Sign in and choose an active Party Room to join");
            return;
        }
        final String requestedRoom966=roomId;
        final String requestedUid966=user.getUid();
        final int requestToken966=++roomJoinGeneration966;
        final DocumentReference requestedRoomRef966=db.collection("live_rooms").document(requestedRoom966);
        final DocumentReference ref=requestedRoomRef966.collection("members").document(requestedUid966);
        final Runnable write=()->{
            if(!isCurrentPartyJoin966(requestedRoom966,requestedUid966,requestToken966))return;
            Map<String,Object> member=memberPayload891();
            member.put("joinedAt",FieldValue.serverTimestamp());
            ref.set(member,SetOptions.merge())
                .addOnSuccessListener(v->{
                    if(isCurrentPartyJoin966(requestedRoom966,requestedUid966,requestToken966))
                        finishMemberJoin921();
                })
                .addOnFailureListener(error->{
                    if(isCurrentPartyJoin966(requestedRoom966,requestedUid966,requestToken966))
                        retryMinimalMember921(ref,error,requestedRoom966,requestedUid966,requestToken966);
                });
        };
        ref.get()
            .addOnSuccessListener(oldDoc->{
                if(!isCurrentPartyJoin966(requestedRoom966,requestedUid966,requestToken966))return;
                String oldDevice=oldDoc!=null&&oldDoc.exists()?oldDoc.getString("deviceId"):null;
                String now=deviceId891();
                if(oldDevice!=null&&!oldDevice.isEmpty()&&!oldDevice.equals(now)){
                    toast("Both phones use the same KING account: one member ID. Use separate Google accounts to appear as different people.");
                    write.run();
                }else if(oldDoc!=null&&oldDoc.exists())write.run();
                else checkCrowdCapacity930(requestedRoomRef966,requestedRoom966,requestedUid966,requestToken966,write);
            })
            .addOnFailureListener(error->{
                // The user may have returned to the lobby or switched rooms
                // while Firebase was resolving this old query.
                if(!isCurrentPartyJoin966(requestedRoom966,requestedUid966,requestToken966))return;
                KingStability.nonFatal(this,"party-member-lookup-966",error);
                checkCrowdCapacity930(requestedRoomRef966,requestedRoom966,requestedUid966,requestToken966,write);
            });
    }
    private void checkCrowdCapacity930(DocumentReference expectedRoom966,
                                        String expectedId966,String expectedUid966,int token966,
                                        Runnable join){
        if(!isCurrentPartyJoin966(expectedId966,expectedUid966,token966))return;
        // This reference was created from a nonempty room ID BEFORE scheduling
        // any callback. Never call .document(roomId) from this async method.
        if(expectedRoom966==null)return;
        if(isOwner()){join.run();return;}
        expectedRoom966.get()
            .addOnSuccessListener(roomDoc->{
                if(!isCurrentPartyJoin966(expectedId966,expectedUid966,token966))return;
                if(roomDoc==null||!roomDoc.exists()||Boolean.TRUE.equals(roomDoc.getBoolean("closed"))){
                    toast("This Party room is no longer available");
                    renderLobby("Hot");
                    return;
                }
                Long max=roomDoc.getLong("maxMembers");
                int limit=max==null?100:(int)Math.max(10,Math.min(200,max));
                maxMembers930=limit;
                expectedRoom966.collection("members").get()
                    .addOnSuccessListener(members->{
                        if(!isCurrentPartyJoin966(expectedId966,expectedUid966,token966))return;
                        if(members==null){toast("Unable to verify Party members");return;}
                        if(members.size()>=limit){
                            toast("This Party Room is full ("+limit+" users)");
                            renderLobby("Hot");
                            return;
                        }
                        join.run();
                    })
                    .addOnFailureListener(error->{
                        if(isCurrentPartyJoin966(expectedId966,expectedUid966,token966))
                            roomJoinError891("Could not check Party room capacity",error);
                    });
            })
            .addOnFailureListener(error->{
                if(isCurrentPartyJoin966(expectedId966,expectedUid966,token966))
                    roomJoinError891("Could not verify Party room",error);
            });
    }
'''
src=src[:start]+replacement+src[stop:]
print('PASS captured non-null room ID, user UID, member reference, generation and guarded all nested async callbacks')

old='''    private void retryMinimalMember921(DocumentReference ref,Exception fullError){
        ref.set(minimalMember921(),SetOptions.merge()).addOnSuccessListener(v->{KingStability.nonFatal(this,"member-full-fallback",fullError);finishMemberJoin921();}).addOnFailureListener(last->{KingStability.nonFatal(this,"member-full",fullError);KingStability.nonFatal(this,"member-minimal",last);roomJoinError891("Could not join this Party Room",last);});
    }'''
new=r'''    private void retryMinimalMember921(DocumentReference ref,Exception fullError,
                                       String expectedRoom966,String expectedUid966,int token966){
        if(!isCurrentPartyJoin966(expectedRoom966,expectedUid966,token966))return;
        ref.set(minimalMember921(),SetOptions.merge())
            .addOnSuccessListener(v->{
                if(!isCurrentPartyJoin966(expectedRoom966,expectedUid966,token966))return;
                KingStability.nonFatal(this,"member-full-fallback",fullError);
                finishMemberJoin921();
            })
            .addOnFailureListener(last->{
                if(!isCurrentPartyJoin966(expectedRoom966,expectedUid966,token966))return;
                KingStability.nonFatal(this,"member-full",fullError);
                KingStability.nonFatal(this,"member-minimal",last);
                roomJoinError891("Could not join this Party Room",last);
            });
    }'''
replace_once(old,new,'guard fallback member write/callback against leaving or switching rooms')

replace_once(
    '        clearListeners(); unregisterMember(); roomId = null; cloudRoom = false;',
    '        ++roomJoinGeneration966; // Invalidate all pending Firebase room-join callbacks.\n        clearListeners(); unregisterMember(); roomId = null; cloudRoom = false;',
    'invalidate room-join callbacks on lobby transition')

replace_once(
    '@Override protected void onDestroy(){roomImageExecutor964.shutdownNow();',
    '@Override protected void onDestroy(){++roomJoinGeneration966;roomImageExecutor964.shutdownNow();',
    'invalidate join callbacks before Activity destroy')

# The v9.6.4 shared ID/follow and v9.6.5 realtime counters remain intact.
party.write_text(src)

gradle=root/'app/build.gradle'
s=gradle.read_text()
old="versionCode 156; versionName '9.6.5-realtime-social-identity'"
if s.count(old)!=1:raise SystemExit('Expected successful 9.6.5 source baseline')
gradle.write_text(s.replace(old,"versionCode 157; versionName '9.6.6-party-join-null-crash-fix'",1))
print('PASS Android v9.6.6 source ready')
