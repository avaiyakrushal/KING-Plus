from pathlib import Path
import re

party = Path('app/src/main/java/com/kingplus/social/PartyActivity.java')
s = party.read_text(encoding='utf-8')

# ---------- v5.3 fields ----------
field_anchor = '    private final Map<Integer,Boolean> seatMics = new HashMap<>();\n'
fields = '''    private LinearLayout liveEmojiStageV530;\n    private ListenerRegistration liveEmojiListenerV530;\n    private ListenerRegistration seatInviteListenerV530;\n    private ListenerRegistration roleListenerV530;\n    private boolean roomModeratorV530 = false;\n    private boolean seatInviteDialogOpenV530 = false;\n    private String lastSeatInviteKeyV530 = "";\n    private final Set<String> seenLiveEmojiEventsV530 = new HashSet<>();\n'''
if fields.strip() not in s:
    if field_anchor not in s:
        raise SystemExit('v5.3: PartyActivity field anchor not found')
    s = s.replace(field_anchor, field_anchor + fields, 1)

# ---------- listener cleanup ----------
cleanup_anchor = '        for (ListenerRegistration l : ls) if (l != null) l.remove();\n'
cleanup_extra = '''        if (liveEmojiListenerV530 != null) liveEmojiListenerV530.remove();\n        if (seatInviteListenerV530 != null) seatInviteListenerV530.remove();\n        if (roleListenerV530 != null) roleListenerV530.remove();\n        liveEmojiListenerV530 = seatInviteListenerV530 = roleListenerV530 = null;\n        roomModeratorV530 = false;\n        seatInviteDialogOpenV530 = false;\n        seenLiveEmojiEventsV530.clear();\n'''
if cleanup_extra.strip() not in s:
    if cleanup_anchor not in s:
        raise SystemExit('v5.3: clearListeners anchor not found')
    s = s.replace(cleanup_anchor, cleanup_anchor + cleanup_extra, 1)

# ---------- BoloHi-inspired KING Plus lobby header ----------
old_create = '        TextView create = pill("＋ Create", PURPLE, this::createRoomDialog); head.addView(create,new LinearLayout.LayoutParams(dp(105),dp(44))); root.addView(head);\n'
new_create = '''        TextView search = pill("⌕", 0xffeeeeF2, this::searchPartyRoomsV530); search.setTextColor(0xff111111);\n        head.addView(search,new LinearLayout.LayoutParams(dp(52),dp(44)));\n        TextView create = pill("🏠＋", 0xffeeeeF2, this::createRoomDialog); create.setTextColor(0xff111111);\n        LinearLayout.LayoutParams createLpV530 = new LinearLayout.LayoutParams(dp(62),dp(44)); createLpV530.setMargins(dp(6),0,0,0);\n        head.addView(create,createLpV530); root.addView(head);\n'''
if old_create in s:
    s = s.replace(old_create, new_create, 1)
elif 'this::searchPartyRoomsV530' not in s:
    raise SystemExit('v5.3: lobby create/header anchor not found')

s = s.replace('TextView title = tv("Party", 30, 0xff171717, true);', 'TextView title = tv("KING Plus", 30, 0xff171717, true);', 1)
s = s.replace('n.equals(selected)?PURPLE:0xffdcd9e4', 'n.equals(selected)?0xffffe600:0xfff0eff4')
s = s.replace('t.setTextColor(n.equals(selected)?Color.WHITE:0xff55515f);', 't.setTextColor(n.equals(selected)?0xff111111:0xff55515f);')

# Remove the old hard-coded preview room rows. Signed-in lobby should show real Firebase rooms only.
static_rooms = re.compile(r'''\n        addRoomGridRow\(page,\n            "💜 Sweet Girls".*?\n            "🎤 Singing Club","Neha • 128","Singing Club","Neha"\);''', re.S)
s, removed = static_rooms.subn('\n        // v5.3: no fake preview rooms; real rooms are supplied by Firebase above.', s, count=1)

