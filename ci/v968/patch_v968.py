#!/usr/bin/env python3
"""KING Plus v9.6.8 Party Mic/Seat realtime integrity on the successful v9.6.7 APK source.

- Align all member mic-seat requests with deployed Firestore rule: document == auth.uid.
- Reject stale room/user callbacks and duplicate pending requests.
- Mic ON/OFF is a server-confirmed transaction against the occupied user's own seat,
  with muted audio until server confirms the change; never start Jitsi for a failed write.
- Stop cross-room mic/seat listener updates; avoid duplicate seat-lock listeners.
- Preserve earlier room join, member presence, social Follow and crash safeguards.
"""
from pathlib import Path
import shutil,sys
root=Path(sys.argv[1]);pkg=root/'app/src/main/java/com/kingplus/social'
party=pkg/'PartyActivity.java';s=party.read_text()
shutil.copy2(Path(__file__).with_name('KingPartyMic968.java'),pkg/'KingPartyMic968.java')
def change(old,new,label):
 global s
 n=s.count(old)
 if n!=1:raise SystemExit(f'{label}: expected unique marker, got {n}: {old[:120]!r}')
 s=s.replace(old,new,1)
 print('PASS',label)

change('    private boolean micOn;',
       '    private boolean micOn;\n    private boolean micTogglePending968;',
       'avoid double Mic taps while Firestore transaction in flight')

change('roomId=id.trim(); roomName=name; ownerUid=hostUid;',
       'roomId=id.trim(); micTogglePending968=false; roomName=name; ownerUid=hostUid;',
       'reset pending mic state when opening a different room')

# Central member request flow. Deployed rules permit only /seat_requests/<auth uid>.
start=s.index('    private void requestSeat(int no){')
end=s.index('    private void showSeatRequests(int no){',start)
replacement=r'''    private void requestSeat(int no){
        final String requestedRoom968=roomId;
        final String requestedUid968=user==null?null:user.getUid();
        if(!KingPartyMic968.validRequest(requestedRoom968,requestedUid968,no,maxSeats,
                cloudRoom&&memberSeen&&!isFinishing()&&!isDestroyed(),true)||db==null){
            toast("Join an active Party before requesting a valid Mic seat");return;
        }
        final int generation968=presenceGeneration967;
        final DocumentReference req968=db.collection("live_rooms").document(requestedRoom968)
            .collection("seat_requests").document(KingPartyMic968.requestId(requestedUid968));
        req968.get().addOnSuccessListener(existing968->{
            if(!isActivePresence967(requestedRoom968,requestedUid968,generation968))return;
            if(existing968!=null&&existing968.exists()){
                Long pendingSeat968=existing968.getLong("seatNo");
                toast("Your Mic request is pending"+(pendingSeat968==null?"":" • Seat "+pendingSeat968));return;
            }
            Map<String,Object>request968=new HashMap<>();
            request968.put("uid",requestedUid968);
            request968.put("name",safeName());
            request968.put("seatNo",no);
            request968.put("createdAt",FieldValue.serverTimestamp());
            req968.set(request968)
                .addOnSuccessListener(v->{
                    if(isActivePresence967(requestedRoom968,requestedUid968,generation968))
                        toast("Seat "+no+" request sent to the host");
                })
                .addOnFailureListener(e->{
                    if(isActivePresence967(requestedRoom968,requestedUid968,generation968))
                        toast("Seat request failed: "+msg(e));
                });
        }).addOnFailureListener(e->{
            if(isActivePresence967(requestedRoom968,requestedUid968,generation968))
                toast("Cannot check your Mic request: "+msg(e));
        });
    }
'''
s=s[:start]+replacement+s[end:];print('PASS', 'Seat requests use one authenticated Firebase UID document and stale-session guards')

# Both mic button and Seat Request button must use the same /seat_requests/<uid> document.
start=s.index('    private void requestMicSeat940(){')
end=s.index('    private void openMicRequests940(){',start)
s=s[:start]+r'''    private void requestMicSeat940(){
        if(!cloudRoom||db==null||user==null||roomId==null||!memberSeen){
            toast("Join a live Party room first");return;
        }
        if(muteAll&&!isModerator()){toast("Host muted room microphones");return;}
        final int seatNo=firstFreeMicSeat940();
        if(seatNo<1){toast("All Mic seats are occupied");return;}
        requestSeat(seatNo);
    }
''' + s[end:]
print('PASS', 'Mic request action shares same authoritative seat request document')

