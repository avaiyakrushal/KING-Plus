from pathlib import Path
import sys
root=Path(sys.argv[1])
party=root/'app/src/main/java/com/kingplus/social/PartyActivity.java'
main=root/'app/src/main/java/com/kingplus/social/MainActivity.java'
games=root/'app/src/main/java/com/kingplus/social/RoomGameActivity.java'
s=party.read_text()

field_marker='''    private final Set<String> seenLiveEmojiEventsV530 = new HashSet<>();
'''
field_add='''    private final Set<String> seenLiveEmojiEventsV530 = new HashSet<>();
    private final android.os.Handler onlineHandler900 = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable onlineTick900 = new Runnable(){@Override public void run(){heartbeat900();if(cloudRoom&&roomId!=null&&user!=null)onlineHandler900.postDelayed(this,20000);}};
    private TextView onlineStateLabel900;
    private boolean voiceLaunched900;
    private long voiceLaunchAt900;
'''
if field_marker not in s: raise SystemExit('field marker missing')
s=s.replace(field_marker,field_add,1)

old='''    @Override protected void onResume(){
        super.onResume();
        if(seatsBox!=null)rebuildSeats();
        refreshOwnMemberPhoto868();
    }
'''
new='''    @Override protected void onResume(){
        super.onResume();
        if(seatsBox!=null)rebuildSeats();
        refreshOwnMemberPhoto868();
        if(voiceLaunched900 && System.currentTimeMillis()-voiceLaunchAt900>500){
            voiceLaunched900=false;
            setVoicePresence900(false);
            if(mySeat>0&&cloudRoom&&db!=null&&user!=null){
                micOn=false;
                db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(mySeat)).update("micOn",false).addOnFailureListener(e->{});
                refreshMicControl();
            }
        }
        if(cloudRoom&&roomId!=null&&user!=null){ensureRoomMembership900();startHeartbeat900();}
    }
'''
if old not in s: raise SystemExit('onResume marker missing')
s=s.replace(old,new,1)

old='''    private void clearListeners() {
        for(View reaction:activeReactions560.values()){reaction.animate().cancel();if(reaction.getParent() instanceof android.view.ViewGroup)((android.view.ViewGroup)reaction.getParent()).removeView(reaction);}activeReactions560.clear();
'''
new='''    private void clearListeners() {
        stopHeartbeat900();
        for(View reaction:activeReactions560.values()){reaction.animate().cancel();if(reaction.getParent() instanceof android.view.ViewGroup)((android.view.ViewGroup)reaction.getParent()).removeView(reaction);}activeReactions560.clear();
'''
if old not in s: raise SystemExit('clearListeners marker missing')
s=s.replace(old,new,1)

old='''    private Map<String,Object> memberPayload891(){
        LevelSystem.Snapshot p720=LevelSystem.read(this);String frame730=KingCosmetics.frame(this),effect730=KingCosmetics.effect(this);
        Map<String,Object>d=new HashMap<>();d.put("uid",user.getUid());d.put("name",safeName());d.put("level",p720.level);d.put("vipLevel",p720.vipLevel);d.put("equippedFrame",frame730);d.put("entranceEffect",effect730);d.put("deviceId",deviceId891());
        String cloudPhoto868=cloudProfilePhoto868();if(!cloudPhoto868.isEmpty())d.put("photoUrl",cloudPhoto868);else if(user.getPhotoUrl()!=null)d.put("photoUrl",user.getPhotoUrl().toString());
        d.put("joinedAt",FieldValue.serverTimestamp());d.put("lastSeenAt",FieldValue.serverTimestamp());return d;
    }
'''
new='''    private Map<String,Object> memberPayload891(){
        LevelSystem.Snapshot p720=LevelSystem.read(this);String frame730=KingCosmetics.frame(this),effect730=KingCosmetics.effect(this);
        Map<String,Object>d=new HashMap<>();d.put("uid",user.getUid());d.put("name",safeName());d.put("level",p720.level);d.put("vipLevel",p720.vipLevel);d.put("equippedFrame",frame730);d.put("entranceEffect",effect730);d.put("deviceId",deviceId891());d.put("online",true);d.put("appVersion","9.0.0");d.put("voiceJoined",voiceLaunched900);
        String cloudPhoto868=cloudProfilePhoto868();if(!cloudPhoto868.isEmpty())d.put("photoUrl",cloudPhoto868);else if(user.getPhotoUrl()!=null)d.put("photoUrl",user.getPhotoUrl().toString());
        d.put("lastSeenAt",FieldValue.serverTimestamp());return d;
    }
'''
if old not in s: raise SystemExit('memberPayload marker missing')
s=s.replace(old,new,1)