# ---------- live emoji stage in the room ----------
stage_anchor = '        addMemberStrip();\n'
stage_code = '''        liveEmojiStageV530 = new LinearLayout(this); liveEmojiStageV530.setGravity(Gravity.CENTER);\n        liveEmojiStageV530.setClipChildren(false); liveEmojiStageV530.setClipToPadding(false);\n        LinearLayout.LayoutParams liveStageLpV530 = new LinearLayout.LayoutParams(-1,dp(88));\n        liveStageLpV530.setMargins(0,dp(2),0,dp(2)); page.addView(liveEmojiStageV530,liveStageLpV530);\n'''
if stage_code.strip() not in s:
    if stage_anchor not in s:
        raise SystemExit('v5.3: room member-strip anchor not found')
    s = s.replace(stage_anchor, stage_anchor + stage_code, 1)

# Put a live-emoji button directly beside the message field when the legacy composer is present.
composer_anchor = 'composer.addView(message,new LinearLayout.LayoutParams(0,dp(50),1));'
composer_add = '''composer.addView(message,new LinearLayout.LayoutParams(0,dp(50),1));\n        TextView liveEmojiBtnV530 = pill("😊",0xff49315f,this::showLiveEmojiPanelV530);\n        LinearLayout.LayoutParams liveEmojiBtnLpV530 = new LinearLayout.LayoutParams(dp(50),dp(48)); liveEmojiBtnLpV530.setMargins(dp(6),0,0,0);\n        composer.addView(liveEmojiBtnV530,liveEmojiBtnLpV530);'''
hooked = False
if 'liveEmojiBtnV530' in s:
    hooked = True
elif composer_anchor in s:
    s = s.replace(composer_anchor, composer_add, 1)
    hooked = True
else:
    quick_anchor = 'addQuick(quick,"🎁\\nGift",this::giftDialog);'
    if quick_anchor in s:
        s = s.replace(quick_anchor, 'addQuick(quick,"😊\\nEmoji",this::showLiveEmojiPanelV530); ' + quick_anchor, 1)
        hooked = True
if not hooked:
    raise SystemExit('v5.3: could not hook live emoji into room bottom controls')

# ---------- intercept seat taps for host/co-host moderation ----------
seat_sig = '    private void seatAction(int no) {\n'
seat_intercept = '''        if (cloudRoom && canModerateSeatsV530() && user != null) {\n            String targetUidV530 = seatUids.get(no);\n            if (targetUidV530 != null && !user.getUid().equals(targetUidV530)) { showOccupiedSeatMenuV530(no); return; }\n            if (targetUidV530 == null) { showEmptySeatMenuV530(no); return; }\n        }\n'''
if seat_intercept.strip() not in s:
    if seat_sig not in s:
        raise SystemExit('v5.3: seatAction anchor not found')
    s = s.replace(seat_sig, seat_sig + seat_intercept, 1)

# ---------- attach role, seat invite, and live emoji listeners ----------
attach_sig = '    private void attachCloudRoom() {\n'
attach_code = '''        attachV530RealtimeHelpers();\n'''
if attach_code.strip() not in s:
    if attach_sig not in s:
        raise SystemExit('v5.3: attachCloudRoom anchor not found')
    s = s.replace(attach_sig, attach_sig + attach_code, 1)

