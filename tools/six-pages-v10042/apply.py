from pathlib import Path
R=Path("/tmp/king-next/app/src/main/java/com/kingplus/social")
def fix(name,a,b):
 p=R/name;s=p.read_text()
 if s.count(a)!=1:raise AssertionError(name+" anchor: "+a[:80]+" count "+str(s.count(a)))
 p.write_text(s.replace(a,b,1))
# Me: photo owner, avoid another user's picture after switching Firebase account.
fix("MainActivity.java",'    private void editProfile(){','''    private String photoPickerOwner10042="";
    private String profilePhoto10042(){
        FirebaseUser u=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
        if(u==null)return "";
        String key="profile_photo_"+u.getUid();
        String cached=getPreferences(0).getString(key,null);
        if(cached!=null)return cached;
        String legacy=u.getUid().equals(getPreferences(0).getString("firebase_uid",""))
            ?getPreferences(0).getString("profile_photo",""):"";
        getPreferences(0).edit().putString(key,legacy).apply();return legacy;
    }
    private void editProfile(){''')
fix("MainActivity.java",'getPreferences(0).edit().putString("profile_photo",uri.toString()).apply();','''FirebaseUser photoOwner=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
                if(photoOwner==null||!photoOwner.getUid().equals(photoPickerOwner10042)){
                    Toast.makeText(this,"Account changed • choose photo again",Toast.LENGTH_LONG).show();return;}
                getPreferences(0).edit().putString("profile_photo_"+photoOwner.getUid(),uri.toString()).apply();''')
fix("MainActivity.java",'av.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);','''av.setOnClickListener(v->{FirebaseUser u=firebaseAuth==null?null:firebaseAuth.getCurrentUser();photoPickerOwner10042=u==null?"":u.getUid();Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);''')
fix("MainActivity.java",'String photo=getPreferences(0).getString("profile_photo","");LinearLayout album=','String photo=profilePhoto10042();LinearLayout album=')
fix("MainActivity.java",'final String photo=ownProfileMedia10031?getPreferences(0).getString("profile_photo",""):"";','final String photo=ownProfileMedia10031?profilePhoto10042():"";')
fix("MainActivity.java",'String photo=getPreferences(0).getString("profile_photo","");\n        if(!photo.isEmpty())','String photo=profilePhoto10042();\n        if(!photo.isEmpty())')
fix("MainActivity.java",'String url=download.toString();getPreferences(0).edit().putString("profile_photo_cloud",url).apply();','''FirebaseUser current=firebaseAuth.getCurrentUser();
                    if(current==null||!me.getUid().equals(current.getUid()))return;
                    String url=download.toString();getPreferences(0).edit().putString("profile_photo_cloud",url).apply();''')
# Home: stable message ID + server check on uncertain timeout.
fix("CommunityHubActivity.java",'r.collection("messages").add(m).addOnSuccessListener(v->{','''final String sendId10042="msg_"+java.util.UUID.randomUUID().toString().replace("-","");
                r.collection("messages").document(sendId10042).set(m).addOnSuccessListener(v->{''')
fix("CommunityHubActivity.java",'toast("Message failed • tap Send to retry");});','''r.collection("messages").document(sendId10042).get(com.google.firebase.firestore.Source.SERVER)
                        .addOnSuccessListener(check->{
                            if(check.exists()){
                                if(msg.getText().toString().trim().equals(t))msg.setText("");
                                toast("Message sent • delivery confirmed");
                            }else toast("Message failed • tap Send to retry");
                        }).addOnFailureListener(error->toast("Delivery uncertain • check chat before retrying"));});''')
# Party: server-deleted or closed rooms must release occupied seats.
fix("PartyActivity.java",'if(e!=null||doc==null||!doc.exists())return;announcement=str(doc,"announcement",announcement);','''if(e!=null||doc==null)return;
            if(!doc.exists()){
                if(!doc.getMetadata().isFromCache()){toast("Room removed from server");leaveRoom();}return;
            }
            if(Boolean.TRUE.equals(doc.getBoolean("closed"))&&!doc.getMetadata().isFromCache()){
                toast("Room closed • leaving");leaveRoom();return;
            }
            announcement=str(doc,"announcement",announcement);''')
