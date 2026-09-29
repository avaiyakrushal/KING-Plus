from pathlib import Path
import re

PARTY = Path('app/src/main/java/com/kingplus/social/PartyActivity.java')
BUILD = Path('app/build.gradle')
src = PARTY.read_text(encoding='utf-8')

# v3.2.2: password/private entry plus seat request/approval and per-seat locks.
needle = '    private ListenerRegistration roomBanListener;\n'
extra = '''    private ListenerRegistration roomBanListener;
    private boolean roomHasPassword;
    private final Set<Integer> lockedSeats = new HashSet<>();
    private ListenerRegistration seatLocksListener;
'''
if needle not in src: raise SystemExit('v3.2.2 Party field insertion point changed')
src = src.replace(needle, extra, 1)

old = '        ListenerRegistration[] ls = {roomsListener,roomListener,seatsListener,membersListener,messagesListener,eventsListener,roleListener,selfMemberListener,roomBanListener};\n'
new = '        ListenerRegistration[] ls = {roomsListener,roomListener,seatsListener,membersListener,messagesListener,eventsListener,roleListener,selfMemberListener,roomBanListener,seatLocksListener};\n'
if old not in src: raise SystemExit('v3.2.2 listener list changed')
src = src.replace(old,new,1)

old = '        roleListener = selfMemberListener = roomBanListener = null;\n'
new = '''        roleListener = selfMemberListener = roomBanListener = null;
        seatLocksListener = null;
'''
if old not in src: raise SystemExit('v3.2.2 listener reset changed')
src = src.replace(old,new,1)

# Room cards expose private/password status and pass it into entry flow.
old = '''                            String category = str(doc,"category","Hot");
                            String q = lobbyFilter.toLowerCase();
'''
new = '''                            String category = str(doc,"category","Hot");
                            boolean priv = Boolean.TRUE.equals(doc.getBoolean("isPrivate"));
                            boolean password = Boolean.TRUE.equals(doc.getBoolean("hasPassword"));
                            String q = lobbyFilter.toLowerCase();
'''
if old not in src: raise SystemExit('Lobby metadata point changed')
src = src.replace(old,new,1)

old = '                            addRoomCard(cloudList,"🔥 ["+category+"] " + name,"Host: " + host + "   •   ID "+shortId(id)+"   •   LIVE",() -> openCloudRoom(id,name,doc.getString("ownerUid"),host));\n'
new = '                            addRoomCard(cloudList,(priv?"🔐 ":"🔥 ")+"["+category+"] "+name,"Host: "+host+"   •   ID "+shortId(id)+(password?"   •   PASSWORD":"")+"   •   LIVE",() -> openCloudRoom(id,name,doc.getString("ownerUid"),host,priv,password));\n'
if old not in src: raise SystemExit('Lobby room card point changed')
src = src.replace(old,new,1)

old = '        r.put("category",category); r.put("theme","Classic"); r.put("maxSeats",12); r.put("isPrivate",false);\n'
new = '        r.put("category",category); r.put("theme","Classic"); r.put("maxSeats",12); r.put("isPrivate",false); r.put("hasPassword",false);\n'
if old not in src: raise SystemExit('Room creation fields changed')
src = src.replace(old,new,1)

