#!/usr/bin/env python3
"""v9.9.12: KING Plus original-code Party real-time correction on v9.9.11 source."""
from pathlib import Path
import re,sys
root=Path(sys.argv[1]); p=root/'app/src/main/java/com/kingplus/social/PartyActivity.java'
s=p.read_text(encoding='utf-8')
def swap_method(start,end,new,label):
    global s
    assert s.count(start)==1 and s.count(end)==1,label
    a=s.index(start);b=s.index(end,a)
    assert a<b,label
    s=s[:a]+new+s[b:]
def once(old,new,label):
    global s
    assert s.count(old)==1,(label,s.count(old))
    s=s.replace(old,new,1)
swap_method('    private void showOccupiedSeatMenuV530(int seatNoV530) {',
'    private void setSeatMicV530(int seatNoV530, boolean onV530) {',
'''    private void showOccupiedSeatMenuV530(int seatNoV530) {
        final String uidV530=seatUids.get(seatNoV530);
        if(uidV530==null||uidV530.trim().isEmpty()){showEmptySeatMenuV530(seatNoV530);return;}
        final String nameV530=seatNames.get(seatNoV530)==null?"Member":seatNames.get(seatNoV530);
        final boolean micV530=!Boolean.FALSE.equals(seatMics.get(seatNoV530));
        final boolean protectV9912=uidV530.equals(ownerUid)||(user!=null&&uidV530.equals(user.getUid()));
        final String[] actionsV530=protectV9912
            ?new String[]{"👤 View real profile",micV530?"🔇 Mute seat":"🎤 Unmute seat"}
            :new String[]{"👤 View real profile",micV530?"🔇 Mute seat":"🎤 Unmute seat","⤵ Remove from seat","🚪 Kick from room"};
        new AlertDialog.Builder(this).setTitle(nameV530+" • Seat "+seatNoV530).setItems(actionsV530,(d,w)->{
            if(w==0){memberProfileDialog(uidV530,nameV530);return;}
            if(w==1){setSeatMicV530(seatNoV530,!micV530);return;}
            if(protectV9912){toast("Host / own account cannot be kicked");return;}
            if(w==2)kickSeatV530(seatNoV530,false);
            else confirmKickRoomV530(seatNoV530,uidV530,nameV530);
        }).setNegativeButton("Cancel",null).show();
    }

    ''','real-profile-menu')
swap_method('    private void cloudSeatAction(int no) {',
'    private boolean canModerateSeatsV530() {',
'''    private void cloudSeatAction(int no) {
        if(!cloudRoom||!memberSeen||user==null||db==null||roomId==null)return;
        if(no<1||no>maxSeats){toast("Invalid seat");return;}
        final String expectedRoom9912=roomId, expectedUid9912=user.getUid();
        final int expectedGeneration9912=presenceGeneration967, oldSeat9912=mySeat;
        final DocumentReference room9912=db.collection("live_rooms").document(expectedRoom9912);
        final DocumentReference target9912=room9912.collection("seats").document(String.valueOf(no));
        final DocumentReference previous9912=(oldSeat9912>0&&oldSeat9912!=no)
            ?room9912.collection("seats").document(String.valueOf(oldSeat9912)):null;
        db.runTransaction(tx9912->{
            DocumentSnapshot targetDoc9912=tx9912.get(target9912);
            DocumentSnapshot previousDoc9912=previous9912==null?null:tx9912.get(previous9912);
            String occupant9912=targetDoc9912.exists()?targetDoc9912.getString("uid"):null;
            if(occupant9912!=null&&!expectedUid9912.equals(occupant9912))
                throw new com.google.firebase.firestore.FirebaseFirestoreException(
                    "Seat already occupied by another member",com.google.firebase.firestore.FirebaseFirestoreException.Code.ABORTED);
            if(targetDoc9912.exists())tx9912.delete(target9912);
            else{
                Map<String,Object> seat9912=new HashMap<>();
                seat9912.put("uid",expectedUid9912);seat9912.put("name",safeName());
                seat9912.put("micOn",false);seat9912.put("joinedAt",FieldValue.serverTimestamp());
                tx9912.set(target9912,seat9912);
            }
            if(previous9912!=null&&previousDoc9912!=null&&previousDoc9912.exists()
                &&expectedUid9912.equals(previousDoc9912.getString("uid")))tx9912.delete(previous9912);
            return targetDoc9912.exists()?"leave":"take";
        }).addOnSuccessListener(action9912->{
            if(!isActivePresence967(expectedRoom9912,expectedUid9912,expectedGeneration9912))return;
            addEvent("seat",safeName()+("leave".equals(action9912)?" left mic seat":" took seat "+no));
        }).addOnFailureListener(e->{
            if(isActivePresence967(expectedRoom9912,expectedUid9912,expectedGeneration9912))
                toast("Seat was not changed: "+msg(e));
        });
    }

    ''','atomic-seat-move')
