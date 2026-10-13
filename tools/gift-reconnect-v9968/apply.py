#!/usr/bin/env python3
"""KING v9.9.68: real Gift recovery on Party network restoration."""
from pathlib import Path
import sys
root=Path(sys.argv[1]);src=root/'app/src/main/java/com/kingplus/social'
f=src/'PartyActivity.java';s=f.read_text()
old='''                        verifyAndRejoinVoice9957();
                    }else if(cloudRoom&&roomId!=null&&!memberSeen&&'''
new='''                        verifyAndRejoinVoice9957();
                        // Resume truly displayed recipient ACK and recover
                        // recent missed server Gifts without reopening Party.
                        recoverGiftsAfterNetworkReturn9968();
                    }else if(cloudRoom&&roomId!=null&&!memberSeen&&'''
assert s.count(old)==1,'Party network restore anchor missing'
s=s.replace(old,new,1)
anchor='''    @Override protected void onResume(){'''
assert s.count(anchor)==1
helper='''    /** Refresh real Gift delivery when Android network comes back while
     * Party remains on screen. Verify current signed-in member from SERVER
     * before attempting any recipient receipt or animation.
     */
    private void recoverGiftsAfterNetworkReturn9968(){
        if(!KingGiftReturnGate9968.shouldRecover(
            cloudRoom,memberSeen,partyVisualForeground995,db!=null,
            user!=null,roomId!=null&&!roomId.isEmpty()))return;
        final String expectedRoom=roomId,expectedUid=user.getUid();
        final int expectedGeneration=presenceGeneration967;
        db.collection("live_rooms").document(expectedRoom)
          .collection("members").document(expectedUid)
          .get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(member->{
              if(!isActivePresence967(expectedRoom,expectedUid,expectedGeneration)
                ||!partyVisualForeground995||isFinishing()||isDestroyed()
                ||member==null||!member.exists()
                ||!expectedUid.equals(member.getString("uid")))return;
              // Previously animated Gifts must only retry ACK; never
              // replay the same sound/burst for already acknowledged Gifts.
              retryDisplayedGiftReceipts9955();
              recoverRecentGifts9942();
              drainPendingGifts9944();
          }).addOnFailureListener(e->
                KingStability.nonFatal(this,"gift-reconnect-server",e));
    }
'''
s=s.replace(anchor,helper+anchor,1)
old='''                          if(event==null||!event.exists()
                             ||!uid9962.equals(event.getString("targetUid"))
                             ||uid9962.equals(event.getString("actorUid")))return;
                          confirmGiftReceived9941(room9962,event,uid9962);'''
new='''                          if(event==null||!event.exists())return;
                          Long count9968=event.getLong("giftQty");
                          if(!KingGiftReturnGate9968.validServerGift(
                                giftId,event.getString("giftId"),
                                event.getString("type"),
                                Boolean.TRUE.equals(event.getBoolean("freeGift")),
                                count9968==null?0:count9968.longValue(),
                                event.getString("actorUid"),
                                event.getString("targetUid"),uid9962))return;
                          confirmGiftReceived9941(room9962,event,uid9962);'''
assert s.count(old)==1,'Durable gift restoration event anchor missing'
s=s.replace(old,new,1)
f.write_text(s)
(src/'KingGiftReturnGate9968.java').write_text('''package com.kingplus.social;
/** Source of truth for genuine FREE Gift after Party connectivity returns. */
public final class KingGiftReturnGate9968 {
 private KingGiftReturnGate9968(){}
 public static boolean shouldRecover(boolean cloud,boolean member,
         boolean foreground,boolean db,boolean user,boolean room){
     return cloud&&member&&foreground&&db&&user&&room;
 }
 public static boolean validServerGift(String documentId,String giftId,String type,
     boolean free,long qty,String sender,String receiver,String signedIn){
     return documentId!=null&&!documentId.isEmpty()
       &&documentId.equals(giftId)&&"gift".equals(type)&&free
       &&qty>=1&&qty<=9&&sender!=null&&!sender.isEmpty()
       &&receiver!=null&&!receiver.isEmpty()
       &&receiver.equals(signedIn)&&!receiver.equals(sender);
 }
}
''')
g=root/'app/build.gradle';v=g.read_text()
old="versionCode 246; versionName '9.9.67-live-party-draw-round-integrity-test'"
assert v.count(old)==1,'requires complete v9.9.67 base'
g.write_text(v.replace(old,"versionCode 247; versionName '9.9.68-live-gift-network-recovery-test'"))
for k in ('KingGiftReceiptDurable9962','KingGiftBurstBackpressure9963',
          'KingPartyVoiceReconnect9957','KingPartyMicConsent9956'):
 assert k in s,k
game=(src/'RoomGameActivity.java').read_text()
assert 'KingLiveDrawRound9967.allowed' in game
assert 'KingTttFairRoster9960.pickPair' in game
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print('PASS real Gift Party reconnect after SERVER member/event verification; no fake receipt or billing')