# Replace cloud-room opening with private/password-aware entry.
pattern = re.compile(r'    private void openCloudRoom\(String id,String name,String hostUid,String hostName\) \{.*?^    private void openLocalRoom\(String name,String hostName\)', re.S | re.M)
replacement = r'''    private void openCloudRoom(String id,String name,String hostUid,String hostName) {
        openCloudRoom(id,name,hostUid,hostName,false,false);
    }
    private void openCloudRoom(String id,String name,String hostUid,String hostName,boolean priv,boolean password) {
        roomId=id; roomName=name; ownerUid=hostUid; ownerName=hostName; cloudRoom=true; mySeat=-1; micOn=false; coHost=false; roomPrivate=priv; roomHasPassword=password; memberSeen=false; localRemovedSeats.clear(); lockedSeats.clear();
        if(roomPrivate&&!isOwner()) tryPrivateEntry(); else { registerMember(); renderParty(); }
    }
    private void tryPrivateEntry(){
        if(user==null||db==null||roomId==null)return;
        Map<String,Object>d=new HashMap<>();d.put("uid",user.getUid());d.put("name",safeName());d.put("joinedAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).collection("members").document(user.getUid()).set(d)
            .addOnSuccessListener(v->{memberSeen=true;addEvent("join",safeName()+" joined the room");renderParty();})
            .addOnFailureListener(e->{if(roomHasPassword)passwordRoomJoinDialog();else toast("Private room • invite access required");});
    }
    private void passwordRoomJoinDialog(){
        final EditText e=new EditText(this);e.setHint("Room password");e.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("🔐 Private Party").setMessage("Enter the room password").setView(e)
            .setNegativeButton("Cancel",null).setPositiveButton("Join",(d,w)->{
                String password=e.getText().toString();if(password.length()<4){toast("Password must be at least 4 characters");return;}
                String proof=passwordProof(roomId,password);if(proof.isEmpty()){toast("Password check unavailable");return;}
                Map<String,Object> access=new HashMap<>();access.put("uid",user.getUid());access.put("passwordHash",proof);access.put("createdAt",FieldValue.serverTimestamp());
                db.collection("live_rooms").document(roomId).collection("access").document(user.getUid()).set(access)
                    .addOnSuccessListener(v->tryPrivateEntry())
                    .addOnFailureListener(x->toast("Wrong room password"));
            }).show();
    }
    private String passwordProof(String id,String password){
        try{
            java.security.MessageDigest md=java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes=md.digest((id+"::"+password).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder out=new StringBuilder();for(byte b:bytes)out.append(String.format("%02x",b));return out.toString();
        }catch(Exception e){return "";}
    }
    private void openLocalRoom(String name,String hostName)'''
src,count = pattern.subn(replacement,src,count=1)
if count != 1: raise SystemExit('Cloud room open method changed')

# Render individual seat locks.
old = '                TextView av=tv(mine?"👑":(n==null?"＋":"●"),mine?27:24,Color.WHITE,true);av.setGravity(Gravity.CENTER);av.setBackground(bg(mine?PURPLE:0xff4a3768,50));seat.addView(av,new LinearLayout.LayoutParams(dp(54),dp(54)));\n                String label=n==null?"Seat "+no:n; if(Boolean.FALSE.equals(seatMics.get(no))) label="🔇 "+label; TextView lab=tv(label,10,mine?Color.WHITE:MUTED,false);lab.setGravity(Gravity.CENTER);seat.addView(lab,new LinearLayout.LayoutParams(-1,dp(26)));\n'
new = '''                boolean seatLocked=cloudRoom&&lockedSeats.contains(no);
                TextView av=tv(mine?"👑":(n==null?(seatLocked?"🔒":"＋"):"●"),mine?27:24,Color.WHITE,true);av.setGravity(Gravity.CENTER);av.setBackground(bg(mine?PURPLE:0xff4a3768,50));seat.addView(av,new LinearLayout.LayoutParams(dp(54),dp(54)));
                String label=n==null?(seatLocked?"🔒 Seat "+no:"Seat "+no):n; if(Boolean.FALSE.equals(seatMics.get(no))) label="🔇 "+label; TextView lab=tv(label,10,mine?Color.WHITE:MUTED,false);lab.setGravity(Gravity.CENTER);seat.addView(lab,new LinearLayout.LayoutParams(-1,dp(26)));
'''
if old not in src: raise SystemExit('Seat render template changed')
src = src.replace(old,new,1)

