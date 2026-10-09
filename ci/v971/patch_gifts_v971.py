#!/usr/bin/env python3
"""KING Plus v9.7.1 – reliable real-time Emojis and fail-closed real Gifts.

Baseline: last successful Android v9.7.0. Never spend TEST credits as fallback
after a failed real Cloud Functions wallet transaction. Make room gifting a
server-confirmed event with clear errors and no fake VIP progression.
Keep local TEST previews clearly separate from authenticated Firebase rooms.
"""
from pathlib import Path
import shutil,sys
root=Path(sys.argv[1]);pkg=root/'app/src/main/java/com/kingplus/social'
shutil.copy2(Path(__file__).with_name('KingGiftSafety971.java'),pkg/'KingGiftSafety971.java')
party=pkg/'PartyActivity.java'
s=party.read_text()
def change(old,new,label):
 global s
 n=s.count(old)
 if n!=1: raise SystemExit(f'{label}: expected 1 marker, got {n}: {old[:150]!r}')
 s=s.replace(old,new,1);print('PASS',label)
def replace_java_method(marker,replacement,label):
 global s
 at=s.find(marker)
 if at<0 or s.count(marker)!=1:raise SystemExit(f'{label}: unique method marker not found')
 b=s.find('{',at)
 if b<0:raise SystemExit(label+': missing body')
 in_string=None;escape=False;depth=0;end=-1
 for i in range(b,len(s)):
  c=s[i]
  if in_string:
   if escape:escape=False
   elif c=='\\':escape=True
   elif c==in_string:in_string=None
  elif c in ("'",'"'):in_string=c
  elif c=='{':depth+=1
  elif c=='}':
   depth-=1
   if depth==0:end=i+1;break
 if end<0:raise SystemExit(label+': unmatched body')
 s=s[:at]+replacement+s[end:];print('PASS',label)

replace_java_method('    private void sendGiftQuantity(',
r'''    private boolean giftSendPending971;
    private int giftFxActive971;
    private long lastLiveEmojiSend971;

    private void sendGiftQuantity(String targetUid,String targetName,String gift,
                                  String icon,int unitCost,int quantity,int totalCost){
        int idx=-1;
        for(int i=0;i<giftNames.length;i++)if(giftNames[i].equals(gift)){idx=i;break;}
        if(idx>=0&&LevelSystem.read(this).vipLevel<giftVipRequired[idx]){
            toast("Gift unlocks at VIP "+giftVipRequired[idx]);return;
        }
        final String sender971=user==null?null:user.getUid();
        final String giftRoom971=roomId;
        if(!cloudRoom){
            // Local demo does NOT mint actual gifts, wallet entries, or VIP status.
            if(quantity<1||unitCost<1||((long)unitCost*quantity)!=totalCost||totalCost<1){
                toast("Invalid TEST gift quantity");return;
            }
            if(localCoins<totalCost){toast("Not enough TEST coins");return;}
            localCoins-=totalCost;
            prefs.edit().putInt("coins",localCoins).apply();
            showGiftEffect(safeName(),targetName,gift,icon,quantity,totalCost);
            toast("TEST animation only • no real gift or VIP points awarded");
            return;
        }
        if(!KingGiftSafety971.validCloudGift(sender971,targetUid,giftRoom971,
              memberSeen&&db!=null&&!isFinishing()&&!isDestroyed(),
              unitCost,quantity,totalCost)){
            toast("Cannot send: sign in, join Party, choose another member and keep the total under 100,000 server coins");
            return;
        }
        if(giftSendPending971){toast("Waiting for secure gift confirmation");return;}
        giftSendPending971=true;
        final int expectedPresence971=presenceGeneration967;
        final String payerName971=safeName();
        CloudBackend.sendGift(targetUid,gift+" x"+quantity,totalCost,(ok,message)->runOnUiThread(()->{
            giftSendPending971=false;
            if(isFinishing()||isDestroyed())return;
            if(!ok){
                toast("Gift NOT sent • server wallet unchanged: "+message);
                return; // NEVER fall back to TEST coins after failed real purchase.
            }
            // The secure Cloud Function acknowledges the wallet transfer first.
            // Do not credit VIP, post events, or animate an unverified transfer.
            toast(message);
            awardGiftProgress700(totalCost);
            if(!isActivePresence967(giftRoom971,sender971,expectedPresence971)){
                toast("Secure gift charged; you left the Party before its animation could sync. Check Wallet history.");
                return;
            }
            showGiftEffect(payerName971,targetName,gift,icon,quantity,totalCost);
            addGiftEvent(targetUid,targetName,gift,icon,unitCost,quantity,totalCost);
        }));
    }''','server-confirmed gifts only, local demo never earns VIP, failure never fakes wallet success')

