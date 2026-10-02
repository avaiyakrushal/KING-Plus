from pathlib import Path
import re

party = Path('app/src/main/java/com/kingplus/social/PartyActivity.java')
s = party.read_text(encoding='utf-8')


def require_replace(old: str, new: str, label: str):
    global s
    if new in s:
        return
    if old not in s:
        raise SystemExit(f'v5.3: {label} anchor not found')
    s = s.replace(old, new, 1)

field_anchor = '    private ListenerRegistration seatRequestsListener;\n'
field_code = field_anchor + '    private ListenerRegistration seatInviteListenerV530;\n'
if 'seatInviteListenerV530' not in s:
    if field_anchor not in s:
        raise SystemExit('v5.3: seat invite listener field anchor not found')
    s = s.replace(field_anchor, field_code, 1)

old_ls = 'ListenerRegistration[] ls = {roomsListener,roomListener,seatsListener,membersListener,messagesListener,eventsListener,roleListener,selfMemberListener,roomBanListener,seatLocksListener,seatRequestsListener,musicStateListener,musicPlaylistListener,gameStateListener};'
new_ls = 'ListenerRegistration[] ls = {roomsListener,roomListener,seatsListener,membersListener,messagesListener,eventsListener,roleListener,selfMemberListener,roomBanListener,seatLocksListener,seatRequestsListener,seatInviteListenerV530,musicStateListener,musicPlaylistListener,gameStateListener};'
require_replace(old_ls, new_ls, 'clearListeners array')
old_null = 'seatLocksListener = seatRequestsListener = musicStateListener = musicPlaylistListener = gameStateListener = null;'
new_null = 'seatLocksListener = seatRequestsListener = seatInviteListenerV530 = musicStateListener = musicPlaylistListener = gameStateListener = null;'
require_replace(old_null, new_null, 'clearListeners nulling')

header_anchor = '        TextView searchIcon = tv("⌕",27,0xff39343e,false); searchIcon.setGravity(Gravity.CENTER); searchIcon.setOnClickListener(v -> showLobbySearch(selected)); head.addView(searchIcon,new LinearLayout.LayoutParams(dp(46),dp(50)));\n        root.addView(head);'
header_repl = '        TextView searchIcon = tv("⌕",27,0xff39343e,false); searchIcon.setGravity(Gravity.CENTER); searchIcon.setOnClickListener(v -> showLobbySearch(selected)); head.addView(searchIcon,new LinearLayout.LayoutParams(dp(46),dp(50)));\n        TextView homeCreateV530 = tv("🏠",24,0xff39343e,false); homeCreateV530.setGravity(Gravity.CENTER); homeCreateV530.setContentDescription("Create Party Room"); homeCreateV530.setOnClickListener(v -> createRoomDialog()); head.addView(homeCreateV530,new LinearLayout.LayoutParams(dp(48),dp(50)));\n        root.addView(head);'
require_replace(header_anchor, header_repl, 'BoloHi lobby HOME/Create control')

old_seat = '''    private void seatAction(int no) {
        if(cloudRoom){
            String existing=seatUids.get(no);
            if(existing!=null){openSeatProfile(no);return;}
            if(existing!=null){cloudSeatAction(no);return;}
            if(isModerator()){moderatorEmptySeatMenu(no);return;}
            if(roomLocked||lockedSeats.contains(no)){requestSeat(no);return;}
            cloudSeatAction(no);return;
        }
'''
new_seat = '''    private void seatAction(int no) {
        if(cloudRoom){
            String existing=seatUids.get(no);
            if(existing!=null){
                String targetUidV530=seatUids.get(no);
                if(isModerator() && user!=null && targetUidV530!=null && !user.getUid().equals(targetUidV530)){seatUserMenu(no);return;}
                openSeatProfile(no);return;
            }
            if(isModerator()){moderatorEmptySeatMenu(no);return;}
            if(roomLocked||lockedSeats.contains(no)){requestSeat(no);return;}
            cloudSeatAction(no);return;
        }
'''
require_replace(old_seat, new_seat, 'seatAction moderation')

