from pathlib import Path
import sys

root=Path(sys.argv[1])
p=root/'app/src/main/java/com/kingplus/social/PartyActivity.java'
s=p.read_text()

# v9.2.1 state
field='''    private long lastCleanup910;
'''
if field not in s:
    raise SystemExit('v9.2.1 field marker missing')
s=s.replace(field,field+'''    private boolean roomCreateInFlight921;
''',1)

# Default a usable room name instead of silently blocking Start Room on blank input.
old='''        final EditText name=new EditText(this);name.setHint("Room name");name.setHintTextColor(0xff9cb8ae);name.setTextColor(Color.WHITE);name.setTextSize(18);name.setSingleLine(true);name.setBackgroundColor(Color.TRANSPARENT);name.setPadding(dp(4),dp(10),dp(4),dp(10));body.addView(name,new LinearLayout.LayoutParams(-1,dp(58)));View line=new View(this);line.setBackgroundColor(0x446bd8b4);body.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));
'''
new='''        final EditText name=new EditText(this);name.setHint("Room name");name.setHintTextColor(0xff9cb8ae);name.setTextColor(Color.WHITE);name.setTextSize(18);name.setSingleLine(true);name.setBackgroundColor(Color.TRANSPARENT);name.setPadding(dp(4),dp(10),dp(4),dp(10));String defaultRoomName921=safeName().trim();if(defaultRoomName921.isEmpty())defaultRoomName921="KING";name.setText(defaultRoomName921+"'s Party");name.setSelection(name.getText().length());body.addView(name,new LinearLayout.LayoutParams(-1,dp(58)));View line=new View(this);line.setBackgroundColor(0x446bd8b4);body.addView(line,new LinearLayout.LayoutParams(-1,dp(1)));
'''
if old not in s:
    raise SystemExit('room name EditText marker missing')
s=s.replace(old,new,1)

old='''        TextView start=tv("🎉 Start Room",15,0xff171717,true);start.setGravity(Gravity.CENTER);start.setBackground(bg(0xffffee00,10));LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(54));sp.setMargins(0,dp(26),0,dp(8));body.addView(start,sp);start.setOnClickListener(v->{String n=name.getText().toString().trim();if(n.length()<2){name.setError("Enter a room name");return;}createCloudRoom(n,pendingCreateCategory,pendingCreateSeats,pendingCreatePrivate);});
'''
new='''        TextView start=tv(roomCreateInFlight921?"Creating Party…":"🎉 Start Room",15,0xff171717,true);start.setGravity(Gravity.CENTER);start.setBackground(bg(roomCreateInFlight921?0xffb9ad48:0xffffee00,10));start.setEnabled(!roomCreateInFlight921);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(54));sp.setMargins(0,dp(26),0,dp(8));body.addView(start,sp);start.setOnClickListener(v->{if(roomCreateInFlight921)return;String n=name.getText().toString().trim();if(n.length()<2){String base=safeName().trim();if(base.isEmpty())base="KING";n=base+"'s Party";name.setText(n);}createCloudRoom(n,pendingCreateCategory,pendingCreateSeats,pendingCreatePrivate);});
'''
if old not in s:
    raise SystemExit('Start Room marker missing')
s=s.replace(old,new,1)

