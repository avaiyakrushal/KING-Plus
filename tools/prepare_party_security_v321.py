from pathlib import Path
import re

PARTY = Path('app/src/main/java/com/kingplus/social/PartyActivity.java')
BUILD = Path('app/build.gradle')
src = PARTY.read_text(encoding='utf-8')

# v3.2.1 cloud moderation state. This stage runs after prepare_party_copy_v320.py.
needle = '    private final Set<Integer> localRemovedSeats = new HashSet<>();\n'
extra = '''    private final Set<Integer> localRemovedSeats = new HashSet<>();
    private boolean coHost;
    private boolean roomPrivate;
    private boolean memberSeen;
    private ListenerRegistration roleListener;
    private ListenerRegistration selfMemberListener;
    private ListenerRegistration roomBanListener;
'''
if needle not in src:
    raise SystemExit('v3.2.1 Party state insertion point changed')
src = src.replace(needle, extra, 1)

old = '        ListenerRegistration[] ls = {roomsListener,roomListener,seatsListener,membersListener,messagesListener,eventsListener};\n'
new = '        ListenerRegistration[] ls = {roomsListener,roomListener,seatsListener,membersListener,messagesListener,eventsListener,roleListener,selfMemberListener,roomBanListener};\n'
if old not in src:
    raise SystemExit('Party listener list changed')
src = src.replace(old, new, 1)

old = '        roomsListener = roomListener = seatsListener = membersListener = messagesListener = eventsListener = null;\n'
new = '''        roomsListener = roomListener = seatsListener = membersListener = messagesListener = eventsListener = null;
        roleListener = selfMemberListener = roomBanListener = null;
'''
if old not in src:
    raise SystemExit('Party listener reset changed')
src = src.replace(old, new, 1)

needle = '        r.put("category",category); r.put("theme","Classic"); r.put("maxSeats",12);\n'
replacement = '        r.put("category",category); r.put("theme","Classic"); r.put("maxSeats",12); r.put("isPrivate",false);\n'
if needle not in src:
    raise SystemExit('Cloud room creation fields changed')
src = src.replace(needle, replacement, 1)

needle = '        roomId=id; roomName=name; ownerUid=hostUid; ownerName=hostName; cloudRoom=true; mySeat=-1; micOn=false; localRemovedSeats.clear();\n'
replacement = '        roomId=id; roomName=name; ownerUid=hostUid; ownerName=hostName; cloudRoom=true; mySeat=-1; micOn=false; coHost=false; roomPrivate=false; memberSeen=false; localRemovedSeats.clear();\n'
if needle not in src:
    raise SystemExit('Open cloud room template changed')
src = src.replace(needle, replacement, 1)

src = src.replace('if (roomLocked && !isOwner() && mySeat<0)', 'if (roomLocked && !isModerator() && mySeat<0)', 1)
src = src.replace('if(muteAll&&!isOwner())', 'if(muteAll&&!isModerator())', 1)

pattern = re.compile(r'    private void roomMenu\(\) \{.*?^    private void inviteDialog\(\)', re.S | re.M)
replacement = r'''    private void roomMenu() {
        List<String> items=new ArrayList<>();
        items.add("🎙 Open voice room"); items.add("🎵 Song request"); items.add("😊 Reaction");
        items.add("👥 Members"); items.add("🎮 Games"); items.add("⚔ PK battle");
        items.add("🔗 Share room"); items.add("🔔 Invite by Firebase UID");
        items.add("⚑ Report host"); items.add("🚫 Block host");
        if(isModerator()){
            items.add(roomLocked?"🔓 Unlock seats":"🔒 Lock seats");
            items.add(muteAll?"🎤 Unmute all seats":"🔇 Mute all seats");
            items.add("📢 Edit announcement");
        }
        if(isOwner()){
            items.add("✏ Rename room"); items.add("🗂 Change category"); items.add("🎨 Room theme");
            items.add("🎙 Change mic seats");
            items.add(roomPrivate?"🔓 Make room public":"🔐 Make room private");
            items.add("⛔ Close room");
        }
        String[] a=items.toArray(new String[0]);
        new AlertDialog.Builder(this).setTitle(roomName+(coHost?" • Co-host":"")).setItems(a,(d,w)->{
            String x=a[w];
            if(x.contains("Open voice"))openVoice();
            else if(x.contains("Song request"))karaokeDialog();
            else if(x.contains("Reaction"))reactionDialog();
            else if(x.contains("Members"))membersDialog();
            else if(x.contains("Games"))openGames();
            else if(x.contains("PK battle"))pkBattle();
            else if(x.contains("Share"))shareRoom();
            else if(x.contains("Invite"))inviteDialog();
            else if(x.contains("Report"))reportHost();
            else if(x.contains("Block"))blockHost();
            else if(x.contains("Rename room"))renameRoomDialog();
            else if(x.contains("Change category"))changeCategoryDialog();
            else if(x.contains("Room theme"))changeThemeDialog();
            else if(x.contains("mic seats"))changeSeatCount();
            else if(x.contains("Lock")||x.contains("Unlock"))setRoomFlag("locked",!roomLocked);
            else if(x.contains("Mute all")||x.contains("Unmute"))setRoomFlag("muteAll",!muteAll);
            else if(x.contains("announcement"))editAnnouncement();
            else if(x.contains("private")||x.contains("public"))setPrivateRoom(!roomPrivate);
            else if(x.contains("Close room"))closeRoom();
        }).show();
    }
    private void inviteDialog()'''
