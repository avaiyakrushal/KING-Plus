#!/usr/bin/env python3
"""KING Plus v9.6.7: make multi-phone Party membership stable across reconnect/leave.

Source: successfully built v9.6.6. One source-of-truth room reference, Firebase UID,
per-visit session identifier and generation token for all async presence callbacks.

Critical fixes:
 * Member leave removes only its own room session, never another device logged
   into the same Firebase UID, and never a newer visit of the same device.
 * Delayed heartbeats/auto-rejoin and Host stale cleanup cannot modify a room
   after leaving/switching to another one.
 * Member list keys use authoritative Firestore document IDs rather than an
   optional / stale "uid" payload field.
 * Server-only scans and session guards prevent an old Host cleanup callback
   from removing a new room's valid participant/seat.
 * Keep v9.6.6 null-room safeguards and v9.6.4 low-memory mitigations.
"""
from pathlib import Path
import sys,shutil

root=Path(sys.argv[1]);pkg=root/'app/src/main/java/com/kingplus/social'
source=pkg/'PartyActivity.java';s=source.read_text()
shutil.copy2(Path(__file__).with_name('KingRoomPresence967.java'),pkg/'KingRoomPresence967.java')

def change(old,new,label):
    global s
    n=s.count(old)
    if n!=1:raise SystemExit(f'{label}: expected exactly one source marker but found {n}: {old[:115]!r}')
    s=s.replace(old,new,1)
    print('PASS',label)

# Room identity: match every async result to the signed-in user and the visit that requested it.
marker='    private int roomJoinGeneration966;'
addition=r'''    private int presenceGeneration967;
    private String presenceSession967="";
    private boolean isActivePresence967(String expectedRoom,String expectedUid,int expectedGeneration){
        return db!=null && KingRoomPresence967.isActive(
            expectedRoom,expectedUid,expectedGeneration,roomId,
            user==null?null:user.getUid(),presenceGeneration967,
            cloudRoom,!isFinishing()&&!isDestroyed());
    }
'''
change(marker,addition+marker,'track immutable Party visit identity')

change(
    '        roomId=id.trim(); roomName=name;',
    '''        if(cloudRoom&&roomId!=null&&!roomId.equals(id.trim()))unregisterMember();
        ++presenceGeneration967;
        presenceSession967=java.util.UUID.randomUUID().toString();
        roomId=id.trim(); roomName=name;''',
    'make new presence session on every Party room entry')

change(
    '        clearListeners(); unregisterMember(); roomId = null; cloudRoom = false;',
    '        ++presenceGeneration967; // Existing callback requests belong to the old room.\n        clearListeners(); unregisterMember(); roomId = null; cloudRoom = false; memberSeen=false;',
    'invalidate delayed presence results and reset visible membership on lobby exit')

change(
    '@Override protected void onDestroy(){++roomJoinGeneration966;roomImageExecutor964.shutdownNow();',
    '@Override protected void onDestroy(){++roomJoinGeneration966;++presenceGeneration967;roomImageExecutor964.shutdownNow();',
    'invalidate outstanding presence operations at Activity destruction')

# Existing membership fields must follow one exact visit, including fallback writes.
change(
    'd.put("deviceId",deviceId891());d.put("online",true);',
    'd.put("deviceId",deviceId891());d.put("sessionId",presenceSession967);d.put("online",true);',
    'attach Party visit session ID to full member payload')

change(
    'Map<String,Object>m=new HashMap<>();m.put("uid",user.getUid());m.put("name",safeName());m.put("joinedAt",FieldValue.serverTimestamp());m.put("lastSeenAt",FieldValue.serverTimestamp());return m;',
    'Map<String,Object>m=new HashMap<>();m.put("uid",user.getUid());m.put("name",safeName());m.put("deviceId",deviceId891());m.put("sessionId",presenceSession967);m.put("joinedAt",FieldValue.serverTimestamp());m.put("lastSeenAt",FieldValue.serverTimestamp());return m;',
    'include visit identity in fallback Firestore member writes')

change(
    'Map<String,Object>d=new HashMap<>();d.put("uid",user.getUid());d.put("name",safeName());d.put("joinedAt",FieldValue.serverTimestamp());',
    'Map<String,Object>d=new HashMap<>();d.put("uid",user.getUid());d.put("name",safeName());d.put("deviceId",deviceId891());d.put("sessionId",presenceSession967);d.put("joinedAt",FieldValue.serverTimestamp());',
    'include session and device in private Party entry')

