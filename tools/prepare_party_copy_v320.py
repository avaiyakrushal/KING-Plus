from pathlib import Path
import re

PARTY = Path('app/src/main/java/com/kingplus/social/PartyActivity.java')
BUILD = Path('app/build.gradle')
src = PARTY.read_text(encoding='utf-8')

# Extra Party state.
needle = '    private int localCoins = 2500;\n'
extra = '''    private int localCoins = 2500;\n    private String roomCategory = "Hot";\n    private String roomTheme = "Classic";\n    private String currentSong = "";\n    private String lobbyFilter = "";\n    private int maxSeats = 12;\n    private final List<String> memberNames = new ArrayList<>();\n    private final Map<String,String> memberUids = new HashMap<>();\n    private final Set<Integer> localRemovedSeats = new HashSet<>();\n'''
if needle not in src: raise SystemExit('Party state insertion point changed')
src = src.replace(needle, extra, 1)

# Search bar below Party categories.
needle = '        root.addView(tabs);\n\n        ScrollView scroll = new ScrollView(this);'
replacement = '''        root.addView(tabs);\n\n        LinearLayout searchRow = new LinearLayout(this); searchRow.setGravity(Gravity.CENTER_VERTICAL); searchRow.setPadding(dp(12),0,dp(12),dp(8));\n        EditText search = new EditText(this); search.setHint("Search room, host or ID"); search.setSingleLine(true); search.setText(lobbyFilter); search.setTextSize(13); search.setBackground(bg(Color.WHITE,18)); search.setPadding(dp(12),0,dp(12),0);\n        searchRow.addView(search,new LinearLayout.LayoutParams(0,dp(42),1));\n        TextView searchBtn = pill("⌕",PURPLE,()->{ lobbyFilter=search.getText().toString().trim(); renderLobby(selected); });\n        LinearLayout.LayoutParams sbp=new LinearLayout.LayoutParams(dp(50),dp(42)); sbp.setMargins(dp(6),0,0,0); searchRow.addView(searchBtn,sbp);\n        if(!lobbyFilter.isEmpty()){ TextView clear=pill("×",0xffaaa2b4,()->{lobbyFilter="";renderLobby(selected);}); LinearLayout.LayoutParams cbp=new LinearLayout.LayoutParams(dp(46),dp(42));cbp.setMargins(dp(5),0,0,0);searchRow.addView(clear,cbp); }\n        root.addView(searchRow);\n\n        ScrollView scroll = new ScrollView(this);'''
if needle not in src: raise SystemExit('Party search insertion point changed')
src = src.replace(needle, replacement, 1)

# Filter cloud room cards and display category/lock-style metadata.
old = '''                            String name = str(doc,"name","Live Party");\n                            String host = str(doc,"ownerName","KING Host");\n                            String id = doc.getId();\n                            addRoomCard(cloudList,"🔥 " + name,"Host: " + host + "   •   LIVE",() -> openCloudRoom(id,name,doc.getString("ownerUid"),host));\n'''
new = '''                            String name = str(doc,"name","Live Party");\n                            String host = str(doc,"ownerName","KING Host");\n                            String id = doc.getId();\n                            String category = str(doc,"category","Hot");\n                            String q = lobbyFilter.toLowerCase();\n                            if(!categoryMatches(selected,category)) continue;\n                            if(!q.isEmpty() && !(name.toLowerCase().contains(q) || host.toLowerCase().contains(q) || category.toLowerCase().contains(q) || shortId(id).contains(q))) continue;\n                            addRoomCard(cloudList,"🔥 ["+category+"] " + name,"Host: " + host + "   •   ID "+shortId(id)+"   •   LIVE",() -> openCloudRoom(id,name,doc.getString("ownerUid"),host));\n'''
if old not in src: raise SystemExit('Cloud room list template changed')
src = src.replace(old,new,1)