old='''    private void createCloudRoom(String name,String category,int seats,boolean priv){
        if(user==null||db==null)return;
        DocumentReference ref=db.collection("live_rooms").document();
        String joinCode=shortId(ref.getId());
        Map<String,Object> r=new HashMap<>();r.put("name",name);r.put("ownerUid",user.getUid());r.put("ownerName",safeName());if(user.getPhotoUrl()!=null)r.put("ownerPhoto",user.getPhotoUrl().toString());r.put("category",category);r.put("theme","Classic");r.put("maxSeats",seats);r.put("isPrivate",priv);r.put("hasPassword",false);r.put("joinCode",joinCode);r.put("announcement",announcement);r.put("locked",false);r.put("muteAll",false);r.put("closed",false);r.put("createdAt",FieldValue.serverTimestamp());r.put("updatedAt",FieldValue.serverTimestamp());
        ref.set(r).addOnSuccessListener(v->{maxSeats=seats;if(pendingCreateCoverUri!=null&&storage!=null)uploadRoomCoverV600(ref,pendingCreateCoverUri);openCloudRoom(ref.getId(),name,user.getUid(),safeName(),priv,false);}).addOnFailureListener(e->roomJoinError891("Room create failed",e));
    }
'''
new='''    private void createCloudRoom(String name,String category,int seats,boolean priv){
        if(user==null||db==null){requireSignInForCreate();return;}
        if(roomCreateInFlight921)return;
        roomCreateInFlight921=true;toast("Creating Party…");renderCreateRoomPage();
        DocumentReference ref=db.collection("live_rooms").document();
        String joinCode=shortId(ref.getId());
        Map<String,Object> full=new HashMap<>();full.put("name",name);full.put("ownerUid",user.getUid());full.put("ownerName",safeName());if(user.getPhotoUrl()!=null)full.put("ownerPhoto",user.getPhotoUrl().toString());full.put("category",category);full.put("theme","Classic");full.put("maxSeats",seats);full.put("isPrivate",priv);full.put("hasPassword",false);full.put("joinCode",joinCode);full.put("announcement",announcement);full.put("locked",false);full.put("muteAll",false);full.put("closed",false);full.put("createdAt",FieldValue.serverTimestamp());full.put("updatedAt",FieldValue.serverTimestamp());
        ref.set(full).addOnSuccessListener(v->finishRoomCreate921(ref,name,seats,priv))
            .addOnFailureListener(first->retryRoomCreateCompact921(ref,name,category,seats,priv,joinCode,first));
    }
    private void retryRoomCreateCompact921(DocumentReference ref,String name,String category,int seats,boolean priv,String joinCode,Exception first){
        Map<String,Object> compact=new HashMap<>();compact.put("name",name);compact.put("ownerUid",user.getUid());compact.put("ownerName",safeName());compact.put("category",category);compact.put("maxSeats",seats);compact.put("isPrivate",priv);compact.put("hasPassword",false);compact.put("joinCode",joinCode);compact.put("locked",false);compact.put("muteAll",false);compact.put("closed",false);compact.put("createdAt",FieldValue.serverTimestamp());compact.put("updatedAt",FieldValue.serverTimestamp());
        ref.set(compact).addOnSuccessListener(v->finishRoomCreate921(ref,name,seats,priv)).addOnFailureListener(second->retryRoomCreateLegacy921(ref,name,category,seats,priv,first,second));
    }
    private void retryRoomCreateLegacy921(DocumentReference ref,String name,String category,int seats,boolean priv,Exception first,Exception second){
        Map<String,Object> legacy=new HashMap<>();legacy.put("name",name);legacy.put("ownerUid",user.getUid());legacy.put("ownerName",safeName());legacy.put("category",category);legacy.put("maxSeats",seats);legacy.put("isPrivate",priv);legacy.put("hasPassword",false);legacy.put("locked",false);legacy.put("muteAll",false);legacy.put("closed",false);legacy.put("createdAt",FieldValue.serverTimestamp());legacy.put("updatedAt",FieldValue.serverTimestamp());
        ref.set(legacy).addOnSuccessListener(v->finishRoomCreate921(ref,name,seats,priv)).addOnFailureListener(last->{roomCreateInFlight921=false;KingStability.nonFatal(this,"room-create-full",first);KingStability.nonFatal(this,"room-create-compact",second);KingStability.nonFatal(this,"room-create-legacy",last);roomJoinError891("Room create failed",last);renderCreateRoomPage();});
    }
    private void finishRoomCreate921(DocumentReference ref,String name,int seats,boolean priv){
        roomCreateInFlight921=false;maxSeats=seats;if(pendingCreateCoverUri!=null&&storage!=null)uploadRoomCoverV600(ref,pendingCreateCoverUri);openCloudRoom(ref.getId(),name,user.getUid(),safeName(),priv,false);
    }
'''
if old not in s:
    raise SystemExit('createCloudRoom marker missing')
s=s.replace(old,new,1)

# Membership compatibility fallback: room can be created but previously never opened when full member write was rejected.
old='''        Runnable write=()->{Map<String,Object>m=memberPayload891();m.put("joinedAt",FieldValue.serverTimestamp());ref.set(m,SetOptions.merge()).addOnSuccessListener(v->{memberSeen=true;renderParty();startHeartbeat900();addEvent("join",safeName()+" joined the room");if(page!=null)page.postDelayed(()->showEntranceEffect(safeName()),350);}).addOnFailureListener(e->roomJoinError891("Could not join this Party Room",e));};
'''
new='''        Runnable write=()->{Map<String,Object>m=memberPayload891();m.put("joinedAt",FieldValue.serverTimestamp());ref.set(m,SetOptions.merge()).addOnSuccessListener(v->finishMemberJoin921()).addOnFailureListener(e->retryMinimalMember921(ref,e));};
'''
if old not in s:
    raise SystemExit('member write marker missing')
s=s.replace(old,new,1)

