#!/usr/bin/env python3
"""KING v9.9.44: actual live Gifting is displayed, then ACKed.

Before this version more than 3 concurrent Gift bursts silently dropped the
4th overlay while Firestore told the sender it was received. This change
queues confirmed Gift bursts (bounded FIFO), waits for animation finish to
ACK, and never credits/claims a dropped or paused Gift as delivered.
"""
from pathlib import Path
import sys
root=Path(sys.argv[1]);src=root/'app/src/main/java/com/kingplus/social'
party=src/'PartyActivity.java';p=party.read_text()
def rep(old,new):
 global p
 n=p.count(old)
 if n!=1:raise RuntimeError('PartyActivity anchor matches '+str(n)+': '+old[:160])
 p=p.replace(old,new,1)
rep(
 '    private final java.util.HashSet<String> shownGiftIds9942=new java.util.HashSet<>();',
 '''    private final java.util.HashSet<String> shownGiftIds9942=new java.util.HashSet<>();
    private final java.util.HashSet<String> inFlightGiftIds9944=new java.util.HashSet<>();
    private final KingGiftDeliveryQueue9944<PendingGift9944> pendingGiftEvents9944=
        new KingGiftDeliveryQueue9944<>();
    private static final class PendingGift9944 {
        final DocumentReference room;
        final DocumentSnapshot event;
        final boolean recipient;
        PendingGift9944(DocumentReference r,DocumentSnapshot d,boolean target){
            room=r;event=d;recipient=target;
        }
    }
''')
rep(
 '''        // Preserve per-room event IDs across renderParty() rerenders.

        ++roomListenerGeneration982;''',
 '''        // Cancel pending/unfinished visual deliveries. Their gift IDs become
        // retryable when the Party comes back, rather than faking an ACK.
        for(KingGiftDeliveryQueue9944.Entry<PendingGift9944> item9944:pendingGiftEvents9944.clear())
            shownGiftIds9942.remove(item9944.id);
        for(String id9944:inFlightGiftIds9944)shownGiftIds9942.remove(id9944);
        inFlightGiftIds9944.clear();
        // Preserve only successfully displayed per-room IDs across rerenders.
        ++roomListenerGeneration982;''')
rep(
 '''        showGiftEffect(str(event,"actorName","User"),str(event,"targetName","Member"),
            name9942,str(event,"giftIcon",giftIconFor(name9942)),
            count9942==null?1:count9942,value9942==null?0:value9942);
        if(isRecipient9942)confirmGiftReceived9941(room,event,ownUid9942);''',
 '''        enqueueConfirmedGift9944(room,event,isRecipient9942);''')
anchor='''    private void confirmGiftReceived9941(DocumentReference room,DocumentSnapshot event,String uid){'''
methods="""    /** A server-confirmed Gift waits for a real foreground animation slot. */
    private void enqueueConfirmedGift9944(DocumentReference room,DocumentSnapshot event,boolean recipient){
        if(room==null||event==null||roomId==null||!room.getId().equals(roomId)){
            if(event!=null)shownGiftIds9942.remove(event.getId());return;
        }
        if(!pendingGiftEvents9944.offer(event.getId(),new PendingGift9944(room,event,recipient))){
            // No fictitious delivery receipt when animation playback is saturated.
            shownGiftIds9942.remove(event.getId());
            return;
        }
        drainPendingGifts9944();
    }

    /** Bounded playback queue, one animation+sound per actual visible Gift. */
    private void drainPendingGifts9944(){
        if(!cloudRoom||!memberSeen||!partyVisualForeground995||partyUiDead()
           ||user==null||roomId==null||liveEmojiStageV530==null
           ||liveEmojiStageV530.getParent()==null)return;
        while(giftFxActive971<3&&!pendingGiftEvents9944.isEmpty()){
            final KingGiftDeliveryQueue9944.Entry<PendingGift9944> entry=pendingGiftEvents9944.poll();
            if(entry==null)break;
            final PendingGift9944 next=entry.payload;
            if(next==null||next.room==null||next.event==null
                ||!roomId.equals(next.room.getId())){
                shownGiftIds9942.remove(entry.id);continue;
            }
            final com.google.firebase.Timestamp created=next.event.getTimestamp("createdAt");
            if(created==null||!KingGiftReplayGuard9942.allow(
                 System.currentTimeMillis(),created.toDate().getTime(),
                 next.event.getString("actorUid"),user.getUid(),false,next.recipient)){
                shownGiftIds9942.remove(entry.id);continue;
            }
            final String expectedRoom=roomId,expectedUid=user.getUid();
            final int expectedGen=presenceGeneration967;
            final String name=str(next.event,"giftName","Gift");
            Long qty=next.event.getLong("giftQty"),value=next.event.getLong("giftValue");
            inFlightGiftIds9944.add(entry.id);
            boolean actuallyStarted=showGiftEffectConfirmed9944(
                str(next.event,"actorName","User"),str(next.event,"targetName","Member"),
                name,str(next.event,"giftIcon",giftIconFor(name)),
                qty==null?1:qty,value==null?0:value,()->{
                    inFlightGiftIds9944.remove(entry.id);
                    // Receipt is written AFTER GiftBurstView completes successfully.
                    if(next.recipient&&isActivePresence967(expectedRoom,expectedUid,expectedGen)
                       &&partyVisualForeground995&&!partyUiDead()&&memberSeen)
                        confirmGiftReceived9941(next.room,next.event,expectedUid);
                    else if(!partyVisualForeground995||!memberSeen||partyUiDead())
                        shownGiftIds9942.remove(entry.id);
                    if(isActivePresence967(expectedRoom,expectedUid,expectedGen))
                        drainPendingGifts9944();
                });
            if(!actuallyStarted){
                inFlightGiftIds9944.remove(entry.id);
                shownGiftIds9942.remove(entry.id);
                // Playback stage may not be ready; caller will retry via
                // recoverRecentGifts9942(), without acknowledging an unseen Gift.
                break;
            }
        }
    }

"""
rep(anchor,methods+anchor)
rep(
 '''    private void showGiftEffect(String actor,String target,String gift,String icon,long qty,long value){
        if(partyUiDead()||!partyVisualForeground995)return;
        KingGiftSound993.play(this,gift);''',
 '''    private void showGiftEffect(String actor,String target,String gift,String icon,long qty,long value){
        showGiftEffectConfirmed9944(actor,target,gift,icon,qty,value,null);
    }
    /** true only when a visible GiftBurstView has actually been started. */
    private boolean showGiftEffectConfirmed9944(String actor,String target,String gift,
                              String icon,long qty,long value,Runnable afterVisualComplete9944){
        if(partyUiDead()||!partyVisualForeground995||liveEmojiStageV530==null
           ||giftFxActive971>=3)return false;
        boolean started9944=false;''')
