#!/usr/bin/env python3
from pathlib import Path
import sys
f=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social/PartyActivity.java'
s=f.read_text()
start=s.index('    private void administratorDialog(){')
end=s.index('    private void chooseAdministrator(',start)
replacement='''    private void administratorDialog(){
        if(!cloudRoom||db==null||roomId==null||!memberSeen){
            toast("Join an active Firebase Party first");return;
        }
        if(!isModerator()){
            new AlertDialog.Builder(this).setTitle("Administrator")
                .setMessage("Only Room Host and authorized administrators can manage roles.")
                .setPositiveButton("OK",null).show();return;
        }
        db.collection("live_rooms").document(roomId).collection("roles")
          .get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(snapshot->{
              List<String> labels=new ArrayList<>();
              List<String> uids=new ArrayList<>();
              labels.add("👑 Host • "+(ownerName==null?"KING Host":ownerName));uids.add("");
              for(DocumentSnapshot entry:snapshot.getDocuments()){
                  if(!"cohost".equals(entry.getString("role")))continue;
                  labels.add("🛡 Administrator • "+memberNameForUid(entry.getId()));
                  uids.add(entry.getId());
              }
              int count=uids.size()-1;
              labels.add("ℹ Administrator permissions ("+count+"/5)");uids.add("");
              if(isOwner()&&count<5){labels.add("＋ Add administrator");uids.add("");}
              new AlertDialog.Builder(this).setTitle("🛡 Room Administrators")
                .setItems(labels.toArray(new String[0]),(d,w)->{
                    String label=labels.get(w);
                    if(label.startsWith("＋")){chooseAdministrator(count);return;}
                    if(label.startsWith("ℹ")){
                        new AlertDialog.Builder(this).setTitle("Admin permissions")
                          .setMessage("Admins can moderate mic seats, members and announcements. "+
                                      "Only the Host can add/remove administrators.")
                          .setPositiveButton("OK",null).show();return;
                    }
                    String selectedUid=uids.get(w);
                    if(selectedUid.isEmpty()||!isOwner())return;
                    final String currentRoom=roomId;
                    new AlertDialog.Builder(this).setTitle("Remove Administrator?")
                      .setMessage("Remove the Administrator role from "+label+
                                  "? The user remains in the Party.")
                      .setNegativeButton("Cancel",null)
                      .setPositiveButton("Remove",(x,y)->{
                          if(!isOwner()||!currentRoom.equals(roomId))return;
                          db.collection("live_rooms").document(currentRoom)
                            .collection("roles").document(selectedUid).delete()
                            .addOnSuccessListener(v->{
                                if(!isFinishing()&&!isDestroyed()){
                                    toast("Administrator role removed");administratorDialog();
                                }
                            }).addOnFailureListener(e->toast("Admin update failed: "+msg(e)));
                      }).show();
                }).setNegativeButton("Close",null).show();
          }).addOnFailureListener(e->toast("Admin list unavailable: "+msg(e)));
    }

'''
s=s[:start]+replacement+s[end:]
f.write_text(s)
print("PASS Host-controlled admin role add/revoke with confirmation")