# Replace seat action with request/approval aware flow.
pattern = re.compile(r'    private void seatAction\(int no\) \{.*?^    private void cloudSeatAction\(int no\) \{', re.S | re.M)
replacement = r'''    private void seatAction(int no) {
        if(cloudRoom){
            String existing=seatUids.get(no);
            if(existing!=null&&!existing.equals(user==null?null:user.getUid())){seatUserMenu(no);return;}
            if(existing!=null){cloudSeatAction(no);return;}
            if(isModerator()){moderatorEmptySeatMenu(no);return;}
            if(roomLocked||lockedSeats.contains(no)){requestSeat(no);return;}
            cloudSeatAction(no);return;
        }
        if (roomLocked && !isModerator() && mySeat<0) { toast("Room seats are locked by host"); return; }
        if (no==mySeat) { mySeat=-1;micOn=false;prefs.edit().remove("seat_"+roomId).putBoolean("mic_"+roomId,false).apply(); }
        else if (seatNames.get(no)!=null) { seatUserMenu(no); return; }
        else { mySeat=no;prefs.edit().putInt("seat_"+roomId,no).apply(); }
        fillLocalSeats(); if(micLabel!=null)micLabel.setText(micOn?"🎤 Mic ON":"🎤 Mic OFF");
    }
    private void moderatorEmptySeatMenu(int no){
        String[] items={lockedSeats.contains(no)?"🔓 Unlock this seat":"🔒 Lock this seat","🎙 Take this seat","📥 Seat requests"};
        new AlertDialog.Builder(this).setTitle("Seat "+no).setItems(items,(d,w)->{
            if(w==0)toggleIndividualSeatLock(no);else if(w==1)cloudSeatAction(no);else showSeatRequests(no);
        }).setNegativeButton("Close",null).show();
    }
    private void toggleIndividualSeatLock(int no){
        if(!isModerator()||db==null)return;
        DocumentReference ref=db.collection("live_rooms").document(roomId).collection("seat_locks").document(String.valueOf(no));
        if(lockedSeats.contains(no))ref.delete().addOnFailureListener(e->toast(msg(e)));
        else{Map<String,Object>d=new HashMap<>();d.put("locked",true);d.put("byUid",user.getUid());d.put("updatedAt",FieldValue.serverTimestamp());ref.set(d).addOnFailureListener(e->toast(msg(e)));}
    }
    private void requestSeat(int no){
        if(user==null||db==null){toast("Sign in required");return;}
        String requestId=user.getUid()+"_"+no;
        Map<String,Object>d=new HashMap<>();d.put("uid",user.getUid());d.put("name",safeName());d.put("seatNo",no);d.put("createdAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).collection("seat_requests").document(requestId).set(d)
            .addOnSuccessListener(v->toast("Seat "+no+" request sent to host"))
            .addOnFailureListener(e->toast("Seat request failed: "+msg(e)));
    }
    private void showSeatRequests(int no){
        if(!isModerator()||db==null)return;
        db.collection("live_rooms").document(roomId).collection("seat_requests").whereEqualTo("seatNo",no).get()
            .addOnSuccessListener(snap->{
                if(snap==null||snap.isEmpty()){toast("No requests for seat "+no);return;}
                List<DocumentSnapshot> docs=snap.getDocuments();List<String> labels=new ArrayList<>();
                for(DocumentSnapshot d:docs)labels.add(str(d,"name","User"));
                new AlertDialog.Builder(this).setTitle("Seat "+no+" requests").setItems(labels.toArray(new String[0]),(x,w)->seatRequestDecision(docs.get(w),no))
                    .setNegativeButton("Close",null).show();
            }).addOnFailureListener(e->toast(msg(e)));
    }
    private void seatRequestDecision(DocumentSnapshot req,int no){
        String uid=req.getString("uid");String name=str(req,"name","User");if(uid==null)return;
        String[] items={"✓ Approve","✕ Deny"};
        new AlertDialog.Builder(this).setTitle(name+" → Seat "+no).setItems(items,(d,w)->{
            if(w==0)approveSeatRequest(req.getId(),uid,name,no);else req.getReference().delete();
        }).show();
    }
    private void approveSeatRequest(String requestId,String uid,String name,int no){
        if(!isModerator()||db==null)return;
        DocumentReference seat=db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(no));
        Map<String,Object>d=new HashMap<>();d.put("uid",uid);d.put("name",name);d.put("micOn",false);d.put("joinedAt",FieldValue.serverTimestamp());
        seat.set(d).addOnSuccessListener(v->{
            db.collection("live_rooms").document(roomId).collection("seat_requests").document(requestId).delete();
            addEvent("seat",name+" was approved for seat "+no);
        }).addOnFailureListener(e->toast("Approve failed: "+msg(e)));
    }
    private void cloudSeatAction(int no) {'''
src,count = pattern.subn(replacement,src,count=1)
if count != 1: raise SystemExit('Seat action template changed')

# Add password controls to host room menu.
old = '''            items.add(roomPrivate?"🔓 Make room public":"🔐 Make room private");
            items.add("⛔ Close room");
'''
new = '''            items.add(roomPrivate?"🔓 Make room public":"🔐 Make room private");
            items.add(roomHasPassword?"🔑 Change room password":"🔑 Set room password");
            if(roomHasPassword)items.add("🗑 Remove room password");
            items.add("⛔ Close room");
'''
if old not in src: raise SystemExit('Owner menu template changed')
src = src.replace(old,new,1)