# ---------- new v5.3 methods ----------
insert_before = '    private void toggleMic() {'
methods = r'''
    private boolean canModerateSeatsV530() {
        return isOwner() || roomModeratorV530;
    }

    private void searchPartyRoomsV530() {
        if (db == null || user == null) { toast("Sign in to search live Party rooms"); return; }
        final EditText inputV530 = new EditText(this); inputV530.setHint("Search room name"); inputV530.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("Search Party rooms").setView(inputV530).setNegativeButton("Cancel",null)
            .setPositiveButton("Search",(d,w) -> {
                final String qV530 = inputV530.getText().toString().trim().toLowerCase();
                if (qV530.isEmpty()) return;
                db.collection("live_rooms").limit(60).get().addOnSuccessListener(snap -> {
                    final List<DocumentSnapshot> hitsV530 = new ArrayList<>();
                    final List<String> labelsV530 = new ArrayList<>();
                    for (DocumentSnapshot docV530 : snap.getDocuments()) {
                        if (Boolean.TRUE.equals(docV530.getBoolean("closed"))) continue;
                        String nameV530 = str(docV530,"name","Live Party");
                        if (nameV530.toLowerCase().contains(qV530)) {
                            hitsV530.add(docV530);
                            labelsV530.add(nameV530 + "  •  " + str(docV530,"ownerName","Host"));
                        }
                    }
                    if (hitsV530.isEmpty()) { toast("No matching live rooms"); return; }
                    new AlertDialog.Builder(this).setTitle("Live rooms").setItems(labelsV530.toArray(new String[0]),(x,which) -> {
                        DocumentSnapshot docV530 = hitsV530.get(which);
                        openCloudRoom(docV530.getId(),str(docV530,"name","Live Party"),docV530.getString("ownerUid"),str(docV530,"ownerName","Host"));
                    }).setNegativeButton("Close",null).show();
                }).addOnFailureListener(e -> toast("Search failed: " + msg(e)));
            }).show();
    }

    private void showLiveEmojiPanelV530() {
        final String[] emojisV530 = {"😇","😈","😘","😍","😴","😂","🤗","🤔","😜","😡","😭","😅","👋","👏","🙌","👍","❤️","💖","💋","🎉","👑","🎲","🎰","🔥"};
        LinearLayout sheetV530 = new LinearLayout(this); sheetV530.setOrientation(LinearLayout.VERTICAL); sheetV530.setPadding(dp(14),dp(12),dp(14),dp(12)); sheetV530.setBackgroundColor(Color.WHITE);
        TextView titleV530 = tv("Live Emoji  •  tap to send",18,0xff222222,true); sheetV530.addView(titleV530);
        TextView catsV530 = tv("😊  Faces     🎉  Fun     ❤️  Love     👑  Party",13,0xff777777,false); sheetV530.addView(catsV530);
        android.widget.GridLayout gridV530 = new android.widget.GridLayout(this); gridV530.setColumnCount(4); gridV530.setAlignmentMode(android.widget.GridLayout.ALIGN_BOUNDS); gridV530.setUseDefaultMargins(true);
        final AlertDialog[] holderV530 = new AlertDialog[1];
        for (String emojiV530 : emojisV530) {
            TextView cellV530 = tv(emojiV530,36,0xff222222,false); cellV530.setGravity(Gravity.CENTER); cellV530.setBackground(bg(0xfff7f7fa,16));
            android.widget.GridLayout.LayoutParams glpV530 = new android.widget.GridLayout.LayoutParams(); glpV530.width=dp(72); glpV530.height=dp(64); glpV530.setMargins(dp(4),dp(4),dp(4),dp(4));
            gridV530.addView(cellV530,glpV530);
            cellV530.setOnClickListener(v -> { sendLiveEmojiV530(emojiV530); if(holderV530[0]!=null) holderV530[0].dismiss(); });
        }
        sheetV530.addView(gridV530,new LinearLayout.LayoutParams(-1,-2));
        AlertDialog dialogV530 = new AlertDialog.Builder(this).setView(sheetV530).setNegativeButton("Close",null).create(); holderV530[0]=dialogV530;
        dialogV530.setOnShowListener(v -> { android.view.Window wV530=dialogV530.getWindow(); if(wV530!=null){wV530.setGravity(Gravity.BOTTOM);wV530.setLayout(-1,-2);} });
        dialogV530.show();
    }

    private void sendLiveEmojiV530(String emojiV530) {
        showLiveEmojiEffectV530(emojiV530, displayName);
        if (cloudRoom && db != null && user != null && roomId != null) {
            Map<String,Object> dV530 = new HashMap<>(); dV530.put("actorUid",user.getUid()); dV530.put("actorName",safeName());
            dV530.put("type","live_emoji"); dV530.put("text",emojiV530); dV530.put("createdAt",FieldValue.serverTimestamp());
            db.collection("live_rooms").document(roomId).collection("events").add(dV530).addOnFailureListener(e -> toast("Emoji failed: "+msg(e)));
        }
    }

    private void showLiveEmojiEffectV530(String emojiV530, String senderV530) {
        if (liveEmojiStageV530 == null) { addFeed((senderV530==null?"User":senderV530)+"  "+emojiV530); return; }
        final TextView fxV530 = tv(emojiV530,48,Color.WHITE,false); fxV530.setGravity(Gravity.CENTER); fxV530.setAlpha(0f); fxV530.setScaleX(.45f); fxV530.setScaleY(.45f);
        liveEmojiStageV530.addView(fxV530,new LinearLayout.LayoutParams(dp(74),dp(72)));
        fxV530.animate().alpha(1f).scaleX(1.25f).scaleY(1.25f).translationY(-dp(30)).setDuration(420).withEndAction(() ->
            fxV530.animate().alpha(0f).translationY(-dp(100)).scaleX(.8f).scaleY(.8f).setStartDelay(450).setDuration(850).withEndAction(() -> {
                if(liveEmojiStageV530!=null) liveEmojiStageV530.removeView(fxV530);
            }).start()
        ).start();
    }

    private void showEmptySeatMenuV530(int seatNoV530) {
        new AlertDialog.Builder(this).setTitle("Seat "+seatNoV530).setItems(new String[]{"👑 Take this seat","📨 Invite member to this seat"},(d,w) -> {
            if(w==0) cloudSeatAction(seatNoV530); else inviteMemberToSeatV530(seatNoV530);
        }).setNegativeButton("Cancel",null).show();
    }

    private void showOccupiedSeatMenuV530(int seatNoV530) {
        final String uidV530 = seatUids.get(seatNoV530); final String nameV530 = seatNames.get(seatNoV530)==null?"Member":seatNames.get(seatNoV530);
        final boolean micV530 = !Boolean.FALSE.equals(seatMics.get(seatNoV530));
        String[] actionsV530 = {micV530?"🔇 Mute seat":"🎤 Unmute seat","⤵ Kick from seat","🚪 Kick from room"};
        new AlertDialog.Builder(this).setTitle(nameV530+" • Seat "+seatNoV530).setItems(actionsV530,(d,w) -> {
            if(w==0) setSeatMicV530(seatNoV530,!micV530);
            else if(w==1) kickSeatV530(seatNoV530,false);
            else confirmKickRoomV530(seatNoV530,uidV530,nameV530);
        }).setNegativeButton("Cancel",null).show();
    }

    private void setSeatMicV530(int seatNoV530, boolean onV530) {
        if(!canModerateSeatsV530() || db==null || roomId==null) { toast("Host / co-host only"); return; }
        db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(seatNoV530)).update("micOn",onV530)
            .addOnSuccessListener(v -> addEvent("seat_mic",(seatNames.get(seatNoV530)==null?"Member":seatNames.get(seatNoV530))+(onV530?" was unmuted":" was muted")))
            .addOnFailureListener(e -> toast("Seat update failed: "+msg(e)));
    }

    private void kickSeatV530(int seatNoV530, boolean silentV530) {
        if(!canModerateSeatsV530() || db==null || roomId==null) { toast("Host / co-host only"); return; }
        String nameV530=seatNames.get(seatNoV530)==null?"Member":seatNames.get(seatNoV530);
        db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(seatNoV530)).delete()
            .addOnSuccessListener(v -> { if(!silentV530)addEvent("seat_kick",nameV530+" was removed from seat "+seatNoV530); })
            .addOnFailureListener(e -> toast("Kick failed: "+msg(e)));
    }

    private void confirmKickRoomV530(int seatNoV530, String uidV530, String nameV530) {
        new AlertDialog.Builder(this).setTitle("Kick "+nameV530+" from room?").setMessage("This removes the member from the current room. It does not ban them.")
            .setNegativeButton("Cancel",null).setPositiveButton("Kick",(d,w) -> kickFromRoomV530(seatNoV530,uidV530,nameV530)).show();
    }

    private void kickFromRoomV530(int seatNoV530, String uidV530, String nameV530) {
        if(!canModerateSeatsV530() || db==null || roomId==null || uidV530==null) { toast("Host / co-host only"); return; }
        kickSeatV530(seatNoV530,true);
        db.collection("live_rooms").document(roomId).collection("members").document(uidV530).delete()
            .addOnSuccessListener(v -> addEvent("room_kick",nameV530+" was kicked from the room"))
            .addOnFailureListener(e -> toast("Room kick failed: "+msg(e)));
    }

    private void inviteMemberToSeatV530(int seatNoV530) {
        if(!canModerateSeatsV530() || db==null || user==null || roomId==null) { toast("Host / co-host only"); return; }
        db.collection("live_rooms").document(roomId).collection("members").get().addOnSuccessListener(snapV530 -> {
            final List<String> uidsV530=new ArrayList<>(); final List<String> namesV530=new ArrayList<>();
            for(DocumentSnapshot docV530:snapV530.getDocuments()){
                String uidV530=docV530.getString("uid"); if(uidV530==null) uidV530=docV530.getId();
                if(uidV530.equals(user.getUid()) || seatUids.containsValue(uidV530)) continue;
                uidsV530.add(uidV530); namesV530.add(str(docV530,"name","Member"));
            }
            if(uidsV530.isEmpty()){toast("No available room members to invite");return;}
            String[] labelsV530=new String[namesV530.size()]; for(int i=0;i<labelsV530.length;i++) labelsV530[i]="👤 "+namesV530.get(i);
            new AlertDialog.Builder(this).setTitle("Invite to Seat "+seatNoV530).setItems(labelsV530,(d,w) -> {
                String uidV530=uidsV530.get(w), nameV530=namesV530.get(w);
                Map<String,Object> inviteV530=new HashMap<>(); inviteV530.put("targetUid",uidV530); inviteV530.put("targetName",nameV530); inviteV530.put("seatId",String.valueOf(seatNoV530)); inviteV530.put("seatNo",seatNoV530);
                inviteV530.put("invitedByUid",user.getUid()); inviteV530.put("invitedByName",safeName()); inviteV530.put("createdAt",FieldValue.serverTimestamp());
                db.collection("live_rooms").document(roomId).collection("seat_invites").document(uidV530).set(inviteV530)
                    .addOnSuccessListener(v -> {toast("Invite sent to "+nameV530);addEvent("seat_invite",safeName()+" invited "+nameV530+" to seat "+seatNoV530);})
                    .addOnFailureListener(e -> toast("Invite failed: "+msg(e)));
            }).setNegativeButton("Cancel",null).show();
        }).addOnFailureListener(e -> toast("Members unavailable: "+msg(e)));
    }

    private void attachV530RealtimeHelpers() {
        if(db==null || user==null || roomId==null) return;
        DocumentReference roomV530=db.collection("live_rooms").document(roomId);
        roleListenerV530=roomV530.collection("roles").document(user.getUid()).addSnapshotListener((docV530,eV530) -> {
            roomModeratorV530 = isOwner() || (docV530!=null && docV530.exists() && "cohost".equals(docV530.getString("role")));
        });
        seatInviteListenerV530=roomV530.collection("seat_invites").document(user.getUid()).addSnapshotListener((docV530,eV530) -> {
            if(eV530!=null || docV530==null || !docV530.exists() || seatInviteDialogOpenV530) return;
            String seatIdV530=docV530.getString("seatId"); if(seatIdV530==null) return;
            Object createdV530=docV530.get("createdAt"); String keyV530=seatIdV530+"|"+String.valueOf(createdV530);
            if(keyV530.equals(lastSeatInviteKeyV530)) return; lastSeatInviteKeyV530=keyV530; seatInviteDialogOpenV530=true;
            int seatNoV530; try{seatNoV530=Integer.parseInt(seatIdV530);}catch(Exception ex){seatInviteDialogOpenV530=false;return;}
            String byV530=str(docV530,"invitedByName","Host");
            new AlertDialog.Builder(this).setTitle("Seat invite").setMessage(byV530+" invited you to Seat "+seatNoV530)
                .setNegativeButton("Decline",(d,w) -> {seatInviteDialogOpenV530=false;docV530.getReference().delete();})
                .setPositiveButton("Accept",(d,w) -> {seatInviteDialogOpenV530=false;acceptSeatInviteV530(docV530,seatNoV530);}).setOnCancelListener(d -> seatInviteDialogOpenV530=false).show();
        });
        liveEmojiListenerV530=roomV530.collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(20).addSnapshotListener((snapV530,eV530) -> {
            if(eV530!=null || snapV530==null) return;
            for(DocumentSnapshot docV530:snapV530.getDocuments()){
                if(!"live_emoji".equals(docV530.getString("type")) || seenLiveEmojiEventsV530.contains(docV530.getId())) continue;
                seenLiveEmojiEventsV530.add(docV530.getId()); Object tsV530=docV530.get("createdAt");
                if(tsV530 instanceof com.google.firebase.Timestamp){long ageV530=System.currentTimeMillis()-((com.google.firebase.Timestamp)tsV530).toDate().getTime();if(ageV530>5500)continue;}
                String actorV530=str(docV530,"actorName","User"); String emojiV530=str(docV530,"text","😊");
                if(!user.getUid().equals(docV530.getString("actorUid"))) showLiveEmojiEffectV530(emojiV530,actorV530);
            }
        });
    }

    private void acceptSeatInviteV530(DocumentSnapshot inviteV530, int seatNoV530) {
        if(db==null || user==null || roomId==null) return;
        if(seatUids.get(seatNoV530)!=null && !user.getUid().equals(seatUids.get(seatNoV530))){toast("That seat is no longer available");inviteV530.getReference().delete();return;}
        Map<String,Object> dV530=new HashMap<>();dV530.put("uid",user.getUid());dV530.put("name",safeName());dV530.put("micOn",false);dV530.put("joinedAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(seatNoV530)).set(dV530)
            .addOnSuccessListener(v -> {mySeat=seatNoV530;inviteV530.getReference().delete();addEvent("seat",safeName()+" accepted a seat invite for seat "+seatNoV530);})
            .addOnFailureListener(e -> toast("Could not accept seat invite: "+msg(e)));
    }

'''
if 'showLiveEmojiPanelV530()' not in s:
    if insert_before not in s:
        raise SystemExit('v5.3: method insertion anchor not found')
    s = s.replace(insert_before, methods + insert_before, 1)

