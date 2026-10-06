from pathlib import Path
import sys
root=Path(sys.argv[1])
party=root/'app/src/main/java/com/kingplus/social/PartyActivity.java'
parity=root/'app/src/main/java/com/kingplus/social/KingParityHubActivity.java'
s=party.read_text()
field='''    private long voiceLaunchAt900;
'''
assert field in s
s=s.replace(field,field+'''    private long lastCleanup910;
''',1)

old='''    private void heartbeat900(){
        if(!cloudRoom||db==null||user==null||roomId==null)return;
        Map<String,Object>m=memberPayload891();
        db.collection("live_rooms").document(roomId).collection("members").document(user.getUid()).set(m,SetOptions.merge())
            .addOnSuccessListener(v->setOnlineState900(true,"Online • synced"))
            .addOnFailureListener(e->setOnlineState900(false,"Sync blocked"));
    }
'''
new='''    private void heartbeat900(){
        if(!cloudRoom||db==null||user==null||roomId==null)return;
        Map<String,Object>m=memberPayload891();
        db.collection("live_rooms").document(roomId).collection("members").document(user.getUid()).set(m,SetOptions.merge())
            .addOnSuccessListener(v->{setOnlineState900(true,"Online • synced");cleanupStaleRoom910();})
            .addOnFailureListener(e->{setOnlineState900(false,"Sync blocked");KingStability.nonFatal(this,"party-heartbeat",e);});
    }
    private void cleanupStaleRoom910(){
        if(!isOwner()||db==null||roomId==null)return;
        long now=System.currentTimeMillis();if(now-lastCleanup910<120000L)return;lastCleanup910=now;
        final java.util.HashSet<String>active=new java.util.HashSet<>();
        db.collection("live_rooms").document(roomId).collection("members").get().addOnSuccessListener(q->{
            WriteBatch batch=db.batch();
            for(DocumentSnapshot d:q.getDocuments()){
                com.google.firebase.Timestamp ts=d.getTimestamp("lastSeenAt");String uid=d.getId();
                boolean stale=ts!=null&&now-ts.toDate().getTime()>300000L;
                if(stale&&!uid.equals(ownerUid))batch.delete(d.getReference());else active.add(uid);
            }
            batch.commit().addOnFailureListener(e->KingStability.nonFatal(this,"stale-members",e));
            db.collection("live_rooms").document(roomId).collection("seats").get().addOnSuccessListener(seats->{
                WriteBatch sb=db.batch();boolean changed=false;
                for(DocumentSnapshot seat:seats.getDocuments()){String uid=seat.getString("uid");if(uid!=null&&!uid.equals(ownerUid)&&!active.contains(uid)){sb.delete(seat.getReference());changed=true;}}
                if(changed)sb.commit().addOnFailureListener(e->KingStability.nonFatal(this,"stale-seats",e));
            });
        }).addOnFailureListener(e->KingStability.nonFatal(this,"stale-scan",e));
    }
'''
assert old in s
s=s.replace(old,new,1)

old='''        new AlertDialog.Builder(this).setTitle(title).setMessage(detail+"\n\n"+hint)
            .setPositiveButton("OK",null).setNeutralButton("Try Room Code",(d,w)->joinRoomByCode891()).show();
'''
new='''        KingStability.nonFatal(this,"party-join",e);
        new AlertDialog.Builder(this).setTitle(title).setMessage(detail+"\n\n"+hint)
            .setPositiveButton("OK",null).setNeutralButton("Try Room Code",(d,w)->joinRoomByCode891()).show();
'''
assert old in s
s=s.replace(old,new,1)
party.write_text(s)

s=parity.read_text()
old='''private void match(){hero("🎲 KING Match","Browse real public KING Plus profiles. Open a card to follow, message or send gifts.");if(!cloud())return;db.collection("public_profiles").limit(40).get().addOnSuccessListener(q->{'''
new='''private void match(){hero("🎲 KING Match","Browse real public KING Plus profiles. Open a card to follow, message or send gifts.");card("🔎","Search KING profiles","Find by name or 6-digit KING ID",this::matchSearch910);if(!cloud())return;db.collection("public_profiles").limit(40).get().addOnSuccessListener(q->{'''
assert old in s
s=s.replace(old,new,1)
marker='''    private String publicId(String uid){'''
assert marker in s
helpers='''    private void matchSearch910(){
        if(!cloud())return;final EditText e=new EditText(this);e.setHint("Name or KING ID");e.setSingleLine(true);new AlertDialog.Builder(this).setTitle("Find KING user").setView(e).setNegativeButton("Cancel",null).setPositiveButton("Search",(d,w)->{String q=e.getText().toString().trim().toLowerCase(Locale.US);if(q.isEmpty())return;db.collection("public_profiles").limit(100).get().addOnSuccessListener(snap->{ArrayList<String>labels=new ArrayList<>();ArrayList<DocumentSnapshot>docs=new ArrayList<>();for(DocumentSnapshot p:snap.getDocuments()){String uid=safe(p.getString("uid"),p.getId()),nm=safe(p.getString("displayName"),"KING User"),id=safe(p.getString("publicId"),publicId(uid));if(nm.toLowerCase(Locale.US).contains(q)||id.equalsIgnoreCase(q)){docs.add(p);labels.add(nm+" • KING ID "+id);if(docs.size()>=30)break;}}if(docs.isEmpty()){toast("No matching KING profile");return;}new AlertDialog.Builder(this).setTitle("Matches").setItems(labels.toArray(new String[0]),(x,i)->openProfile(safe(docs.get(i).getString("uid"),docs.get(i).getId()),safe(docs.get(i).getString("displayName"),"KING User"))).setNegativeButton("Close",null).show();}).addOnFailureListener(err->toast("Profile search unavailable"));}).show();
    }

'''
s=s.replace(marker,helpers+marker,1)
parity.write_text(s)
print("party/match patched")
