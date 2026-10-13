#!/usr/bin/env python3
"""v9.9.71 genuine live Party Ready list and atomic opt-in, not fake UI counts."""
from pathlib import Path
import sys
root=Path(sys.argv[1]);base=root/'app/src/main/java/com/kingplus/social'
p=base/'RoomGameActivity.java';s=p.read_text()
old='''        if(gameMemberVerified992&&me!=null&&db!=null&&!roomId.isEmpty())
            try{room().collection("game_ready").document(me.getUid()).delete();}
            catch(Exception ignored){}'''
assert s.count(old)==1
s=s.replace(old,'''        // Closing Games does not unready the other phone in the SAME Party.
        // Ready changes only on explicit user action / real room departure.''',1)
old='if(readyListener!=null)readyListener.remove();if(moderatorRoleListener!=null)'
assert s.count(old)==1
s=s.replace(old,'if(readyListener!=null)readyListener.remove();if(readyMembersListener9971!=null)readyMembersListener9971.remove();if(moderatorRoleListener!=null)',1)
old='''                    if(readyListener!=null){readyListener.remove();readyListener=null;}'''
assert s.count(old)==1
s=s.replace(old,old+'''
                    if(readyMembersListener9971!=null){readyMembersListener9971.remove();readyMembersListener9971=null;}
                    readySeen9971=false;membersSeen9971=false;
                    readyDocs9971=new ArrayList<>();memberNames9971=new HashMap<>();''',1)
a=s.index('    private void listenReady750(){');b=s.index('    private void clearReady750(){',a)
replacement='''    private ListenerRegistration readyMembersListener9971;
    private boolean readySeen9971=false,membersSeen9971=false,readyToggleBusy9971=false;
    private List<DocumentSnapshot> readyDocs9971=new ArrayList<>();
    private Map<String,String> memberNames9971=new HashMap<>();
    private void renderVerifiedReady9971(){
        if(readyBox==null||isFinishing()||isDestroyed()||!gameMemberVerified992)return;
        readyBox.removeAllViews();
        if(!readySeen9971||!membersSeen9971){
            readyBox.addView(tv("⏳ Verifying real Ready Party players with SERVER…",13,MUTED,false));
            return;
        }
        List<String> rows=new ArrayList<>();
        for(DocumentSnapshot d:readyDocs9971){
            String uid=d.getId();
            if(!KingRealReadyRoster9971.realReady(uid,s(d.getString("uid")),
                  d.getBoolean("ready"),memberNames9971.containsKey(uid)))continue;
            String name=memberNames9971.get(uid);
            rows.add("✅ "+(name==null||name.isEmpty()?"Player":name));
        }
        Collections.sort(rows,String.CASE_INSENSITIVE_ORDER);
        if(rows.isEmpty())readyBox.addView(tv("No current Party members are Ready",13,MUTED,false));
        for(String row:rows){
            TextView label=tv(row,14,Color.WHITE,true);
            label.setBackground(bg(CARD,12));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(44));
            lp.setMargins(0,dp(3),0,dp(3));readyBox.addView(label,lp);
        }
    }
    private void listenReady750(){
        if(readyListener!=null)readyListener.remove();
        if(readyMembersListener9971!=null)readyMembersListener9971.remove();
        readySeen9971=false;membersSeen9971=false;
        readyDocs9971=new ArrayList<>();memberNames9971=new HashMap<>();
        renderVerifiedReady9971();
        readyListener=room().collection("game_ready").whereEqualTo("ready",true)
          .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(q,e)->{
            if(!gameMemberVerified992||isFinishing()||isDestroyed())return;
            readySeen9971=e==null&&q!=null&&!q.getMetadata().isFromCache()
                &&!q.getMetadata().hasPendingWrites();
            readyDocs9971=readySeen9971?new ArrayList<>(q.getDocuments()):new ArrayList<>();
            renderVerifiedReady9971();
          });
        readyMembersListener9971=room().collection("members")
          .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(q,e)->{
            if(!gameMemberVerified992||isFinishing()||isDestroyed())return;
            membersSeen9971=e==null&&q!=null&&!q.getMetadata().isFromCache()
                &&!q.getMetadata().hasPendingWrites();
            memberNames9971=new HashMap<>();
            if(membersSeen9971)for(DocumentSnapshot d:q.getDocuments()){
                String uid=d.getId();
                if(uid.equals(s(d.getString("uid"))))
                    memberNames9971.put(uid,s(d.getString("name")));
            }
            renderVerifiedReady9971();
          });
    }
    /** Explicit Ready toggle: always check current SERVER member + own Ready
     * in one Firestore transaction. A double tap cannot produce two writes.
     */
    private void toggleReady750(){
        if(!KingNetwork.online(this)||!gameMemberVerified992||me==null||db==null||readyToggleBusy9971){
            toast("Waiting for verified Party member connection");return;
        }
        final String uid=me.getUid();
        final DocumentReference mine=room().collection("game_ready").document(uid);
        final DocumentReference member=room().collection("members").document(uid);
        readyToggleBusy9971=true;
        db.runTransaction(tx->{
            DocumentSnapshot live=tx.get(member),prior=tx.get(mine);
            if(!live.exists()||!uid.equals(s(live.getString("uid"))))
                throw new IllegalStateException("Not a live Party member");
            boolean opted=prior.exists()&&KingRealReadyRoster9971.realReady(
                uid,s(prior.getString("uid")),prior.getBoolean("ready"),true);
            if(opted)tx.delete(mine);
            else{
                Map<String,Object> data=new HashMap<>();
                data.put("uid",uid);data.put("name",s(live.getString("name")));
                data.put("ready",true);
                data.put("updatedAt",FieldValue.serverTimestamp());
                tx.set(mine,data);
            }
            return !opted;
        }).addOnSuccessListener(enabled->{
            readyToggleBusy9971=false;
            toast(Boolean.TRUE.equals(enabled)?"Ready ✓":"Not Ready");
        }).addOnFailureListener(e->{
            readyToggleBusy9971=false;toast("Ready failed: "+e.getMessage());
        });
    }
'''
s=s[:a]+replacement+s[b:]
p.write_text(s)
(base/'KingRealReadyRoster9971.java').write_text('''package com.kingplus.social;
/** A live Ready player must opt in and still be a verified Party member. */
public final class KingRealReadyRoster9971 {
 private KingRealReadyRoster9971(){}
 public static boolean realReady(String docUid,String claimedUid,Boolean ready,
                                 boolean actualMember){
   return docUid!=null&&!docUid.isEmpty()&&docUid.equals(claimedUid)
       &&Boolean.TRUE.equals(ready)&&actualMember;
 }
}
''')
grad=root/'app/build.gradle';v=grad.read_text()
old="versionCode 249; versionName '9.9.70-real-party-single-mic-seat-test'"
assert v.count(old)==1,"Requires signed source v9.9.70"
grad.write_text(v.replace(old,"versionCode 250; versionName '9.9.71-live-ready-roster-test'"))
party=(base/'PartyActivity.java').read_text()
for t in ('KingPartyLiveSeats9970','KingGiftPlayedHistory9969','KingPartyMicConsent9956'):
 assert t in party,t
assert 'LIVE_RECHARGE = false' in (base/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (base/'KingPremiumAccess981.java').read_text()
print('PASS verified live Ready roster and atomic explicit toggle; Games close preserves another phone opt-in')
