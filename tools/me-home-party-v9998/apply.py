from pathlib import Path
import sys

root=Path(sys.argv[1]).resolve()
gradle=root/'app/build.gradle'
assert "versionCode 276;" in gradle.read_text(), "Exact v9.9.97 source required"

def replace_one(s,old,new):
    count=s.count(old)
    assert count==1, (count,old[:100])
    return s.replace(old,new)

src=root/'app/src/main/java/com/kingplus/social'
profile=src/'KingPublicProfileActivity.java'
s=profile.read_text()
s=replace_one(s,'TextView pid=tv("◇ KING ID  "+publicId(uid)+" ◇",12,MUTED,true);',
'''String issued9998=KingPublicId9996.verified(p.getString("publicId"));
        TextView pid=tv("◇ KING ID  "+(issued9998==null?"Not issued":issued9998)+" ◇",12,MUTED,true);''')
a=s.index('    private void toggleFollow(){')
b=s.index('    private void openChat(){',a)
s=s[:a]+'''    private void toggleFollow(){
        if(me==null||db==null){toast("Sign in first");return;}
        final String target=uid;
        KingFollowLive9997.toggle(db,me,target,displayName(),(following,error)->{
            if(isFinishing()||isDestroyed()||!target.equals(uid))return;
            if(error!=null){toast("Follow not changed • "+safe(error.getMessage()));return;}
            toast(Boolean.TRUE.equals(following)?"Following":"Unfollowed");
            checkFollow();
        });
    }
'''+s[b:]
a=s.index('    private void share(){')
b=s.index('    private void report(){',a)
s=s[:a]+'''    private void share(){
        if(me==null||db==null||uid==null||uid.isEmpty()){toast("Sign in to share a KING profile");return;}
        final String target=uid;
        db.collection("public_profiles").document(target)
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(p->{
                if(isFinishing()||isDestroyed()||!target.equals(uid))return;
                String id=KingPublicId9996.verified(p.getString("publicId"));
                if(id==null){toast("This profile has no issued KING ID");return;}
                Intent send=new Intent(Intent.ACTION_SEND);send.setType("text/plain");
                send.putExtra(Intent.EXTRA_TEXT,"KING Plus • "+name+" • KING ID "+id);
                startActivity(Intent.createChooser(send,"Share profile"));
            }).addOnFailureListener(error->{if(!isFinishing()&&!isDestroyed())toast("Could not verify KING ID • try again");});
    }
'''+s[b:]
profile.write_text(s)

main=src/'MainActivity.java'
s=main.read_text()
s=replace_one(s,'()->shareText("Join me on KING Plus. My ID: "+(firebaseAuth!=null&&firebaseAuth.getCurrentUser()!=null?publicId(firebaseAuth.getCurrentUser().getUid()):displayName))','()->shareMyIssuedKingId9998()')
mark='    private void shareText(String value){'
assert s.count(mark)==1
s=s.replace(mark,'''    private void shareMyIssuedKingId9998(){
        FirebaseUser current=firebaseAuth==null?null:firebaseAuth.getCurrentUser();
        if(current==null||firestore==null){Toast.makeText(this,"Sign in to share your KING ID",Toast.LENGTH_SHORT).show();return;}
        final String account=current.getUid();
        firestore.collection("public_profiles").document(account)
            .get(com.google.firebase.firestore.Source.SERVER)
            .addOnSuccessListener(p->{
                if(isFinishing()||isDestroyed()||firebaseAuth.getCurrentUser()==null
                    ||!account.equals(firebaseAuth.getCurrentUser().getUid()))return;
                String id=KingPublicId9996.verified(p.getString("publicId"));
                if(id==null){Toast.makeText(this,"Your KING ID is not issued yet",Toast.LENGTH_SHORT).show();return;}
                shareText("Join me on KING Plus • KING ID "+id);
            }).addOnFailureListener(e->{if(!isFinishing()&&!isDestroyed())
                Toast.makeText(this,"Cannot verify your KING ID now. Retry online.",Toast.LENGTH_LONG).show();});
    }
'''+mark)
main.write_text(s)

