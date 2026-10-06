from pathlib import Path
import sys

root=Path(sys.argv[1])
party=root/'app/src/main/java/com/kingplus/social/PartyActivity.java'
game=root/'app/src/main/java/com/kingplus/social/RoomGameActivity.java'
test=root/'app/src/main/java/com/kingplus/social/KingProductionTestActivity.java'

s=party.read_text()

# Large-room state: audience/member capacity is independent from mic-seat count.
marker='''    private int maxSeats = 8;
'''
if marker not in s: raise SystemExit('maxSeats marker missing')
s=s.replace(marker,marker+'''    private int maxMembers930 = 100;
''',1)

# Make create screen explain the crowd/audience model.
marker='''        LinearLayout flags=new LinearLayout(this);flags.setGravity(Gravity.CENTER);TextView pub=tv(pendingCreatePrivate?"🔒 Private":"🔓 Public",13,Color.WHITE,true);pub.setGravity(Gravity.CENTER);pub.setOnClickListener(v->{pendingCreatePrivate=!pendingCreatePrivate;renderCreateRoomPage();});flags.addView(pub,new LinearLayout.LayoutParams(0,dp(42),1));TextView seat=tv("Seat: "+pendingCreateSeats,13,Color.WHITE,true);seat.setGravity(Gravity.CENTER);seat.setOnClickListener(v->{int[] vals={8,10,12};int idx=0;for(int i=0;i<vals.length;i++)if(vals[i]==pendingCreateSeats)idx=i;pendingCreateSeats=vals[(idx+1)%vals.length];renderCreateRoomPage();});flags.addView(seat,new LinearLayout.LayoutParams(0,dp(42),1));body.addView(flags,new LinearLayout.LayoutParams(-1,dp(48)));
'''
replacement=marker+'''        TextView audience930=tv("👥 Up to 100 users can join • "+pendingCreateSeats+" stage/mic seats",11,0xffbfe5d8,true);audience930.setGravity(Gravity.CENTER);body.addView(audience930,new LinearLayout.LayoutParams(-1,dp(34)));
'''
if marker not in s: raise SystemExit('create flags marker missing')
s=s.replace(marker,replacement,1)

# Store large-room metadata when rules accept it. Older deployed schemas still have existing fallbacks.
s=s.replace('''full.put("maxSeats",seats);full.put("isPrivate",priv);''','''full.put("maxSeats",seats);full.put("maxMembers",100);full.put("audienceEnabled",true);full.put("isPrivate",priv);''',1)
s=s.replace('''compact.put("maxSeats",seats);compact.put("isPrivate",priv);''','''compact.put("maxSeats",seats);compact.put("maxMembers",100);compact.put("audienceEnabled",true);compact.put("isPrivate",priv);''',1)

# App version marker.
s=s.replace('''d.put("appVersion","9.2.1")''','''d.put("appVersion","9.3.0")''',1)

# Soft crowd-cap admission before writing a brand-new member doc.
old='''            }else write.run();
        }).addOnFailureListener(e->write.run());
    }
'''
new='''            }else if(oldDoc!=null&&oldDoc.exists()) write.run();
            else checkCrowdCapacity930(write);
        }).addOnFailureListener(e->checkCrowdCapacity930(write));
    }
    private void checkCrowdCapacity930(Runnable join){
        if(isOwner()){join.run();return;}
        DocumentReference room=db.collection("live_rooms").document(roomId);
        room.get().addOnSuccessListener(rd->{Long cap=rd.getLong("maxMembers");int limit=cap==null?100:(int)Math.max(10,Math.min(200,cap));maxMembers930=limit;room.collection("members").get().addOnSuccessListener(q->{if(q.size()>=limit){toast("This Party Room is full ("+limit+" users)");renderLobby("Hot");return;}join.run();}).addOnFailureListener(e->join.run());}).addOnFailureListener(e->join.run());
    }
'''
if old not in s: raise SystemExit('join admission marker missing')
s=s.replace(old,new,1)

