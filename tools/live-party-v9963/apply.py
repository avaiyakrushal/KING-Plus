#!/usr/bin/env python3
"""KING v9.9.63, gameplay not resources:
(1) 65th+ displayed gift is persisted even if 64 in-memory ACKs are pending;
    refill ACK delivery when a slot opens. Never infer payment or replay visuals.
(2) Party hidden-role games and TTT fetch only opt-in Ready:true from Firestore
    SERVER, then revalidate identity + membership inside transaction as before.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1])
src=root/'app/src/main/java/com/kingplus/social'
party=src/'PartyActivity.java'
p=party.read_text()
old='''        if(receiptBacklog9955.containsKey(key9955))return;
        if(!KingGiftReceiptRetry9955.allowEnqueue(receiptBacklog9955.size())){
            // Never claim a delivery that has no durable recipient receipt.
            KingStability.nonFatal(this,"gift-receipt-queue-full",
                new IllegalStateException("Receipt queue at safe capacity"));
            return;
        }
        PendingGiftReceipt9955 job9955='''
new='''        if(receiptBacklog9955.containsKey(key9955))return;
        // v9.9.62 wrongly returned at the 64-job in-memory limit BEFORE
        // journaling the actual completed GiftBurst. Preserve that display
        // first so reconnect/process-kill can safely finish the ACK later.
        boolean journaled9962=storeGiftReceipt9962(room.getId(),id,uid);
        if(!journaled9962)KingStability.nonFatal(this,"gift-journal-save",
            new IllegalStateException("Playback confirmation not durable"));
        if(!KingGiftBurstBackpressure9963.canSendNow(receiptBacklog9955.size())){
            KingStability.nonFatal(this,"gift-receipt-inmemory-full",
                new IllegalStateException("Gift receipt saved, will drain when in-memory slot opens"));
            return;
        }
        PendingGiftReceipt9955 job9955='''
assert p.count(old)==1,"v9.9.62 queue gate not found"
p=p.replace(old,new,1)
old='''        // Called from completed GiftBurst playback only. Journal first;
        // never infer receipt from sender write, preview, or animation start.
        boolean journaled9962=storeGiftReceipt9962(room.getId(),id,uid);
        if(!journaled9962)KingStability.nonFatal(this,"gift-journal-save",
            new IllegalStateException("Playback confirmation not durable"));
        receiptBacklog9955.put(key9955,job9955);'''
new='''        // Real on-screen GiftBurst was recorded before the capacity gate.
        receiptBacklog9955.put(key9955,job9955);'''
assert p.count(old)==1,"v9.9.62 duplicate-journal anchor missing"
p=p.replace(old,new,1)
old='''        receiptBacklog9955.remove(job.key);
        removeGiftReceipt9962(job.room.getId(),job.event.getId(),job.uid);
    }
    private void retryFailureDisplayedGiftReceipt9955'''
new='''        receiptBacklog9955.remove(job.key);
        removeGiftReceipt9962(job.room.getId(),job.event.getId(),job.uid);
        // A slot opened: drain genuinely displayed overflow Gift ACKs.
        // Server still verifies membership, gift event and recipient.
        if(partyVisualForeground995&&memberSeen)
            recoverDurableGiftReceipts9962();
    }
    private void retryFailureDisplayedGiftReceipt9955'''
assert p.count(old)==1,"v9.9.62 queue refill anchor missing"
p=p.replace(old,new,1)
assert 'Source.SERVER' in p and 'recoverDurableGiftReceipts9962()' in p
party.write_text(p)

(src/'KingGiftBurstBackpressure9963.java').write_text('''package com.kingplus.social;
/** In-memory delivery guard. Durable on-screen receipts are journaled
 * BEFORE this cap; overflow must NOT be dropped as a visual duplicate.
 */
public final class KingGiftBurstBackpressure9963 {
 private KingGiftBurstBackpressure9963(){}
 public static boolean canSendNow(int inMemory){
   return KingGiftReceiptRetry9955.allowEnqueue(inMemory);
 }
 public static boolean mustRetainOnDisk(int inMemory){
   return inMemory>=KingGiftReceiptRetry9955.MAX_PENDING;
 }
}
''')

game=src/'RoomGameActivity.java'
s=game.read_text()
for start_marker,end_marker in (
    ('    private void startSocialRound866(String game){','    private void startRound(String game){'),
    ('    private void startTtt864(){','    private void submitTtt864(int idx){')
):
    a=s.index(start_marker)
    b=s.index(end_marker,a)
    segment=s[a:b]
    old='''room().collection("game_ready")
           .get(com.google.firebase.firestore.Source.SERVER)'''
    new='''room().collection("game_ready")
           .whereEqualTo("ready",true)
           .get(com.google.firebase.firestore.Source.SERVER)'''
    assert segment.count(old)==1,(start_marker,segment.count(old))
    segment=segment.replace(old,new,1)
    for token in ('db.runTransaction(tx->','participantUids'):
        assert token in segment,(start_marker,token)
    s=s[:a]+segment+s[b:]
game.write_text(s)

gradle=root/'app/build.gradle'
v=gradle.read_text()
oldver="versionCode 241; versionName '9.9.62-durable-gift-delivery-acks-test'"
assert v.count(oldver)==1,'must start at verified full v9.9.62 APK source'
gradle.write_text(v.replace(oldver,
    "versionCode 242; versionName '9.9.63-live-ready-game-gift-overflow-test'"))

assert 'KingPartyMicConsent9956' in p
assert 'KingPartyVoiceReconnect9957' in p
assert 'KingTttFairRoster9960.pickPair' in s
assert 'KingPartySocialRoster9959.isEligible' in s
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print('PASS real Gifts: 64 queue overflow kept durably/refilled; TTT/Social true Ready-only server queries')
