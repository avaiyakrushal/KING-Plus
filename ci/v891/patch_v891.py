from pathlib import Path
import sys
root=Path(sys.argv[1])
party=root/'app/src/main/java/com/kingplus/social/PartyActivity.java'
rules=root/'firestore.rules'
s=party.read_text()

old='''    private void showPartyActions(String selected){
        new AlertDialog.Builder(this).setItems(new String[]{"＋ Create Party","↻ Refresh live rooms"},(d,w)->{
            if(w==0){if(user!=null&&db!=null)createRoomDialog();else requireSignInForCreate();}
            else renderLobby(selected);
        }).show();
    }
'''
new='''    private void showPartyActions(String selected){
        new AlertDialog.Builder(this).setItems(new String[]{"＋ Create Party","🔗 Join by Room ID / Code","↻ Refresh live rooms"},(d,w)->{
            if(w==0){if(user!=null&&db!=null)createRoomDialog();else requireSignInForCreate();}
            else if(w==1)joinRoomByCode891();
            else renderLobby(selected);
        }).show();
    }
'''
if old not in s: raise SystemExit('showPartyActions block missing')
s=s.replace(old,new,1)

old='''        TextView create=tv("＋ Create Party",14,Color.WHITE,true);create.setGravity(Gravity.CENTER);create.setBackground(bg(PURPLE,18));create.setOnClickListener(v->createRoomDialog());LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(dp(160),dp(44));cp.gravity=Gravity.CENTER_HORIZONTAL;cp.setMargins(0,dp(8),0,0);box.addView(create,cp);
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(210));bp.setMargins(dp(6),dp(12),dp(6),0);host.addView(box,bp);
'''
new='''        TextView create=tv("＋ Create Party",14,Color.WHITE,true);create.setGravity(Gravity.CENTER);create.setBackground(bg(PURPLE,18));create.setOnClickListener(v->createRoomDialog());LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(dp(160),dp(44));cp.gravity=Gravity.CENTER_HORIZONTAL;cp.setMargins(0,dp(8),0,0);box.addView(create,cp);
        TextView join=tv("🔗 Join by Room Code",13,0xff5b2aa8,true);join.setGravity(Gravity.CENTER);join.setOnClickListener(v->joinRoomByCode891());LinearLayout.LayoutParams jp=new LinearLayout.LayoutParams(dp(180),dp(38));jp.gravity=Gravity.CENTER_HORIZONTAL;jp.setMargins(0,dp(4),0,0);box.addView(join,jp);
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(250));bp.setMargins(dp(6),dp(12),dp(6),0);host.addView(box,bp);
'''
if old not in s: raise SystemExit('empty lobby create block missing')
s=s.replace(old,new,1)

old='''    private void createCloudRoom(String name,String category,int seats,boolean priv){
        Map<String,Object> r=new HashMap<>();r.put("name",name);r.put("ownerUid",user.getUid());r.put("ownerName",safeName());if(user.getPhotoUrl()!=null)r.put("ownerPhoto",user.getPhotoUrl().toString());r.put("category",category);r.put("theme","Classic");r.put("maxSeats",seats);r.put("isPrivate",priv);r.put("hasPassword",false);r.put("announcement",announcement);r.put("locked",false);r.put("muteAll",false);r.put("closed",false);r.put("createdAt",FieldValue.serverTimestamp());r.put("updatedAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").add(r).addOnSuccessListener(ref->{maxSeats=seats;if(pendingCreateCoverUri!=null&&storage!=null)uploadRoomCoverV600(ref,pendingCreateCoverUri);openCloudRoom(ref.getId(),name,user.getUid(),safeName(),priv,false);}).addOnFailureListener(e->toast("Room create failed: "+msg(e)));
    }
'''
new='''    private void createCloudRoom(String name,String category,int seats,boolean priv){
        if(user==null||db==null)return;
        DocumentReference ref=db.collection("live_rooms").document();
        String joinCode=shortId(ref.getId());
        Map<String,Object> r=new HashMap<>();r.put("name",name);r.put("ownerUid",user.getUid());r.put("ownerName",safeName());if(user.getPhotoUrl()!=null)r.put("ownerPhoto",user.getPhotoUrl().toString());r.put("category",category);r.put("theme","Classic");r.put("maxSeats",seats);r.put("isPrivate",priv);r.put("hasPassword",false);r.put("joinCode",joinCode);r.put("announcement",announcement);r.put("locked",false);r.put("muteAll",false);r.put("closed",false);r.put("createdAt",FieldValue.serverTimestamp());r.put("updatedAt",FieldValue.serverTimestamp());
        ref.set(r).addOnSuccessListener(v->{maxSeats=seats;if(pendingCreateCoverUri!=null&&storage!=null)uploadRoomCoverV600(ref,pendingCreateCoverUri);openCloudRoom(ref.getId(),name,user.getUid(),safeName(),priv,false);}).addOnFailureListener(e->roomJoinError891("Room create failed",e));
    }
'''
if old not in s: raise SystemExit('createCloudRoom block missing')
s=s.replace(old,new,1)