# Do not start an independent heartbeat which races the initial join in OnResume.
change(
    'if(cloudRoom&&roomId!=null&&user!=null){ensureRoomMembership900();startHeartbeat900();}',
    'if(cloudRoom&&roomId!=null&&user!=null&&memberSeen){ensureRoomMembership900();startHeartbeat900();}',
    'wait until the initial member join is confirmed before resume heartbeat')

# With the new permission logic, a leave only removes the exact last-written
# session. This also prevents a stale device from removing a newer device with
# the same Google/Firebase account.
change(
    '    private void unregisterMember(){stopHeartbeat900();if(user!=null&&db!=null&&cloudRoom&&roomId!=null)db.collection("live_rooms").document(roomId).collection("members").document(user.getUid()).delete();}',
    r'''    private void unregisterMember(){
        stopHeartbeat900();
        if(user==null||db==null||!cloudRoom||roomId==null||roomId.trim().isEmpty())return;
        final String leavingRoom967=roomId;
        final String leavingUid967=user.getUid();
        final String leavingSession967=presenceSession967;
        final String leavingDevice967=deviceId891();
        final DocumentReference leavingMember967=db.collection("live_rooms")
            .document(leavingRoom967).collection("members").document(leavingUid967);
        db.runTransaction(tx->{
            DocumentSnapshot doc=tx.get(leavingMember967);
            if(doc.exists()&&leavingUid967.equals(doc.getString("uid"))&&
                KingRoomPresence967.canRemove(doc.getString("sessionId"),leavingSession967,
                    doc.getString("deviceId"),leavingDevice967))tx.delete(leavingMember967);
            return null;
        }).addOnFailureListener(e->KingStability.nonFatal(this,"party-leave-member-967",e));
    }''',
    'transactionally remove only the correct device and Party visit, not another phone')

change(
    'private void leaveRoom(){roomVoiceOptedIn957=false;partyShell940=null;stopInRoomVoice940(true);unregisterMember();clearListeners();renderLobby("Hot");}',
    'private void leaveRoom(){roomVoiceOptedIn957=false;partyShell940=null;stopInRoomVoice940(true);renderLobby("Hot");}',
    'avoid double leave transactions and listener teardown')

# Heartbeat callback should not write or update the UI for another visit.
begin='    private void heartbeat900(){'
end='    private void cleanupStaleRoom910(){'
a=s.find(begin);b=s.find(end,a)
if a<0 or b<0 or s.count(begin)!=1 or s.count(end)!=1:raise SystemExit('heartbeat method boundaries changed')
s=s[:a]+r'''    private void heartbeat900(){
        if(!cloudRoom||db==null||user==null||roomId==null||roomId.trim().isEmpty()||!memberSeen)return;
        final String expectedRoom967=roomId,expectedUid967=user.getUid();
        final int expectedGeneration967=presenceGeneration967;
        final DocumentReference memberRef967=db.collection("live_rooms").document(expectedRoom967)
            .collection("members").document(expectedUid967);
        final Map<String,Object>memberPayload967=memberPayload891();
        memberRef967.set(memberPayload967,SetOptions.merge())
            .addOnSuccessListener(v->{
                if(!isActivePresence967(expectedRoom967,expectedUid967,expectedGeneration967))return;
                setOnlineState900(true,"Online • synced");
                cleanupStaleRoom910();
            })
            .addOnFailureListener(error->{
                if(!isActivePresence967(expectedRoom967,expectedUid967,expectedGeneration967))return;
                Map<String,Object>minimal967=new HashMap<>();
                minimal967.put("uid",expectedUid967);
                minimal967.put("name",safeName());
                minimal967.put("deviceId",deviceId891());
                minimal967.put("sessionId",presenceSession967);
                minimal967.put("lastSeenAt",FieldValue.serverTimestamp());
                memberRef967.set(minimal967,SetOptions.merge())
                    .addOnSuccessListener(v->{
                        if(!isActivePresence967(expectedRoom967,expectedUid967,expectedGeneration967))return;
                        setOnlineState900(true,"Online • compatibility");
                        cleanupStaleRoom910();
                    })
                    .addOnFailureListener(last->{
                        if(!isActivePresence967(expectedRoom967,expectedUid967,expectedGeneration967))return;
                        setOnlineState900(false,"Sync blocked");
                        KingStability.nonFatal(this,"party-heartbeat",error);
                        KingStability.nonFatal(this,"party-heartbeat-minimal",last);
                    });
            });
    }
''' + s[b:]
print('PASS heartbeat retries never re-create a departed room member')