moderator_pattern = re.compile(r'''    private void moderatorEmptySeatMenu\(int no\)\{.*?\n    \}\n''', re.S)
moderator_repl = '''    private void moderatorEmptySeatMenu(int no){
        String[] items={lockedSeats.contains(no)?"🔓 Unlock this seat":"🔒 Lock this seat","🎙 Take this seat","📨 Invite member to this seat","📥 Seat requests"};
        new AlertDialog.Builder(this).setTitle("Seat "+no).setItems(items,(d,w)->{
            if(w==0)toggleIndividualSeatLock(no);else if(w==1)cloudSeatAction(no);else if(w==2)inviteMemberToSeatV530(no);else showSeatRequests(no);
        }).setNegativeButton("Close",null).show();
    }
'''
if '📨 Invite member to this seat' not in s:
    s, n = moderator_pattern.subn(moderator_repl, s, count=1)
    if n != 1:
        raise SystemExit('v5.3: moderatorEmptySeatMenu replace failed')

old_send = '''    private void sendStickerOrEmoji(String category,String value){
        if("VIP".equals(category)&&prefs.getInt("vip_level",0)<1){toast("VIP sticker is locked in this no-billing build");return;}
        if(composerBox!=null){composerBox.setText(value);sendMessage(composerBox);}
        showReactionEffect(value);addEvent("sticker",safeName()+" sent "+category+" sticker "+value);
    }
'''
new_send = '''    private void sendStickerOrEmoji(String category,String value){
        if("VIP".equals(category)&&prefs.getInt("vip_level",0)<1){toast("VIP sticker is locked in this no-billing build");return;}
        if(composerBox!=null){composerBox.setText(value);sendMessage(composerBox);}
        if("Emoji".equals(category)){publishLiveEmojiV530(value);return;}
        showReactionEffect(value);addEvent("sticker",safeName()+" sent "+category+" sticker "+value);
    }
'''
require_replace(old_send, new_send, 'live emoji sender')

old_fx = '''    private void showReactionEffect(String emoji){
        if(reactionBanner==null)return;reactionBanner.setText(emoji+"   "+safeName());reactionBanner.setAlpha(1f);reactionBanner.setVisibility(View.VISIBLE);
        reactionBanner.postDelayed(()->{if(reactionBanner!=null)reactionBanner.animate().alpha(0f).setDuration(500).withEndAction(()->{if(reactionBanner!=null){reactionBanner.setVisibility(View.GONE);reactionBanner.setAlpha(1f);}}).start();},1400);
    }
'''
new_fx = '''    private void showReactionEffect(String emoji){
        if(reactionBanner==null)return;
        reactionBanner.animate().cancel();
        reactionBanner.setText(emoji);
        reactionBanner.setTextSize(50);
        reactionBanner.setAlpha(0f);
        reactionBanner.setScaleX(.55f);reactionBanner.setScaleY(.55f);reactionBanner.setTranslationY(dp(28));
        reactionBanner.setVisibility(View.VISIBLE);
        reactionBanner.animate().alpha(1f).scaleX(1.32f).scaleY(1.32f).translationY(-dp(18)).setDuration(360).withEndAction(()->{
            if(reactionBanner==null)return;
            reactionBanner.animate().alpha(0f).scaleX(.82f).scaleY(.82f).translationY(-dp(82)).setStartDelay(520).setDuration(760).withEndAction(()->{
                if(reactionBanner!=null){reactionBanner.setVisibility(View.GONE);reactionBanner.setAlpha(1f);reactionBanner.setScaleX(1f);reactionBanner.setScaleY(1f);reactionBanner.setTranslationY(0f);}
            }).start();
        }).start();
    }
'''
require_replace(old_fx, new_fx, 'live emoji animation')

old_event = '''                else if("join".equals(type))showEntranceEffect(str(d,"actorName","Guest"));
                else if("reaction".equals(type)){String text=str(d,"text","");String emoji=text.isEmpty()?"✨":text.substring(text.length()-Math.min(2,text.length()));showReactionEffect(emoji);}
'''
new_event = '''                else if("join".equals(type))showEntranceEffect(str(d,"actorName","Guest"));
                else if("live_emoji_v530".equals(type))showReactionEffect(str(d,"emoji","✨"));
                else if("reaction".equals(type)){String text=str(d,"text","");String emoji=text.isEmpty()?"✨":text.substring(text.length()-Math.min(2,text.length()));showReactionEffect(emoji);}
'''
require_replace(old_event, new_event, 'live emoji event receiver')

attach_anchor = '        membersListener=room.collection("members").addSnapshotListener((snap,e)->{if(snap==null)return;memberNames.clear();memberUids.clear();for(DocumentSnapshot m:snap.getDocuments()){String n=str(m,"name","User");memberNames.add(n);memberUids.put(n,m.getString("uid"));}liveMemberCount=snap.size();refreshPeopleCounts();rebuildMemberStrip();});\n'
attach_repl = attach_anchor + '        attachSeatInviteListenerV530(room);\n'
require_replace(attach_anchor, attach_repl, 'seat invite realtime listener')

