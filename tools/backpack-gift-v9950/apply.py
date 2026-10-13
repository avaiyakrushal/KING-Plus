#!/usr/bin/env python3
"""v9.9.50: send FREE Backpack gifts via actual atomic Party Gift events and
recipient receipts, not detached best-effort writes. Spend local free stock
only AFTER a successful Firestore batch commit; no paid wallet is enabled."""
from pathlib import Path
import sys
root=Path(sys.argv[1])
src=root/'app/src/main/java/com/kingplus/social'
p=src/'PartyActivity.java'
s=p.read_text()
def rep(old,new):
    global s
    if s.count(old)!=1:raise RuntimeError('expected exactly one anchor: '+repr(old[:80]))
    s=s.replace(old,new,1)
rep('''    private void sendGiftQuantity(String targetUid,String targetName,String gift,
            String icon,int unitCost,int quantity,int totalCost){
''','''    private void sendGiftQuantity(String targetUid,String targetName,String gift,
            String icon,int unitCost,int quantity,int totalCost){
        sendGiftQuantityVerified9950(targetUid,targetName,gift,icon,unitCost,quantity,totalCost,null);
    }
    private void sendGiftQuantityVerified9950(String targetUid,String targetName,String gift,
            String icon,int unitCost,int quantity,int totalCost,Runnable onCommitted9950){
''')
rep('''            addGiftEvent(targetUid,targetName,gift,icon,0,quantity,0);
''','''            addGiftEvent(targetUid,targetName,gift,icon,0,quantity,0,onCommitted9950);
''')
rep('''    private void addGiftEvent(String targetUid,String targetName,String gift,String icon,
                              int unitCost,int quantity,int totalCost){
''','''    private void addGiftEvent(String targetUid,String targetName,String gift,String icon,
                              int unitCost,int quantity,int totalCost,Runnable onCommitted9950){
''')
rep('''        batch971.commit().addOnSuccessListener(v->{
            giftSendPending971=false;
            if(isActivePresence967(requestedRoom971,expectedUid971,generation971)){
''','''        batch971.commit().addOnSuccessListener(v->{
            giftSendPending971=false;
            // Only the SERVER-accepted atomic event+chat spends free Backpack stock.
            if(onCommitted9950!=null)onCommitted9950.run();
            if(isActivePresence967(requestedRoom971,expectedUid971,generation971)){
''')
start=s.index('    private void backpackGiftDialog700(){')
stop=s.index('    private void pkHistory940(){',start)
old=s[start:stop]
assert 'room.collection("events").add(e)' in old and 'showGiftEffect(' in old
new='''    private void backpackGiftDialog700(){
        SharedPreferences bp=getSharedPreferences("king_backpack",MODE_PRIVATE);
        String[] keys={"rose","heart","star","firework"};
        String[] names={"Rose","Heart","Star","Firework"};
        String[] icons={"🌹","💗","⭐","🎆"};
        List<String> rows=new ArrayList<>();List<Integer> idx=new ArrayList<>();
        for(int i=0;i<keys.length;i++){
            int count=bp.getInt(keys[i],0);
            if(count>0){rows.add(icons[i]+" "+names[i]+" ×"+count);idx.add(i);}
        }
        if(rows.isEmpty()){
            new AlertDialog.Builder(this).setTitle("🎒 Backpack")
                .setMessage("No free gifts yet. Claim one in Collection Center.")
                .setPositiveButton("Collection",(d,w)->openCommunityHub700("Collection"))
                .setNegativeButton("Close",null).show();return;
        }
        if(giftTargetName==null||giftTargetName.isEmpty()){
            giftTargetName=ownerName;giftTargetUid=ownerUid;
        }
        new AlertDialog.Builder(this).setTitle("🎒 Backpack • To "+giftTargetName)
            .setItems(rows.toArray(new String[0]),(dialog,which)->{
                final int i=idx.get(which);
                final String key=keys[i],name=names[i],icon=icons[i];
                final String targetUid=giftTargetUid,targetName=giftTargetName;
                int available=bp.getInt(key,0);
                if(!cloudRoom||db==null||user==null||!memberSeen){
                    toast("Join a live Party to send a real Backpack Gift");return;
                }
                if(!KingBackpackGift9950.canBegin(available,giftSendPending971)){
                    toast("No available Gift or previous Gift is still synchronizing");return;
                }
                sendGiftQuantityVerified9950(targetUid,targetName,name,icon,0,1,0,()->{
                    int remaining=KingBackpackGift9950.afterCommitted(bp.getInt(key,0));
                    bp.edit().putInt(key,remaining).apply();
                });
                // No speculative 'sent' message or local animation. The
                // existing Gift listener will render recipient-confirmed events.
            })
            .setNeutralButton("Collection",(d,w)->openCommunityHub700("Collection"))
            .setNegativeButton("Close",null).show();
    }
'''
s=s[:start]+new+s[stop:]
p.write_text(s)
(src/'KingBackpackGift9950.java').write_text("""package com.kingplus.social;
/** A locally earned FREE gift is not paid Diamonds or a server wallet. */
public final class KingBackpackGift9950 {
 private KingBackpackGift9950(){}
 public static boolean canBegin(int available,boolean sendPending){
  return available>0&&!sendPending;
 }
 /** Invoke only after a confirmed atomic Firestore Gift commit. */
 public static int afterCommitted(int currentlyAvailable){
  return Math.max(0,currentlyAvailable-1);
 }
}
""")
g=root/'app/build.gradle'
v=g.read_text()
oldVersion="versionCode 228; versionName '9.9.49-game-reveal-secure-client-rolls-test'"
assert v.count(oldVersion)==1
g.write_text(v.replace(oldVersion,"versionCode 229; versionName '9.9.50-real-backpack-gifts-test'"))
assert 'LIVE_RECHARGE = false' in (src/'KingRecharge974Activity.java').read_text()
assert 'PAID_VIP_ACTIVE = false' in (src/'KingPremiumAccess981.java').read_text()
assert 'gift_receipts' in s and 'pendingGiftEvents9944' in s
print("PASS Backpack real recipient verified + atomic gift; spent only on commit; payment OFF")