src, count = pattern.subn(replacement, src, count=1)
if count != 1:
    raise SystemExit('Room menu template changed')

pattern = re.compile(r'    private void inviteDialog\(\)\{.*?\}\n    private void reportHost\(\)', re.S)
replacement = r'''    private void inviteDialog(){
        if(!cloudRoom||user==null){shareRoom();return;}
        final EditText e=new EditText(this);e.setHint("Friend Firebase UID");
        new AlertDialog.Builder(this).setTitle("Invite to Party").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Invite",(d,w)->{
            String uid=e.getText().toString().trim();if(uid.isEmpty())return;
            Runnable send=()->CloudBackend.sendRoomInvite(uid,roomId,roomName,(ok,m)->runOnUiThread(()->toast(m)));
            if(roomPrivate&&isModerator()&&db!=null){
                Map<String,Object> grant=new HashMap<>();grant.put("uid",uid);grant.put("grantedBy",user.getUid());grant.put("createdAt",FieldValue.serverTimestamp());
                db.collection("live_rooms").document(roomId).collection("access").document(uid).set(grant)
                    .addOnSuccessListener(v->send.run()).addOnFailureListener(x->toast("Private invite failed: "+msg(x)));
            }else send.run();
        }).show();
    }
    private void reportHost()'''
src, count = pattern.subn(replacement, src, count=1)
if count != 1:
    raise SystemExit('Invite dialog template changed')

src = src.replace('private void setRoomFlag(String key,boolean value){if(!isOwner()){toast("Host only");return;}',
                  'private void setRoomFlag(String key,boolean value){if(!isModerator()){toast("Host/co-host only");return;}', 1)

src = src.replace('if(!isOwner()){toast("Host only");return;} boolean next=', 'if(!isModerator()){toast("Host/co-host only");return;} boolean next=', 1)
src = src.replace('if(!isOwner()){toast("Host only");return;}\n        if(cloudRoom&&db!=null)db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(no)).delete()',
                  'if(!isModerator()){toast("Host/co-host only");return;}\n        if(cloudRoom&&db!=null)db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(no)).delete()', 1)

pattern = re.compile(r'    private void membersDialog\(\)\{.*?^    private void seatUserMenu\(int no\)\{', re.S | re.M)
replacement = r'''    private void membersDialog(){
        List<String> labels=new ArrayList<>(); List<String> ids=new ArrayList<>();
        if(cloudRoom){
            if(memberNames.isEmpty()){labels.add("No members loaded");ids.add("");}
            else for(String n:memberNames){labels.add(n);String uid=memberUids.get(n);ids.add(uid==null?"":uid);}
        } else {
            labels.add(ownerName+" • Host"); ids.add("");
            for(int i=1;i<=maxSeats;i++){String n=seatNames.get(i);if(n!=null&&!labels.contains(n)){labels.add(n);ids.add("");}}
        }
        new AlertDialog.Builder(this).setTitle("👥 Room members").setItems(labels.toArray(new String[0]),(d,w)->{
            if(!cloudRoom||ids.get(w).isEmpty()||!isModerator())return;
            memberUserMenu(ids.get(w),labels.get(w));
        }).setNegativeButton("Close",null).show();
    }
    private void seatUserMenu(int no){'''