# Replace fixed local lobby cards with category-aware samples.
old = '''        addRoomGridRow(page,\n            "💜 Sweet Girls","Priya • 358","Sweet Girls","Priya",\n            "🎵 Music & Friends","DJ Max • 212","Music & Friends","DJ Max");\n        addRoomGridRow(page,\n            "👑 KING Lounge","KING Host • 186","KING Lounge","KING Host",\n            "🎮 Game Talk","Alex • 96","Game Talk","Alex");\n        addRoomGridRow(page,\n            "💞 Make Friends","Riya • 154","Make Friends","Riya",\n            "🎤 Singing Club","Neha • 128","Singing Club","Neha");\n'''
new = '        addLobbySamples(selected);\n'
if old not in src: raise SystemExit('Local lobby samples changed')
src = src.replace(old,new,1)

# Category-aware create flow.
pattern = re.compile(r'    private void createRoomDialog\(\) \{.*?    private void createCloudRoom\(String name\) \{',re.S)
replacement = r'''    private void createRoomDialog() {
        final EditText e = new EditText(this); e.setHint("Party room name"); e.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("Create Party Room").setView(e).setNegativeButton("Cancel",null)
            .setPositiveButton("Next",(d,w) -> {
                String name = e.getText().toString().trim(); if (name.length()<2) { toast("Enter a room name"); return; }
                chooseCreateCategory(name);
            }).show();
    }
    private void chooseCreateCategory(String name){
        String[] cats={"Chat","Date","Music","Sing","Game","Event","Birthday","Wedding"};
        new AlertDialog.Builder(this).setTitle("Choose room type").setItems(cats,(d,w)->{
            String category=cats[w];
            if(user!=null&&db!=null) createCloudRoom(name,category); else openLocalRoom(name,displayName,category);
        }).setNegativeButton("Cancel",null).show();
    }
    private void createCloudRoom(String name,String category) {'''
src,count = pattern.subn(replacement,src,count=1)
if count != 1: raise SystemExit('Create room method template changed')

old = '''        Map<String,Object> r = new HashMap<>(); r.put("name",name); r.put("ownerUid",user.getUid()); r.put("ownerName",safeName());\n        r.put("announcement",announcement); r.put("locked",false); r.put("muteAll",false); r.put("closed",false); r.put("createdAt",FieldValue.serverTimestamp()); r.put("updatedAt",FieldValue.serverTimestamp());\n'''
new = '''        Map<String,Object> r = new HashMap<>(); r.put("name",name); r.put("ownerUid",user.getUid()); r.put("ownerName",safeName());\n        r.put("category",category); r.put("theme","Classic"); r.put("maxSeats",12);\n        r.put("announcement",announcement); r.put("locked",false); r.put("muteAll",false); r.put("closed",false); r.put("createdAt",FieldValue.serverTimestamp()); r.put("updatedAt",FieldValue.serverTimestamp());\n'''
if old not in src: raise SystemExit('Create room fields changed')
src = src.replace(old,new,1)

# Preserve category for local rooms and reset Party state when opening rooms.
old = '''    private void openCloudRoom(String id,String name,String hostUid,String hostName) {\n        roomId=id; roomName=name; ownerUid=hostUid; ownerName=hostName; cloudRoom=true; mySeat=-1; micOn=false;\n        registerMember(); renderParty();\n    }\n    private void openLocalRoom(String name,String hostName) {\n        roomName=name; roomId="local_"+Math.abs(name.hashCode()); ownerName=hostName; ownerUid=null; cloudRoom=false;\n        mySeat=prefs.getInt("seat_"+roomId,-1); micOn=prefs.getBoolean("mic_"+roomId,false); roomLocked=prefs.getBoolean("locked_"+roomId,false);\n        muteAll=prefs.getBoolean("mute_"+roomId,false); announcement=prefs.getString("notice_"+roomId,"Welcome to KING Plus • Be friendly and have fun"); renderParty();\n    }\n'''
new = '''    private void openCloudRoom(String id,String name,String hostUid,String hostName) {\n        roomId=id; roomName=name; ownerUid=hostUid; ownerName=hostName; cloudRoom=true; mySeat=-1; micOn=false; localRemovedSeats.clear();\n        registerMember(); renderParty();\n    }\n    private void openLocalRoom(String name,String hostName) { openLocalRoom(name,hostName,"Chat"); }\n    private void openLocalRoom(String name,String hostName,String category) {\n        roomName=name; roomCategory=category; roomTheme=prefs.getString("theme_"+name,"Classic"); maxSeats=prefs.getInt("maxSeats_"+name,12); roomId="local_"+Math.abs(name.hashCode()); ownerName=hostName; ownerUid=null; cloudRoom=false; localRemovedSeats.clear();\n        mySeat=prefs.getInt("seat_"+roomId,-1); micOn=prefs.getBoolean("mic_"+roomId,false); roomLocked=prefs.getBoolean("locked_"+roomId,false);\n        muteAll=prefs.getBoolean("mute_"+roomId,false); announcement=prefs.getString("notice_"+roomId,"Welcome to KING Plus • Be friendly and have fun"); renderParty();\n    }\n'''
if old not in src: raise SystemExit('Open room methods changed')
src = src.replace(old,new,1)