insert='''    private void startHeartbeat900(){stopHeartbeat900();if(cloudRoom&&roomId!=null&&user!=null){heartbeat900();onlineHandler900.postDelayed(onlineTick900,20000);}}
'''
helpers='''    private Map<String,Object> minimalMember921(){
        Map<String,Object>m=new HashMap<>();m.put("uid",user.getUid());m.put("name",safeName());m.put("joinedAt",FieldValue.serverTimestamp());m.put("lastSeenAt",FieldValue.serverTimestamp());return m;
    }
    private void retryMinimalMember921(DocumentReference ref,Exception fullError){
        ref.set(minimalMember921(),SetOptions.merge()).addOnSuccessListener(v->{KingStability.nonFatal(this,"member-full-fallback",fullError);finishMemberJoin921();}).addOnFailureListener(last->{KingStability.nonFatal(this,"member-full",fullError);KingStability.nonFatal(this,"member-minimal",last);roomJoinError891("Could not join this Party Room",last);});
    }
    private void finishMemberJoin921(){
        memberSeen=true;renderParty();startHeartbeat900();addEvent("join",safeName()+" joined the room");if(page!=null)page.postDelayed(()->showEntranceEffect(safeName()),350);
    }
    private void startHeartbeat900(){stopHeartbeat900();if(cloudRoom&&roomId!=null&&user!=null){heartbeat900();onlineHandler900.postDelayed(onlineTick900,20000);}}
'''
if insert not in s:
    raise SystemExit('heartbeat insert marker missing')
s=s.replace(insert,helpers,1)

# If the full heartbeat schema is rejected by an older deployed rule, keep the member alive with the compact schema.
old='''        db.collection("live_rooms").document(roomId).collection("members").document(user.getUid()).set(m,SetOptions.merge())
            .addOnSuccessListener(v->{setOnlineState900(true,"Online • synced");cleanupStaleRoom910();})
            .addOnFailureListener(e->{setOnlineState900(false,"Sync blocked");KingStability.nonFatal(this,"party-heartbeat",e);});
'''
new='''        DocumentReference memberRef921=db.collection("live_rooms").document(roomId).collection("members").document(user.getUid());
        memberRef921.set(m,SetOptions.merge())
            .addOnSuccessListener(v->{setOnlineState900(true,"Online • synced");cleanupStaleRoom910();})
            .addOnFailureListener(e->{Map<String,Object>minimal=new HashMap<>();minimal.put("uid",user.getUid());minimal.put("name",safeName());minimal.put("lastSeenAt",FieldValue.serverTimestamp());memberRef921.set(minimal,SetOptions.merge()).addOnSuccessListener(v->{setOnlineState900(true,"Online • compatibility");cleanupStaleRoom910();}).addOnFailureListener(last->{setOnlineState900(false,"Sync blocked");KingStability.nonFatal(this,"party-heartbeat",e);KingStability.nonFatal(this,"party-heartbeat-minimal",last);});});
'''
if old not in s:
    raise SystemExit('heartbeat payload marker missing')
s=s.replace(old,new,1)

# Ensure self-heal uses the same compatibility fallback.
old='''        room.get().addOnSuccessListener(r->{if(r==null||!r.exists()||Boolean.TRUE.equals(r.getBoolean("closed"))){setOnlineState900(false,"Room unavailable");return;}member.get().addOnSuccessListener(m->{if(m==null||!m.exists()){Map<String,Object>x=memberPayload891();x.put("joinedAt",FieldValue.serverTimestamp());member.set(x,SetOptions.merge()).addOnSuccessListener(v->{memberSeen=true;setOnlineState900(true,"Online • rejoined");}).addOnFailureListener(e->setOnlineState900(false,"Join blocked"));}else heartbeat900();});}).addOnFailureListener(e->setOnlineState900(false,"Backend blocked"));
'''
new='''        room.get().addOnSuccessListener(r->{if(r==null||!r.exists()||Boolean.TRUE.equals(r.getBoolean("closed"))){setOnlineState900(false,"Room unavailable");return;}member.get().addOnSuccessListener(m->{if(m==null||!m.exists()){Map<String,Object>x=memberPayload891();x.put("joinedAt",FieldValue.serverTimestamp());member.set(x,SetOptions.merge()).addOnSuccessListener(v->{memberSeen=true;setOnlineState900(true,"Online • rejoined");}).addOnFailureListener(e->member.set(minimalMember921(),SetOptions.merge()).addOnSuccessListener(v->{memberSeen=true;setOnlineState900(true,"Online • compatibility rejoin");}).addOnFailureListener(last->setOnlineState900(false,"Join blocked")));}else heartbeat900();});}).addOnFailureListener(e->setOnlineState900(false,"Backend blocked"));
'''
if old not in s:
    raise SystemExit('ensure membership marker missing')
s=s.replace(old,new,1)

s=s.replace('d.put("appVersion","9.0.0")','d.put("appVersion","9.2.1")',1)

p.write_text(s)
print('v9.2.1 room create/open fix applied')
