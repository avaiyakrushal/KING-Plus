#!/usr/bin/env python3
"""v9.9.42 restores Gift playback and real recipient receipts after background/reconnect.

No new display-only XML/Drawable resources. Apply to exact verified v9.9.41.
"""
from pathlib import Path
import sys,re
r=Path(sys.argv[1]);s=r/'app/src/main/java/com/kingplus/social'
party=s/'PartyActivity.java';t=party.read_text()
def one(old,new):
 global t
 n=t.count(old)
 if n!=1:raise RuntimeError(f"Party source anchor mismatch {n}: {old[:130]}")
 t=t.replace(old,new,1)
one("    private final java.util.HashSet<String> seenGiftReceipts9941=new java.util.HashSet<>();",
"""    private final java.util.HashSet<String> seenGiftReceipts9941=new java.util.HashSet<>();
    private final java.util.HashSet<String> shownGiftIds9942=new java.util.HashSet<>();
    private String shownGiftRoom9942="";
""")
one("""        seenGiftReceipts9941.clear();

        ++roomListenerGeneration982;""",
"""        seenGiftReceipts9941.clear();
        // Preserve per-room event IDs across renderParty() rerenders.

        ++roomListenerGeneration982;""")
one("""        partyVisualForeground995=true;""",
"""        partyVisualForeground995=true;
        recoverRecentGifts9942();""")
one('''        eventsListener=room.collection("events").orderBy("createdAt",Query.Direction.ASCENDING).limitToLast(80).addSnapshotListener((snap,e)->{''',
'''        eventsListener=room.collection("events").orderBy("createdAt",Query.Direction.ASCENDING).limitToLast(80)
          .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE,(snap,e)->{''')
one('''            if(!activeRoomCallback982(expectedRoom982,expectedUid982,visit982,binding982)||e!=null||snap==null||feedBox==null)return;feedBox.removeAllViews();refreshSupporters(snap);''',
'''            if(!activeRoomCallback982(expectedRoom982,expectedUid982,visit982,binding982)||e!=null||snap==null||feedBox==null)return;
            if(snap.getMetadata().isFromCache())return; // server must verify before Gift ID dedupe
            feedBox.removeAllViews();refreshSupporters(snap);''')
one("""                String id=d.getId();if(!eventSnapshotReady){processedEventIds.add(id);continue;}if(processedEventIds.contains(id))continue;processedEventIds.add(id);""",
"""                String id=d.getId();
                if(!eventSnapshotReady){
                    processedEventIds.add(id);
                    // Previous versions permanently skipped initial snapshots, so
                    // a gift sent while the phone was offline could never be received.
                    if("gift".equals(d.getString("type"))&&!snap.getMetadata().isFromCache())
                        displayConfirmedGift9942(room,d,true);
                    continue;
                }
                if(processedEventIds.contains(id))continue;
                processedEventIds.add(id);""")
old='''                if("gift".equals(type)){
                    Long q=d.getLong("giftQty"),v=d.getLong("giftValue");
                    String gift=d.getString("giftName"),actorUid=d.getString("actorUid");
                    boolean fromSomeoneElse=user==null||actorUid==null||!user.getUid().equals(actorUid);
                    if(fromSomeoneElse){
                        showGiftEffect(str(d,"actorName","User"),str(d,"targetName","Host"),
                            gift,str(d,"giftIcon",giftIconFor(gift)),q==null?1:q,v==null?0:v);
                        // This is a real display receipt from the recipient phone.
                        // Other Party spectators can watch the effect but cannot ACK it.
                        if(user!=null&&partyVisualForeground995
                            &&user.getUid().equals(d.getString("targetUid"))
                            && !d.getMetadata().hasPendingWrites()
                            && !snap.getMetadata().isFromCache()){
                            confirmGiftReceived9941(room,d,user.getUid());
                        }
                    }
                }'''
new='''                if("gift".equals(type)){
                    if(!snap.getMetadata().isFromCache())
                        displayConfirmedGift9942(room,d,false);
                }'''