# Theme-aware room canvas and richer room metadata.
src = src.replace('scroll.setBackgroundColor(BG2);','scroll.setBackgroundColor(themeBackground());',1)
src = src.replace('TextView rn = tv(roomName,19,Color.WHITE,true); info.addView(rn); TextView id = tv("ID: "+shortId()+"   •   "+(cloudRoom?"LIVE":"TEST"),11,MUTED,false); info.addView(id);',
'''TextView rn = tv(roomName,19,Color.WHITE,true); info.addView(rn); TextView id = tv("ID: "+shortId()+"   •   "+roomCategory+"   •   "+roomTheme+"   •   "+(cloudRoom?"LIVE":"TEST"),11,MUTED,false); info.addView(id);''',1)
src = src.replace('viewerLabel = pill("👥 1",0xff49315f,null);','viewerLabel = pill("👥 1",0xff49315f,this::membersDialog);',1)

# Gift recipient selector and second tools row.
old = '''        addQuick(quick,"🎙\\nVoice",this::openVoice); addQuick(quick,"🔊\\nSound",this::toggleSound); addQuick(quick,"💬\\nChat",() -> toast("Room chat is below")); addQuick(quick,"🎁\\nGift",this::giftDialog); addQuick(quick,"⋯\\nMore",this::roomMenu);\n        page.addView(quick,new LinearLayout.LayoutParams(-1,dp(64)));\n'''
new = '''        addQuick(quick,"🎙\\nVoice",this::openVoice); addQuick(quick,"🔊\\nSound",this::toggleSound); addQuick(quick,"💬\\nChat",() -> toast("Room chat is below")); addQuick(quick,"🎁\\nGift",this::giftRecipientDialog); addQuick(quick,"⋯\\nMore",this::roomMenu);\n        page.addView(quick,new LinearLayout.LayoutParams(-1,dp(64)));\n        LinearLayout tools = new LinearLayout(this); tools.setGravity(Gravity.CENTER); tools.setPadding(0,0,0,dp(5));\n        addQuick(tools,"🎵\\nSing",this::karaokeDialog); addQuick(tools,"😊\\nReact",this::reactionDialog); addQuick(tools,"🎮\\nGames",this::openGames); addQuick(tools,"👥\\nMembers",this::membersDialog);\n        page.addView(tools,new LinearLayout.LayoutParams(-1,dp(62)));\n'''
if old not in src: raise SystemExit('Party quick tools changed')
src = src.replace(old,new,1)

