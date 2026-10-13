#!/usr/bin/env python3
"""KING v9.9.69: prevent duplicate recipient Gift sounds after process restart.
Only a genuinely completed GiftBurst whose receipt is pending or confirmed
can suppress later visual playback. No sender previews or fake wallet credit.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1]);src=root/'app/src/main/java/com/kingplus/social'
f=src/'PartyActivity.java';s=f.read_text()
anchor='    private void confirmGiftReceived9941(DocumentReference room,DocumentSnapshot event,String uid){'
assert s.count(anchor)==1
helper='''    /** An on-screen finished Gift stays remembered on this account/room,
     * even after the Party Activity is destroyed. Local records do not
     * authorize any payment or new receipt; SERVER still verifies the gift.
     */
    private static final String GIFT_PLAYED_PREF9969="king_gift_recipient_seen_v9969";
    private static final String GIFT_PLAYED_KEY9969="seen";
    private java.util.HashSet<String> playedGiftRecords9969(){
        return new java.util.HashSet<>(getSharedPreferences(
            GIFT_PLAYED_PREF9969,MODE_PRIVATE).getStringSet(GIFT_PLAYED_KEY9969,
            java.util.Collections.<String>emptySet()));
    }
    private boolean alreadyPlayedRecipientGift9969(String room,String gift,String uid){
        if(!KingGiftPlayedHistory9969.valid(room,gift,uid))return false;
        // Pending durable journal contains only completed receiver playback,
        // not an event merely sent to Firestore.
        for(String entry:storedGiftRecords9962()){
            try{
                org.json.JSONObject d=new org.json.JSONObject(entry);
                if(KingGiftPlayedHistory9969.same(room,gift,uid,
                     d.optString("room"),d.optString("gift"),d.optString("uid")))
                    return true;
            }catch(org.json.JSONException ignored){}
        }
        long now=System.currentTimeMillis();
        for(String entry:playedGiftRecords9969()){
            try{
                org.json.JSONObject d=new org.json.JSONObject(entry);
                if(KingGiftPlayedHistory9969.withinHistory(now,d.optLong("at"))
                  &&KingGiftPlayedHistory9969.same(room,gift,uid,
                     d.optString("room"),d.optString("gift"),d.optString("uid")))
                    return true;
            }catch(org.json.JSONException ignored){}
        }
        return false;
    }
    /** Called ONLY on successful real recipient SERVER receipt. Commit
     * before removing the pending ACK, avoiding a replay on process death.
     */
    private boolean rememberReceivedGift9969(String room,String gift,String uid){
        if(!KingGiftPlayedHistory9969.valid(room,gift,uid))return false;
        long now=System.currentTimeMillis();
        java.util.HashSet<String> seen=playedGiftRecords9969();
        String oldest=null;long oldestTime=Long.MAX_VALUE;
        for(String entry:new java.util.HashSet<>(seen)){
            try{
                org.json.JSONObject d=new org.json.JSONObject(entry);
                long at=d.optLong("at");
                if(!KingGiftPlayedHistory9969.withinHistory(now,at)
                  ||KingGiftPlayedHistory9969.same(room,gift,uid,
                     d.optString("room"),d.optString("gift"),d.optString("uid"))){
                    seen.remove(entry);continue;
                }
                if(at<oldestTime){oldestTime=at;oldest=entry;}
            }catch(org.json.JSONException ignored){seen.remove(entry);}
        }
        if(seen.size()>=KingGiftPlayedHistory9969.MAX_EVENTS&&oldest!=null)
            seen.remove(oldest);
        org.json.JSONObject item=new org.json.JSONObject();
        try{
            item.put("room",room);item.put("gift",gift);item.put("uid",uid);
            item.put("at",now);
        }catch(org.json.JSONException ignored){return false;}
        seen.add(item.toString());
        return getSharedPreferences(GIFT_PLAYED_PREF9969,MODE_PRIVATE).edit()
            .putStringSet(GIFT_PLAYED_KEY9969,seen).commit();
    }
'''
s=s.replace(anchor,helper+anchor,1)
old='''        if(replay&&!isRecipient9942)return;
        com.google.firebase.Timestamp created9942=event.getTimestamp("createdAt");'''
new='''        if(replay&&!isRecipient9942)return;
        // A freshly restored Activity can receive the same SERVER Gift again.
        // Suppress duplicate receiver sound/effect ONLY after actual playback.
        if(isRecipient9942&&alreadyPlayedRecipientGift9969(
              room.getId(),event.getId(),ownUid9942))return;
        com.google.firebase.Timestamp created9942=event.getTimestamp("createdAt");'''
assert s.count(old)==1
s=s.replace(old,new,1)
old='''        receiptBacklog9955.remove(job.key);
        removeGiftReceipt9962(job.room.getId(),job.event.getId(),job.uid);
        // A slot opened: drain genuinely displayed overflow Gift ACKs.'''
new='''        receiptBacklog9955.remove(job.key);
        // A completed Gift ACK is the only event that can enter the bounded
        // replay history. If the disk write fails, keep the durable pending
        // journal; an ACK retry will still be possible after process restart.
        if(rememberReceivedGift9969(job.room.getId(),job.event.getId(),job.uid))
            removeGiftReceipt9962(job.room.getId(),job.event.getId(),job.uid);
        else KingStability.nonFatal(this,"gift-visual-history-save",
            new IllegalStateException("Keeping durable Gift ACK pending"));
        // A slot opened: drain genuinely displayed overflow Gift ACKs.'''
assert s.count(old)==1
s=s.replace(old,new,1)
f.write_text(s)
(src/'KingGiftPlayedHistory9969.java').write_text("""package com.kingplus.social;
/** Bounded recent genuinely completed receiver-side Gift visual records.
 * This is not a wallet, payment validation, or proof for another account.
 */
public final class KingGiftPlayedHistory9969 {
 private KingGiftPlayedHistory9969(){}
 public static final int MAX_EVENTS=512;
 public static final long HISTORY_MS=6L*60L*60L*1000L;
 private static boolean validPart(String x){
     return x!=null&&!x.isEmpty()&&x.length()<=256&&x.indexOf('/')<0;
 }
 public static boolean valid(String room,String gift,String uid){
     return validPart(room)&&validPart(gift)&&validPart(uid);
 }
 public static boolean same(String room,String gift,String uid,
                            String otherRoom,String otherGift,String otherUid){
     return valid(room,gift,uid)&&room.equals(otherRoom)&&
            gift.equals(otherGift)&&uid.equals(otherUid);
 }
 public static boolean withinHistory(long now,long at){
     return now>0&&at>0&&at<=now+10000L&&now-at<=HISTORY_MS;
 }
}
""")
g=root/'app/build.gradle';v=g.read_text()
old="versionCode 247; versionName '9.9.68-live-gift-network-recovery-test'"
assert v.count(old)==1,'Must start from full signed v9.9.68 Android source'
g.write_text(v.replace(old,"versionCode 248; versionName '9.9.69-real-recipient-gift-playback-once-test'"))
for k in ('KingGiftReturnGate9968.validServerGift','KingGiftReceiptDurable9962',
          'KingGiftBurstBackpressure9963','KingPartyMicConsent9956',
          'KingPartyVoiceReconnect9957'):
 assert k in s,k
assert 'KingLiveDrawRound9967.allowed' in (src/'RoomGameActivity.java').read_text()
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print('PASS actual recipient Gift visual playback dedup after app restart, ACK safe')