# Prevent old asynchronous Host scans affecting new rooms or using a new owner UID.
begin='    private void cleanupStaleRoom910(){'
end='    private void setOnlineState900(boolean ok,String text){'
a=s.find(begin);b=s.find(end,a)
if a<0 or b<0 or s.count(begin)!=1:raise SystemExit('cleanup source boundaries changed')
s=s[:a]+r'''    private void cleanupStaleRoom910(){
        if(!isOwner()||db==null||roomId==null||user==null||roomId.isEmpty())return;
        long now967=System.currentTimeMillis();
        if(now967-lastCleanup910<120000L)return;
        lastCleanup910=now967;
        final String expectedRoom967=roomId,expectedUid967=user.getUid(),owner967=ownerUid;
        final int generation967=presenceGeneration967;
        final DocumentReference expectedRoomRef967=db.collection("live_rooms").document(expectedRoom967);
        // Server-only reads prevent cached stale snapshots from evicting valid remote members.
        expectedRoomRef967.collection("members").get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(members967->{
                if(!isActivePresence967(expectedRoom967,expectedUid967,generation967)||!isOwner())return;
                WriteBatch removedMembers967=db.batch();
                int removed967=0;
                final java.util.HashSet<String>active967=new java.util.HashSet<>();
                for(DocumentSnapshot m967:members967.getDocuments()){
                    String uid967=m967.getId();
                    com.google.firebase.Timestamp last967=m967.getTimestamp("lastSeenAt");
                    boolean stale967=last967!=null
                        && KingRoomPresence967.isStale(last967.toDate().getTime(),now967,300000L);
                    if(stale967&&!uid967.equals(owner967)){
                        removedMembers967.delete(m967.getReference());
                        removed967++;
                    }else active967.add(uid967);
                }
                if(removed967>0)removedMembers967.commit()
                    .addOnFailureListener(e->{
                        if(isActivePresence967(expectedRoom967,expectedUid967,generation967))
                            KingStability.nonFatal(this,"stale-members",e);
                    });
                expectedRoomRef967.collection("seats")
                    .get(com.google.firebase.firestore.Source.SERVER)
                    .addOnSuccessListener(seats967->{
                        if(!isActivePresence967(expectedRoom967,expectedUid967,generation967)||!isOwner())return;
                        WriteBatch seatBatch967=db.batch();
                        int removedSeats967=0;
                        for(DocumentSnapshot seat967:seats967.getDocuments()){
                            String sittingUid967=seat967.getString("uid");
                            if(sittingUid967!=null&&!sittingUid967.equals(owner967)
                               &&!active967.contains(sittingUid967)){
                                seatBatch967.delete(seat967.getReference());
                                removedSeats967++;
                            }
                        }
                        if(removedSeats967>0)seatBatch967.commit()
                            .addOnFailureListener(e->{
                                if(isActivePresence967(expectedRoom967,expectedUid967,generation967))
                                    KingStability.nonFatal(this,"stale-seats",e);
                            });
                    })
                    .addOnFailureListener(e->{
                        if(isActivePresence967(expectedRoom967,expectedUid967,generation967))
                            KingStability.nonFatal(this,"stale-seats-scan",e);
                    });
            })
            .addOnFailureListener(e->{
                if(isActivePresence967(expectedRoom967,expectedUid967,generation967))
                    KingStability.nonFatal(this,"stale-scan",e);
            });
    }
''' + s[b:]
print('PASS Host cleanup uses same original room, verified presence and fresh Firebase data')