old='''    private void openCloudRoom(String id,String name,String hostUid,String hostName,boolean priv,boolean password) {
        roomId=id; roomName=name; ownerUid=hostUid; ownerName=hostName; cloudRoom=true; mySeat=-1; micOn=false; coHost=false; roomPrivate=priv; roomHasPassword=password; memberSeen=false; localRemovedSeats.clear(); lockedSeats.clear();
        processedEventIds.clear(); eventSnapshotReady=false;
        if(roomPrivate&&!isOwner()) tryPrivateEntry(); else { registerMember(); renderParty(); }
    }
'''
new='''    private void openCloudRoom(String id,String name,String hostUid,String hostName,boolean priv,boolean password) {
        if(id==null||id.trim().isEmpty()){toast("Invalid Party Room ID");return;}
        roomId=id.trim(); roomName=name; ownerUid=hostUid; ownerName=hostName; cloudRoom=true; mySeat=-1; micOn=false; coHost=false; roomPrivate=priv; roomHasPassword=password; memberSeen=false; localRemovedSeats.clear(); lockedSeats.clear();
        processedEventIds.clear(); eventSnapshotReady=false;
        if(roomPrivate&&!isOwner()) tryPrivateEntry(); else joinMemberThenOpen891();
    }
'''
if old not in s: raise SystemExit('openCloudRoom block missing')
s=s.replace(old,new,1)

