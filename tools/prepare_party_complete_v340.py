from pathlib import Path
import re

PARTY = Path('app/src/main/java/com/kingplus/social/PartyActivity.java')
BUILD = Path('app/build.gradle')
src = PARTY.read_text(encoding='utf-8')

# v3.4.0 Party parity controls run after v3.2.2 Party + v3.3.0 Games stages.

# Make host header open the host profile/actions panel.
old = '''        TextView hn = tv("Host  •  "+ownerName,15,Color.WHITE,true); hn.setGravity(Gravity.CENTER); host.addView(hn);\n        if (!isOwner()) { followLabel = pill("＋ Follow",PINK,this::toggleFollow); LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(dp(106),dp(36));fp.gravity=Gravity.CENTER_HORIZONTAL;host.addView(followLabel,fp); }\n'''
new = '''        TextView hn = tv("Host  •  "+ownerName,15,Color.WHITE,true); hn.setGravity(Gravity.CENTER); host.addView(hn);\n        host.setOnClickListener(v->hostProfileDialog());\n        if (!isOwner()) { followLabel = pill("＋ Follow",PINK,this::toggleFollow); LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(dp(106),dp(36));fp.gravity=Gravity.CENTER_HORIZONTAL;host.addView(followLabel,fp); }\n'''
if old not in src: raise SystemExit('v3.4.0 host header template changed')
src = src.replace(old,new,1)

# Expand Party More menu with social, ranking, history, request and moderation centers.
pattern = re.compile(r'    private void roomMenu\(\) \{.*?^    private void inviteDialog\(\)', re.S | re.M)
replacement = r'''    private void roomMenu() {
        List<String> items=new ArrayList<>();
        items.add("ℹ Room info"); items.add("👤 Host profile");
        items.add("🎙 Open voice room"); items.add("🎵 Song request"); items.add("😊 Reaction");
        items.add("👥 Members"); items.add("🏆 Room ranking"); items.add("🎁 Gift history"); items.add("🕘 Recent activity");
        items.add("🎮 Games"); items.add("⚔ PK battle");
        items.add("🔗 Share room"); items.add("🔔 Invite by Firebase UID");
        items.add("⚑ Report host"); items.add("🚫 Block host");
        if(isModerator()){
            items.add("📥 Seat request center"); items.add("🚫 Banned users");
            items.add(roomLocked?"🔓 Unlock seats":"🔒 Lock seats");
            items.add(muteAll?"🎤 Unmute all seats":"🔇 Mute all seats");
            items.add("📢 Edit announcement");
        }
        if(isOwner()){
            items.add("👑 Co-host list");
            items.add("✏ Rename room"); items.add("🗂 Change category"); items.add("🎨 Room theme");
            items.add("🎙 Change mic seats");
            items.add(roomPrivate?"🔓 Make room public":"🔐 Make room private");
            items.add(roomHasPassword?"🔑 Change room password":"🔑 Set room password");
            if(roomHasPassword)items.add("🗑 Remove room password");
            items.add("⛔ Close room");
        }
        String[] a=items.toArray(new String[0]);
        new AlertDialog.Builder(this).setTitle(roomName+(coHost?" • Co-host":"")).setItems(a,(d,w)->{
            String x=a[w];
            if(x.contains("Room info"))roomInfoDialog();
            else if(x.contains("Host profile"))hostProfileDialog();
            else if(x.contains("Open voice"))openVoice();
            else if(x.contains("Song request"))karaokeDialog();
            else if(x.contains("Reaction"))reactionDialog();
            else if(x.contains("Members"))membersDialog();
            else if(x.contains("Room ranking"))roomRankingDialog();
            else if(x.contains("Gift history"))giftHistoryDialog();
            else if(x.contains("Recent activity"))recentActivityDialog();
            else if(x.contains("Seat request center"))allSeatRequestsDialog();
            else if(x.contains("Banned users"))banListDialog();
            else if(x.contains("Co-host list"))coHostListDialog();
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
            else if(x.contains("Set room password")||x.contains("Change room password"))setRoomPasswordDialog();
            else if(x.contains("Remove room password"))removeRoomPassword();
            else if(x.contains("Close room"))closeRoom();
        }).show();
    }
    private void inviteDialog()'''
