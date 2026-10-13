#!/usr/bin/env python3
"""v9.9.55: ACK genuinely displayed recipient gift, retry on temporary Firebase failure.
Receipts are only queued after GiftBurstView playback finishes, never on send.
No coin balance, VIP billing or fake delivery status is changed.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1]); src=root/'app/src/main/java/com/kingplus/social'
p=src/'PartyActivity.java';s=p.read_text()
def rep(old,new):
 global s
 count=s.count(old)
 if count!=1:raise AssertionError(f'Expected one source anchor ({count}): {old[:100]!r}')
 s=s.replace(old,new,1)
rep('    private final java.util.HashSet<String> seenGiftReceipts9941=new java.util.HashSet<>();',
'''    private final java.util.HashSet<String> seenGiftReceipts9941=new java.util.HashSet<>();
    private final java.util.LinkedHashMap<String,PendingGiftReceipt9955> receiptBacklog9955=
        new java.util.LinkedHashMap<>();
    private final android.os.Handler receiptHandler9955=
        new android.os.Handler(android.os.Looper.getMainLooper());
    private static final class PendingGiftReceipt9955 {
        final String key,uid; final DocumentReference room;
        final DocumentSnapshot event; final int generation;
        int failures; boolean inFlight; Runnable retryTask;
        PendingGiftReceipt9955(String k,DocumentReference r,DocumentSnapshot e,
                                String u,int gen){key=k;room=r;event=e;uid=u;generation=gen;}
    }''')
rep('''        drainPendingGifts9944();
        recoverRecentGifts9942();''',
'''        drainPendingGifts9944();
        retryDisplayedGiftReceipts9955();
        recoverRecentGifts9942();''')
rep('''        clearListeners(); unregisterMember(); roomId = null; cloudRoom = false; memberSeen=false;''',
'''        clearListeners(); clearDisplayedGiftReceipts9955();
        unregisterMember(); roomId = null; cloudRoom = false; memberSeen=false;''')
rep('''        partyVisualForeground995=false;
        KingGiftSound993.stopRoomAudio();''',
'''        partyVisualForeground995=false;
        pauseDisplayedGiftReceipts9955();
        KingGiftSound993.stopRoomAudio();''')
rep('''stopInRoomVoice940(true);unregisterMember();clearListeners();try{if(roomMusicPlayer''',
'''stopInRoomVoice940(true);unregisterMember();clearListeners();clearDisplayedGiftReceipts9955();try{if(roomMusicPlayer''')
start=s.index('    private void confirmGiftReceived9941(DocumentReference room,DocumentSnapshot event,String uid){')
end=s.index('    private void awardGiftProgress700(long value)',start)
replacement='''    /** A gift is put in this backlog ONLY after its on-screen burst completes.
     * Failures of the Firestore receipt write must not be lost to gift-ID
     * visual deduplication, nor may retries replay a sound/animation.
     */
    private void confirmGiftReceived9941(DocumentReference room,DocumentSnapshot event,String uid){
        if(room==null||event==null||!event.exists()||uid==null||uid.isEmpty()||db==null)return;
        String id=event.getId(),sender=event.getString("actorUid");
        if(sender==null||sender.isEmpty()||uid.equals(sender)
            ||!uid.equals(event.getString("targetUid")))return;
        String key9955=room.getId()+"/"+id;
        if(receiptBacklog9955.containsKey(key9955))return;
        if(!KingGiftReceiptRetry9955.allowEnqueue(receiptBacklog9955.size())){
            // Never claim a delivery that has no durable recipient receipt.
            KingStability.nonFatal(this,"gift-receipt-queue-full",
                new IllegalStateException("Receipt queue at safe capacity"));
            return;
        }
        PendingGiftReceipt9955 job9955=new PendingGiftReceipt9955(
            key9955,room,event,uid,presenceGeneration967);
        receiptBacklog9955.put(key9955,job9955);
        attemptDisplayedGiftReceipt9955(job9955);
    }
    private boolean receiptContext9955(PendingGiftReceipt9955 job){
        return job!=null&&receiptBacklog9955.get(job.key)==job
            &&cloudRoom&&memberSeen&&db!=null&&user!=null
            &&roomId!=null&&roomId.equals(job.room.getId())
            &&job.uid.equals(user.getUid())&&job.generation==presenceGeneration967
            &&!isFinishing()&&!isDestroyed();
    }
    private void pauseDisplayedGiftReceipts9955(){
        for(PendingGiftReceipt9955 job:new java.util.ArrayList<>(receiptBacklog9955.values())){
            if(job.retryTask!=null){receiptHandler9955.removeCallbacks(job.retryTask);
                job.retryTask=null;}
        }
    }
    private void clearDisplayedGiftReceipts9955(){
        pauseDisplayedGiftReceipts9955();
        receiptBacklog9955.clear();
    }
    private void retryDisplayedGiftReceipts9955(){
        if(!partyVisualForeground995||!memberSeen)return;
        for(PendingGiftReceipt9955 job:new java.util.ArrayList<>(receiptBacklog9955.values())){
            if(!receiptContext9955(job))continue;
            if(job.failures>=KingGiftReceiptRetry9955.MAX_RETRIES)job.failures=0;
            if(!job.inFlight&&job.retryTask==null)
                attemptDisplayedGiftReceipt9955(job);
        }
    }
    private void finishedDisplayedGiftReceipt9955(PendingGiftReceipt9955 job){
        if(receiptBacklog9955.get(job.key)!=job)return;
        if(job.retryTask!=null)receiptHandler9955.removeCallbacks(job.retryTask);
        receiptBacklog9955.remove(job.key);
    }
    private void retryFailureDisplayedGiftReceipt9955(PendingGiftReceipt9955 job,Exception failure){
        job.inFlight=false;
        if(!receiptContext9955(job))return;
        KingStability.nonFatal(this,"gift-receipt-retry",failure);
        if(!partyVisualForeground995||job.retryTask!=null)return;
        if(!KingGiftReceiptRetry9955.shouldRetry(job.failures,true,true)){
            toast("Gift animation shown • receipt not synced. Reopen Party to retry.");
            return;
        }
        job.failures++;
        final long wait9955=KingGiftReceiptRetry9955.delayMs(job.failures);
        job.retryTask=()->{
            job.retryTask=null;
            if(receiptContext9955(job)&&partyVisualForeground995)
                attemptDisplayedGiftReceipt9955(job);
        };
        receiptHandler9955.postDelayed(job.retryTask,wait9955);
    }
    private void attemptDisplayedGiftReceipt9955(PendingGiftReceipt9955 job){
        if(job.inFlight||!receiptContext9955(job)||!partyVisualForeground995)return;
        job.inFlight=true;
        final String id=job.event.getId(),sender=job.event.getString("actorUid");
        if(sender==null||sender.isEmpty()||!job.uid.equals(job.event.getString("targetUid"))){
            job.inFlight=false;finishedDisplayedGiftReceipt9955(job);return;
        }
        final DocumentReference receipt=job.room.collection("gift_receipts").document(id);
        receipt.get(com.google.firebase.firestore.Source.SERVER)
          .addOnSuccessListener(existing->{
              if(!receiptContext9955(job)){job.inFlight=false;return;}
              if(existing!=null&&existing.exists()){
                  job.inFlight=false;
                  if(id.equals(existing.getString("giftId"))&&
                      job.uid.equals(existing.getString("recipientUid"))&&
                      sender.equals(existing.getString("senderUid")))
                      finishedDisplayedGiftReceipt9955(job);
                  else KingStability.nonFatal(this,"gift-receipt-conflict",
                      new IllegalStateException("Existing receipt mismatched"));
                  return;
              }
              java.util.Map<String,Object> data=new HashMap<>();
              data.put("giftId",id);data.put("senderUid",sender);
              data.put("recipientUid",job.uid);
              data.put("giftName",str(job.event,"giftName","Gift"));
              data.put("createdAt",FieldValue.serverTimestamp());
              receipt.set(data).addOnSuccessListener(v->{
                  job.inFlight=false;
                  finishedDisplayedGiftReceipt9955(job);
              }).addOnFailureListener(e->retryFailureDisplayedGiftReceipt9955(job,e));
          }).addOnFailureListener(e->retryFailureDisplayedGiftReceipt9955(job,e));
    }
'''
s=s[:start]+replacement+s[end:]
p.write_text(s)
(src/'KingGiftReceiptRetry9955.java').write_text('''package com.kingplus.social;
/** Live real-gift recipient receipt retry. Never confuses a sent Gift with delivery. */
public final class KingGiftReceiptRetry9955 {
 private KingGiftReceiptRetry9955(){}
 public static final int MAX_PENDING=64, MAX_RETRIES=4;
 public static boolean allowEnqueue(int pending){return pending>=0&&pending<MAX_PENDING;}
 public static boolean shouldRetry(int failures,boolean liveMember,boolean foreground){
   return failures>=0&&failures<MAX_RETRIES&&liveMember&&foreground;
 }
 public static long delayMs(int attempt){return 1000L*(1L<<Math.min(4,Math.max(1,attempt)));}
}
''')
g=root/'app/build.gradle';v=g.read_text();old="versionCode 233; versionName '9.9.54-retry-real-party-results-test'"
assert v.count(old)==1
v=v.replace(old,"versionCode 234; versionName '9.9.55-real-gift-receipt-retry-test'")
g.write_text(v)
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print('PASS genuine played gift receipt ACK retry, bounded and room-scoped, no paid recharge')
