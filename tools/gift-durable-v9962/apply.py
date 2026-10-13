#!/usr/bin/env python3
"""KING v9.9.62: real gift ACK journal survives Android process death.
No fake receipt before visible GiftBurst completion; server-checks persisted
event + genuine current Party member before retrying. Paid Recharge stays OFF.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1])
src=root/'app/src/main/java/com/kingplus/social'
file=src/'PartyActivity.java'
s=file.read_text()
anchor='    private void confirmGiftReceived9941(DocumentReference room,DocumentSnapshot event,String uid){'
assert s.count(anchor)==1, 'missing v9.9.61 complete PartyActivity receipt method'
helper='''    /** Durable local record written ONLY after real recipient burst playback.
     * The Firestore event and current room membership are re-read from SERVER.
     * Local preferences are not trusted payment proof or a wallet balance.
     */
    private static final String GIFT_JOURNAL9962="king_gift_receipt_journal_v9962";
    private static final String GIFT_PENDING9962="pending";
    private final java.util.HashSet<String> restoringGiftIds9962=new java.util.HashSet<>();
    private android.content.SharedPreferences giftPrefs9962(){
        return getSharedPreferences(GIFT_JOURNAL9962,MODE_PRIVATE);
    }
    private org.json.JSONObject giftRecord9962(String room,String id,String uid){
        org.json.JSONObject o=new org.json.JSONObject();
        try{
            o.put("room",room);o.put("gift",id);o.put("uid",uid);
            o.put("at",System.currentTimeMillis());
        }catch(org.json.JSONException ignored){}
        return o;
    }
    private java.util.HashSet<String> storedGiftRecords9962(){
        java.util.Set<String> stored=giftPrefs9962().getStringSet(
            GIFT_PENDING9962,java.util.Collections.<String>emptySet());
        return new java.util.HashSet<>(stored);
    }
    private boolean storeGiftReceipt9962(String room,String gift,String uid){
        if(!KingGiftReceiptDurable9962.valid(room,gift,uid))return false;
        java.util.HashSet<String> all=storedGiftRecords9962();
        for(String entry:all){
            try{
                org.json.JSONObject obj=new org.json.JSONObject(entry);
                if(KingGiftReceiptDurable9962.same(room,gift,uid,
                    obj.optString("room"),obj.optString("gift"),obj.optString("uid")))
                    return true; // same physical on-screen Gift ACK, never duplicate
            }catch(org.json.JSONException ignored){}
        }
        if(!KingGiftReceiptDurable9962.canAdd(all.size()))return false;
        all.add(giftRecord9962(room,gift,uid).toString());
        // Synchronous commit: survive process death immediately after playback.
        return giftPrefs9962().edit().putStringSet(GIFT_PENDING9962,all).commit();
    }
    private void removeGiftReceipt9962(String room,String gift,String uid){
        java.util.HashSet<String> all=storedGiftRecords9962();
        boolean changed=false;
        for(String entry:new java.util.HashSet<>(all)){
            try{
                org.json.JSONObject obj=new org.json.JSONObject(entry);
                if(KingGiftReceiptDurable9962.same(room,gift,uid,
                    obj.optString("room"),obj.optString("gift"),obj.optString("uid"))){
                    all.remove(entry);changed=true;
                }
            }catch(org.json.JSONException ignored){}
        }
        if(changed)giftPrefs9962().edit().putStringSet(GIFT_PENDING9962,all).commit();
    }
    private boolean recoverReceiptContext9962(String id,String uid){
        return cloudRoom&&memberSeen&&partyVisualForeground995&&db!=null&&user!=null
            &&roomId!=null&&roomId.equals(id)&&uid.equals(user.getUid())
            &&!isFinishing()&&!isDestroyed();
    }
    /** After reopen, replay ACK not animation. Always verify original gift and
     * receiver membership online. Failed reads leave the durable record intact.
     */
    private void recoverDurableGiftReceipts9962(){
        if(!cloudRoom||!memberSeen||!partyVisualForeground995||user==null||db==null
             ||roomId==null||roomId.isEmpty())return;
        final String current9962=roomId,uid9962=user.getUid();
        final DocumentReference room9962=db.collection("live_rooms").document(current9962);
        for(String record:storedGiftRecords9962()){
            final org.json.JSONObject obj;
            try{obj=new org.json.JSONObject(record);}
            catch(org.json.JSONException ignored){continue;}
            final String storedRoom=obj.optString("room");
            final String giftId=obj.optString("gift");
            final String recipient=obj.optString("uid");
            if(!KingGiftReceiptDurable9962.valid(storedRoom,giftId,recipient)
                ||!current9962.equals(storedRoom)||!uid9962.equals(recipient))continue;
            final String key=current9962+"/"+giftId;
            if(receiptBacklog9955.containsKey(key)||!restoringGiftIds9962.add(key))continue;
            room9962.collection("members").document(uid9962)
                .get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(member->{
                    if(!recoverReceiptContext9962(current9962,uid9962)
                       ||member==null||!member.exists()
                       ||!uid9962.equals(member.getString("uid"))){
                        restoringGiftIds9962.remove(key);return;
                    }
                    room9962.collection("events").document(giftId)
                      .get(com.google.firebase.firestore.Source.SERVER)
                      .addOnSuccessListener(event->{
                          restoringGiftIds9962.remove(key);
                          if(!recoverReceiptContext9962(current9962,uid9962))return;
                          // A device journal is untrusted. Never acknowledge a
                          // different recipient, nonexistent gift or own gift.
                          if(event==null||!event.exists()
                             ||!uid9962.equals(event.getString("targetUid"))
                             ||uid9962.equals(event.getString("actorUid")))return;
                          confirmGiftReceived9941(room9962,event,uid9962);
                      }).addOnFailureListener(e->{
                          restoringGiftIds9962.remove(key);
                          KingStability.nonFatal(this,"gift-journal-event",e);
                      });
                }).addOnFailureListener(e->{
                    restoringGiftIds9962.remove(key);
                    KingStability.nonFatal(this,"gift-journal-member",e);
                });
        }
    }
'''
s=s.replace(anchor,helper+anchor,1)
old='''        receiptBacklog9955.put(key9955,job9955);
        attemptDisplayedGiftReceipt9955(job9955);'''
new='''        // Called from completed GiftBurst playback only. Journal first;
        // never infer receipt from sender write, preview, or animation start.
        boolean journaled9962=storeGiftReceipt9962(room.getId(),id,uid);
        if(!journaled9962)KingStability.nonFatal(this,"gift-journal-save",
            new IllegalStateException("Playback confirmation not durable"));
        receiptBacklog9955.put(key9955,job9955);
        attemptDisplayedGiftReceipt9955(job9955);'''
assert s.count(old)==1,'no completed playback queue anchor'
s=s.replace(old,new,1)
old='''        receiptBacklog9955.remove(job.key);
    }
    private void retryFailureDisplayedGiftReceipt9955'''
new='''        receiptBacklog9955.remove(job.key);
        removeGiftReceipt9962(job.room.getId(),job.event.getId(),job.uid);
    }
    private void retryFailureDisplayedGiftReceipt9955'''
assert s.count(old)==1,'no receipt confirmed cleanup anchor'
s=s.replace(old,new,1)
old='''    private void retryDisplayedGiftReceipts9955(){
        if(!partyVisualForeground995||!memberSeen)return;'''
new='''    private void retryDisplayedGiftReceipts9955(){
        if(!partyVisualForeground995||!memberSeen)return;
        // Recover displayed-but-not-yet-ACKed gifts even after process death.
        // Never replay visuals or grant diamonds from journal entries.
        recoverDurableGiftReceipts9962();'''
assert s.count(old)==1,'no foreground recovery entry'
s=s.replace(old,new,1)
old='''    private void clearDisplayedGiftReceipts9955(){
        pauseDisplayedGiftReceipts9955();
        receiptBacklog9955.clear();
    }'''
new='''    private void clearDisplayedGiftReceipts9955(){
        pauseDisplayedGiftReceipts9955();
        receiptBacklog9955.clear();
        // In-flight async tasks belong to the prior room generation.
        restoringGiftIds9962.clear();
        // Persisted items are deliberately NOT discarded on exit/destroy.
        // They may be retried after same account rejoins that Party.
    }'''
assert s.count(old)==1,'no receipt process-death preservation anchor'
s=s.replace(old,new,1)
file.write_text(s)
(src/'KingGiftReceiptDurable9962.java').write_text('''package com.kingplus.social;
/** Bounded local journal for genuinely displayed Party Gift receiver ACKs.
 * The journal is NOT a balance or server authorization, and every restored
 * gift event + room membership must be re-read online before acknowledgement.
 */
public final class KingGiftReceiptDurable9962 {
 private KingGiftReceiptDurable9962(){}
 public static final int MAX_PENDING=128;
 public static boolean valid(String room,String id,String uid){
   return validPart(room)&&validPart(id)&&validPart(uid);
 }
 private static boolean validPart(String str){
   return str!=null&&!str.isEmpty()&&str.length()<=256
       &&str.indexOf('/')<0&&str.indexOf('\\\\')<0;
 }
 public static boolean canAdd(int count){
   return count>=0&&count<MAX_PENDING;
 }
 public static boolean same(String r,String id,String u,String er,String eid,String eu){
   return valid(r,id,u)&&r.equals(er)&&id.equals(eid)&&u.equals(eu);
 }
}
''')
gradle=root/'app/build.gradle';v=gradle.read_text()
oldver="versionCode 240; versionName '9.9.61-persistent-host-private-game-role-test'"
assert v.count(oldver)==1,'require full v9.9.61 signed APK source'
gradle.write_text(v.replace(oldver,
    "versionCode 241; versionName '9.9.62-durable-gift-delivery-acks-test'"))
assert 'KingPartyVoiceReconnect9957' in s
assert 'KingPartyMicConsent9956' in s
assert 'KingGiftReceiptRetry9955' in s
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print('PASS gift delivery acknowledgment journal persists completed playbacks; no paid money or fake previews')