insert_before = '    private void toggleMic() {'
methods = r'''
    private void publishLiveEmojiV530(String emojiV530){
        if(emojiV530==null||emojiV530.trim().isEmpty())return;
        if(!cloudRoom||db==null||user==null||roomId==null){showReactionEffect(emojiV530);return;}
        Map<String,Object>dV530=new HashMap<>();
        dV530.put("actorUid",user.getUid());dV530.put("actorName",safeName());dV530.put("type","live_emoji_v530");
        dV530.put("text",safeName()+" sent "+emojiV530);dV530.put("emoji",emojiV530);dV530.put("createdAt",FieldValue.serverTimestamp());
        db.collection("live_rooms").document(roomId).collection("events").add(dV530)
            .addOnFailureListener(e->toast("Live emoji failed: "+msg(e)));
    }

    private void inviteMemberToSeatV530(int seatNoV530){
        if(!isModerator()||!cloudRoom||db==null||user==null||roomId==null){toast("Host / co-host only");return;}
        if(seatUids.get(seatNoV530)!=null){toast("Seat is already occupied");return;}
        List<String>labelsV530=new ArrayList<>();List<String>idsV530=new ArrayList<>();
        for(String nV530:memberNames){
            String uidV530=memberUids.get(nV530);
            if(uidV530==null||uidV530.isEmpty()||uidV530.equals(user.getUid()))continue;
            boolean seatedV530=false;for(String seatedUidV530:seatUids.values())if(uidV530.equals(seatedUidV530)){seatedV530=true;break;}
            if(seatedV530)continue;labelsV530.add(nV530);idsV530.add(uidV530);
        }
        if(labelsV530.isEmpty()){toast("No available room members to invite");return;}
        new AlertDialog.Builder(this).setTitle("Invite to Seat "+seatNoV530).setItems(labelsV530.toArray(new String[0]),(d,w)->{
            String targetUidV530=idsV530.get(w),targetNameV530=labelsV530.get(w);
            Map<String,Object>inviteV530=new HashMap<>();
            inviteV530.put("recipientUid",targetUidV530);inviteV530.put("recipientName",targetNameV530);inviteV530.put("seatNo",seatNoV530);
            inviteV530.put("seatId",String.valueOf(seatNoV530));inviteV530.put("senderUid",user.getUid());inviteV530.put("senderName",safeName());
            inviteV530.put("status","pending");inviteV530.put("createdAt",FieldValue.serverTimestamp());
            db.collection("live_rooms").document(roomId).collection("seat_invites").document(targetUidV530).set(inviteV530)
                .addOnSuccessListener(v->{addEvent("seat_invite",safeName()+" invited "+targetNameV530+" to seat "+seatNoV530);toast("Seat invite sent");})
                .addOnFailureListener(e->toast("Seat invite failed: "+msg(e)));
        }).setNegativeButton("Close",null).show();
    }

    private void attachSeatInviteListenerV530(DocumentReference roomV530){
        if(user==null||roomV530==null)return;
        if(seatInviteListenerV530!=null)seatInviteListenerV530.remove();
        seatInviteListenerV530=roomV530.collection("seat_invites").document(user.getUid()).addSnapshotListener((docV530,eV530)->{
            if(eV530!=null||docV530==null||!docV530.exists())return;
            if(!"pending".equals(str(docV530,"status","")))return;
            Long seatLongV530=docV530.getLong("seatNo");if(seatLongV530==null)return;int seatNoV530=seatLongV530.intValue();
            String senderV530=str(docV530,"senderName","Host");
            new AlertDialog.Builder(this).setTitle("📨 Seat Invite").setMessage(senderV530+" invited you to Seat "+seatNoV530)
                .setNegativeButton("Decline",(d,w)->docV530.getReference().delete())
                .setPositiveButton("Accept",(d,w)->acceptSeatInviteV530(docV530.getReference(),seatNoV530)).show();
        });
    }

    private void acceptSeatInviteV530(DocumentReference inviteRefV530,int seatNoV530){
        if(!cloudRoom||db==null||user==null||roomId==null)return;
        if(seatNoV530<1||seatNoV530>maxSeats){toast("Invalid seat invite");return;}
        DocumentReference targetV530=db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(seatNoV530));
        DocumentReference oldV530=mySeat>0&&mySeat!=seatNoV530?db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(mySeat)):null;
        Map<String,Object>seatV530=new HashMap<>();seatV530.put("uid",user.getUid());seatV530.put("name",safeName());seatV530.put("micOn",false);
        seatV530.put("seatId",String.valueOf(seatNoV530));seatV530.put("joinedAt",FieldValue.serverTimestamp());
        db.runTransaction(txV530->{
            DocumentSnapshot currentV530=txV530.get(targetV530);if(currentV530.exists()&&!user.getUid().equals(currentV530.getString("uid")))throw new RuntimeException("Seat is already occupied");
            if(oldV530!=null)txV530.delete(oldV530);txV530.set(targetV530,seatV530);return null;
        }).addOnSuccessListener(v->{inviteRefV530.delete();addEvent("seat",safeName()+" accepted Seat "+seatNoV530);toast("You are now on Seat "+seatNoV530);})
          .addOnFailureListener(e->toast("Could not accept seat: "+msg(e)));
    }

'''
if 'private void publishLiveEmojiV530(' not in s:
    if insert_before not in s:
        raise SystemExit('v5.3: method insertion anchor not found')
    s = s.replace(insert_before, methods + insert_before, 1)