# Cloud room listener reads crowd capacity, keeping 8/10/12 stage seats separate.
old='''roomListener=room.addSnapshotListener((doc,e)->{if(e!=null||doc==null||!doc.exists())return;announcement=str(doc,"announcement",announcement);roomCategory=str(doc,"category",roomCategory);roomTheme=str(doc,"theme",roomTheme);Long ms=doc.getLong("maxSeats");if(ms!=null){maxSeats=(int)Math.max(8,Math.min(12,ms));}hostSeatMode=Boolean.TRUE.equals(doc.getBoolean("hostSeatMode"));'''
new='''roomListener=room.addSnapshotListener((doc,e)->{if(e!=null||doc==null||!doc.exists())return;announcement=str(doc,"announcement",announcement);roomCategory=str(doc,"category",roomCategory);roomTheme=str(doc,"theme",roomTheme);Long ms=doc.getLong("maxSeats");if(ms!=null){maxSeats=(int)Math.max(8,Math.min(12,ms));}Long mm=doc.getLong("maxMembers");if(mm!=null)maxMembers930=(int)Math.max(10,Math.min(200,mm));hostSeatMode=Boolean.TRUE.equals(doc.getBoolean("hostSeatMode"));'''
if old not in s: raise SystemExit('room listener marker missing')
s=s.replace(old,new,1)

# Avoid collisions when two crowd members choose the same visible display name.
old='''membersListener=room.collection("members").addSnapshotListener((snap,e)->{if(snap==null)return;memberNames.clear();memberUids.clear();memberPhotos540.clear();memberVip720.clear();memberLevel720.clear();memberFrame730.clear();memberEffect730.clear();for(DocumentSnapshot m:snap.getDocuments()){String n=str(m,"name","User"),uid=m.getString("uid");memberNames.add(n);memberUids.put(n,uid);if(uid!=null&&m.getString("photoUrl")!=null)memberPhotos540.put(uid,m.getString("photoUrl"));Long lv=m.getLong("level"),vip=m.getLong("vipLevel");if(uid!=null){memberLevel720.put(uid,lv==null?1:lv.intValue());memberVip720.put(uid,vip==null?0:vip.intValue());String fr=m.getString("equippedFrame"),ef=m.getString("entranceEffect");if(fr!=null)memberFrame730.put(uid,fr);if(ef!=null)memberEffect730.put(uid,ef);}}rebuildSeats();liveMemberCount=snap.size();refreshPeopleCounts();rebuildMemberStrip();});
'''
new='''membersListener=room.collection("members").addSnapshotListener((snap,e)->{if(snap==null)return;memberNames.clear();memberUids.clear();memberPhotos540.clear();memberVip720.clear();memberLevel720.clear();memberFrame730.clear();memberEffect730.clear();for(DocumentSnapshot m:snap.getDocuments()){String base=str(m,"name","User"),uid=m.getString("uid");String n=base;if(memberUids.containsKey(n)&&uid!=null&&!uid.equals(memberUids.get(n))){String tail=uid.length()>4?uid.substring(uid.length()-4):uid;n=base+" · "+tail;}memberNames.add(n);memberUids.put(n,uid);if(uid!=null&&m.getString("photoUrl")!=null)memberPhotos540.put(uid,m.getString("photoUrl"));Long lv=m.getLong("level"),vip=m.getLong("vipLevel");if(uid!=null){memberLevel720.put(uid,lv==null?1:lv.intValue());memberVip720.put(uid,vip==null?0:vip.intValue());String fr=m.getString("equippedFrame"),ef=m.getString("entranceEffect");if(fr!=null)memberFrame730.put(uid,fr);if(ef!=null)memberEffect730.put(uid,ef);}}rebuildSeats();liveMemberCount=snap.size();refreshPeopleCounts();rebuildMemberStrip();});
'''
if old not in s: raise SystemExit('member listener marker missing')
s=s.replace(old,new,1)

# Member strip: show crowd count and overflow.
old='''        TextView label=tv(cloudRoom?"Members":"Host",11,MUTED,false); memberStripBox.addView(label,new LinearLayout.LayoutParams(0,dp(34),1));
'''
new='''        int extra=Math.max(0,names.size()-6);TextView label=tv(cloudRoom?("Members "+liveMemberCount+(extra>0?" • +"+extra:"")):"Host",11,MUTED,false); memberStripBox.addView(label,new LinearLayout.LayoutParams(0,dp(34),1));
'''
if old not in s: raise SystemExit('member strip label missing')
s=s.replace(old,new,1)

# Room state exposes audience count separately from stage seats.
old='''        roomStateLabel.setText(access+"  •  "+role+"  •  "+lock+"  •  "+mute+"  •  🎙 "+occupied+"/"+maxSeats+req);
'''
new='''        roomStateLabel.setText(access+"  •  "+role+"  •  "+lock+"  •  "+mute+"  •  👥 "+(cloudRoom?liveMemberCount:Math.max(1,memberNames.size()))+"/"+maxMembers930+"  •  🎙 "+occupied+"/"+maxSeats+req);
'''
if old not in s: raise SystemExit('room state label marker missing')
s=s.replace(old,new,1)

