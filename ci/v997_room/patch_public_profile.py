#!/usr/bin/env python3
from pathlib import Path
import sys
f=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social/KingPublicProfileActivity.java'
s=f.read_text()
old='private FirebaseFirestore db; private FirebaseUser me; private String uid="", name="KING User"; private LinearLayout body; private TextView followBtn;'
assert s.count(old)==1
s=s.replace(old,old+' private String activeRoomId997="";',1)
old='render();logProfileVisit940();load();'
assert s.count(old)==1
s=s.replace(old,'activeRoomId997=getIntent().getStringExtra("roomId");if(activeRoomId997==null)activeRoomId997="";render();logProfileVisit940();load();',1)
point='        TextView cosmetics=tv('
assert s.count(point)==1
new='''        // Noble level is not a paid entitlement until a trusted server ledger exists.
        Long noble=p.getLong("nobleLevel");
        String nobleText=noble!=null&&noble>0
            ?"👑 Noble Lv."+Math.min(50L,noble)+" • unverified profile badge"
            :"👑 Noble Level 0 • verified unlock not active";
        TextView nobleBadge997=tv(nobleText,13,0xff684c91,true);
        nobleBadge997.setBackground(bg(0xfff4ecff,12));
        LinearLayout.LayoutParams nobleLp997=new LinearLayout.LayoutParams(-1,dp(44));
        nobleLp997.topMargin=dp(4);
        body.addView(nobleBadge997,nobleLp997);
        if(!activeRoomId997.isEmpty()&&db!=null){
            final String viewingUid=uid;
            final String viewingRoom=activeRoomId997;
            TextView charisma997=tv("🔥 Room Charisma • Loading actual room gifts…",
                13,0xff6d4e1b,true);
            charisma997.setBackground(bg(0xfffff5dc,12));
            LinearLayout.LayoutParams charismaLp997=new LinearLayout.LayoutParams(-1,dp(58));
            charismaLp997.topMargin=dp(4);
            body.addView(charisma997,charismaLp997);
            db.collection("live_rooms").document(viewingRoom).collection("events")
                .orderBy("createdAt",Query.Direction.DESCENDING).limit(150)
                .get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(events->{
                    if(isFinishing()||isDestroyed())return;
                    long received=0;
                    for(DocumentSnapshot event:events.getDocuments()){
                        if(!"gift".equals(event.getString("type"))
                            ||!viewingUid.equals(event.getString("targetUid")))continue;
                        Long quantity=event.getLong("quantity");
                        if(quantity==null)quantity=event.getLong("qty");
                        received+=Math.min(9,Math.max(1,quantity==null?1:quantity));
                    }
                    charisma997.setText("🔥 Room Charisma • "+received+
                        " received gift(s) in recent 150 events\\n"+
                        "Free activity count only • no paid rank");
                })
                .addOnFailureListener(error->{
                    if(!isFinishing()&&!isDestroyed())
                        charisma997.setText("🔥 Room Charisma unavailable: "+
                            String.valueOf(error.getLocalizedMessage()));
                });
        }
'''
s=s.replace(point,new+point,1)
f.write_text(s)
print("PASS Noble status shown transparently; only real live room gift events count as Charisma")