# Critical: current code sets Mic ON and launches Jitsi BEFORE Firestore responds, so
# a rejected permission/write leaves local UI and native engine in the wrong state.
start=s.index('    private void toggleMic() {')
end=s.index('    private void refreshMicControl(){',start)
s=s[:start]+r'''    private void toggleMic() {
        if(mySeat<1){
            if(cloudRoom){requestMicSeat940();return;}
            toast("Take a Mic seat first");return;
        }
        if(muteAll&&!isModerator()){toast("Host muted all seats");return;}
        if(!cloudRoom){
            micOn=!micOn;
            prefs.edit().putBoolean("mic_"+roomId,micOn).apply();
            seatMics.put(mySeat,micOn);rebuildSeats();refreshMicControl();
            return;
        }
        if(micTogglePending968){toast("Waiting for Mic sync…");return;}
        final String expectedRoom968=roomId;
        final String expectedUid968=user==null?null:user.getUid();
        final int expectedSeat968=mySeat;
        final int expectedGeneration968=presenceGeneration967;
        final boolean desiredMic968=!micOn;
        if(db==null||!memberSeen||!isActivePresence967(expectedRoom968,expectedUid968,expectedGeneration968)
           ||!KingPartyMic968.canToggle(expectedRoom968,expectedUid968,
               seatUids.get(expectedSeat968),expectedSeat968,mySeat,
               cloudRoom,!isFinishing()&&!isDestroyed(),muteAll,isModerator())){
            toast("Your Mic seat is not ready. Wait for the room to sync.");return;
        }
        micTogglePending968=true;
        // Privacy: on an OFF request mute native audio immediately, even if offline.
        if(!desiredMic968)setInlineAudioMuted940(true);
        final DocumentReference seatRef968=db.collection("live_rooms").document(expectedRoom968)
            .collection("seats").document(String.valueOf(expectedSeat968));
        db.runTransaction(transaction968->{
            DocumentSnapshot occupied968=transaction968.get(seatRef968);
            if(!occupied968.exists()||!expectedUid968.equals(occupied968.getString("uid")))
                throw new IllegalStateException("Mic seat is no longer assigned to this account");
            transaction968.update(seatRef968,"micOn",desiredMic968);
            return null;
        }).addOnSuccessListener(v->{
            if(!isActivePresence967(expectedRoom968,expectedUid968,expectedGeneration968))return;
            micTogglePending968=false;
            if(mySeat!=expectedSeat968)return;
            micOn=desiredMic968;
            seatMics.put(expectedSeat968,desiredMic968);
            if(desiredMic968){
                roomVoiceOptedIn957=true;
                if(partyShell940!=null&&!inRoomVoiceJoined940)attachInRoomVoice940(partyShell940);
            }
            setInlineAudioMuted940(!desiredMic968);
            setVoicePresence900(desiredMic968);
            refreshMicControl();
            toast(desiredMic968?"Mic ON • live inside Party":"Mic OFF");
        }).addOnFailureListener(e->{
            if(!isActivePresence967(expectedRoom968,expectedUid968,expectedGeneration968))return;
            micTogglePending968=false;
            if(!desiredMic968)setInlineAudioMuted940(true);
            refreshMicControl();
            toast("Mic sync failed: "+msg(e));
        });
    }
''' + s[end:]
print('PASS', 'Mic toggle verifies real seat ownership before update and native Jitsi startup')

# Rebuild views only from the current room. Stale Firestore callbacks from a
# previous room could restore wrong micOn / mySeat and mute or open remote audio.
change(
 'seatsListener=room.collection("seats").addSnapshotListener((snap,e)->{if(e!=null||snap==null)return;',
 'seatsListener=room.collection("seats").addSnapshotListener((snap,e)->{if(e!=null||snap==null||isFinishing()||isDestroyed()||user==null||!cloudRoom||roomId==null||!room.getId().equals(roomId))return;',
 'ignore stale room seat snapshots after navigation or account logout')

change(
 'seatMics.put(no,!Boolean.FALSE.equals(d.getBoolean("micOn")));',
 'seatMics.put(no,Boolean.TRUE.equals(d.getBoolean("micOn")));',
 'never render missing Mic field as ON')

# Legacy implementation subscribed twice to the same collection; merging them
# reduces background memory and keeps old and new locked seat UIs consistent.
old='''        seatLocksListener=room.collection("seat_locks").addSnapshotListener((snap,e)->{
            if(e!=null||snap==null)return;lockedSeats.clear();
            for(DocumentSnapshot d:snap.getDocuments()){if(Boolean.TRUE.equals(d.getBoolean("locked"))){try{lockedSeats.add(Integer.parseInt(d.getId()));}catch(Exception ignored){}}}
            rebuildSeats();
        });'''
new='''        seatLocksListener=room.collection("seat_locks").addSnapshotListener((snap,e)->{
            if(e!=null||snap==null||isFinishing()||isDestroyed()||!cloudRoom||roomId==null||!room.getId().equals(roomId))return;
            lockedSeats.clear();lockedSeats940.clear();
            for(DocumentSnapshot d:snap.getDocuments()){
                if(Boolean.TRUE.equals(d.getBoolean("locked"))){
                    try{int seat968=Integer.parseInt(d.getId());lockedSeats.add(seat968);lockedSeats940.add(seat968);}
                    catch(Exception ignored){}
                }
            }
            rebuildSeats();
        });'''
change(old,new,'combine legacy and modern seat-lock states in one guarded listener')
old='''        seatLocksListener940=room.collection("seat_locks").addSnapshotListener((snap,e)->{if(e!=null||snap==null)return;lockedSeats940.clear();for(DocumentSnapshot d:snap.getDocuments())if(Boolean.TRUE.equals(d.getBoolean("locked"))){try{lockedSeats940.add(Integer.parseInt(d.getId()));}catch(Exception ignored){}}rebuildSeats();});'''
change(old,'        // Reuses seatLocksListener for both views; no duplicate Firestore subscription.','remove duplicate seat-lock Firestore listener')

# A user navigating away should be able to tap Mic again on the next room,
# even if an old transaction's delayed callback never arrives.
change(
 '++presenceGeneration967; // Existing callback requests belong to the old room.',
 '++presenceGeneration967; micTogglePending968=false; // Old room Mic transactions must not block a new room.',
 'cancel pending Mic status on room leave')

party.write_text(s)
gradle=root/'app/build.gradle';g=gradle.read_text()
old="versionCode 158; versionName '9.6.7-live-room-presence'"
if g.count(old)!=1:raise SystemExit('Expected successful v9.6.7 source baseline')
gradle.write_text(g.replace(old,"versionCode 159; versionName '9.6.8-party-mic-seat-sync'",1))
print('PASS: v9.6.8 Mic/Seat sync patch applied')