swap_method('    private void acceptSeatInviteV530(DocumentSnapshot inviteV530, int seatNoV530) {',
'    private void toggleMic() {',
'''    private void acceptSeatInviteV530(DocumentSnapshot inviteV530, int seatNoV530) {
        if(!cloudRoom||!memberSeen||db==null||user==null||roomId==null)return;
        if(seatNoV530<1||seatNoV530>maxSeats){toast("Invalid invite seat");return;}
        final String expectedRoom9912=roomId, expectedUid9912=user.getUid();
        final int generation9912=presenceGeneration967, previousSeat9912=mySeat;
        final DocumentReference room9912=db.collection("live_rooms").document(expectedRoom9912);
        final DocumentReference seat9912=room9912.collection("seats").document(String.valueOf(seatNoV530));
        final DocumentReference previous9912=(previousSeat9912>0&&previousSeat9912!=seatNoV530)
            ?room9912.collection("seats").document(String.valueOf(previousSeat9912)):null;
        final DocumentReference invitation9912=inviteV530.getReference();
        db.runTransaction(tx9912->{
            DocumentSnapshot currentInvite9912=tx9912.get(invitation9912);
            DocumentSnapshot occupied9912=tx9912.get(seat9912);
            DocumentSnapshot old9912=previous9912==null?null:tx9912.get(previous9912);
            if(!currentInvite9912.exists()||!expectedUid9912.equals(currentInvite9912.getString("targetUid"))
                ||!String.valueOf(seatNoV530).equals(currentInvite9912.getString("seatId")))
                throw new com.google.firebase.firestore.FirebaseFirestoreException(
                    "Invite is no longer valid",com.google.firebase.firestore.FirebaseFirestoreException.Code.ABORTED);
            if(occupied9912.exists()&&!expectedUid9912.equals(occupied9912.getString("uid")))
                throw new com.google.firebase.firestore.FirebaseFirestoreException(
                    "This mic seat was taken",com.google.firebase.firestore.FirebaseFirestoreException.Code.ABORTED);
            if(!occupied9912.exists()){
                Map<String,Object> seatData9912=new HashMap<>();
                seatData9912.put("uid",expectedUid9912);seatData9912.put("name",safeName());
                seatData9912.put("micOn",false);seatData9912.put("joinedAt",FieldValue.serverTimestamp());
                tx9912.set(seat9912,seatData9912);
            }
            if(previous9912!=null&&old9912!=null&&old9912.exists()
                &&expectedUid9912.equals(old9912.getString("uid")))tx9912.delete(previous9912);
            tx9912.delete(invitation9912);
            return null;
        }).addOnSuccessListener(v->{
            if(!isActivePresence967(expectedRoom9912,expectedUid9912,generation9912))return;
            addEvent("seat",safeName()+" accepted a seat invite for seat "+seatNoV530);
        }).addOnFailureListener(e->{
            if(isActivePresence967(expectedRoom9912,expectedUid9912,generation9912))
                toast("Could not accept seat invite: "+msg(e));
        });
    }

    ''','safe-seat-invite')