src, count = pattern.subn(replacement, src, count=1)
if count != 1:
    raise SystemExit('Members dialog template changed')

pattern = re.compile(r'    private void seatUserMenu\(int no\)\{.*?^    private void followUser\(String uid,String name\)\{', re.S | re.M)
replacement = r'''    private void seatUserMenu(int no){
        String name=seatNames.get(no); if(name==null)return; String uid=seatUids.get(no);
        List<String> items=new ArrayList<>(); items.add("🎁 Send gift"); items.add("＋ Follow"); items.add("⚑ Report");
        if(isModerator()){
            items.add(Boolean.FALSE.equals(seatMics.get(no))?"🎤 Unmute seat":"🔇 Mute seat");
            items.add("⬇ Remove from seat"); items.add("🚪 Kick from room"); items.add("🚫 Ban from room");
        }
        if(isOwner()&&uid!=null&&!uid.isEmpty()&&!uid.equals(ownerUid))items.add("👑 Co-host role");
        String[] a=items.toArray(new String[0]);
        new AlertDialog.Builder(this).setTitle(name+" • Seat "+no).setItems(a,(d,w)->{
            String x=a[w];
            if(x.contains("Gift"))giftDialogFor(uid==null?"":uid,name);
            else if(x.contains("Follow"))followUser(uid,name);
            else if(x.contains("Report"))reportUser(uid,name);
            else if(x.contains("Mute seat")||x.contains("Unmute seat"))hostToggleSeat(no);
            else if(x.contains("Remove"))hostRemoveSeat(no);
            else if(x.contains("Kick"))kickUser(uid,name);
            else if(x.contains("Ban"))banUser(uid,name);
            else if(x.contains("Co-host"))coHostDialog(uid,name);
        }).setNegativeButton("Close",null).show();
    }
    private void memberUserMenu(String uid,String name){
        if(uid==null||uid.isEmpty()||uid.equals(ownerUid)){toast("Host cannot be moderated here");return;}
        List<String> items=new ArrayList<>();items.add("🚪 Kick from room");items.add("🚫 Ban from room");
        if(isOwner())items.add("👑 Co-host role");
        String[] a=items.toArray(new String[0]);
        new AlertDialog.Builder(this).setTitle(name).setItems(a,(d,w)->{
            String x=a[w];if(x.contains("Kick"))kickUser(uid,name);else if(x.contains("Ban"))banUser(uid,name);else coHostDialog(uid,name);
        }).setNegativeButton("Close",null).show();
    }
    private void followUser(String uid,String name){'''
src, count = pattern.subn(replacement, src, count=1)
if count != 1:
    raise SystemExit('Seat user menu template changed')

marker = '    private void followUser(String uid,String name){'
helpers = r'''    private void kickUser(String uid,String name){
        if(!isModerator()||uid==null||uid.isEmpty()||uid.equals(ownerUid)){toast("Cannot kick this member");return;}
        if(!cloudRoom||db==null){toast("Live room required");return;}
        for(Map.Entry<Integer,String> entry:new HashMap<>(seatUids).entrySet()){
            if(uid.equals(entry.getValue()))db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(entry.getKey())).delete();
        }
        db.collection("live_rooms").document(roomId).collection("members").document(uid).delete()
            .addOnSuccessListener(v->{addEvent("kick",name+" was removed by a moderator");toast(name+" kicked");})
            .addOnFailureListener(e->toast("Kick failed: "+msg(e)));
    }
    private void banUser(String uid,String name){
        if(!isModerator()||uid==null||uid.isEmpty()||uid.equals(ownerUid)){toast("Cannot ban this member");return;}
        if(!cloudRoom||db==null){toast("Live room required");return;}
        Map<String,Object> ban=new HashMap<>();ban.put("uid",uid);ban.put("active",true);ban.put("byUid",user.getUid());ban.put("createdAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).collection("room_bans").document(uid).set(ban)
            .addOnSuccessListener(v->kickUser(uid,name)).addOnFailureListener(e->toast("Ban failed: "+msg(e)));
    }
    private void coHostDialog(String uid,String name){
        if(!isOwner()||uid==null||uid.isEmpty()||uid.equals(ownerUid)){toast("Host only");return;}
        String[] actions={"Make co-host","Remove co-host"};
        new AlertDialog.Builder(this).setTitle("Co-host • "+name).setItems(actions,(d,w)->{
            DocumentReference ref=db.collection("live_rooms").document(roomId).collection("roles").document(uid);
            if(w==0){
                Map<String,Object> role=new HashMap<>();role.put("uid",uid);role.put("role","cohost");role.put("grantedBy",user.getUid());role.put("updatedAt",FieldValue.serverTimestamp());
                ref.set(role).addOnSuccessListener(v->toast(name+" is now co-host")).addOnFailureListener(e->toast(msg(e)));
            }else ref.delete().addOnSuccessListener(v->toast("Co-host removed")).addOnFailureListener(e->toast(msg(e)));
        }).show();
    }
    private void setPrivateRoom(boolean value){
        if(!isOwner()){toast("Host only");return;}
        roomPrivate=value;setRoomValue("isPrivate",value);toast(value?"Private room enabled":"Room is public");
    }

'''
if marker not in src:
    raise SystemExit('Moderation helper insertion point changed')