rep(
 '''        if(liveEmojiStageV530!=null&&!partyUiDead()&&giftFxActive971<3){try{giftFxActive971++;GiftBurstView fx=new GiftBurstView(this,ic,title,sub,accent);FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-1,-1);liveEmojiStageV530.addView(fx,lp);long duration=total>=50000?4300:total>=10000?3600:2900;fx.start(()->{giftFxActive971=Math.max(0,giftFxActive971-1);try{if(liveEmojiStageV530!=null&&fx.getParent()==liveEmojiStageV530)liveEmojiStageV530.removeView(fx);}catch(Exception ignored){}},duration);}catch(Exception e){android.util.Log.e("KINGPlusParty","Gift burst failed",e);}}''',
 '''        if(!partyUiDead()&&liveEmojiStageV530.getParent()!=null&&giftFxActive971<3){
            GiftBurstView fx=null;
            try{
                fx=new GiftBurstView(this,ic,title,sub,accent);
                FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-1,-1);
                liveEmojiStageV530.addView(fx,lp);
                giftFxActive971++;
                final GiftBurstView startedView9944=fx;
                long duration=total>=50000?4300:total>=10000?3600:2900;
                fx.start(()->{
                    giftFxActive971=Math.max(0,giftFxActive971-1);
                    try{if(liveEmojiStageV530!=null
                       &&startedView9944.getParent()==liveEmojiStageV530)
                       liveEmojiStageV530.removeView(startedView9944);}
                    catch(Exception ignored){}
                    if(afterVisualComplete9944!=null)afterVisualComplete9944.run();
                    else drainPendingGifts9944();
                },duration);
                KingGiftSound993.play(this,gift);
                started9944=true;
            }catch(Exception e){
                if(fx!=null&&fx.getParent()==liveEmojiStageV530)
                    liveEmojiStageV530.removeView(fx);
                giftFxActive971=Math.max(0,giftFxActive971-1);
                android.util.Log.e("KINGPlusParty","Gift burst failed",e);
            }
        }''')
rep(
 '''    }
    private void refreshSupporters(com.google.firebase.firestore.QuerySnapshot snap){''',
 '''        return started9944;
    }
    private void refreshSupporters(com.google.firebase.firestore.QuerySnapshot snap){''')
# Return to Party from game/lock re-drains pending effects if the visual stage
# is ready; server recovery handles those aborted before completion.
rep(
 '''        partyVisualForeground995=true;
        recoverRecentGifts9942();''',
 '''        partyVisualForeground995=true;
        drainPendingGifts9944();
        recoverRecentGifts9942();''')
party.write_text(p)
(src/'KingGiftDeliveryQueue9944.java').write_text("""package com.kingplus.social;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
/** Bounded FIFO for actual on-screen Gift effects, no bogus delivery ACK.
 * Call only from Android main thread.
 */
public final class KingGiftDeliveryQueue9944<T> {
 public static final int LIMIT=24;
 public static final class Entry<T> {
   public final String id; public final T payload;
   Entry(String id,T payload){this.id=id;this.payload=payload;}
 }
 private final ArrayDeque<Entry<T>> pending=new ArrayDeque<>();
 private final HashSet<String> ids=new HashSet<>();
 public boolean offer(String id,T value){
   if(id==null||id.isEmpty()||value==null||pending.size()>=LIMIT||!ids.add(id))return false;
   pending.addLast(new Entry<>(id,value));return true;
 }
 public Entry<T> poll(){
   Entry<T> e=pending.pollFirst();if(e!=null)ids.remove(e.id);return e;
 }
 public List<Entry<T>> clear(){
   List<Entry<T>> prior=new ArrayList<>(pending);pending.clear();ids.clear();return prior;
 }
 public boolean isEmpty(){return pending.isEmpty();}
 public int size(){return pending.size();}
}
""")
build=root/'app/build.gradle'
s=build.read_text()
old="versionCode 222; versionName '9.9.43-immutable-live-game-moves-test'"
if s.count(old)!=1:raise RuntimeError("Not exact verified v9.9.43 source")
build.write_text(s.replace(old,"versionCode 223; versionName '9.9.44-gift-visual-confirmation-test'"))
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
print("PASS queued real Gift playback, receipt only after GiftBurst completion, no fake paid currency")