once('''    private void attachV530RealtimeHelpers() {
        if(db==null || user==null || roomId==null) return;
        DocumentReference roomV530=db.collection("live_rooms").document(roomId);
        roleListenerV530=roomV530.collection("roles").document(user.getUid()).addSnapshotListener((docV530,eV530) -> {
            roomModeratorV530 = isOwner() || (docV530!=null && docV530.exists() && "cohost".equals(docV530.getString("role")));
        });
        seatInviteListenerV530=roomV530.collection("seat_invites").document(user.getUid()).addSnapshotListener((docV530,eV530) -> {
            if(eV530!=null || docV530==null || !docV530.exists() || seatInviteDialogOpenV530) return;''',
'''    private void attachV530RealtimeHelpers() {
        if(db==null || user==null || roomId==null) return;
        final String boundRoom9912=roomId, boundUid9912=user.getUid();
        final int boundGeneration9912=presenceGeneration967;
        DocumentReference roomV530=db.collection("live_rooms").document(boundRoom9912);
        roleListenerV530=roomV530.collection("roles").document(boundUid9912).addSnapshotListener((docV530,eV530) -> {
            if(!isActivePresence967(boundRoom9912,boundUid9912,boundGeneration9912))return;
            roomModeratorV530 = isOwner() || (eV530==null && docV530!=null && docV530.exists() && "cohost".equals(docV530.getString("role")));
        });
        seatInviteListenerV530=roomV530.collection("seat_invites").document(boundUid9912).addSnapshotListener((docV530,eV530) -> {
            if(!isActivePresence967(boundRoom9912,boundUid9912,boundGeneration9912))return;
            if(eV530!=null || docV530==null || !docV530.exists() || seatInviteDialogOpenV530) return;''','old-room-callback')
once('''    private void kickFromRoomV530(int seatNoV530, String uidV530, String nameV530) {
        if(!canModerateSeatsV530() || db==null || roomId==null || uidV530==null) { toast("Host / co-host only"); return; }
        kickSeatV530(seatNoV530,true);
        db.collection("live_rooms").document(roomId).collection("members").document(uidV530).delete()
            .addOnSuccessListener(v -> addEvent("room_kick",nameV530+" was kicked from the room"))
            .addOnFailureListener(e -> toast("Room kick failed: "+msg(e)));
    }''',
'''    private void kickFromRoomV530(int seatNoV530, String uidV530, String nameV530) {
        if(!canModerateSeatsV530()||db==null||roomId==null||uidV530==null||uidV530.equals(ownerUid)
            ||(user!=null&&uidV530.equals(user.getUid()))){toast("Cannot kick this account");return;}
        final String boundRoom9912=roomId;
        DocumentReference room9912=db.collection("live_rooms").document(boundRoom9912);
        WriteBatch batch9912=db.batch();
        if(seatNoV530>0&&seatNoV530<=maxSeats)
            batch9912.delete(room9912.collection("seats").document(String.valueOf(seatNoV530)));
        batch9912.delete(room9912.collection("members").document(uidV530));
        batch9912.commit().addOnSuccessListener(v->{
            if(boundRoom9912.equals(roomId))addEvent("room_kick",nameV530+" was kicked from the room");
        }).addOnFailureListener(e->{
            if(boundRoom9912.equals(roomId))toast("Room kick failed: "+msg(e));
        });
    }''','atomic-room-kick')
p.write_text(s,encoding='utf-8')
g=root/'app/build.gradle'; t=g.read_text(encoding='utf-8')
t,n=re.subn(r"versionCode\s+190;\s*versionName\s+'9\.9\.11-parity-pass1-test'",
    "versionCode 191; versionName '9.9.12-party-sync-test'",t)
assert n==1,'wrong input version'
g.write_text(t,encoding='utf-8')
assert s.count('db.runTransaction(tx9912')==2 and 'View real profile' in s
print('PASS: secure seat handoff/invite, role callbacks, moderator controls, compile source for v9.9.12')