# The old membership auto-repair callbacks can also resurrect a document
# after a user has left. Each nested read/write uses the captured room session.
begin='    private void ensureRoomMembership900(){'
end='    private void setVoicePresence900(boolean joined){'
a=s.find(begin);b=s.find(end,a)
if a<0 or b<0 or s.count(begin)!=1:raise SystemExit('auto-rejoin method boundaries changed')
s=s[:a]+r'''    private void ensureRoomMembership900(){
        if(!cloudRoom||db==null||user==null||roomId==null||roomId.isEmpty()||!memberSeen)return;
        final String requestedRoom967=roomId,requestedUid967=user.getUid();
        final int requestedGeneration967=presenceGeneration967;
        final DocumentReference room967=db.collection("live_rooms").document(requestedRoom967);
        final DocumentReference member967=room967.collection("members").document(requestedUid967);
        room967.get()
            .addOnSuccessListener(roomDoc967->{
                if(!isActivePresence967(requestedRoom967,requestedUid967,requestedGeneration967))return;
                if(roomDoc967==null||!roomDoc967.exists()||
                   Boolean.TRUE.equals(roomDoc967.getBoolean("closed"))){
                    setOnlineState900(false,"Room unavailable");return;
                }
                member967.get()
                    .addOnSuccessListener(memberDoc967->{
                        if(!isActivePresence967(requestedRoom967,requestedUid967,requestedGeneration967))return;
                        if(memberDoc967!=null&&memberDoc967.exists()){
                            heartbeat900();return;
                        }
                        Map<String,Object>join967=memberPayload891();
                        join967.put("joinedAt",FieldValue.serverTimestamp());
                        member967.set(join967,SetOptions.merge())
                            .addOnSuccessListener(v->{
                                if(!isActivePresence967(requestedRoom967,requestedUid967,requestedGeneration967))return;
                                memberSeen=true;setOnlineState900(true,"Online • rejoined");
                            })
                            .addOnFailureListener(error->{
                                if(!isActivePresence967(requestedRoom967,requestedUid967,requestedGeneration967))return;
                                member967.set(minimalMember921(),SetOptions.merge())
                                    .addOnSuccessListener(v->{
                                        if(!isActivePresence967(requestedRoom967,requestedUid967,requestedGeneration967))return;
                                        memberSeen=true;setOnlineState900(true,"Online • compatibility rejoin");
                                    })
                                    .addOnFailureListener(last->{
                                        if(isActivePresence967(requestedRoom967,requestedUid967,requestedGeneration967))
                                            setOnlineState900(false,"Join blocked");
                                    });
                            });
                    })
                    .addOnFailureListener(e->{
                        if(isActivePresence967(requestedRoom967,requestedUid967,requestedGeneration967))
                            setOnlineState900(false,"Member lookup failed");
                    });
            })
            .addOnFailureListener(e->{
                if(isActivePresence967(requestedRoom967,requestedUid967,requestedGeneration967))
                    setOnlineState900(false,"Backend blocked");
            });
    }
''' + s[b:]
print('PASS async automatic member rejoin cannot resurrect membership after leaving')

# Firestore members document key is the authenticated Firebase UID, even if
# an older payload has missing / incorrect uid.
change(
    'membersListener=room.collection("members").addSnapshotListener((snap,e)->{if(snap==null)return;',
    'final String memberRoom967=room.getId();\n        membersListener=room.collection("members").addSnapshotListener((snap,e)->{if(snap==null||isFinishing()||isDestroyed()||!cloudRoom||roomId==null||!roomId.equals(memberRoom967))return;',
    'stop old-room live-members callbacks on Party switch')

change(
    'String base=str(m,"name","User"),uid=m.getString("uid");',
    'String base=str(m,"name","User"),uid=m.getId();',
    'use authentic Firestore UID for all visible Party member cards')

# Show actual installed version rather than a hard-coded 9.3.1 in every room.
change(
    'd.put("appVersion","9.3.1");d.put("voiceJoined",voiceLaunched900);',
    '''try{d.put("appVersion",getPackageManager().getPackageInfo(getPackageName(),0).versionName);}
        catch(Exception ignored){d.put("appVersion","9.6.7");}
        d.put("voiceJoined",voiceLaunched900);''',
    'make Room member status version match installed KING Plus')

source.write_text(s)

gradle=root/'app/build.gradle';g=gradle.read_text()
old="versionCode 157; versionName '9.6.6-party-join-null-crash-fix'"
if g.count(old)!=1:raise SystemExit('Expected successful v9.6.6 source')
gradle.write_text(g.replace(old,"versionCode 158; versionName '9.6.7-live-room-presence'",1))
print('PASS source patched: v9.6.7 live room presence and multi-phone member safety')