insert_before='''    private void tryPrivateEntry(){
'''
helpers='''    private String deviceId891(){
        try{
            String id=android.provider.Settings.Secure.getString(getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);
            return id==null?"unknown-device":id;
        }catch(Exception e){return "unknown-device";}
    }
    private Map<String,Object> memberPayload891(){
        LevelSystem.Snapshot p720=LevelSystem.read(this);String frame730=KingCosmetics.frame(this),effect730=KingCosmetics.effect(this);
        Map<String,Object>d=new HashMap<>();d.put("uid",user.getUid());d.put("name",safeName());d.put("level",p720.level);d.put("vipLevel",p720.vipLevel);d.put("equippedFrame",frame730);d.put("entranceEffect",effect730);d.put("deviceId",deviceId891());
        String cloudPhoto868=cloudProfilePhoto868();if(!cloudPhoto868.isEmpty())d.put("photoUrl",cloudPhoto868);else if(user.getPhotoUrl()!=null)d.put("photoUrl",user.getPhotoUrl().toString());
        d.put("joinedAt",FieldValue.serverTimestamp());d.put("lastSeenAt",FieldValue.serverTimestamp());return d;
    }
    private void joinMemberThenOpen891(){
        if(user==null||db==null||roomId==null){toast("Sign in required to join Party Room");return;}
        DocumentReference ref=db.collection("live_rooms").document(roomId).collection("members").document(user.getUid());
        Runnable write=()->ref.set(memberPayload891()).addOnSuccessListener(v->{memberSeen=true;renderParty();addEvent("join",safeName()+" joined the room");if(page!=null)page.postDelayed(()->showEntranceEffect(safeName()),350);}).addOnFailureListener(e->roomJoinError891("Could not join this Party Room",e));
        ref.get().addOnSuccessListener(oldDoc->{
            String oldDevice=oldDoc!=null&&oldDoc.exists()?oldDoc.getString("deviceId"):null;
            String now=deviceId891();
            if(oldDevice!=null&&!oldDevice.isEmpty()&&!oldDevice.equals(now)){
                new AlertDialog.Builder(this).setTitle("Same KING account on another phone")
                    .setMessage("This Google/Firebase account is already present in the room from another device. Two phones using the same account count as one KING user. To appear as two different people, sign in with two different Google accounts.\\n\\nContinue as the same KING user?")
                    .setNegativeButton("Cancel",(d,w)->renderLobby("Hot"))
                    .setPositiveButton("Continue", (d,w)->write.run()).show();
            }else write.run();
        }).addOnFailureListener(e->write.run());
    }
    private void roomJoinError891(String title,Exception e){
        String detail=msg(e);String lower=detail.toLowerCase(java.util.Locale.US);
        String hint=(lower.contains("permission")||lower.contains("denied"))
            ?"Firebase backend permission denied. The latest Firestore rules must be deployed to project king-plus-2f365."
            :"Check internet, sign-in and that both phones use the same KING Plus Firebase project.";
        new AlertDialog.Builder(this).setTitle(title).setMessage(detail+"\\n\\n"+hint)
            .setPositiveButton("OK",null).setNeutralButton("Try Room Code",(d,w)->joinRoomByCode891()).show();
    }
    private void joinRoomByCode891(){
        if(user==null||db==null){requireSignInForCreate();return;}
        final EditText input=new EditText(this);input.setHint("6-digit code or KINGROOM:full-id");input.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("Join KING Party").setMessage("Paste the Room Code or full Room ID shared by the host.").setView(input)
            .setNegativeButton("Cancel",null).setPositiveButton("Join",(d,w)->{
                String raw=input.getText().toString().trim();if(raw.isEmpty())return;
                if(raw.startsWith("KINGROOM:"))raw=raw.substring("KINGROOM:".length()).trim();
                final String token=raw;
                if(token.length()>10&&!token.contains(" ")){joinRoomDocument891(token);return;}
                db.collection("live_rooms").whereEqualTo("joinCode",token).limit(5).get().addOnSuccessListener(q->{
                    if(q!=null&&!q.isEmpty()){openRoomDoc891(q.getDocuments().get(0));return;}
                    fallbackFindRoom891(token);
                }).addOnFailureListener(e->fallbackFindRoom891(token));
            }).show();
    }
    private void fallbackFindRoom891(String token){
        db.collection("live_rooms").limit(100).get().addOnSuccessListener(q->{
            for(DocumentSnapshot d:q.getDocuments()){
                if(Boolean.TRUE.equals(d.getBoolean("closed")))continue;
                String code=str(d,"joinCode",shortId(d.getId()));
                if(token.equalsIgnoreCase(code)||token.equalsIgnoreCase(shortId(d.getId()))||token.equalsIgnoreCase(d.getId())||token.equalsIgnoreCase(str(d,"name",""))){openRoomDoc891(d);return;}
            }
            toast("Room not found. Ask host to share the full KINGROOM ID.");
        }).addOnFailureListener(e->roomJoinError891("Room lookup failed",e));
    }
    private void joinRoomDocument891(String id){
        db.collection("live_rooms").document(id).get().addOnSuccessListener(d->{if(d==null||!d.exists()||Boolean.TRUE.equals(d.getBoolean("closed"))){toast("Room not found or already closed");return;}openRoomDoc891(d);}).addOnFailureListener(e->roomJoinError891("Room lookup failed",e));
    }
    private void openRoomDoc891(DocumentSnapshot d){
        openCloudRoom(d.getId(),str(d,"name","Live Party"),d.getString("ownerUid"),str(d,"ownerName","Host"),Boolean.TRUE.equals(d.getBoolean("isPrivate")),Boolean.TRUE.equals(d.getBoolean("hasPassword")));
    }

'''
if insert_before not in s: raise SystemExit('tryPrivateEntry marker missing')
s=s.replace(insert_before,helpers+insert_before,1)