party.write_text(s, encoding='utf-8')
print('v5.3 PartyActivity live emoji + seat controls applied')

# ---------- Firestore rules for targeted seat invites ----------
rules_path = Path('firestore.rules')
r = rules_path.read_text(encoding='utf-8')

if 'function invitedToSeat(roomId, uid, seatId)' not in r:
    helper_anchor = '''    function seatLocked(roomId, seatId) {\n      return exists(/databases/$(database)/documents/live_rooms/$(roomId)/seat_locks/$(seatId))\n        && get(/databases/$(database)/documents/live_rooms/$(roomId)/seat_locks/$(seatId)).data.locked == true;\n    }\n'''
    helper = helper_anchor + '''    function invitedToSeat(roomId, uid, seatId) {\n      return exists(/databases/$(database)/documents/live_rooms/$(roomId)/seat_invites/$(uid))\n        && get(/databases/$(database)/documents/live_rooms/$(roomId)/seat_invites/$(uid)).data.targetUid == uid\n        && get(/databases/$(database)/documents/live_rooms/$(roomId)/seat_invites/$(uid)).data.seatId == seatId;\n    }\n'''
    if helper_anchor not in r:
        raise SystemExit('v5.3: Firestore seatLocked helper anchor not found')
    r = r.replace(helper_anchor, helper, 1)