party=src/'PartyActivity.java'
s=party.read_text()
a=s.index('    private void fanClubPanel940(){')
b=s.index('    private void toggleFanClub940(',a)
s=s[:a]+'''    private int fanClubRequestGeneration9998;
    private void fanClubPanel940(){
        if(!cloudRoom||db==null||user==null||ownerUid==null||ownerUid.isEmpty()){toast("Open a live Party room first");return;}
        final String hostUid=ownerUid, account=user.getUid(), openRoom=roomId;
        final int ticket=++fanClubRequestGeneration9998;
        final String relation=KingFollowPolicy9997.key(account,hostUid);
        if(relation==null){toast("Host cannot join their own Fan Club");return;}
        // Exact Follow doc, not a 100-person preview, determines Fan Club membership.
        db.collection("follows").document(relation)
            .get(com.google.firebase.firestore.Source.SERVER).addOnSuccessListener(member->{
                if(partyUiDead()||ticket!=fanClubRequestGeneration9998||user==null
                    ||!account.equals(user.getUid())||!hostUid.equals(ownerUid)
                    ||!java.util.Objects.equals(openRoom,roomId))return;
                final boolean joined=member.exists()
                    && account.equals(member.getString("followerUid"))
                    && hostUid.equals(member.getString("targetUid"));
                com.google.firebase.firestore.Query fans=db.collection("follows").whereEqualTo("targetUid",hostUid);
                fans.count().get(com.google.firebase.firestore.AggregateSource.SERVER)
                    .addOnSuccessListener(count->{
                        if(partyUiDead()||ticket!=fanClubRequestGeneration9998||!hostUid.equals(ownerUid)
                            ||!java.util.Objects.equals(openRoom,roomId))return;
                        final long total=count.getCount();
                        fans.limit(100).get(com.google.firebase.firestore.Source.SERVER)
                            .addOnSuccessListener(preview->{
                                if(partyUiDead()||ticket!=fanClubRequestGeneration9998||!hostUid.equals(ownerUid)
                                    ||!java.util.Objects.equals(openRoom,roomId))return;
                                List<String> names=new ArrayList<>();
                                for(DocumentSnapshot fan:preview.getDocuments()){
                                    String uid=fan.getString("followerUid"),name=fan.getString("followerName");
                                    if(name==null||name.trim().isEmpty())name=uid==null?"KING Fan":"KING "+uid.substring(0,Math.min(6,uid.length()));
                                    names.add(name);
                                }
                                String[] actions={(joined?"💔 Leave Fan Club":"💜 Join Fan Club"),
                                    "👥 View fans ("+total+")", "🎁 Open Gift Center"};
                                new AlertDialog.Builder(this).setTitle("💜 "+(ownerName==null?"KING Host":ownerName)+" Fan Club")
                                    .setMessage("Fans: "+total+"\\n"+(joined?"You follow this host.":"Follow the host to join.")
                                        +(total>100?"\\nShowing first 100 members in the preview.":""))
                                    .setItems(actions,(dialog,which)->{
                                        if(which==0)toggleFanClub940(joined);
                                        else if(which==1){
                                            if(names.isEmpty())new AlertDialog.Builder(this).setTitle("Fan members").setMessage("No fans yet.").setPositiveButton("OK",null).show();
                                            else new AlertDialog.Builder(this).setTitle("Fan members • "+names.size()+" of "+total)
                                                .setItems(names.toArray(new String[0]),null).setNegativeButton("Close",null).show();
                                        }else giftDialogFor(hostUid,ownerName==null?"Host":ownerName);
                                    }).setNegativeButton("Close",null).show();
                            }).addOnFailureListener(e->{if(!partyUiDead())toast("Fan preview unavailable: "+msg(e));});
                    }).addOnFailureListener(e->{if(!partyUiDead())toast("Fan count unavailable: "+msg(e));});
            }).addOnFailureListener(e->{if(!partyUiDead())toast("Fan membership unavailable: "+msg(e));});
    }
'''+s[b:]
party.write_text(s)
s=replace_one(gradle.read_text(),"versionCode 276; versionName '9.9.97-follow-live-me-home-party-test'","versionCode 277; versionName '9.9.98-public-id-fan-club-test'")
gradle.write_text(s)
print("PASS v9.9.98 source transformations")