# Dynamic 8/12 mic seats.
pattern = re.compile(r'    private void rebuildSeats\(\) \{.*?    private void fillLocalSeats\(\) \{',re.S)
replacement = r'''    private void rebuildSeats() {
        if (seatsBox == null) return; seatsBox.removeAllViews();
        int rows=(maxSeats+3)/4;
        for (int r=0;r<rows;r++) {
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);
            for (int c=0;c<4;c++) {
                int no=r*4+c+1; if(no>maxSeats) break;
                LinearLayout seat=new LinearLayout(this);seat.setOrientation(LinearLayout.VERTICAL);seat.setGravity(Gravity.CENTER);
                String n=seatNames.get(no); boolean mine=cloudRoom?user!=null&&user.getUid().equals(seatUids.get(no)):no==mySeat;
                TextView av=tv(mine?"👑":(n==null?"＋":"●"),mine?27:24,Color.WHITE,true);av.setGravity(Gravity.CENTER);av.setBackground(bg(mine?PURPLE:0xff4a3768,50));seat.addView(av,new LinearLayout.LayoutParams(dp(54),dp(54)));
                String label=n==null?"Seat "+no:n; if(Boolean.FALSE.equals(seatMics.get(no))) label="🔇 "+label; TextView lab=tv(label,10,mine?Color.WHITE:MUTED,false);lab.setGravity(Gravity.CENTER);seat.addView(lab,new LinearLayout.LayoutParams(-1,dp(26)));
                final int seatNo=no;seat.setOnClickListener(v->seatAction(seatNo));row.addView(seat,new LinearLayout.LayoutParams(0,dp(84),1));
            }
            seatsBox.addView(row,new LinearLayout.LayoutParams(-1,dp(84)));
        }
    }
    private void fillLocalSeats() {'''
src,count = pattern.subn(replacement,src,count=1)
if count != 1: raise SystemExit('Mic seat layout changed')

old = '        for(int i=0;i<samples.length;i++){int no=i+1;if(no==mySeat)continue;seatNames.put(no,samples[i]);seatMics.put(no,i%3!=0);}\n'
new = '        for(int i=0;i<samples.length;i++){int no=i+1;if(no>maxSeats||no==mySeat||localRemovedSeats.contains(no))continue;seatNames.put(no,samples[i]);seatMics.put(no,i%3!=0);}\n'
if old not in src: raise SystemExit('Local seats seed changed')
src = src.replace(old,new,1)

# Occupied seat opens user/host controls instead of dead toast.
old = '''            if (no==mySeat) { mySeat=-1;micOn=false;prefs.edit().remove("seat_"+roomId).putBoolean("mic_"+roomId,false).apply(); }\n            else if (seatNames.get(no)!=null) { toast("Seat is occupied"); return; }\n            else { mySeat=no;prefs.edit().putInt("seat_"+roomId,no).apply(); }\n'''
new = '''            if (no==mySeat) { mySeat=-1;micOn=false;prefs.edit().remove("seat_"+roomId).putBoolean("mic_"+roomId,false).apply(); }\n            else if (seatNames.get(no)!=null) { seatUserMenu(no); return; }\n            else { mySeat=no;prefs.edit().putInt("seat_"+roomId,no).apply(); }\n'''
if old not in src: raise SystemExit('Local seat action changed')
src = src.replace(old,new,1)
src = src.replace('if(existing!=null&&!existing.equals(user.getUid())){toast("Seat is occupied");return;}','if(existing!=null&&!existing.equals(user.getUid())){seatUserMenu(no);return;}',1)