replace_java_method('    private void addGiftEvent(',
r'''    private void addGiftEvent(String targetUid,String targetName,String gift,String icon,
                              int unitCost,int quantity,int totalCost){
        if(!cloudRoom||user==null||db==null||roomId==null||!memberSeen)return;
        final String requestedRoom971=roomId,expectedUid971=user.getUid();
        final int generation971=presenceGeneration967;
        String actor971=safeName();
        String recipient971=targetName==null||targetName.trim().isEmpty()?"Member":targetName;
        String text971=actor971+" sent "+KingGiftSafety971.giftMessage(gift,icon,quantity)+" to "+recipient971;
        if(text971.length()>300)text971=text971.substring(0,300);
        LevelSystem.Snapshot progress971=LevelSystem.read(this);
        Map<String,Object> event971=new HashMap<>();
        event971.put("actorUid",expectedUid971);
        event971.put("actorName",actor971);
        event971.put("actorLevel",progress971.level);
        event971.put("actorVip",progress971.vipLevel);
        event971.put("type","gift");
        event971.put("text",text971);
        event971.put("giftName",gift);
        event971.put("giftIcon",icon);
        event971.put("giftValue",totalCost);
        event971.put("giftQty",quantity);
        event971.put("giftUnitCost",unitCost);
        event971.put("targetUid",targetUid);
        event971.put("targetName",recipient971);
        event971.put("createdAt",FieldValue.serverTimestamp());
        Map<String,Object> message971=new HashMap<>();
        message971.put("senderUid",expectedUid971);
        message971.put("senderName",actor971);
        message971.put("senderLevel",progress971.level);
        message971.put("senderVip",progress971.vipLevel);
        message971.put("text",text971);
        message971.put("type","gift");
        message971.put("giftName",gift);
        message971.put("giftIcon",icon);
        message971.put("giftValue",totalCost);
        message971.put("giftQty",quantity);
        message971.put("targetUid",targetUid);
        message971.put("targetName",recipient971);
        message971.put("createdAt",FieldValue.serverTimestamp());
        DocumentReference room971=db.collection("live_rooms").document(requestedRoom971);
        WriteBatch batch971=db.batch();
        batch971.set(room971.collection("events").document(),event971);
        batch971.set(room971.collection("messages").document(),message971);
        batch971.commit().addOnFailureListener(e->{
            if(!isFinishing()&&!isDestroyed())
                toast("Gift paid securely but Party animation/history could not sync: "+msg(e));
        });
    }''','atomic Firestore gift event and chat write after secure confirmation')

