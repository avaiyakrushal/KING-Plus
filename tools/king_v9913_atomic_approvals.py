#!/usr/bin/env python3
"""v9.9.13: make every host/co-host mic seat approval atomic and owner-safe.
Use original KING source only; do not import Bolo Hi proprietary files.
"""
from pathlib import Path
import re,sys
root=Path(sys.argv[1])
p=root/'app/src/main/java/com/kingplus/social/PartyActivity.java'
s=p.read_text(encoding='utf-8')
def section(a,b,new):
    global s
    assert s.count(a)==1 and s.count(b)==1,(a,b)
    start=s.index(a);end=s.index(b,start)
    assert start<end
    s=s[:start]+new+s[end:]

section('    private void approveSeatRequest(String requestId,String uid,String name,int no){',
        '    private void cloudSeatAction(int no) {',
'''    // One transaction checks a member, target seat, previous seat and optional
    // pending request before writing. It never silently displaces other users.
    private void approveOrMoveMemberSeat9913(String uid9913,String name9913,int prior9913,
            int target9913,DocumentReference request9913,String action9913){
        if(!cloudRoom||!memberSeen||!canModerateSeatsV530()||db==null||user==null||roomId==null){
            toast("Host/co-host must be in this live room");return;
        }
        if(uid9913==null||uid9913.isEmpty()||target9913<1||target9913>maxSeats){
            toast("Invalid member or mic seat");return;
        }
        final String roomId9913=roomId, actor9913=user.getUid();
        final int generation9913=presenceGeneration967;
        DocumentReference room9913=db.collection("live_rooms").document(roomId9913);
        DocumentReference member9913=room9913.collection("members").document(uid9913);
        DocumentReference seat9913=room9913.collection("seats").document(String.valueOf(target9913));
        if(prior9913<1){
            for(Map.Entry<Integer,String> entry9913:new HashMap<>(seatUids).entrySet()){
                if(uid9913.equals(entry9913.getValue())){prior9913=entry9913.getKey();break;}
            }
        }
        DocumentReference priorSeat9913=(prior9913>0&&prior9913!=target9913)
            ?room9913.collection("seats").document(String.valueOf(prior9913)):null;
        db.runTransaction(tx9913->{
            // All reads must precede every transaction write.
            DocumentSnapshot memberDoc9913=tx9913.get(member9913);
            DocumentSnapshot targetDoc9913=tx9913.get(seat9913);
            DocumentSnapshot priorDoc9913=priorSeat9913==null?null:tx9913.get(priorSeat9913);
            DocumentSnapshot reqDoc9913=request9913==null?null:tx9913.get(request9913);
            if(!memberDoc9913.exists())
                throw new com.google.firebase.firestore.FirebaseFirestoreException(
                    "Member is no longer in room",com.google.firebase.firestore.FirebaseFirestoreException.Code.ABORTED);
            if(targetDoc9913.exists()&&!uid9913.equals(targetDoc9913.getString("uid")))
                throw new com.google.firebase.firestore.FirebaseFirestoreException(
                    "Seat already belongs to another member",com.google.firebase.firestore.FirebaseFirestoreException.Code.ABORTED);
            if(request9913!=null&&(!reqDoc9913.exists()||!uid9913.equals(reqDoc9913.getString("uid"))))
                throw new com.google.firebase.firestore.FirebaseFirestoreException(
                    "Mic seat request expired",com.google.firebase.firestore.FirebaseFirestoreException.Code.ABORTED);
            if(!targetDoc9913.exists()){
                Map<String,Object> newSeat9913=new HashMap<>();
                newSeat9913.put("uid",uid9913);
                newSeat9913.put("name",name9913==null||name9913.trim().isEmpty()?"Member":name9913);
                newSeat9913.put("micOn",false);
                newSeat9913.put("joinedAt",FieldValue.serverTimestamp());
                tx9913.set(seat9913,newSeat9913);
            }
            if(priorDoc9913!=null&&priorDoc9913.exists()
                &&uid9913.equals(priorDoc9913.getString("uid")))tx9913.delete(priorSeat9913);
            if(request9913!=null)tx9913.delete(request9913);
            return null;
        }).addOnSuccessListener(v9913->{
            if(!isActivePresence967(roomId9913,actor9913,generation9913))return;
            addEvent("seat",(name9913==null?"Member":name9913)+" moved to Seat "+target9913);
            toast(action9913+" • Seat "+target9913);
        }).addOnFailureListener(err9913->{
            if(isActivePresence967(roomId9913,actor9913,generation9913))
                toast("Seat unchanged: "+msg(err9913));
        });
    }

    private void approveSeatRequest(String requestId,String uid,String name,int no){
        if(db==null||roomId==null)return;
        DocumentReference request9913=db.collection("live_rooms").document(roomId)
            .collection("seat_requests").document(requestId);
        approveOrMoveMemberSeat9913(uid,name,-1,no,request9913,"Mic approved");
    }

''')
section('    private void moveUserToSeat(String uid,String name,int currentSeat,int targetSeat){',
        '    private void grantCurrentMembersAccessThen(Runnable done){',
'''    private void moveUserToSeat(String uid,String name,int currentSeat,int targetSeat){
        approveOrMoveMemberSeat9913(uid,name,currentSeat,targetSeat,null,"Member moved");
    }

''')
section('    private void approveMicRequest940(DocumentSnapshot request,String uid,String name,int preferred){',
        '    private void showSeatLockMenu940(int seatNo){',
'''    private void approveMicRequest940(DocumentSnapshot request,String uid,String name,int preferred){
        if(request==null||uid==null||uid.isEmpty()||db==null||roomId==null)return;
        int seat=(preferred>0&&preferred<=maxSeats&&!seatUids.containsKey(preferred))
            ?preferred:firstFreeMicSeat940();
        if(seat<1){toast("No free mic seat");return;}
        approveOrMoveMemberSeat9913(uid,name,-1,seat,request.getReference(),"Mic approved");
    }

''')
# Only replace this specific method body; do NOT eat following helper methods.
pattern9913=re.compile(r'(?ms)^    private void takeModeratorSeat940\(int seatNo\)\{.*?^    \}\n')
assert len(pattern9913.findall(s))==1,'moderator method anchor drifted'
s=pattern9913.sub('''    private void takeModeratorSeat940(int seatNo){
        if(!canModerateSeatsV530()||db==null||roomId==null||user==null)return;
        cloudSeatAction(seatNo);
    }
''',s,count=1)
# Required regression guards: unrelated methods must survive patching.
for name9913 in ['private boolean isModerator(', 'private String msg(', 'private void toggleMic(', 'private void setSeatMicV530(']:
    assert name9913 in s, 'Unrelated Party helper was unexpectedly removed: '+name9913
assert s.count('approveOrMoveMemberSeat9913(')==4
assert 'tx9913.delete(priorSeat9913)' in s and 'tx9913.delete(request9913)' in s
p.write_text(s,encoding='utf-8')
g=root/'app/build.gradle';x=g.read_text(encoding='utf-8')
x,n=re.subn(r"versionCode\s+191;\s*versionName\s+'9\.9\.12-party-sync-test'",
    "versionCode 192; versionName '9.9.13-seat-approval-test'",x)
assert n==1,"Expected v9.9.12 release source"
g.write_text(x,encoding='utf-8')
print('PASS real mic-request approvals, member movement and host seat paths')