old='''                        if (nameV530.toLowerCase().contains(qV530)) {
                            hitsV530.add(docV530);
                            labelsV530.add(nameV530 + "  •  " + str(docV530,"ownerName","Host"));
                        }
'''
new='''                        String codeV530=str(docV530,"joinCode",shortId(docV530.getId()));
                        if (nameV530.toLowerCase().contains(qV530) || codeV530.toLowerCase().contains(qV530) || shortId(docV530.getId()).toLowerCase().contains(qV530) || docV530.getId().toLowerCase().contains(qV530)) {
                            hitsV530.add(docV530);
                            labelsV530.add(nameV530 + "  •  " + str(docV530,"ownerName","Host") + "  •  " + codeV530);
                        }
'''
if old not in s: raise SystemExit('search room match block missing')
s=s.replace(old,new,1)

old='''    private void shareRoom(){Intent s=new Intent(Intent.ACTION_SEND);s.setType("text/plain");s.putExtra(Intent.EXTRA_TEXT,"Join my KING Plus Party: "+roomName+" • Room ID "+shortId());startActivity(Intent.createChooser(s,"Share Party"));}
'''
new='''    private void shareRoom(){Intent s=new Intent(Intent.ACTION_SEND);s.setType("text/plain");String full=roomId==null?"":roomId;s.putExtra(Intent.EXTRA_TEXT,"Join my KING Plus Party: "+roomName+"\\nRoom Code: "+shortId()+"\\nKINGROOM:"+full+"\\n\\nOpen KING Plus → Party → menu → Join by Room ID / Code. Use a different Google account on each phone if you want two separate people.");startActivity(Intent.createChooser(s,"Share Party"));}
'''
if old not in s: raise SystemExit('shareRoom block missing')
s=s.replace(old,new,1)

old='''    private void registerMember(){if(user==null||db==null||roomId==null)return;LevelSystem.Snapshot p720=LevelSystem.read(this);String frame730=KingCosmetics.frame(this),effect730=KingCosmetics.effect(this);Map<String,Object>d=new HashMap<>();d.put("uid",user.getUid());d.put("name",safeName());d.put("level",p720.level);d.put("vipLevel",p720.vipLevel);d.put("equippedFrame",frame730);d.put("entranceEffect",effect730);String cloudPhoto868=cloudProfilePhoto868();if(!cloudPhoto868.isEmpty())d.put("photoUrl",cloudPhoto868);else if(user.getPhotoUrl()!=null)d.put("photoUrl",user.getPhotoUrl().toString());d.put("joinedAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).collection("members").document(user.getUid()).set(d)
            .addOnSuccessListener(v->{addEvent("join",safeName()+" joined the room");if(page!=null)page.postDelayed(()->showEntranceEffect730(safeName(),p720.level,p720.vipLevel,effect730,frame730),350);})
            .addOnFailureListener(e->toast("Join blocked: private room, room ban, or permission denied"));}
'''
new='''    private void registerMember(){joinMemberThenOpen891();}
'''
if old not in s: raise SystemExit('registerMember block missing')
s=s.replace(old,new,1)

party.write_text(s)

rs=rules.read_text()
old='''      allow create: if activeUser()
        && request.resource.data.ownerUid == request.auth.uid
        && request.resource.data.isPrivate != true;
'''
new='''      allow create: if activeUser()
        && request.resource.data.ownerUid == request.auth.uid
        && request.resource.data.isPrivate is bool
        && request.resource.data.hasPassword == false
        && request.resource.data.joinCode is string
        && request.resource.data.joinCode.size() == 6;
'''
if old not in rs: raise SystemExit('live room create rule missing')
rs=rs.replace(old,new,1)
rules.write_text(rs)
print('v8.9.1 multidevice room patch applied')