old='''        DocumentReference ref=db.collection("live_rooms").document(roomId).collection("members").document(user.getUid());
        Runnable write=()->ref.set(memberPayload891()).addOnSuccessListener(v->{memberSeen=true;renderParty();addEvent("join",safeName()+" joined the room");if(page!=null)page.postDelayed(()->showEntranceEffect(safeName()),350);}).addOnFailureListener(e->roomJoinError891("Could not join this Party Room",e));
'''
new='''        DocumentReference ref=db.collection("live_rooms").document(roomId).collection("members").document(user.getUid());
        Runnable write=()->{Map<String,Object>m=memberPayload891();m.put("joinedAt",FieldValue.serverTimestamp());ref.set(m,SetOptions.merge()).addOnSuccessListener(v->{memberSeen=true;renderParty();startHeartbeat900();addEvent("join",safeName()+" joined the room");if(page!=null)page.postDelayed(()->showEntranceEffect(safeName()),350);}).addOnFailureListener(e->roomJoinError891("Could not join this Party Room",e));};
'''
if old not in s: raise SystemExit('joinMember write marker missing')
s=s.replace(old,new,1)

insert_before='''    private void roomJoinError891(String title,Exception e){
'''
helpers='''    private void startHeartbeat900(){stopHeartbeat900();if(cloudRoom&&roomId!=null&&user!=null){heartbeat900();onlineHandler900.postDelayed(onlineTick900,20000);}}
    private void stopHeartbeat900(){onlineHandler900.removeCallbacks(onlineTick900);}
    private void heartbeat900(){
        if(!cloudRoom||db==null||user==null||roomId==null)return;
        Map<String,Object>m=memberPayload891();
        db.collection("live_rooms").document(roomId).collection("members").document(user.getUid()).set(m,SetOptions.merge())
            .addOnSuccessListener(v->setOnlineState900(true,"Online • synced"))
            .addOnFailureListener(e->setOnlineState900(false,"Sync blocked"));
    }
    private void setOnlineState900(boolean ok,String text){if(onlineStateLabel900!=null){onlineStateLabel900.setText((ok?"● ":"● ")+text);onlineStateLabel900.setTextColor(ok?0xff4ff0a3:0xffff9a9a);}}
    private void ensureRoomMembership900(){
        if(!cloudRoom||db==null||user==null||roomId==null)return;
        DocumentReference room=db.collection("live_rooms").document(roomId),member=room.collection("members").document(user.getUid());
        room.get().addOnSuccessListener(r->{if(r==null||!r.exists()||Boolean.TRUE.equals(r.getBoolean("closed"))){setOnlineState900(false,"Room unavailable");return;}member.get().addOnSuccessListener(m->{if(m==null||!m.exists()){Map<String,Object>x=memberPayload891();x.put("joinedAt",FieldValue.serverTimestamp());member.set(x,SetOptions.merge()).addOnSuccessListener(v->{memberSeen=true;setOnlineState900(true,"Online • rejoined");}).addOnFailureListener(e->setOnlineState900(false,"Join blocked"));}else heartbeat900();});}).addOnFailureListener(e->setOnlineState900(false,"Backend blocked"));
    }
    private void setVoicePresence900(boolean joined){
        if(!cloudRoom||db==null||user==null||roomId==null)return;
        Map<String,Object>m=new HashMap<>();m.put("voiceJoined",joined);m.put("lastSeenAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).collection("members").document(user.getUid()).set(m,SetOptions.merge()).addOnFailureListener(e->{});
    }
    private void onlineDiagnostics900(){
        if(user==null||db==null||roomId==null){toast("Open a signed-in live Party Room first");return;}
        final String project;try{project=String.valueOf(com.google.firebase.FirebaseApp.getInstance().getOptions().getProjectId());}catch(Exception e){toast("Firebase not ready");return;}
        DocumentReference room=db.collection("live_rooms").document(roomId),member=room.collection("members").document(user.getUid());
        room.get().addOnSuccessListener(rd->member.get().addOnSuccessListener(md->room.collection("messages").limit(1).get().addOnSuccessListener(q->{
            String text="Firebase project: "+project+"\nRoom ID: "+roomId+"\nRoom Code: "+shortId()+"\nSigned in: YES\nRoom readable: "+(rd!=null&&rd.exists()?"YES":"NO")+"\nMembership: "+(md!=null&&md.exists()?"YES":"NO")+"\nMessages readable: YES\nMembers seen: "+liveMemberCount+"\nSeat: "+(mySeat>0?mySeat:"none")+"\n\nIf the second phone shows permission denied, Firestore Security Rules are still not deployed.";
            new AlertDialog.Builder(this).setTitle("🧪 Online diagnostics").setMessage(text).setPositiveButton("Refresh membership",(d,w)->ensureRoomMembership900()).setNeutralButton("Share Room",(d,w)->shareRoom()).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->roomJoinError891("Message sync test failed",e))).addOnFailureListener(e->roomJoinError891("Membership test failed",e))).addOnFailureListener(e->roomJoinError891("Room backend test failed",e));
    }

'''
if insert_before not in s: raise SystemExit('roomJoinError insert marker missing')
s=s.replace(insert_before,helpers+insert_before,1)