fix("PartyActivity.java",'if(Boolean.TRUE.equals(doc.getBoolean("closed"))&&!isOwner()){toast("Room was closed by host");leaveRoom();}});','});')
# Game: obsolete account cannot complete server result on delayed reads.
fix("RoomGameActivity.java",'final String expectedType=type;\n        // Finishing','final String expectedType=type;\n        final String roundOwner10042=me.getUid();\n        // Finishing')
fix("RoomGameActivity.java",'.addOnSuccessListener(q->{\n                java.util.ArrayList<DocumentSnapshot> a=new java.util.ArrayList<>();','''.addOnSuccessListener(q->{
                FirebaseUser now10042=FirebaseAuth.getInstance().getCurrentUser();
                if(isFinishing()||isDestroyed()||now10042==null
                   ||!roundOwner10042.equals(now10042.getUid())||!gameMemberVerified992)return;
                java.util.ArrayList<DocumentSnapshot> a=new java.util.ArrayList<>();''')
fix("RoomGameActivity.java",'.addOnSuccessListener(serverRound->{\n                    if(!serverRound.exists()', '''.addOnSuccessListener(serverRound->{
                    FirebaseUser serverNow10042=FirebaseAuth.getInstance().getCurrentUser();
                    if(isFinishing()||isDestroyed()||serverNow10042==null
                       ||!roundOwner10042.equals(serverNow10042.getUid())||!gameMemberVerified992)return;
                    if(!serverRound.exists()''')
# Discover: account-specific influence deltas and non-fabricated profile labels.
fix("DiscoverActivity.java",'private int scoreDelta(){int prev=getSharedPreferences("discover_public",MODE_PRIVATE).getInt("last_total",0),now=gameScore+chatScore+eventScore;getSharedPreferences("discover_public",MODE_PRIVATE).edit().putInt("last_total",now).apply();return now-prev;}', '''private int scoreDelta(){
        FirebaseUser signed=FirebaseAuth.getInstance().getCurrentUser();
        if(signed==null||me==null||!signed.getUid().equals(me.getUid()))return 0;
        String key="last_total_"+signed.getUid();
        int now=gameScore+chatScore+eventScore;
        android.content.SharedPreferences prefs=getSharedPreferences("discover_public",MODE_PRIVATE);
        int previous=prefs.getInt(key,now);prefs.edit().putInt(key,now).apply();
        return now-previous;
    }''')
fix("DiscoverActivity.java",'i.putExtra("name","KING "+publicId(uid));','i.putExtra("name","KING Player");')
# Messages: prevent rapid double-submit while a direct text write is unresolved.
fix("ChatActivity.java",'private void sendText(String text) {','''private boolean cloudTextSending10042;
    private void sendText(String text) {
        if(cloudTextSending10042){Toast.makeText(this,"Sending previous message…",Toast.LENGTH_SHORT).show();return;}''')
fix("ChatActivity.java",'if (cloudMode) sendCloudMessage(text, "text", ""); else saveLocal(text, "text", "", true);','if (cloudMode){cloudTextSending10042=true;sendCloudMessage(text, "text", "");} else saveLocal(text, "text", "", true);')
fix("ChatActivity.java",'Toast.makeText(this,"Account changed • reopen chat",Toast.LENGTH_LONG).show();return;\n        }\n        final String sendOwner10040=me.getUid();','cloudTextSending10042=false;Toast.makeText(this,"Account changed • reopen chat",Toast.LENGTH_LONG).show();return;\n        }\n        final String sendOwner10040=me.getUid();')
fix("ChatActivity.java",'.addOnSuccessListener(sent->{\n                    if(!chatSessionCurrent10040())return;', '.addOnSuccessListener(sent->{\n                    cloudTextSending10042=false;\n                    if(!chatSessionCurrent10040())return;')
fix("ChatActivity.java",'private void handleSendFailure10040(String id,String text,String type,String mediaUrl){\n        if(!chatSessionCurrent10040())return;', 'private void handleSendFailure10040(String id,String text,String type,String mediaUrl){\n        cloudTextSending10042=false;\n        if(!chatSessionCurrent10040())return;')
# Keep same package/signature and increment version for side-by-side build upgrade.
p=Path("/tmp/king-next/app/build.gradle");s=p.read_text()
assert "versionCode 320; versionName '10.0.41'" in s
p.write_text(s.replace("versionCode 320; versionName '10.0.41'","versionCode 321; versionName '10.0.42'"))
print("Six-page v10.0.42 patch applied")
