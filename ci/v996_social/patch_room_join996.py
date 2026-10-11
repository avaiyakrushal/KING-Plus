#!/usr/bin/env python3
from pathlib import Path
import sys
f=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social/SocialActivity.java'
s=f.read_text()
start=s.index('    private void socialJoinParty992(){')
end=s.index('    private void runSearch() {',start)
replacement=r'''    private void joinFullRoom996(String roomId){
        if(!currentAccount970())return;
        db.collection("live_rooms").document(roomId)
          .get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(room->{
              if(!currentAccount970())return;
              if(room==null||!room.exists()||Boolean.TRUE.equals(room.getBoolean("closed"))){
                  toast("Room is closed or not found on Firebase SERVER");return;
              }
              if(Boolean.TRUE.equals(room.getBoolean("isPrivate")))
                  toast("Private room requires host permission to enter.");
              Intent join=new Intent(this,PartyActivity.class);
              join.putExtra("directRoomId",room.getId());
              join.putExtra("displayName",me.getDisplayName()==null?"KING User":me.getDisplayName());
              startActivity(join);
          }).addOnFailureListener(err->toast("Room lookup failed: "+safe(err.getLocalizedMessage())));
    }
    private void socialJoinParty992(){
        if(!currentAccount970()){toast("Sign in first");return;}
        final EditText entry=new EditText(this);
        entry.setHint("Full KINGROOM invite or six-digit Join Code");
        entry.setSingleLine(true);
        new AlertDialog.Builder(this)
          .setTitle("🎉 Join another phone's Party")
          .setMessage("Ask your friend to tap ↗ Share while inside their live Party.")
          .setView(entry).setNegativeButton("Cancel",null)
          .setPositiveButton("Find Room",(d,w)->{
              final String raw=entry.getText().toString().trim();
              final String full=KingRoomInvite959.parse(raw);
              if(!full.isEmpty()){joinFullRoom996(full);return;}
              if(!raw.matches("[0-9]{6}")){
                  toast("Paste a complete KINGROOM invitation or 6-digit Join Code");return;
              }
              db.collection("live_rooms").whereEqualTo("joinCode",raw).limit(8)
                .get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(q->{
                    if(!currentAccount970())return;
                    List<DocumentSnapshot> rooms=new ArrayList<>();
                    for(DocumentSnapshot room:q.getDocuments()){
                        if(!Boolean.TRUE.equals(room.getBoolean("closed")))rooms.add(room);
                    }
                    if(rooms.isEmpty()){toast("No open Party matches "+raw);return;}
                    if(rooms.size()==1){joinFullRoom996(rooms.get(0).getId());return;}
                    String[] labels=new String[rooms.size()];
                    for(int k=0;k<rooms.size();k++)
                        labels[k]=safe(rooms.get(k).getString("name"))+" • "+rooms.get(k).getId();
                    new AlertDialog.Builder(this).setTitle("Choose Party")
                        .setItems(labels,(dialog,index)->joinFullRoom996(rooms.get(index).getId()))
                        .setNegativeButton("Cancel",null).show();
                }).addOnFailureListener(error->
                    toast("Room code lookup failed: "+safe(error.getLocalizedMessage())));
          }).show();
    }

''';
s=s[:start]+replacement+s[end:]
f.write_text(s)
print("PASS friends can join by full Firestore Room ID OR six-digit joinCode")