old='''        LinearLayout.LayoutParams heartLp=new LinearLayout.LayoutParams(-2,dp(24)); heartLp.setMargins(dp(8),0,0,0); badges.addView(heartLevelLabel,heartLp);
        View badgeSpacer=new View(this); badges.addView(badgeSpacer,new LinearLayout.LayoutParams(0,1,1));
'''
new='''        LinearLayout.LayoutParams heartLp=new LinearLayout.LayoutParams(-2,dp(24)); heartLp.setMargins(dp(8),0,0,0); badges.addView(heartLevelLabel,heartLp);
        onlineStateLabel900=pill("● Online",0x33000000,this::onlineDiagnostics900);onlineStateLabel900.setTextSize(9);onlineStateLabel900.setTextColor(0xff4ff0a3);LinearLayout.LayoutParams onlineLp=new LinearLayout.LayoutParams(-2,dp(24));onlineLp.setMargins(dp(8),0,0,0);badges.addView(onlineStateLabel900,onlineLp);
        View badgeSpacer=new View(this); badges.addView(badgeSpacer,new LinearLayout.LayoutParams(0,1,1));
'''
if old not in s: raise SystemExit('badges marker missing')
s=s.replace(old,new,1)

old='''    private void openVoice(){NativeMeetBridge.launch(this,roomId,roomName,safeName(),false);}
'''
new='''    private void openVoice(){
        if(!cloudRoom||user==null||db==null||roomId==null){toast("Join a live Firebase Party room first");return;}
        if(mySeat<1){toast("Take a mic seat first");return;}
        if(muteAll&&!isModerator()){toast("Host muted all seats");return;}
        micOn=true;refreshMicControl();setVoicePresence900(true);
        db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(mySeat)).update("micOn",true).addOnFailureListener(e->{});
        voiceLaunched900=true;voiceLaunchAt900=System.currentTimeMillis();
        NativeMeetBridge.launch(this,roomId,roomName,safeName(),false);
    }
'''
if old not in s: raise SystemExit('openVoice marker missing')
s=s.replace(old,new,1)

old='''    private void toggleMic() {
        if(mySeat<1){toast("Take a mic seat first");return;} if(muteAll&&!isModerator()){toast("Host muted all seats");return;} micOn=!micOn;
        if(cloudRoom&&user!=null&&db!=null) db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(mySeat)).update("micOn",micOn);
        else { prefs.edit().putBoolean("mic_"+roomId,micOn).apply();seatMics.put(mySeat,micOn);rebuildSeats(); }
        refreshMicControl();
    }
'''
new='''    private void toggleMic() {
        if(cloudRoom){if(micOn){micOn=false;setVoicePresence900(false);if(user!=null&&db!=null&&mySeat>0)db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(mySeat)).update("micOn",false).addOnFailureListener(e->{});refreshMicControl();toast("Mic state off • leave the voice conference if it is still open");}else openVoice();return;}
        if(mySeat<1){toast("Take a mic seat first");return;} if(muteAll&&!isModerator()){toast("Host muted all seats");return;} micOn=!micOn;
        prefs.edit().putBoolean("mic_"+roomId,micOn).apply();seatMics.put(mySeat,micOn);rebuildSeats();refreshMicControl();
    }
'''
if old not in s: raise SystemExit('toggleMic marker missing')
s=s.replace(old,new,1)