old = '''            else if(x.contains("private")||x.contains("public"))setPrivateRoom(!roomPrivate);
            else if(x.contains("Close room"))closeRoom();
'''
new = '''            else if(x.contains("private")||x.contains("public"))setPrivateRoom(!roomPrivate);
            else if(x.contains("Set room password")||x.contains("Change room password"))setRoomPasswordDialog();
            else if(x.contains("Remove room password"))removeRoomPassword();
            else if(x.contains("Close room"))closeRoom();
'''
if old not in src: raise SystemExit('Owner menu dispatcher changed')
src = src.replace(old,new,1)

# Add password setup/removal helpers next to private room control.
marker = '    private void followUser(String uid,String name){'
helpers = r'''    private void setRoomPasswordDialog(){
        if(!isOwner()||!cloudRoom||db==null){toast("Live host room required");return;}
        final EditText e=new EditText(this);e.setHint("At least 4 characters");e.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle(roomHasPassword?"Change room password":"Set room password").setView(e)
            .setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{
                String password=e.getText().toString();if(password.length()<4){toast("Password must be at least 4 characters");return;}
                String hash=passwordProof(roomId,password);if(hash.isEmpty()){toast("Could not secure password");return;}
                Map<String,Object> secret=new HashMap<>();secret.put("passwordHash",hash);secret.put("ownerUid",user.getUid());secret.put("updatedAt",FieldValue.serverTimestamp());
                db.collection("live_rooms").document(roomId).collection("secrets").document("password").set(secret)
                    .addOnSuccessListener(v->db.collection("live_rooms").document(roomId).update("isPrivate",true,"hasPassword",true,"updatedAt",FieldValue.serverTimestamp())
                        .addOnSuccessListener(x->{roomPrivate=true;roomHasPassword=true;toast("Password room enabled");})
                        .addOnFailureListener(x->toast(msg(x))))
                    .addOnFailureListener(x->toast("Password save failed: "+msg(x)));
            }).show();
    }
    private void removeRoomPassword(){
        if(!isOwner()||!cloudRoom||db==null)return;
        db.collection("live_rooms").document(roomId).collection("secrets").document("password").delete()
            .addOnSuccessListener(v->db.collection("live_rooms").document(roomId).update("hasPassword",false,"updatedAt",FieldValue.serverTimestamp())
                .addOnSuccessListener(x->{roomHasPassword=false;toast("Password removed • room remains private");}))
            .addOnFailureListener(e->toast(msg(e)));
    }

'''
if marker not in src: raise SystemExit('Password helper insertion point changed')
src = src.replace(marker,helpers+marker,1)

# Sync password state and seat locks from Firestore.
old = 'roomPrivate=Boolean.TRUE.equals(doc.getBoolean("isPrivate"));if(announcementLabel!=null)'
new = 'roomPrivate=Boolean.TRUE.equals(doc.getBoolean("isPrivate"));roomHasPassword=Boolean.TRUE.equals(doc.getBoolean("hasPassword"));if(announcementLabel!=null)'
if old not in src: raise SystemExit('Room listener private state changed')
src = src.replace(old,new,1)

marker = '        seatsListener=room.collection("seats").addSnapshotListener'
listeners = r'''        seatLocksListener=room.collection("seat_locks").addSnapshotListener((snap,e)->{
            if(e!=null||snap==null)return;lockedSeats.clear();
            for(DocumentSnapshot d:snap.getDocuments()){if(Boolean.TRUE.equals(d.getBoolean("locked"))){try{lockedSeats.add(Integer.parseInt(d.getId()));}catch(Exception ignored){}}}
            rebuildSeats();
        });
'''
if marker not in src: raise SystemExit('Seat locks listener insertion point changed')
src = src.replace(marker,listeners+marker,1)

PARTY.write_text(src, encoding='utf-8')

gradle=BUILD.read_text(encoding='utf-8')
gradle=re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'","versionCode 46; versionName '3.2.2'",gradle)
BUILD.write_text(gradle,encoding='utf-8')
print('Prepared KING Plus v3.2.2 password rooms + seat request/approve + individual seat locks')