# Replace gift methods with recipient-capable gift flow.
pattern = re.compile(r'    private void giftDialog\(\) \{.*?    private void shareRoom\(\)\{',re.S)
replacement = r'''    private void giftRecipientDialog(){
        List<String> labels=new ArrayList<>(); List<String> ids=new ArrayList<>();
        labels.add("👑 Host • "+ownerName); ids.add(ownerUid==null?"":ownerUid);
        for(int no=1;no<=maxSeats;no++){String n=seatNames.get(no);if(n==null)continue;String uid=seatUids.get(no);if(n.equals(displayName))continue;labels.add("🎙 Seat "+no+" • "+n);ids.add(uid==null?"":uid);}
        new AlertDialog.Builder(this).setTitle("Send gift to").setItems(labels.toArray(new String[0]),(d,w)->giftDialogFor(ids.get(w),labels.get(w))).setNegativeButton("Close",null).show();
    }
    private void giftDialog(){ giftDialogFor(ownerUid==null?"":ownerUid,"Host • "+ownerName); }
    private void giftDialogFor(String targetUid,String targetLabel) {
        String[] gifts={"🌹 Rose • 10","❤️ Heart • 50","🍫 Chocolate • 100","🚗 Car • 500","👑 Crown • 1000","🏰 Castle • 5000","🎆 Firework • 10000"};
        int[] costs={10,50,100,500,1000,5000,10000};String[] names={"Rose","Heart","Chocolate","Car","Crown","Castle","Firework"};
        new AlertDialog.Builder(this).setTitle("🎁 "+targetLabel+" • "+localCoins+" coins").setItems(gifts,(d,w)->sendGiftTo(targetUid,targetLabel,names[w],costs[w])).setNegativeButton("Close",null).show();
    }
    private void sendGift(String gift,int cost) { sendGiftTo(ownerUid==null?"":ownerUid,ownerName,gift,cost); }
    private void sendGiftTo(String targetUid,String targetName,String gift,int cost) {
        if(cloudRoom&&user!=null&&targetUid!=null&&!targetUid.isEmpty()&&!targetUid.equals(user.getUid())){
            CloudBackend.sendGift(targetUid,gift,cost,(ok,message)->runOnUiThread(()->{toast(message);if(ok)addEvent("gift",safeName()+" sent "+gift+" to "+targetName+" 🎁 x1");}));return;
        }
        if(localCoins<cost){toast("Not enough TEST coins");return;}localCoins-=cost;prefs.edit().putInt("coins",localCoins).apply();addFeed(displayName+" sent "+gift+" to "+targetName+" 🎁 x1");toast("Gift sent • TEST balance "+localCoins);
    }

    private void shareRoom(){'''
src,count = pattern.subn(replacement,src,count=1)
if count != 1: raise SystemExit('Gift methods template changed')

# Rich room More menu.
old = '        List<String> items=new ArrayList<>();items.add("🎙 Open voice room");items.add("⚔ PK battle");items.add("🔗 Share room");items.add("🔔 Invite by Firebase UID");items.add("⚑ Report host");items.add("🚫 Block host");if(isOwner()){items.add(roomLocked?"🔓 Unlock seats":"🔒 Lock seats");items.add(muteAll?"🎤 Unmute all seats":"🔇 Mute all seats");items.add("📢 Edit announcement");items.add("⛔ Close room");}\n'
new = '        List<String> items=new ArrayList<>();items.add("🎙 Open voice room");items.add("🎵 Song request");items.add("😊 Reaction");items.add("👥 Members");items.add("🎮 Games");items.add("⚔ PK battle");items.add("🔗 Share room");items.add("🔔 Invite by Firebase UID");items.add("⚑ Report host");items.add("🚫 Block host");if(isOwner()){items.add("✏ Rename room");items.add("🏷 Change category");items.add("🎨 Room theme");items.add(maxSeats==12?"🎙 Use 8 mic seats":"🎙 Use 12 mic seats");items.add(roomLocked?"🔓 Unlock seats":"🔒 Lock seats");items.add(muteAll?"🎤 Unmute all seats":"🔇 Mute all seats");items.add("📢 Edit announcement");items.add("⛔ Close room");}\n'
if old not in src: raise SystemExit('Room menu items changed')
src = src.replace(old,new,1)
old = '            String x=a[w];if(x.contains("Open voice"))openVoice();else if(x.contains("PK battle"))pkBattle();else if(x.contains("Share"))shareRoom();else if(x.contains("Invite"))inviteDialog();else if(x.contains("Report"))reportHost();else if(x.contains("Block"))blockHost();else if(x.contains("Lock")||x.contains("Unlock"))setRoomFlag("locked",!roomLocked);else if(x.contains("Mute all")||x.contains("Unmute"))setRoomFlag("muteAll",!muteAll);else if(x.contains("announcement"))editAnnouncement();else if(x.contains("Close room"))closeRoom();\n'
new = '            String x=a[w];if(x.contains("Open voice"))openVoice();else if(x.contains("Song request"))karaokeDialog();else if(x.contains("Reaction"))reactionDialog();else if(x.contains("Members"))membersDialog();else if(x.contains("Games"))openGames();else if(x.contains("PK battle"))pkBattle();else if(x.contains("Share"))shareRoom();else if(x.contains("Invite"))inviteDialog();else if(x.contains("Report"))reportHost();else if(x.contains("Block"))blockHost();else if(x.contains("Rename room"))renameRoomDialog();else if(x.contains("Change category"))changeCategoryDialog();else if(x.contains("Room theme"))changeThemeDialog();else if(x.contains("mic seats"))changeSeatCount();else if(x.contains("Lock")||x.contains("Unlock"))setRoomFlag("locked",!roomLocked);else if(x.contains("Mute all")||x.contains("Unmute"))setRoomFlag("muteAll",!muteAll);else if(x.contains("announcement"))editAnnouncement();else if(x.contains("Close room"))closeRoom();\n'
if old not in src: raise SystemExit('Room menu dispatcher changed')
src = src.replace(old,new,1)