old='''        micLabel.setContentDescription(micOn?"Microphone on. Tap to mute":"Microphone off. Tap to unmute");
'''
new='''        micLabel.setContentDescription(micOn?"Live voice active. Tap to mark mic off":"Tap to join the shared live voice room");
'''
if old not in s: raise SystemExit('mic description marker missing')
s=s.replace(old,new,1)

old='''        if(cloudRoom&&user!=null&&targetUid!=null&&!targetUid.isEmpty()&&!targetUid.equals(user.getUid())){
            CloudBackend.sendGift(targetUid,gift+" x"+quantity,totalCost,(ok,message)->runOnUiThread(()->{
                toast(message);
                if(ok)addGiftEvent(targetUid,targetName,gift,icon,unitCost,quantity,totalCost);
            }));return;
        }
'''
new='''        if(cloudRoom&&user!=null&&targetUid!=null&&!targetUid.isEmpty()&&!targetUid.equals(user.getUid())){
            CloudBackend.sendGift(targetUid,gift+" x"+quantity,totalCost,(ok,message)->runOnUiThread(()->{
                if(ok){toast(message);addGiftEvent(targetUid,targetName,gift,icon,unitCost,quantity,totalCost);return;}
                if(localCoins>=totalCost){localCoins-=totalCost;prefs.edit().putInt("coins",localCoins).apply();addGiftEvent(targetUid,targetName,gift,icon,unitCost,quantity,totalCost);toast("TEST room gift synced • backend wallet unavailable");}
                else toast(message);
            }));return;
        }
'''
if old not in s: raise SystemExit('gift cloud fallback marker missing')
s=s.replace(old,new,1)

old='''        items.add("🔗 Share room"); items.add("🔔 Invite by Firebase UID"); items.add("⚑ Report host"); items.add("🚫 Block host");
'''
new='''        items.add("🧪 Online diagnostics"); items.add("🔗 Share room"); items.add("🔔 Invite by Firebase UID"); items.add("⚑ Report host"); items.add("🚫 Block host");
'''
if old not in s: raise SystemExit('room menu diagnostics list marker missing')
s=s.replace(old,new,1)

old='''        else if(x.contains("VIP & Rank"))openParity870("vip");
        else if(x.contains("Share"))shareRoom();
'''
new='''        else if(x.contains("VIP & Rank"))openParity870("vip");
        else if(x.contains("Online diagnostics"))onlineDiagnostics900();
        else if(x.contains("Share"))shareRoom();
'''
if old not in s: raise SystemExit('room menu diagnostics handler marker missing')
s=s.replace(old,new,1)

old='''    private void setRoomFlag(String key,boolean value){if(!isModerator()){toast("Host/co-host only");return;}if(cloudRoom)setRoomValue(key,value);else{if("locked".equals(key))roomLocked=value;else muteAll=value;prefs.edit().putBoolean(("locked".equals(key)?"locked_":"mute_")+roomId,value).apply();toast("Room updated");}}
'''
new='''    private void setRoomFlag(String key,boolean value){if(!isModerator()){toast("Host/co-host only");return;}if(cloudRoom){setRoomValue(key,value);if("muteAll".equals(key)&&value&&db!=null){db.collection("live_rooms").document(roomId).collection("seats").get().addOnSuccessListener(q->{WriteBatch b=db.batch();for(DocumentSnapshot d:q.getDocuments())b.update(d.getReference(),"micOn",false);b.commit().addOnFailureListener(e->{});});}}else{if("locked".equals(key))roomLocked=value;else muteAll=value;prefs.edit().putBoolean(("locked".equals(key)?"locked_":"mute_")+roomId,value).apply();toast("Room updated");}}
'''
if old not in s: raise SystemExit('setRoomFlag marker missing')
s=s.replace(old,new,1)