src = src.replace(marker, helpers + marker, 1)

old = 'roomLocked=Boolean.TRUE.equals(doc.getBoolean("locked"));muteAll=Boolean.TRUE.equals(doc.getBoolean("muteAll"));'
new = 'roomLocked=Boolean.TRUE.equals(doc.getBoolean("locked"));muteAll=Boolean.TRUE.equals(doc.getBoolean("muteAll"));roomPrivate=Boolean.TRUE.equals(doc.getBoolean("isPrivate"));'
if old not in src:
    raise SystemExit('Room state listener changed')
src = src.replace(old, new, 1)

marker = '        seatsListener=room.collection("seats").addSnapshotListener'
listeners = r'''        roleListener=room.collection("roles").document(user.getUid()).addSnapshotListener((doc,e)->{
            coHost=e==null&&doc!=null&&doc.exists()&&"cohost".equals(doc.getString("role"));
        });
        selfMemberListener=room.collection("members").document(user.getUid()).addSnapshotListener((doc,e)->{
            if(e!=null||doc==null)return;
            if(doc.exists())memberSeen=true;
            else if(memberSeen&&!isOwner()){toast("You were removed from this room");leaveRoom();}
        });
        roomBanListener=room.collection("room_bans").document(user.getUid()).addSnapshotListener((doc,e)->{
            if(e==null&&doc!=null&&doc.exists()&&Boolean.TRUE.equals(doc.getBoolean("active"))&&!isOwner()){toast("You are banned from this room");leaveRoom();}
        });
'''
if marker not in src:
    raise SystemExit('Cloud listener insertion point changed')
src = src.replace(marker, listeners + marker, 1)

old = 'db.collection("live_rooms").document(roomId).collection("members").document(user.getUid()).set(d);addEvent("join",safeName()+" joined the room");'
new = '''db.collection("live_rooms").document(roomId).collection("members").document(user.getUid()).set(d)
            .addOnSuccessListener(v->addEvent("join",safeName()+" joined the room"))
            .addOnFailureListener(e->toast("Join blocked: private room, room ban, or permission denied"));'''
if old not in src:
    raise SystemExit('Member registration template changed')
src = src.replace(old, new, 1)

old = '    private boolean isOwner(){return cloudRoom?user!=null&&ownerUid!=null&&ownerUid.equals(user.getUid()):displayName.equals(ownerName);}\n'
new = '''    private boolean isOwner(){return cloudRoom?user!=null&&ownerUid!=null&&ownerUid.equals(user.getUid()):displayName.equals(ownerName);}
    private boolean isModerator(){return isOwner() || (cloudRoom && coHost);}
'''
if old not in src:
    raise SystemExit('Owner helper changed')
src = src.replace(old, new, 1)

PARTY.write_text(src, encoding='utf-8')

gradle = BUILD.read_text(encoding='utf-8')
gradle = re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 45; versionName '3.2.1'", gradle)
BUILD.write_text(gradle, encoding='utf-8')

print('Prepared KING Plus v3.2.1 Party roles, moderation and private-room security')