# All joined room members may enter the shared audio room. Seats remain stage/mic controls.
old='''    private void openVoice(){
        if(!cloudRoom||user==null||db==null||roomId==null){toast("Join a live Firebase Party room first");return;}
        if(mySeat<1){toast("Take a mic seat first");return;}
        if(muteAll&&!isModerator()){toast("Host muted all seats");return;}
        micOn=true;refreshMicControl();setVoicePresence900(true);
        db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(mySeat)).update("micOn",true).addOnFailureListener(e->{});
        voiceLaunched900=true;voiceLaunchAt900=System.currentTimeMillis();
        NativeMeetBridge.launch(this,roomId,roomName,safeName(),false);
    }
'''
new='''    private void openVoice(){
        if(!cloudRoom||user==null||db==null||roomId==null){toast("Join a live Firebase Party room first");return;}
        if(muteAll&&!isModerator()){toast("Host muted room voice");return;}
        setVoicePresence900(true);
        if(mySeat>0){micOn=true;refreshMicControl();db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(mySeat)).update("micOn",true).addOnFailureListener(e->{});}
        else toast("Joining shared room voice • mic seats remain stage controls");
        voiceLaunched900=true;voiceLaunchAt900=System.currentTimeMillis();
        NativeMeetBridge.launch(this,roomId,roomName,safeName(),false);
    }
'''
if old not in s: raise SystemExit('openVoice marker missing')
s=s.replace(old,new,1)

# Cloud mic button: non-seated members can still open shared room voice.
old='''        if(cloudRoom){if(micOn){micOn=false;setVoicePresence900(false);if(user!=null&&db!=null&&mySeat>0)db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(mySeat)).update("micOn",false).addOnFailureListener(e->{});refreshMicControl();toast("Mic state off • leave the voice conference if it is still open");}else openVoice();return;}
'''
new='''        if(cloudRoom){if(micOn&&mySeat>0){micOn=false;setVoicePresence900(false);if(user!=null&&db!=null)db.collection("live_rooms").document(roomId).collection("seats").document(String.valueOf(mySeat)).update("micOn",false).addOnFailureListener(e->{});refreshMicControl();toast("Stage mic off • leave the voice conference if it is still open");}else openVoice();return;}
'''
if old not in s: raise SystemExit('toggleMic cloud marker missing')
s=s.replace(old,new,1)

# Diagnostics: expose crowd model with a small escaped-string replacement.
diag_old='Members seen: "+liveMemberCount+"\\nSeat: "+(mySeat>0?mySeat:"none")'
diag_new='Members seen: "+liveMemberCount+"/"+maxMembers930+"\\nStage seats: "+seatNames.size()+"/"+maxSeats+"\\nMy seat: "+(mySeat>0?mySeat:"audience")'
if diag_old not in s: raise SystemExit('diagnostics count marker missing')
s=s.replace(diag_old,diag_new,1)

party.write_text(s)

# Group games: support large-room ready lobbies while pair games still select the needed players.
g=game.read_text()
g=g.replace('''if(q.size()>12){toast("This round supports up to 12 ready players");return;}''','''if(q.size()>50){toast("This round supports up to 50 ready players");return;}''')
# Make ready-state clearer for crowd rooms.
g=g.replace('''roleText.setText("● Multiplayer backend connected");loadRoleAndListen();''','''roleText.setText("● Multiplayer backend connected • large-room ready");loadRoleAndListen();''',1)
game.write_text(g)

# Production Test Center: validate crowd size separately from mic seats.
t=test.read_text()
t=t.replace('/** Final production verification dashboard for two-phone room testing. */','/** Production verification dashboard for two-phone and crowd-room testing. */',1)
voice_old='            state("Voice presence",voiceCount>=2,"voiceJoined="+voiceCount+" • both phones should tap Mic/Voice");'
voice_new='            state("Crowd room capacity",memberCount<=100,"joined members="+memberCount+" • target capacity=100");\n            state("Voice presence",voiceCount>=2,"voiceJoined="+voiceCount+" • joined members may open shared room voice");'
if voice_old not in t: raise SystemExit('production voice-state marker missing')
t=t.replace(voice_old,voice_new,1)
t=t.replace('state("Multiplayer game backend",true,"Ready players="+readyCount+" • open Room Games on both phones");','state("Multiplayer game backend",true,"Ready players="+readyCount+" • group rounds support up to 50 ready players");',1)
test.write_text(t)

print('v9.3.0 crowd-room multiplayer patch applied')
