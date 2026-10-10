#!/usr/bin/env python3
"""KING Plus v9.8.2 – stop old Party callbacks touching a newer room or re-render.
Based strictly on successful compiled v9.8.1 APK source. Retains games, frames,
VIP, emoji, Mic/Seat and Firestore Room reconnect features.

Fix: Firestore.remove() does NOT retroactively undo already queued callbacks.
Previously old room listeners for bans, member deletion, role, chat and gift
events could mutate or remove users from a newer room. Guard each callback with
UID + room ID + visit generation + listener binding generation.
Also add a safe "Copy Connection Report" on the existing Join failure screen.
"""
from pathlib import Path
import shutil,sys
root=Path(sys.argv[1]);pkg=root/'app/src/main/java/com/kingplus/social'
sfile=pkg/'PartyActivity.java';s=sfile.read_text()
shutil.copy2(Path(__file__).with_name('KingRoomCallback982.java'),pkg/'KingRoomCallback982.java')

def one(a,b,why):
    global s
    count=s.count(a)
    if count!=1:raise SystemExit(f'{why}: expected exactly one marker got {count}: {a[:150]!r}')
    s=s.replace(a,b,1)
    print('PASS',why)

one(
'    private void clearListeners() {',
'''    private int roomListenerGeneration982;
    private boolean activeRoomCallback982(String room982,String uid982,
                                          int visit982,int binding982){
        if(roomListenerGeneration982!=binding982)return false;
        com.google.firebase.auth.FirebaseUser signed982=null;
        try{signed982=com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();}
        catch(Exception ignored){}
        return KingRoomCallback982.current(room982,uid982,visit982,roomId,
            signed982==null?null:signed982.getUid(),presenceGeneration967,
            cloudRoom,!isFinishing()&&!isDestroyed());
    }

    private void clearListeners() {
        ++roomListenerGeneration982; // Cancel queued callbacks from old subscriptions.
        eventSnapshotReady=false;
        processedEventIds.clear();''',
'revoke old Firestore listener epoch and processed Party events on render/switch')

one(
'''        if(db==null||user==null)return;DocumentReference room=db.collection("live_rooms").document(roomId);
        roomListener=room.addSnapshotListener((doc,e)->{if(e!=null||doc==null||!doc.exists())return;''',
'''        if(db==null||user==null||roomId==null||roomId.trim().isEmpty())return;
        final DocumentReference room=db.collection("live_rooms").document(roomId);
        final String expectedRoom982=room.getId(),expectedUid982=user.getUid();
        final int visit982=presenceGeneration967,binding982=roomListenerGeneration982;
        roomListener=room.addSnapshotListener((doc,e)->{
            if(!activeRoomCallback982(expectedRoom982,expectedUid982,visit982,binding982))return;
            if(e!=null||doc==null||!doc.exists())return;''',
'guard Room document listener, capture immutable UID, Room and listener binding')

one(
'''        roleListener=room.collection("roles").document(user.getUid()).addSnapshotListener((doc,e)->{
            boolean old=coHost;''',
'''        roleListener=room.collection("roles").document(expectedUid982).addSnapshotListener((doc,e)->{
            if(!activeRoomCallback982(expectedRoom982,expectedUid982,visit982,binding982))return;
            boolean old=coHost;''',
'stale old Host/cohost callbacks cannot re-render a new room')

one(
'''        selfMemberListener=room.collection("members").document(user.getUid()).addSnapshotListener((doc,e)->{
            if(e!=null||doc==null)return;''',
'''        selfMemberListener=room.collection("members").document(expectedUid982).addSnapshotListener((doc,e)->{
            if(!activeRoomCallback982(expectedRoom982,expectedUid982,visit982,binding982))return;
            if(e!=null||doc==null)return;''',
'late member removal callback cannot force leave of next Party')

one(
'''        roomBanListener=room.collection("room_bans").document(user.getUid()).addSnapshotListener((doc,e)->{
            if(e==null&&doc!=null&&doc.exists()''',
'''        roomBanListener=room.collection("room_bans").document(expectedUid982).addSnapshotListener((doc,e)->{
            if(!activeRoomCallback982(expectedRoom982,expectedUid982,visit982,binding982))return;
            if(e==null&&doc!=null&&doc.exists()''',
'old Room bans do not unexpectedly kick user from a new Room')

one(
'''        seatLocksListener=room.collection("seat_locks").addSnapshotListener((snap,e)->{
            if(e!=null||snap==null||isFinishing()||isDestroyed()||!cloudRoom||roomId==null||!room.getId().equals(roomId))return;''',
'''        seatLocksListener=room.collection("seat_locks").addSnapshotListener((snap,e)->{
            if(!activeRoomCallback982(expectedRoom982,expectedUid982,visit982,binding982)||e!=null||snap==null)return;''',
'stop stale Seat Lock updates even when re-entering same Room')

