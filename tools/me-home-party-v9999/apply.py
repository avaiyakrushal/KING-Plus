from pathlib import Path
import sys
r=Path(sys.argv[1])
assert "versionCode 277;" in (r/'app/build.gradle').read_text()
def change(file,old,new):
 p=r/file;s=p.read_text();assert s.count(old)==1,(file,old[:120],s.count(old));p.write_text(s.replace(old,new))
party='app/src/main/java/com/kingplus/social/PartyActivity.java'
change(party,'m.put("publicId",publicId(user.getUid()));','/* Keep the Firebase-issued KING ID; XP cannot overwrite it. */')
change(party,'labels.add("👤 KING User • "+publicId(uid))','labels.add("👤 KING User • Firebase member")')
change(party,'TextView identity=tv("▣  KING ID "+publicId(uid)+"   |   Followers",13,0xff66606b,true);identity.setGravity(Gravity.CENTER);identity.setOnClickListener(v->copyKingId(uid));root.addView(identity,new LinearLayout.LayoutParams(-1,dp(36)));', '''final String[] issued9999={"Not issued"}; final int[] followers9999={-1};
        TextView identity=tv("▣ KING ID checking… | Followers",13,0xff66606b,true);
        identity.setGravity(Gravity.CENTER);identity.setOnClickListener(v->copyKingId(uid));root.addView(identity,new LinearLayout.LayoutParams(-1,dp(36)));
        if(db!=null)db.collection("public_profiles").document(uid).get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(profile->{if(isFinishing()||isDestroyed())return;
                String verified=KingPublicId9996.verified(profile.getString("publicId"));
                issued9999[0]=verified==null?"Not issued":verified;
                identity.setText("▣ KING ID "+issued9999[0]+" | "+(followers9999[0]<0?"Followers":followers9999[0]+" Followers"));
            }).addOnFailureListener(err->{if(!isFinishing()&&!isDestroyed())identity.setText("▣ KING ID unavailable");});''')
change(party,'db.collection("follows").whereEqualTo("targetUid",uid).get().addOnSuccessListener(q->identity.setText("▣  KING ID "+publicId(uid)+"   |   "+q.size()+" Followers"));', '''db.collection("follows").whereEqualTo("targetUid",uid).get().addOnSuccessListener(q->{if(isFinishing()||isDestroyed())return;
                followers9999[0]=q.size();identity.setText("▣ KING ID "+issued9999[0]+" | "+q.size()+" Followers");});''')
change(party,'private void copyKingId(String uid){String value=publicId(uid);ClipboardManager cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);if(cm!=null)cm.setPrimaryClip(ClipData.newPlainText("KING ID",value));toast("KING ID copied: "+value);}', '''private void copyKingId(String uid){
        if(db==null||uid==null||uid.isEmpty()){toast("KING ID unavailable");return;}
        db.collection("public_profiles").document(uid).get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(profile->{if(isFinishing()||isDestroyed())return;
             String id=KingPublicId9996.verified(profile.getString("publicId"));
             if(id==null){toast("Verified KING ID not issued");return;}
             ClipboardManager cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
             if(cm!=null)cm.setPrimaryClip(ClipData.newPlainText("KING ID",id));toast("KING ID copied: "+id);
          }).addOnFailureListener(err->toast("KING ID unavailable: "+msg(err)));
    }''')
change(party,'x.put("options",opts);publishGameState("vote",q,"",x);','x.put("options",opts);x.put("participantUids",new ArrayList<String>());publishGameState("vote",q,"",x);')
change(party,'"closed".equals(doc.getString("status"))){toast("No active room vote");return;}','!"active".equals(doc.getString("status"))){toast("No active room vote");return;}')
change(party,'String[] known={"👍 Yes","👎 No","❤️ Love it","😂 Funny"};for(String k:known)out.append(k).append("  •  ").append(counts.containsKey(k)?counts.get(k):0).append(\'\\n\');', '''ArrayList<String> known=new ArrayList<>();Object rawOptions=state.get("options");
                if(rawOptions instanceof List)for(Object o:(List<?>)rawOptions)if(o instanceof String&&!known.contains(o))known.add((String)o);
                if(known.isEmpty()){known.add("👍 Yes");known.add("👎 No");known.add("❤️ Love it");known.add("😂 Funny");}
                for(String k:known)out.append(k).append("  •  ").append(counts.containsKey(k)?counts.get(k):0).append('\\n');''')
hub='app/src/main/java/com/kingplus/social/KingParityHubActivity.java'
for variable in ('d','p'):
 change(hub,f'safe({variable}.getString("publicId"),publicId(uid))',f'(KingPublicId9996.verified({variable}.getString("publicId"))==null?"Not issued":KingPublicId9996.verified({variable}.getString("publicId")))')
main=r/'app/src/main/java/com/kingplus/social/MainActivity.java';s=main.read_text();i=s.index('    private void userProfile(String who){');j=s.index('    private void profileInfoRowV600(',i)
s=s[:i]+'''    private void userProfile(String who){
        // A device-only seat label has no authenticated UID. No invented stats or Follow.
        new AlertDialog.Builder(this).setTitle("Find a real KING user")
            .setMessage("Open People Search to find a verified account by its issued KING ID.")
            .setPositiveButton("People Search",(d,w)->startActivity(new Intent(this,SocialActivity.class)))
            .setNegativeButton("Close",null).show();
    }
'''+s[j:];main.write_text(s)
rule='firestore.rules'
old='''          && get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.participantUids is list
          && request.auth.uid in get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.participantUids
          && (get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.type
                in ['werewolf','spy']
              ? request.resource.data.choice in get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.participantUids
              : request.resource.data.choice is string);'''
new='''          // Open room votes accept all current room members, but ONLY published choices.
          // Other social games retain strict enrolled-player roster restrictions.
          && (get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.type == 'vote'
            ? (get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.get('options',[]) is list
               && get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.get('options',[]).size() >= 2
               && request.resource.data.choice in get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.options)
            : (get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.participantUids is list
               && request.auth.uid in get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.participantUids
               && (get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.type in ['werewolf','spy']
                 ? request.resource.data.choice in get(/databases/$(database)/documents/live_rooms/$(roomId)/game_state/current).data.participantUids
                 : request.resource.data.choice is string)));'''
change(rule,old,new)
change('app/build.gradle',"versionCode 277; versionName '9.9.98-public-id-fan-club-test'","versionCode 278; versionName '9.9.99-room-vote-issued-id-test'")
print('PASS v9.9.99: Me/Home verified IDs and Party real room vote + Firestore rules')