seat_old = '''          (roomMember(roomId)\n            && request.resource.data.uid == request.auth.uid\n            && roomData(roomId).locked != true\n            && !seatLocked(roomId, seatId))\n'''
seat_new = '''          (roomMember(roomId)\n            && request.resource.data.uid == request.auth.uid\n            && ((roomData(roomId).locked != true && !seatLocked(roomId, seatId))\n                || invitedToSeat(roomId, request.auth.uid, seatId)))\n'''
if seat_old in r:
    r = r.replace(seat_old, seat_new, 1)
elif 'invitedToSeat(roomId, request.auth.uid, seatId)' not in r:
    raise SystemExit('v5.3: Firestore seat create anchor not found')

if 'match /seat_invites/{inviteUid}' not in r:
    request_anchor = '      match /seat_requests/{requestId} {\n'
    invite_rules = '''      match /seat_invites/{inviteUid} {\n        allow read: if signedIn()\n          && (inviteUid == request.auth.uid || roomModerator(roomId));\n        allow create, update: if roomModerator(roomId)\n          && request.resource.data.targetUid == inviteUid\n          && request.resource.data.seatId is string\n          && request.resource.data.seatNo is int\n          && request.resource.data.seatNo >= 1\n          && request.resource.data.seatNo <= 12\n          && exists(/databases/$(database)/documents/live_rooms/$(roomId)/members/$(inviteUid));\n        allow delete: if signedIn()\n          && (inviteUid == request.auth.uid || roomModerator(roomId));\n      }\n\n'''
    if request_anchor not in r:
        raise SystemExit('v5.3: Firestore seat_requests anchor not found')
    r = r.replace(request_anchor, invite_rules + request_anchor, 1)

rules_path.write_text(r, encoding='utf-8')
print('v5.3 Firestore seat invite rules applied')