replace_java_method('    private void sendLiveEmojiV530(',
r'''    private void sendLiveEmojiV530(String emoji){
        String visual=stickerFallback610(emoji);
        if(!cloudRoom){
            showLiveEmojiEffect560(visual,displayName,user==null?null:user.getUid());
            appendLocalChat(displayName,visual); // clearly local/test effect.
            return;
        }
        if(user==null||db==null||roomId==null||!KingGiftSafety971.validLiveEmoji(
                visual,memberSeen)){
            toast("Join a live KING Plus Party to send animated emoji");return;
        }
        long now971=android.os.SystemClock.elapsedRealtime();
        if(now971-lastLiveEmojiSend971<350L){toast("Emoji is sending • try again shortly");return;}
        lastLiveEmojiSend971=now971;
        final String expectedRoom971=roomId,expectedUid971=user.getUid();
        final int expectedGeneration971=presenceGeneration967;
        Map<String,Object> ev971=new HashMap<>();
        ev971.put("actorUid",expectedUid971);
        ev971.put("actorName",safeName());
        ev971.put("type","live_emoji");
        ev971.put("text",visual);
        ev971.put("emoji",visual);
        ev971.put("createdAt",FieldValue.serverTimestamp());
        Map<String,Object> msg971=new HashMap<>();
        msg971.put("senderUid",expectedUid971);
        msg971.put("senderName",safeName());
        msg971.put("text",visual);
        msg971.put("createdAt",FieldValue.serverTimestamp());
        DocumentReference room971=db.collection("live_rooms").document(expectedRoom971);
        WriteBatch batch971=db.batch();
        batch971.set(room971.collection("messages").document(),msg971);
        batch971.set(room971.collection("events").document(),ev971);
        batch971.commit().addOnSuccessListener(v->{
            if(isActivePresence967(expectedRoom971,expectedUid971,expectedGeneration971))
                showLiveEmojiEffect560(visual,safeName(),expectedUid971);
        }).addOnFailureListener(error->{
            if(isActivePresence967(expectedRoom971,expectedUid971,expectedGeneration971))
                toast("Emoji was not sent to Party: "+msg(error));
        });
    }''','live emoji broadcasts only from joined member, rate limited and server-acknowledged')

change(
 'showGiftEffect(str(d,"actorName","User"),str(d,"targetName","Host"),gift,giftIconFor(gift),q==null?1:q,v==null?1:v)',
 'showGiftEffect(str(d,"actorName","User"),str(d,"targetName","Host"),gift,str(d,"giftIcon",giftIconFor(gift)),q==null?1:q,v==null?1:v)',
 'receiver uses exact gift icon as sender')

change(
 'if(liveEmojiStageV530!=null&&!partyUiDead()){try{GiftBurstView fx=new GiftBurstView(this,ic,title,sub,accent);',
 'if(liveEmojiStageV530!=null&&!partyUiDead()&&giftFxActive971<3){try{giftFxActive971++;GiftBurstView fx=new GiftBurstView(this,ic,title,sub,accent);',
 'bounded three concurrent GiftBurst native/animated overlays')

change(
 'fx.start(()->{try{if(liveEmojiStageV530!=null&&fx.getParent()==liveEmojiStageV530)liveEmojiStageV530.removeView(fx);}catch(Exception ignored){}},duration);',
 'fx.start(()->{giftFxActive971=Math.max(0,giftFxActive971-1);try{if(liveEmojiStageV530!=null&&fx.getParent()==liveEmojiStageV530)liveEmojiStageV530.removeView(fx);}catch(Exception ignored){}},duration);',
 'release Gift animation slot after effect ends')

change(
 '++presenceGeneration967; micTogglePending968=false; // Old room Mic transactions must not block a new room.',
 '++presenceGeneration967; micTogglePending968=false; giftSendPending971=false; giftFxActive971=0; // Clear previous room visuals.',
 'reset previous room gift callbacks and bounded effects on navigation')

party.write_text(s)
print("PASS Party Gift/LiveEmoji v9.7.1")

# Use actual callable JSON "ok" result, not only successful HTTPS status.
backend=pkg/'CloudBackend.java';b=backend.read_text()
old='''.addOnSuccessListener(result -> callback.onResult(true, successMessage(result.getData())))'''
new='''.addOnSuccessListener(result -> {
                Object payload=result.getData();
                if(payload instanceof Map && Boolean.FALSE.equals(((Map<?,?>)payload).get("ok")))
                    callback.onResult(false,successMessage(payload));
                else callback.onResult(true,successMessage(payload));
            })'''
if b.count(old)!=1:raise SystemExit('CloudBackend callable status handling marker changed')
backend.write_text(b.replace(old,new,1))
print('PASS Cloud Function response uses real backend ok=false status')

gradle=root/'app/build.gradle';g=gradle.read_text()
old="versionCode 161; versionName '9.7.0-realtime-social-room-id'"
if g.count(old)!=1:raise SystemExit('Expected successful v9.7.0 source')
gradle.write_text(g.replace(old,"versionCode 162; versionName '9.7.1-secure-live-gifts-emoji'",1))
print('PASS Android v9.7.1 built-source patch ready')