one(old,new)
anchor='''    private void confirmGiftReceived9941(DocumentReference room,DocumentSnapshot event,String uid){'''
methods="""    // As a user returns from a game, call, or screen lock, reload recent Gift
    // events from SERVER. Old static initial-snapshot dedupe must not eat Gifts.
    private void recoverRecentGifts9942(){
        if(!cloudRoom||roomId==null||user==null||db==null||!memberSeen
           ||!partyVisualForeground995||partyShell940==null)return;
        final String recoveringRoom9942=roomId, recoveringUid9942=user.getUid();
        final int gen9942=presenceGeneration967;
        final DocumentReference room9942=db.collection("live_rooms").document(recoveringRoom9942);
        room9942.collection("events")
          .orderBy("createdAt",Query.Direction.DESCENDING).limit(60)
          .get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(events->{
             if(!isActivePresence967(recoveringRoom9942,recoveringUid9942,gen9942)
                 ||!partyVisualForeground995||events==null)return;
             for(DocumentSnapshot event:events.getDocuments())
                 if("gift".equals(event.getString("type")))
                     displayConfirmedGift9942(room9942,event,true);
          }).addOnFailureListener(error->KingStability.nonFatal(this,"gift-reconnect",error));
    }

    // ACK only a real, fresh, server-confirmed event while Party UI is
    // actually foregrounded. Suppress duplicate sounds/effects per event ID.
    private void displayConfirmedGift9942(DocumentReference room,DocumentSnapshot event,boolean replay){
        if(room==null||event==null||user==null||!cloudRoom||!memberSeen
           ||!partyVisualForeground995||partyShell940==null||liveEmojiStageV530==null
           ||liveEmojiStageV530.getParent()==null||event.getMetadata().hasPendingWrites()
           ||!"gift".equals(event.getString("type")))return;
        String ownUid9942=user.getUid();
        String sender9942=event.getString("actorUid");
        String recipient9942=event.getString("targetUid");
        boolean isRecipient9942=ownUid9942.equals(recipient9942);
        if(replay&&!isRecipient9942)return;
        com.google.firebase.Timestamp created9942=event.getTimestamp("createdAt");
        if(created9942==null||
           !KingGiftReplayGuard9942.allow(System.currentTimeMillis(),
               created9942.toDate().getTime(),sender9942,ownUid9942,replay,isRecipient9942))return;
        if(!room.getId().equals(shownGiftRoom9942)){
            shownGiftRoom9942=room.getId();shownGiftIds9942.clear();
        }
        if(!shownGiftIds9942.add(event.getId()))return;
        if(shownGiftIds9942.size()>220){
            shownGiftIds9942.clear();shownGiftIds9942.add(event.getId());
        }
        String name9942=str(event,"giftName","Gift");
        Long count9942=event.getLong("giftQty"),value9942=event.getLong("giftValue");
        showGiftEffect(str(event,"actorName","User"),str(event,"targetName","Member"),
            name9942,str(event,"giftIcon",giftIconFor(name9942)),
            count9942==null?1:count9942,value9942==null?0:value9942);
        if(isRecipient9942)confirmGiftReceived9941(room,event,ownUid9942);
    }

"""
one(anchor,methods+anchor)
# Avoid unnecessary overwrites of a previously recorded receipt when returning.
one('''        room.collection("gift_receipts").document(id).set(receipt)
            .addOnFailureListener(err->KingStability.nonFatal(this,"gift-delivery-ack",err));''',
'''        final DocumentReference receiptRef9942=room.collection("gift_receipts").document(id);
        receiptRef9942.get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(existing->{
             if(existing!=null&&existing.exists())return;
             if(!cloudRoom||roomId==null||!roomId.equals(room.getId())
                ||user==null||!uid.equals(user.getUid())||!partyVisualForeground995)return;
             receiptRef9942.set(receipt)
                 .addOnFailureListener(err->KingStability.nonFatal(this,"gift-delivery-ack",err));
          }).addOnFailureListener(err->KingStability.nonFatal(this,"gift-receipt-check",err));''')
# A reconnect must also not cause the Game moderator to evaluate stale cache
# snapshots as new Tic Tac Toe turns.
game=s/'RoomGameActivity.java';g=game.read_text()
a='''if(moderator&&"ttt".equals(type)&&"active".equals(status))processTttMove864();'''
b='''if(moderator&&"ttt".equals(type)&&"active".equals(status)
    &&q!=null&&!q.getMetadata().isFromCache())processTttMove864();'''
if g.count(a)!=1:raise RuntimeError("Tic Tac Toe stale turn guard anchor changed")
game.write_text(g.replace(a,b,1))
party.write_text(t)
(s/'KingGiftReplayGuard9942.java').write_text("""package com.kingplus.social;
/** Exact-bound event replay policy; independent of Android and Firebase classes. */
public final class KingGiftReplayGuard9942 {
 private KingGiftReplayGuard9942(){}
 public static boolean allow(long now,long created,String sender,String ownUid,
                             boolean replay,boolean recipient){
   if(now<=0||created<=0||sender==null||ownUid==null
       ||sender.isEmpty()||ownUid.isEmpty()||sender.equals(ownUid))return false;
   if(replay&&!recipient)return false;
   long age=now-created;
   return age>=-10000L && age<=120000L;
 }
}
""")
gradle=r/'app/build.gradle';v=gradle.read_text()
old="versionCode 220; versionName '9.9.41-recipient-confirmed-gifting-test'"
if v.count(old)!=1:raise RuntimeError("Not the verified v9.9.41 source")
gradle.write_text(v.replace(old,"versionCode 221; versionName '9.9.42-gift-resume-game-sync-test'"))
assert 'LIVE_RECHARGE = false' in (s/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (s/'KingPremiumAccess981.java').read_text()
print("PASS real Gift on-resume/rejoin delivery, server receipt dedupe, game cache turn guard, payment OFF")