# Cloud listener now syncs Party settings/member names.
old = '        roomListener=room.addSnapshotListener((doc,e)->{if(e!=null||doc==null||!doc.exists())return;announcement=str(doc,"announcement",announcement);roomLocked=Boolean.TRUE.equals(doc.getBoolean("locked"));muteAll=Boolean.TRUE.equals(doc.getBoolean("muteAll"));if(announcementLabel!=null)announcementLabel.setText((roomLocked?"🔒  ":"📢  ")+announcement);if(Boolean.TRUE.equals(doc.getBoolean("closed"))&&!isOwner()){toast("Room was closed by host");leaveRoom();}});\n'
new = '        roomListener=room.addSnapshotListener((doc,e)->{if(e!=null||doc==null||!doc.exists())return;announcement=str(doc,"announcement",announcement);roomCategory=str(doc,"category",roomCategory);roomTheme=str(doc,"theme",roomTheme);Long ms=doc.getLong("maxSeats");if(ms!=null){maxSeats=(int)Math.max(4,Math.min(12,ms));rebuildSeats();}roomLocked=Boolean.TRUE.equals(doc.getBoolean("locked"));muteAll=Boolean.TRUE.equals(doc.getBoolean("muteAll"));if(announcementLabel!=null)announcementLabel.setText((roomLocked?"🔒  ":"📢  ")+announcement);if(Boolean.TRUE.equals(doc.getBoolean("closed"))&&!isOwner()){toast("Room was closed by host");leaveRoom();}});\n'
if old not in src: raise SystemExit('Cloud room listener changed')
src = src.replace(old,new,1)
old = '        membersListener=room.collection("members").addSnapshotListener((snap,e)->{if(viewerLabel!=null&&snap!=null)viewerLabel.setText("👥 "+snap.size());});\n'
new = '        membersListener=room.collection("members").addSnapshotListener((snap,e)->{if(snap==null)return;memberNames.clear();memberUids.clear();for(DocumentSnapshot m:snap.getDocuments()){String n=str(m,"name","User");memberNames.add(n);memberUids.put(n,m.getString("uid"));}if(viewerLabel!=null)viewerLabel.setText("👥 "+snap.size());});\n'
if old not in src: raise SystemExit('Members listener changed')
src = src.replace(old,new,1)