old='''    private void unregisterMember(){if(user!=null&&db!=null&&cloudRoom&&roomId!=null)db.collection("live_rooms").document(roomId).collection("members").document(user.getUid()).delete();}
'''
new='''    private void unregisterMember(){stopHeartbeat900();if(user!=null&&db!=null&&cloudRoom&&roomId!=null)db.collection("live_rooms").document(roomId).collection("members").document(user.getUid()).delete();}
'''
if old not in s: raise SystemExit('unregister marker missing')
s=s.replace(old,new,1)

party.write_text(s)

# Profile name sync into FirebaseAuth so Party room safeName() updates across devices.
ms=main.read_text()
old='''        Runnable doSave=()->{String n=name.getText().toString().trim();if(n.isEmpty()){name.setError("Name required");return;}displayName=n;getPreferences(0).edit().putString("name",n).putString("bio",bio.getText().toString().trim()).putString("hometown",hometown.getText().toString().trim()).putString("birthday",birthday.getText().toString().trim()).putString("tags",tags.getText().toString().trim()).apply();if(CloudSync.isSignedIn())syncPublicProfile();Toast.makeText(this,"Profile updated",Toast.LENGTH_SHORT).show();profile();};save.setOnClickListener(v->doSave.run());saveTop.setOnClickListener(v->doSave.run());
'''
new='''        Runnable doSave=()->{String n=name.getText().toString().trim();if(n.isEmpty()){name.setError("Name required");return;}displayName=n;getPreferences(0).edit().putString("name",n).putString("bio",bio.getText().toString().trim()).putString("hometown",hometown.getText().toString().trim()).putString("birthday",birthday.getText().toString().trim()).putString("tags",tags.getText().toString().trim()).apply();if(firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null){firebaseAuth.getCurrentUser().updateProfile(new com.google.firebase.auth.UserProfileChangeRequest.Builder().setDisplayName(n).build()).addOnFailureListener(e->{});}if(CloudSync.isSignedIn())syncPublicProfile();Toast.makeText(this,"Profile updated",Toast.LENGTH_SHORT).show();profile();};save.setOnClickListener(v->doSave.run());saveTop.setOnClickListener(v->doSave.run());
'''
if old not in ms: raise SystemExit('profile save marker missing')
ms=ms.replace(old,new,1)
main.write_text(ms)

# Multiplayer game self-heal membership before loading ready/game state.
gs=games.read_text()
old='''        build(); if(db==null||me==null||roomId.isEmpty()){stateText.setText("Sign in and open this from a live Party room.");return;} loadRoleAndListen(); }
'''
new='''        build(); if(db==null||me==null||roomId.isEmpty()){stateText.setText("Sign in and open this from a live Party room.");return;} ensureMembership900(); }
'''
if old not in gs: raise SystemExit('game onCreate marker missing')
gs=gs.replace(old,new,1)
insert='''    private DocumentReference room(){return db.collection("live_rooms").document(roomId);} private DocumentReference state(){return room().collection("game_state").document("current");}
'''
replacement='''    private DocumentReference room(){return db.collection("live_rooms").document(roomId);} private DocumentReference state(){return room().collection("game_state").document("current");}
    private void ensureMembership900(){
        Map<String,Object>m=new HashMap<>();m.put("uid",me.getUid());m.put("name",displayName.length()>80?displayName.substring(0,80):displayName);m.put("online",true);m.put("appVersion","9.0.0");m.put("lastSeenAt",FieldValue.serverTimestamp());
        room().collection("members").document(me.getUid()).set(m,com.google.firebase.firestore.SetOptions.merge()).addOnSuccessListener(v->{roleText.setText("● Multiplayer backend connected");loadRoleAndListen();}).addOnFailureListener(e->{roleText.setText("Multiplayer join blocked: "+e.getMessage());toast("Reopen Party room or deploy Firestore rules");});
    }
'''
if insert not in gs: raise SystemExit('game room marker missing')
gs=gs.replace(insert,replacement,1)
games.write_text(gs)
print('v9.0.0 online multiplayer completion patch applied')
