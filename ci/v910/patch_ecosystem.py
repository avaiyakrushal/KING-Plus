from pathlib import Path
import sys
p=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social/KingEcosystemProActivity.java'; s=p.read_text()
assert 'TextView stopStage=button("■ Stop shared KTV stage",this::stopKtvStage890);' in s
s=s.replace('TextView stopStage=button("■ Stop shared KTV stage",this::stopKtvStage890);','TextView stopStage=button("■ Finish & save KTV",this::finishKtvSong910);',1)
assert 'section("Singer stage");LinearLayout stage=new LinearLayout(this);' in s
s=s.replace('section("Singer stage");LinearLayout stage=new LinearLayout(this);','LinearLayout kh=new LinearLayout(this);kh.addView(button("📜 KTV history",this::ktvHistory910),new LinearLayout.LayoutParams(0,dp(44),1));TextView refreshK=button("↻ Refresh",()->show("ktv"));LinearLayout.LayoutParams kr=new LinearLayout.LayoutParams(0,dp(44),1);kr.setMargins(dp(8),0,0,0);kh.addView(refreshK,kr);LinearLayout.LayoutParams khp=new LinearLayout.LayoutParams(-1,dp(44));khp.setMargins(0,dp(6),0,0);body.addView(kh,khp);section("Singer stage");LinearLayout stage=new LinearLayout(this);',1)
marker='    private void stopKtvStage890(){'
assert marker in s
helpers='''    private void finishKtvSong910(){
        if(!room())return;DocumentReference state=db.collection("live_rooms").document(roomId).collection("room_settings").document("ktv");
        state.get().addOnSuccessListener(d->{String song=safe(d.getString("song"),"");String singer=safe(d.getString("singerName"),"");if(!song.isEmpty()&&!"Waiting for singer".equals(song)){Map<String,Object>h=new HashMap<>();h.put("actorUid",me.getUid());h.put("actorName",displayName);h.put("type","ktv_history");h.put("songName",song);h.put("singerName",singer);h.put("text","KTV finished: "+song+" • "+singer);h.put("createdAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).collection("events").add(h).addOnFailureListener(e->{});}stopKtvStage890();}).addOnFailureListener(e->toast("KTV state unavailable"));
    }
    private void ktvHistory910(){
        if(!room())return;db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(100).get().addOnSuccessListener(q->{ArrayList<String>a=new ArrayList<>();for(DocumentSnapshot d:q.getDocuments())if("ktv_history".equals(d.getString("type")))a.add("🎤 "+safe(d.getString("songName"),"Song")+" • "+safe(d.getString("singerName"),"Singer"));if(a.isEmpty())a.add("No completed KTV songs yet.");new AlertDialog.Builder(this).setTitle("KTV history").setItems(a.toArray(new String[0]),null).setPositiveButton("Close",null).show();}).addOnFailureListener(e->toast("KTV history unavailable"));
    }
'''
s=s.replace(marker,helpers+marker,1)
assert 'TextView end=button("■ End",()->setPkRound(false,0));' in s
s=s.replace('TextView end=button("■ End",()->setPkRound(false,0));','TextView end=button("■ Finish + result",this::finishPkRound910);',1)
assert 'TextView result=tv("Waiting for PK round…",15,MUTED,true);' in s
s=s.replace('TextView result=tv("Waiting for PK round…",15,MUTED,true);','TextView history=button("📜 PK history",this::pkHistory910);LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,dp(44));hp.setMargins(0,0,0,dp(6));body.addView(history,hp);TextView result=tv("Waiting for PK round…",15,MUTED,true);',1)
marker='    private void setPkRound(boolean active,long sec){'
assert marker in s
helpers='''    private void finishPkRound910(){
        if(!room())return;db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(300).get().addOnSuccessListener(q->{Map<String,String>team=new LinkedHashMap<>();for(DocumentSnapshot d:q.getDocuments())if("pk_join".equals(d.getString("type"))){String uid=d.getString("actorUid");if(uid!=null&&!team.containsKey(uid))team.put(uid,safe(d.getString("team"),""));}long red=0,blue=0;for(DocumentSnapshot d:q.getDocuments())if("gift".equals(d.getString("type"))){Long v=d.getLong("giftValue");long score=v==null?0:Math.max(0,v);String t=team.get(d.getString("actorUid"));if("red".equals(t))red+=score;else if("blue".equals(t))blue+=score;}String winner=red==blue?"Draw":red>blue?"Red":"Blue";Map<String,Object>h=new HashMap<>();h.put("actorUid",me.getUid());h.put("actorName",displayName);h.put("type","pk_result");h.put("redScore",red);h.put("blueScore",blue);h.put("winner",winner);h.put("text","PK result • "+winner+" • "+red+"-"+blue);h.put("createdAt",FieldValue.serverTimestamp());db.collection("live_rooms").document(roomId).collection("events").add(h).addOnSuccessListener(v->{setPkRound(false,0);new AlertDialog.Builder(this).setTitle("PK Result").setMessage("🔴 "+red+"   VS   "+blue+" 🔵"+System.lineSeparator()+"Winner: "+winner).setPositiveButton("OK",null).show();}).addOnFailureListener(e->toast("PK result could not be saved"));}).addOnFailureListener(e->toast("PK score unavailable"));
    }
    private void pkHistory910(){
        if(!room())return;db.collection("live_rooms").document(roomId).collection("events").orderBy("createdAt",Query.Direction.DESCENDING).limit(100).get().addOnSuccessListener(q->{ArrayList<String>a=new ArrayList<>();for(DocumentSnapshot d:q.getDocuments())if("pk_result".equals(d.getString("type"))){Long r=d.getLong("redScore"),b=d.getLong("blueScore");a.add("⚔️ "+safe(d.getString("winner"),"Result")+" • "+(r==null?0:r)+"-"+(b==null?0:b));}if(a.isEmpty())a.add("No completed PK rounds yet.");new AlertDialog.Builder(this).setTitle("PK history").setItems(a.toArray(new String[0]),null).setPositiveButton("Close",null).show();}).addOnFailureListener(e->toast("PK history unavailable"));
    }
'''
s=s.replace(marker,helpers+marker,1)
old='''    private void familyActivity(String code,String type){Map<String,Object>m=new HashMap<>();m.put("uid",me.getUid());m.put("name",displayName);m.put("type",type);m.put("text","checkin".equals(type)?displayName+" completed daily family check-in":displayName+" joined a Family activity");m.put("createdAt",FieldValue.serverTimestamp());db.collection("families").document(code).collection("activities").add(m).addOnSuccessListener(v->{toast("Family activity added");show("family");}).addOnFailureListener(e->toast("Activity unavailable"));}'''
assert old in s
new='''    private void familyActivity(String code,String type){Map<String,Object>m=new HashMap<>();m.put("uid",me.getUid());m.put("name",displayName);m.put("type",type);m.put("text","checkin".equals(type)?displayName+" completed daily family check-in":displayName+" joined a Family activity");m.put("createdAt",FieldValue.serverTimestamp());if("checkin".equals(type)){String day=new java.text.SimpleDateFormat("yyyyMMdd",Locale.US).format(new java.util.Date());db.collection("families").document(code).collection("activities").document(me.getUid()+"_"+day).set(m).addOnSuccessListener(v->{toast("Daily Family check-in complete");show("family");}).addOnFailureListener(e->toast("Already checked in today or activity unavailable"));}else db.collection("families").document(code).collection("activities").add(m).addOnSuccessListener(v->{toast("Family activity added");show("family");}).addOnFailureListener(e->toast("Activity unavailable"));}'''
s=s.replace(old,new,1)
p.write_text(s)
print("ecosystem patched")