# Add complete Party helpers before toggleFollow.
marker = '    private void toggleFollow(){'
helpers = r'''    private boolean categoryMatches(String selected,String category){
        if(selected==null||"Hot".equalsIgnoreCase(selected))return true;
        if("Music".equalsIgnoreCase(selected))return "Music".equalsIgnoreCase(category)||"Sing".equalsIgnoreCase(category);
        if("Event".equalsIgnoreCase(selected))return "Event".equalsIgnoreCase(category)||"Birthday".equalsIgnoreCase(category)||"Wedding".equalsIgnoreCase(category);
        return selected.equalsIgnoreCase(category);
    }
    private void addLobbySamples(String selected){
        if(categoryMatches(selected,"Date")) addRoomGridRow(page,"💞 It’s a Date","Riya • 298","It’s a Date","Riya","💗 Make Friends","Pooja • 154","Make Friends","Pooja");
        if(categoryMatches(selected,"Music")) addRoomGridRow(page,"🎤 Sing-along","Neha • 276","Sing-along","Neha","🎵 Music & Chat","DJ Max • 134","Music & Chat","DJ Max");
        if(categoryMatches(selected,"Game")) addRoomGridRow(page,"🎮 Draw & Guess","Alex • 166","Draw & Guess","Alex","🎲 Game Club","KING Host • 96","Game Club","KING Host");
        if(categoryMatches(selected,"Event")) addRoomGridRow(page,"🎂 Birthday Party","Anjali • 188","Birthday Party","Anjali","💍 Wedding Party","Simran • 142","Wedding Party","Simran");
        if("Hot".equalsIgnoreCase(selected)) addRoomGridRow(page,"⭐ Official Welcome","KING Team • LIVE","Official Welcome","KING Team","👑 KING Lounge","KING Host • 186","KING Lounge","KING Host");
    }
    private int themeBackground(){
        if("KTV".equals(roomTheme))return 0xff101d46;
        if("Love".equals(roomTheme))return 0xff3d1830;
        if("Game".equals(roomTheme))return 0xff16382f;
        if("Royal".equals(roomTheme))return 0xff28194d;
        return BG2;
    }
    private void membersDialog(){
        List<String> items=new ArrayList<>();
        if(cloudRoom){ if(memberNames.isEmpty())items.add("No members loaded"); else items.addAll(memberNames); }
        else { items.add(ownerName+" • Host"); for(int i=1;i<=maxSeats;i++){String n=seatNames.get(i);if(n!=null&&!items.contains(n))items.add(n);} }
        new AlertDialog.Builder(this).setTitle("👥 Room members").setItems(items.toArray(new String[0]),null).setNegativeButton("Close",null).show();
    }
    private void seatUserMenu(int no){
        String name=seatNames.get(no); if(name==null)return; String uid=seatUids.get(no);
        List<String> items=new ArrayList<>(); items.add("🎁 Send gift"); items.add("＋ Follow"); items.add("⚑ Report"); if(isOwner())items.add(Boolean.FALSE.equals(seatMics.get(no))?"🎤 Unmute seat":"🔇 Mute seat"); if(isOwner())items.add("⬇ Remove from seat");
        String[] a=items.toArray(new String[0]); new AlertDialog.Builder(this).setTitle(name+" • Seat "+no).setItems(a,(d,w)->{
            String x=a[w]; if(x.contains("Gift"))giftDialogFor(uid==null?"":uid,name); else if(x.contains("Follow"))followUser(uid,name); else if(x.contains("Report"))reportUser(uid,name); else if(x.contains("Mute seat")||x.contains("Unmute seat"))hostToggleSeat(no); else if(x.contains("Remove"))hostRemoveSeat(no);
        }).setNegativeButton("Close",null).show();
    }
    private void followUser(String uid,String name){
        if(user==null||db==null||uid==null||uid.isEmpty()){toast("Live Firebase user required");return;} if(uid.equals(user.getUid())){toast("This is you");return;}
        String id=user.getUid()+"_"+uid;DocumentReference ref=db.collection("follows").document(id);Map<String,Object>d=new HashMap<>();d.put("followerUid",user.getUid());d.put("targetUid",uid);d.put("followerName",safeName());d.put("createdAt",FieldValue.serverTimestamp());ref.set(d).addOnSuccessListener(v->toast("Following "+name)).addOnFailureListener(e->toast(msg(e)));
    }
    private void reportUser(String uid,String name){String target=(uid==null||uid.isEmpty())?name:uid;CloudSync.submitReport(this,target,"Party room user report",(ok,m)->runOnUiThread(()->toast(m)));}
    private void hostToggleSeat(int no){
        if(!isOwner()){toast("Host only");return;} boolean next=!Boolean.TRUE.equals(seatMics.get(no));
        if(cloudRoom&&db!=null)db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(no)).update("micOn",next).addOnFailureListener(e->toast(msg(e)));
        else{seatMics.put(no,next);rebuildSeats();}
    }
    private void hostRemoveSeat(int no){
        if(!isOwner()){toast("Host only");return;}
        if(cloudRoom&&db!=null)db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(no)).delete().addOnSuccessListener(v->addEvent("seat",safeName()+" removed a mic seat")).addOnFailureListener(e->toast(msg(e)));
        else{localRemovedSeats.add(no);seatNames.remove(no);seatMics.remove(no);rebuildSeats();}
    }
    private void reactionDialog(){
        String[] r={"❤️ Love","😂 Haha","👏 Clap","🔥 Fire","🎉 Party","😍 Wow"};
        new AlertDialog.Builder(this).setTitle("Room reaction").setItems(r,(d,w)->{String emoji=r[w].substring(0,r[w].indexOf(' '));addEvent("reaction",safeName()+" reacted "+emoji);}).setNegativeButton("Close",null).show();
    }
    private void karaokeDialog(){
        String[] songs={"🎵 Romantic Hits","🎤 Bollywood Mix","🎶 Party Beats","💜 Love Songs","🔥 Trending Music","＋ Custom song title"};
        new AlertDialog.Builder(this).setTitle("Sing / Song request").setItems(songs,(d,w)->{
            if(w==songs.length-1){final EditText e=new EditText(this);e.setHint("Song title");new AlertDialog.Builder(this).setTitle("Request a song").setView(e).setPositiveButton("Request",(x,y)->requestSong(e.getText().toString().trim())).setNegativeButton("Cancel",null).show();}
            else requestSong(songs[w].substring(songs[w].indexOf(' ')+1));
        }).setNegativeButton("Close",null).show();
    }
    private void requestSong(String song){if(song==null||song.isEmpty())return;currentSong=song;addEvent("song",safeName()+" requested 🎵 "+song);toast("Song request added: "+song);}
    private void openGames(){Intent i=new Intent(this,MainActivity.class);i.putExtra("openTab",1);startActivity(i);finish();}
    private void renameRoomDialog(){if(!isOwner())return;final EditText e=new EditText(this);e.setText(roomName);new AlertDialog.Builder(this).setTitle("Rename room").setView(e).setPositiveButton("Save",(d,w)->{String n=e.getText().toString().trim();if(n.length()<2)return;roomName=n;if(cloudRoom)setRoomValue("name",n);renderParty();}).setNegativeButton("Cancel",null).show();}
    private void changeCategoryDialog(){if(!isOwner())return;String[] cats={"Chat","Date","Music","Sing","Game","Event","Birthday","Wedding"};new AlertDialog.Builder(this).setTitle("Room category").setItems(cats,(d,w)->{roomCategory=cats[w];if(cloudRoom)setRoomValue("category",roomCategory);renderParty();}).show();}
    private void changeThemeDialog(){if(!isOwner())return;String[] themes={"Classic","KTV","Love","Game","Royal"};new AlertDialog.Builder(this).setTitle("Room theme").setItems(themes,(d,w)->{roomTheme=themes[w];if(cloudRoom)setRoomValue("theme",roomTheme);else prefs.edit().putString("theme_"+roomName,roomTheme).apply();renderParty();}).show();}
    private void changeSeatCount(){if(!isOwner())return;maxSeats=maxSeats==12?8:12;if(cloudRoom)setRoomValue("maxSeats",maxSeats);else prefs.edit().putInt("maxSeats_"+roomName,maxSeats).apply();rebuildSeats();toast("Mic seats: "+maxSeats);}

'''
if marker not in src: raise SystemExit('Party helper insertion point changed')
src = src.replace(marker,helpers+marker,1)

# Short room ID helper for lobby search/cards.
old = '    private String shortId(){if(roomId==null)return "000000";return String.valueOf(Math.abs(roomId.hashCode()%900000)+100000);}\n'
new = '    private String shortId(){return shortId(roomId);}\n    private String shortId(String id){if(id==null)return "000000";return String.valueOf(Math.abs(id.hashCode()%900000)+100000);}\n'
if old not in src: raise SystemExit('Short ID helper changed')
src = src.replace(old,new,1)

PARTY.write_text(src,encoding='utf-8')

gradle=BUILD.read_text(encoding='utf-8')
gradle=re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'","versionCode 44; versionName '3.2.0'",gradle)
BUILD.write_text(gradle,encoding='utf-8')
print('Prepared KING Plus v3.2.0 BoloHi-style Party complete')