one(
'''        seatsListener=room.collection("seats").addSnapshotListener((snap,e)->{if(e!=null||snap==null||isFinishing()||isDestroyed()||user==null||!cloudRoom||roomId==null||!room.getId().equals(roomId))return;''',
'''        seatsListener=room.collection("seats").addSnapshotListener((snap,e)->{if(!activeRoomCallback982(expectedRoom982,expectedUid982,visit982,binding982)||e!=null||snap==null)return;''',
'old seat/mic snapshots cannot unmute or assign wrong seat in current Party')

one(
'''        final String memberRoom967=room.getId();
        membersListener=room.collection("members").addSnapshotListener((snap,e)->{if(snap==null||isFinishing()||isDestroyed()||!cloudRoom||roomId==null||!roomId.equals(memberRoom967))return;''',
'''        membersListener=room.collection("members").addSnapshotListener((snap,e)->{if(!activeRoomCallback982(expectedRoom982,expectedUid982,visit982,binding982)||snap==null||e!=null)return;''',
'Member list/UI and avatars are never filled from prior Party visit')

one(
'''            seatRequestsListener=room.collection("seat_requests").addSnapshotListener((snap,e)->{
                pendingSeatRequests=(e==null&&snap!=null)?snap.size():0;''',
'''            seatRequestsListener=room.collection("seat_requests").addSnapshotListener((snap,e)->{
                if(!activeRoomCallback982(expectedRoom982,expectedUid982,visit982,binding982))return;
                pendingSeatRequests=(e==null&&snap!=null)?snap.size():0;''',
'Host requests list is specific to bound Party visit')

one(
'''        messagesListener=room.collection("messages").orderBy("createdAt",Query.Direction.ASCENDING).limitToLast(120).addSnapshotListener((snap,e)->{if(e!=null||snap==null||chatBox==null)return;''',
'''        messagesListener=room.collection("messages").orderBy("createdAt",Query.Direction.ASCENDING).limitToLast(120).addSnapshotListener((snap,e)->{if(!activeRoomCallback982(expectedRoom982,expectedUid982,visit982,binding982)||e!=null||snap==null||chatBox==null)return;''',
'older Room Chat messages cannot replace current chat')

one(
'''        eventsListener=room.collection("events").orderBy("createdAt",Query.Direction.ASCENDING).limitToLast(80).addSnapshotListener((snap,e)->{
            if(e!=null||snap==null||feedBox==null)return;''',
'''        eventsListener=room.collection("events").orderBy("createdAt",Query.Direction.ASCENDING).limitToLast(80).addSnapshotListener((snap,e)->{
            if(!activeRoomCallback982(expectedRoom982,expectedUid982,visit982,binding982)||e!=null||snap==null||feedBox==null)return;''',
'late Room event/Gift/reaction callbacks cannot display on another Party')

one(
'''        TextView detail977=tv(reasonUi979+"\\n\\n"+hint979+''',
'''        String version982="unknown";
        try{version982=getPackageManager().getPackageInfo(getPackageName(),0).versionName;}
        catch(Exception ignored){}
        final String support982=KingRoomCallback982.supportReport(
            version982,roomId==null?"none":shortId(roomId),shownUid979,
            reasonUi979,KingNetwork.online(this),user!=null);
        TextView detail977=tv(reasonUi979+"\\n\\n"+hint979+''',
'capture safe report with installed version, room code, UID prefix and network state')

one(
'''        retry977.setOnClickListener(v->{
            if(isFinishing()||isDestroyed())return;''',
'''        TextView copy982=tv("📋  Copy connection report",15,Color.WHITE,true);
        copy982.setGravity(Gravity.CENTER);
        copy982.setBackgroundColor(0xff45587a);
        LinearLayout.LayoutParams copyParams982=
            new LinearLayout.LayoutParams(-1,dp(52));
        copyParams982.setMargins(0,dp(10),0,0);
        root977.addView(copy982,copyParams982);
        copy982.setOnClickListener(v->{
            try{
                android.content.ClipboardManager cb982=
                    (android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                if(cb982==null){toast("Clipboard unavailable");return;}
                cb982.setPrimaryClip(android.content.ClipData.newPlainText(
                    "KING Plus Party connection report",support982));
                toast("Copied KING Plus connection report. Share it for troubleshooting.");
            }catch(Exception error){toast("Unable to copy Party report");}
        });
        retry977.setOnClickListener(v->{
            if(isFinishing()||isDestroyed())return;''',
'copyable error report on join failure, no login tokens exposed')

sfile.write_text(s)

gradle=root/'app/build.gradle';g=gradle.read_text()
old="versionCode 172; versionName '9.8.1-games-vip-frames-gifts-emoji'"
if g.count(old)!=1:raise SystemExit('Expected only verified v9.8.1 source baseline')
gradle.write_text(g.replace(old,"versionCode 173; versionName '9.8.2-party-room-session-isolation'",1))
print('PASS KING Plus v9.8.2 source patched, prior games/frames/VIP/emoji maintained')