src,count = pattern.subn(replacement,src,count=1)
if count != 1: raise SystemExit('v3.4.0 room menu template changed')

# Member list now acts as a real profile/action list for every participant; moderation items are role-gated.
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
            String uid=ids.get(w); if(!cloudRoom||uid.isEmpty()){toast(labels.get(w));return;}
            memberProfileDialog(uid,labels.get(w));
        }).setNegativeButton("Close",null).show();
    }
    private void seatUserMenu(int no){'''
src,count = pattern.subn(replacement,src,count=1)
if count != 1: raise SystemExit('v3.4.0 members dialog template changed')

# Replace member moderator-only dialog with a full social profile/action dialog.
pattern = re.compile(r'    private void memberUserMenu\(String uid,String name\)\{.*?^    private void kickUser\(String uid,String name\)\{', re.S | re.M)
replacement = r'''    private void memberUserMenu(String uid,String name){ memberProfileDialog(uid,name); }
    private void memberProfileDialog(String uid,String name){
        if(uid==null||uid.isEmpty())return;
        if(user!=null&&uid.equals(user.getUid())){toast("This is you");return;}
        List<String> items=new ArrayList<>();
        items.add("🎁 Send gift"); items.add("＋ Follow"); items.add("⚑ Report"); items.add("🚫 Block locally");
        if(isModerator()&&!uid.equals(ownerUid)){items.add("🚪 Kick from room");items.add("🚫 Ban from room");}
        if(isOwner()&&!uid.equals(ownerUid))items.add("👑 Co-host role");
        String[] a=items.toArray(new String[0]);
        new AlertDialog.Builder(this).setTitle("👤 "+name).setMessage("UID: "+uid).setItems(a,(d,w)->{
            String x=a[w];
            if(x.contains("Gift"))giftDialogFor(uid,name);
            else if(x.contains("Follow"))followUser(uid,name);
            else if(x.contains("Report"))reportUser(uid,name);
            else if(x.contains("Block locally"))blockUserLocally(uid,name);
            else if(x.contains("Kick"))kickUser(uid,name);
            else if(x.contains("Ban"))banUser(uid,name);
            else if(x.contains("Co-host"))coHostDialog(uid,name);
        }).setNegativeButton("Close",null).show();
    }
    private void kickUser(String uid,String name){'''
src,count = pattern.subn(replacement,src,count=1)
if count != 1: raise SystemExit('v3.4.0 member profile template changed')

# Add Party parity helpers before followUser.
marker = '    private void followUser(String uid,String name){'
helpers = r'''    private void blockUserLocally(String uid,String name){
        Set<String>b=new HashSet<>(prefs.getStringSet("blocked",new HashSet<>()));b.add(uid);prefs.edit().putStringSet("blocked",b).apply();toast(name+" blocked locally");
    }
    private String memberNameForUid(String uid){
        if(uid==null)return "User"; if(uid.equals(ownerUid))return ownerName==null?"Host":ownerName;
        for(Map.Entry<String,String> e:memberUids.entrySet())if(uid.equals(e.getValue()))return e.getKey();
        return uid.length()>8?"User "+uid.substring(0,8):uid;
    }
    private void hostProfileDialog(){
        if(ownerName==null)ownerName="Host";
        String details="Host: "+ownerName+"\nRoom: "+roomName+"\nRoom ID: "+shortId();
        if(ownerUid!=null&&!ownerUid.isEmpty())details+="\nUID: "+ownerUid;
        List<String> items=new ArrayList<>();
        if(!isOwner()){items.add("🎁 Send gift");items.add("＋ Follow / Unfollow");items.add("⚑ Report host");items.add("🚫 Block host");}
        items.add("🔗 Share room");
        String[] a=items.toArray(new String[0]);
        new AlertDialog.Builder(this).setTitle("👑 Host profile").setMessage(details).setItems(a,(d,w)->{
            String x=a[w];if(x.contains("Gift"))giftDialog();else if(x.contains("Follow"))toggleFollow();else if(x.contains("Report"))reportHost();else if(x.contains("Block"))blockHost();else shareRoom();
        }).setNegativeButton("Close",null).show();
    }
    private void roomInfoDialog(){
        String privacy=roomPrivate?(roomHasPassword?"Private • password":"Private • invite only"):"Public";
        String role=isOwner()?"Host":(coHost?"Co-host":"Member");
        String info="Room: "+roomName+"\nID: "+shortId()+"\nCategory: "+roomCategory+"\nTheme: "+roomTheme+
            "\nMic seats: "+maxSeats+"\nAccess: "+privacy+"\nRole: "+role+"\nSeat lock: "+(roomLocked?"ON":"OFF")+
            "\nMute all: "+(muteAll?"ON":"OFF")+(currentSong==null||currentSong.isEmpty()?"":"\nSong: "+currentSong);
        new AlertDialog.Builder(this).setTitle("ℹ Party room info").setMessage(info).setPositiveButton("OK",null).show();
    }
    private void recentActivityDialog(){
        if(!cloudRoom||db==null){new AlertDialog.Builder(this).setTitle("🕘 Recent activity").setMessage("Local test room activity is shown directly on the Party page.").setPositiveButton("OK",null).show();return;}
        db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(40).get()
            .addOnSuccessListener(snap->{List<String> rows=new ArrayList<>();for(DocumentSnapshot d:snap.getDocuments())rows.add(str(d,"text","Room activity"));showRows("🕘 Recent activity",rows,"No room activity yet");})
            .addOnFailureListener(e->toast(msg(e)));
    }
    private void giftHistoryDialog(){
        if(!cloudRoom||db==null){new AlertDialog.Builder(this).setTitle("🎁 Gift history").setMessage("Gift history for local rooms stays in test activity only.").setPositiveButton("OK",null).show();return;}
        db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(100).get()
            .addOnSuccessListener(snap->{List<String> rows=new ArrayList<>();for(DocumentSnapshot d:snap.getDocuments())if("gift".equals(d.getString("type")))rows.add(str(d,"text","Gift sent"));showRows("🎁 Gift history",rows,"No gifts sent in this room yet");})
            .addOnFailureListener(e->toast(msg(e)));
    }
    private void roomRankingDialog(){
        if(!cloudRoom||db==null){new AlertDialog.Builder(this).setTitle("🏆 Room ranking").setMessage("Live gift ranking is available in Firebase rooms.").setPositiveButton("OK",null).show();return;}
        db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(150).get()
            .addOnSuccessListener(snap->{Map<String,Integer>score=new HashMap<>();for(DocumentSnapshot d:snap.getDocuments())if("gift".equals(d.getString("type"))){String n=str(d,"actorName","User");score.put(n,score.containsKey(n)?score.get(n)+1:1);}List<Map.Entry<String,Integer>>list=new ArrayList<>(score.entrySet());java.util.Collections.sort(list,(a,b)->b.getValue()-a.getValue());List<String>rows=new ArrayList<>();int rank=1;for(Map.Entry<String,Integer>e:list){rows.add((rank==1?"🥇 ":rank==2?"🥈 ":rank==3?"🥉 ":rank+". ")+e.getKey()+" • "+e.getValue()+" gifts");rank++;if(rank>20)break;}showRows("🏆 Room gift ranking",rows,"No gift ranking yet");})
            .addOnFailureListener(e->toast(msg(e)));
    }
    private void allSeatRequestsDialog(){
        if(!isModerator()||!cloudRoom||db==null){toast("Host/co-host only");return;}
        db.collection("live_rooms").document(roomId).collection("seat_requests").get().addOnSuccessListener(snap->{
            if(snap.isEmpty()){toast("No pending seat requests");return;}List<DocumentSnapshot>docs=snap.getDocuments();List<String>rows=new ArrayList<>();
            for(DocumentSnapshot d:docs){Long n=d.getLong("seatNo");rows.add(str(d,"name","User")+" • Seat "+(n==null?"?":n));}
            new AlertDialog.Builder(this).setTitle("📥 Seat requests").setItems(rows.toArray(new String[0]),(x,w)->{DocumentSnapshot req=docs.get(w);Long n=req.getLong("seatNo");if(n!=null)seatRequestDecision(req,n.intValue());}).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast(msg(e)));
    }
    private void coHostListDialog(){
        if(!isOwner()||!cloudRoom||db==null){toast("Host only");return;}
        db.collection("live_rooms").document(roomId).collection("roles").get().addOnSuccessListener(snap->{
            List<DocumentSnapshot>docs=new ArrayList<>();List<String>rows=new ArrayList<>();
            for(DocumentSnapshot d:snap.getDocuments())if("cohost".equals(d.getString("role"))){docs.add(d);rows.add("👑 "+memberNameForUid(d.getId()));}
            if(rows.isEmpty()){toast("No co-hosts assigned");return;}
            new AlertDialog.Builder(this).setTitle("👑 Co-host list").setItems(rows.toArray(new String[0]),(x,w)->{DocumentSnapshot d=docs.get(w);String uid=d.getId();String name=memberNameForUid(uid);new AlertDialog.Builder(this).setTitle(name).setMessage("Remove co-host role?").setPositiveButton("Remove",(a,b)->d.getReference().delete().addOnSuccessListener(v->toast("Co-host removed")).addOnFailureListener(e->toast(msg(e)))).setNegativeButton("Cancel",null).show();}).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast(msg(e)));
    }
    private void banListDialog(){
        if(!isModerator()||!cloudRoom||db==null){toast("Host/co-host only");return;}
        db.collection("live_rooms").document(roomId).collection("room_bans").get().addOnSuccessListener(snap->{
            List<DocumentSnapshot>docs=new ArrayList<>();List<String>rows=new ArrayList<>();
            for(DocumentSnapshot d:snap.getDocuments())if(Boolean.TRUE.equals(d.getBoolean("active"))){docs.add(d);rows.add("🚫 "+memberNameForUid(d.getId()));}
            if(rows.isEmpty()){toast("No banned users");return;}
            new AlertDialog.Builder(this).setTitle("🚫 Banned users").setItems(rows.toArray(new String[0]),(x,w)->{DocumentSnapshot d=docs.get(w);String name=memberNameForUid(d.getId());new AlertDialog.Builder(this).setTitle(name).setMessage("Allow this user to join again?").setPositiveButton("Unban",(a,b)->d.getReference().delete().addOnSuccessListener(v->toast(name+" unbanned")).addOnFailureListener(e->toast(msg(e)))).setNegativeButton("Cancel",null).show();}).setNegativeButton("Close",null).show();
        }).addOnFailureListener(e->toast(msg(e)));
    }
    private void showRows(String title,List<String> rows,String empty){
        if(rows==null||rows.isEmpty()){new AlertDialog.Builder(this).setTitle(title).setMessage(empty).setPositiveButton("OK",null).show();return;}
        new AlertDialog.Builder(this).setTitle(title).setItems(rows.toArray(new String[0]),null).setNegativeButton("Close",null).show();
    }

'''
if marker not in src: raise SystemExit('v3.4.0 helper insertion point changed')
src = src.replace(marker,helpers+marker,1)

PARTY.write_text(src,encoding='utf-8')

gradle=BUILD.read_text(encoding='utf-8')
gradle=re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'","versionCode 48; versionName '3.4.0'",gradle)
BUILD.write_text(gradle,encoding='utf-8')
print('Prepared KING Plus v3.4.0 Party complete controls')