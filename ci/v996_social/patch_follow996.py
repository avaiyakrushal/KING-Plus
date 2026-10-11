#!/usr/bin/env python3
from pathlib import Path
import sys
f=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social/SocialActivity.java'
s=f.read_text()
start=s.index('    private void openProfile(String uid, String name, String bio) {')
end=s.index('    private void toggleFollow(',start)
replacement=r'''    private void openProfile(String uid,String name,String bio){
        if(!currentAccount970()||uid==null||uid.equals(me.getUid()))return;
        final String mine=me.getUid();
        db.collection("follows").document(followId(mine,uid))
          .get(com.google.firebase.firestore.Source.SERVER)
          .addOnCompleteListener(own->{
              if(!currentAccount970())return;
              final boolean following=own.isSuccessful()
                  &&own.getResult()!=null&&own.getResult().exists();
              db.collection("follows").document(followId(uid,mine))
                .get(com.google.firebase.firestore.Source.SERVER)
                .addOnCompleteListener(reverse->{
                    if(!currentAccount970())return;
                    boolean follower=reverse.isSuccessful()
                        &&reverse.getResult()!=null&&reverse.getResult().exists();
                    boolean readsOk=own.isSuccessful()&&reverse.isSuccessful();
                    String relationship=following&&follower?"💜 Mutual friends":
                        following?"You follow this person":
                        follower?"Follows you • Follow Back to become friends":"Not following";
                    if(!readsOk)relationship+="\n⚠ Server follow status unavailable. "+
                        "You can still attempt Follow; the error will be shown.";
                    String[] actions={following?"✓ Unfollow":
                        follower?"💜 Follow Back":"＋ Follow",
                        "✉ Message","↗ Share KING ID","⚑ Report"};
                    new AlertDialog.Builder(this).setTitle(name)
                        .setMessage((bio.isEmpty()?"KING Plus member":bio)+
                            "\n\n"+relationship+"\nKING ID: "+publicId(uid))
                        .setItems(actions,(d,w)->{
                            if(w==0)toggleFollow(uid,name,following);
                            else if(w==1)openChat(uid,name);
                            else if(w==2)shareKingId(uid,name);
                            else report(uid,name);
                        }).setNegativeButton("Close",null).show();
                });
          });
    }

''';
s=s[:start]+replacement+s[end:]
f.write_text(s)
print('PASS server read error can no longer silently block Follow / Follow Back dialog')
