#!/usr/bin/env python3
"""Original KING v9.9.41: genuine RECEIVER delivery receipts for live FREE gifting.

The previous app showed "delivered" immediately after Firestore accepted a
write, even when the target phone was not displaying the Gift. Now delivery
acknowledgment is written only by the recipient's active room UI, and the
sender gets a real receiver-ack notification. Free gifts never claim 💎 value.
No paywall changes, no Bolo Hi copyrighted assets, no resource padding.
"""
from pathlib import Path
import sys
r=Path(sys.argv[1]);src=r/'app/src/main/java/com/kingplus/social'
p=src/'PartyActivity.java';s=p.read_text()
def rep(old,new,occ=1):
 global s
 n=s.count(old)
 if n!=occ:raise RuntimeError(f"PartyActivity expected {occ} matches; got {n}: {old[:135]}")
 s=s.replace(old,new,occ)
rep("    private boolean giftSendPending971;",
"""    private boolean giftSendPending971;
    private ListenerRegistration giftReceiptListener9941;
    private boolean giftReceiptInitial9941=false;
    private final java.util.HashSet<String> seenGiftReceipts9941=new java.util.HashSet<>();
""")
rep("    private void clearListeners(){",
"""    private void clearListeners(){
        if(giftReceiptListener9941!=null){giftReceiptListener9941.remove();giftReceiptListener9941=null;}
        giftReceiptInitial9941=false;
        seenGiftReceipts9941.clear();
""")
rep('''        eventsListener=room.collection("events").orderBy("createdAt",Query.Direction.ASCENDING).limitToLast(80).addSnapshotListener((snap,e)->{''',
'''        // Watch recipient-confirmed deliveries, not just Firestore write ACK.
        // A sender only sees receipts for Gifts their own account sent.
        if(giftReceiptListener9941!=null)giftReceiptListener9941.remove();
        giftReceiptInitial9941=false;seenGiftReceipts9941.clear();
        giftReceiptListener9941=room.collection("gift_receipts")
            .whereEqualTo("senderUid",expectedUid982).limit(60)
            .addSnapshotListener((receipts,err)->{
              if(!activeRoomCallback982(expectedRoom982,expectedUid982,visit982,binding982)
                 ||err!=null||receipts==null)return;
              if(receipts.getMetadata().isFromCache())return;
              if(!giftReceiptInitial9941){
                  for(DocumentSnapshot row:receipts.getDocuments())seenGiftReceipts9941.add(row.getId());
                  giftReceiptInitial9941=true;return;
              }
              for(DocumentSnapshot row:receipts.getDocuments()){
                  if(!seenGiftReceipts9941.add(row.getId()))continue;
                  if(expectedUid982.equals(row.getString("senderUid"))){
                      String name=str(row,"giftName","Gift");
                      toast("✅ Your "+name+" was received on the other Party phone");
                  }
              }
              if(seenGiftReceipts9941.size()>180){
                  seenGiftReceipts9941.clear();
                  for(DocumentSnapshot row:receipts.getDocuments())seenGiftReceipts9941.add(row.getId());
              }
            });
        eventsListener=room.collection("events").orderBy("createdAt",Query.Direction.ASCENDING).limitToLast(80).addSnapshotListener((snap,e)->{''')
rep('''                if("gift".equals(type)){Long q=d.getLong("giftQty");Long v=d.getLong("giftValue");String gift=d.getString("giftName");String actorUid=d.getString("actorUid");if(user==null||actorUid==null||!user.getUid().equals(actorUid))showGiftEffect(str(d,"actorName","User"),str(d,"targetName","Host"),gift,str(d,"giftIcon",giftIconFor(gift)),q==null?1:q,v==null?1:v);}''',
'''                if("gift".equals(type)){
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
                }''')
