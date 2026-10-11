#!/usr/bin/env python3
"""KING Plus v9.9.19: safely clear stale mic seats when leaving a Firebase Room."""
from pathlib import Path
import re,sys
root=Path(sys.argv[1]); java=root/'app/src/main/java/com/kingplus/social/PartyActivity.java'
s=java.read_text()
start='    private void unregisterMember(){'
end='    private void addEvent(String type,String text)'
assert s.count(start)==1 and s.count(end)==1
l=s.index(start); r=s.index(end,l); assert l<r
new='''    // Leaving or switching a Firebase Room must also release its live mic seat.
    // Formerly only the member record was deleted, leaving a ghost occupied seat.
    // The session guard prevents an older phone from removing the newer session.
    private void unregisterMember(){
        stopHeartbeat900();
        if(user==null||db==null||!cloudRoom||roomId==null||roomId.trim().isEmpty())return;
        final String leavingRoom9919=roomId;
        final String leavingUid9919=user.getUid();
        final String leavingSession9919=presenceSession967;
        final String leavingDevice9919=deviceId891();
        final DocumentReference room9919=db.collection("live_rooms").document(leavingRoom9919);
        final DocumentReference member9919=room9919.collection("members").document(leavingUid9919);
        // Query only seats belonging to this account, rather than trusting a
        // potentially stale mySeat value from a previous Party screen.
        room9919.collection("seats").whereEqualTo("uid",leavingUid9919)
            .limit(16).get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(seatQuery9919->{
                final java.util.ArrayList<DocumentReference> refs9919=new java.util.ArrayList<>();
                for(DocumentSnapshot seat9919:seatQuery9919.getDocuments())
                    refs9919.add(seat9919.getReference());
                db.runTransaction(tx9919->{
                    // Read all documents first; Firestore transactions require it.
                    DocumentSnapshot joined9919=tx9919.get(member9919);
                    final java.util.ArrayList<DocumentSnapshot> held9919=new java.util.ArrayList<>();
                    for(DocumentReference ref9919:refs9919)held9919.add(tx9919.get(ref9919));
                    boolean ownsSession9919=joined9919.exists()
                        && leavingUid9919.equals(joined9919.getString("uid"))
                        && KingRoomPresence967.canRemove(
                            joined9919.getString("sessionId"),leavingSession9919,
                            joined9919.getString("deviceId"),leavingDevice9919);
                    if(!ownsSession9919)return 0;
                    int removed9919=0;
                    for(int i9919=0;i9919<refs9919.size();i9919++){
                        DocumentSnapshot seat9919=held9919.get(i9919);
                        if(seat9919.exists()&&leavingUid9919.equals(seat9919.getString("uid"))){
                            tx9919.delete(refs9919.get(i9919));removed9919++;
                        }
                    }
                    tx9919.delete(member9919);
                    return removed9919;
                }).addOnSuccessListener(removed9919->{
                    android.util.Log.i("KINGPlusParty","Room membership exit synced; mic seats released="+removed9919);
                }).addOnFailureListener(e9919->
                    KingStability.nonFatal(this,"room-exit-seat-transaction-9919",e9919));
            }).addOnFailureListener(e9919->{
                // Fallback never guesses which seat is owned from local UI;
                // only the session-authenticated membership record is removed.
                KingStability.nonFatal(this,"room-exit-seat-query-9919",e9919);
                db.runTransaction(tx9919->{
                    DocumentSnapshot joined9919=tx9919.get(member9919);
                    if(joined9919.exists()&&leavingUid9919.equals(joined9919.getString("uid"))
                        && KingRoomPresence967.canRemove(
                            joined9919.getString("sessionId"),leavingSession9919,
                            joined9919.getString("deviceId"),leavingDevice9919))
                        tx9919.delete(member9919);
                    return null;
                }).addOnFailureListener(err9919->
                    KingStability.nonFatal(this,"room-exit-member-fallback-9919",err9919));
            });
    }

'''
s=s[:l]+new+s[r:];java.write_text(s)
assert 'whereEqualTo("uid",leavingUid9919)' in s
assert s.count('tx9919.delete(member9919)')==2
for token in ['approveOrMoveMemberSeat9913','openLiveChess9917','db.runTransaction(tx9912','inRoomVoiceJoined940']:
 assert token in s,token
p=root/'app/build.gradle'; t=p.read_text()
t,n=re.subn(r"versionCode\s+197;\s*versionName\s+'9\.9\.18[^']*'",
    "versionCode 198; versionName '9.9.19-room-seat-exit-test'",t)
assert n==1,"Expected verified v9.9.18 source"
p.write_text(t)
print("PASS: protected room-leave membership and mic-seat cleanup, prior Party/Chess retained")