party.write_text(s, encoding='utf-8')

rules = Path('firestore.rules')
r = rules.read_text(encoding='utf-8')
old_seat_rule = '''        allow create: if roomAccess(roomId) && request.resource.data.name is string && (
          (roomMember(roomId)
            && request.resource.data.uid == request.auth.uid
            && roomData(roomId).locked != true
            && !seatLocked(roomId, seatId))
          || (roomModerator(roomId)
            && request.resource.data.uid is string
            && exists(/databases/$(database)/documents/live_rooms/$(roomId)/members/$(request.resource.data.uid)))
        );'''
new_seat_rule = '''        allow create: if roomAccess(roomId) && request.resource.data.name is string && (
          (roomMember(roomId)
            && request.resource.data.uid == request.auth.uid
            && roomData(roomId).locked != true
            && !seatLocked(roomId, seatId))
          || (roomMember(roomId)
            && request.resource.data.uid == request.auth.uid
            && request.resource.data.seatId == seatId
            && exists(/databases/$(database)/documents/live_rooms/$(roomId)/seat_invites/$(request.auth.uid))
            && get(/databases/$(database)/documents/live_rooms/$(roomId)/seat_invites/$(request.auth.uid)).data.status == 'pending'
            && get(/databases/$(database)/documents/live_rooms/$(roomId)/seat_invites/$(request.auth.uid)).data.seatId == seatId)
          || (roomModerator(roomId)
            && request.resource.data.uid is string
            && exists(/databases/$(database)/documents/live_rooms/$(roomId)/members/$(request.resource.data.uid)))
        );'''
if new_seat_rule not in r:
    if old_seat_rule not in r:
        raise SystemExit('v5.3: firestore seats create rule anchor not found')
    r = r.replace(old_seat_rule, new_seat_rule, 1)

seat_requests_block = '''      match /seat_requests/{requestId} {
        allow read: if signedIn()
          && (roomModerator(roomId)
              || (roomAccess(roomId) && resource.data.uid == request.auth.uid));
        allow create: if roomAccess(roomId)
          && roomMember(roomId)
          && request.resource.data.uid == request.auth.uid
          && request.resource.data.name is string
          && request.resource.data.seatNo is int
          && request.resource.data.seatNo >= 1
          && request.resource.data.seatNo <= 12;
        allow update: if false;
        allow delete: if signedIn()
          && (roomModerator(roomId) || resource.data.uid == request.auth.uid);
      }
'''
seat_invites_block = seat_requests_block + '''
      match /seat_invites/{recipientUid} {
        allow read: if signedIn()
          && (recipientUid == request.auth.uid || roomModerator(roomId));
        allow create, update: if roomModerator(roomId)
          && request.resource.data.recipientUid == recipientUid
          && request.resource.data.senderUid == request.auth.uid
          && request.resource.data.status == 'pending'
          && request.resource.data.seatNo is int
          && request.resource.data.seatNo >= 1
          && request.resource.data.seatNo <= 12
          && request.resource.data.seatId is string;
        allow delete: if signedIn()
          && (recipientUid == request.auth.uid || roomModerator(roomId));
      }
'''
if 'match /seat_invites/{recipientUid}' not in r:
    if seat_requests_block not in r:
        raise SystemExit('v5.3: firestore seat_requests block anchor not found')
    r = r.replace(seat_requests_block, seat_invites_block, 1)

rules.write_text(r, encoding='utf-8')
print('v5.3 current Party patch applied: HOME/Create + room-wide live emoji + seat Invite/Kick controls')