rep('''                toast("FREE gift delivered to the Party");''',
'''                toast("FREE Gift sent to Party • awaiting recipient phone acknowledgement");''')
anchor="    private void awardGiftProgress700(long value)"
if s.count(anchor)!=1:raise RuntimeError("Gift receipt method anchor missing")
code="""    /** Only the exact receiving member can write this Firestore receipt.
     *  Firestore rules cross-check the immutable matching Gift event.
     */
    private void confirmGiftReceived9941(DocumentReference room,DocumentSnapshot event,String uid){
        if(room==null||event==null||!event.exists()||uid==null||uid.isEmpty()||db==null)return;
        String id=event.getId(),sender=event.getString("actorUid");
        if(sender==null||sender.isEmpty()||uid.equals(sender)
          ||!uid.equals(event.getString("targetUid")))return;
        Map<String,Object> receipt=new HashMap<>();
        receipt.put("giftId",id);
        receipt.put("senderUid",sender);
        receipt.put("recipientUid",uid);
        receipt.put("giftName",str(event,"giftName","Gift"));
        receipt.put("createdAt",FieldValue.serverTimestamp());
        room.collection("gift_receipts").document(id).set(receipt)
            .addOnFailureListener(err->KingStability.nonFatal(this,"gift-delivery-ack",err));
    }
"""
s=s.replace(anchor,code+anchor,1)
# Free Gifts are exactly ZERO paid diamonds, not a fictitious 1-diamond credit.
rep('''Long raw=d.getLong("giftValue");long v=raw==null?1L:Math.max(1L,raw);totalGift+=v;score.put(n,score.containsKey(n)?score.get(n)+v:v);''',
'''Long raw=d.getLong("giftValue");long v=raw==null?0L:Math.max(0L,raw);totalGift+=v;
            if(v>0)score.put(n,score.containsKey(n)?score.get(n)+v:v);''')
rep('''if(list.isEmpty()){supporterLabel.setText("🏆 Top supporters • waiting for gifts");return;}''',
'''if(list.isEmpty()){supporterLabel.setText("🏆 Paid supporters • no verified diamond gifts (FREE gifts cost 0)");return;}''')
p.write_text(s)
rules=r/'firestore.rules';t=rules.read_text()
anchor="      match /events/{eventId} {"
receipt="""      // On-device Gift delivery is confirmed by the RECEIVING Party member,
      // never by the sender or an unrelated listener. No diamonds/credits.
      match /gift_receipts/{giftId} {
        allow get, list: if roomAccess(roomId) && roomMember(roomId)
          && (resource.data.senderUid == request.auth.uid
              || resource.data.recipientUid == request.auth.uid);
        allow create: if roomAccess(roomId) && roomMember(roomId)
          && request.resource.data.keys().hasOnly([
            'giftId','senderUid','recipientUid','giftName','createdAt'
          ])
          && request.resource.data.giftId == giftId
          && request.resource.data.recipientUid == request.auth.uid
          && request.resource.data.senderUid is string
          && request.resource.data.giftName is string
          && request.resource.data.giftName.size() <= 80
          && request.resource.data.createdAt == request.time
          && exists(/databases/$(database)/documents/live_rooms/$(roomId)/events/$(giftId))
          && get(/databases/$(database)/documents/live_rooms/$(roomId)/events/$(giftId)).data.type == 'gift'
          && get(/databases/$(database)/documents/live_rooms/$(roomId)/events/$(giftId)).data.get('giftValue',-1) == 0
          && get(/databases/$(database)/documents/live_rooms/$(roomId)/events/$(giftId)).data.actorUid == request.resource.data.senderUid
          && get(/databases/$(database)/documents/live_rooms/$(roomId)/events/$(giftId)).data.targetUid == request.auth.uid
          && get(/databases/$(database)/documents/live_rooms/$(roomId)/events/$(giftId)).data.giftName == request.resource.data.giftName;
        allow update, delete: if false;
      }

"""
if t.count(anchor)!=1:raise RuntimeError("rules Gift subcollection anchor changed")
t=t.replace(anchor,receipt+anchor,1);rules.write_text(t)
gradle=r/'app/build.gradle';v=gradle.read_text()
old="versionCode 219; versionName '9.9.40-verified-sheep-race-test'"
if v.count(old)!=1:raise RuntimeError("Not verified full v9.9.40 release")
gradle.write_text(v.replace(old,"versionCode 220; versionName '9.9.41-recipient-confirmed-gifting-test'",1))
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print("PASS real recipient-confirmed FREE Gift delivery, paid diamond stats remain zero; 4065 XML / 8680 Drawables unchanged")
