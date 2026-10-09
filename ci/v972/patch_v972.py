#!/usr/bin/env python3
"""KING Plus v9.7.2 – verified direct gifts and server-authoritative VIP.

Build atop green v9.7.1. Social private chat used TEST coins and immediately
published a real Firebase gift before checking any server wallet. This changes
live chat gifts to the idempotent Cloud Function, with genuine receipt only
after a server success. VIP now displays real wallet spend, never locally forged
activity/test-point progress. Local offline gift remains clearly TEST-only.
"""
from pathlib import Path
import shutil,sys

root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
shutil.copy2(Path(__file__).with_name('KingSocialGift972.java'),pkg/'KingSocialGift972.java')

def replace_once(s,old,new,tag):
    n=s.count(old)
    if n!=1:raise SystemExit(f'{tag}: expected one marker, found {n}: {old[:135]!r}')
    print('PASS',tag)
    return s.replace(old,new,1)

def method_replace(s,anchor,replacement,label):
    start=s.find(anchor)
    if start<0 or s.count(anchor)!=1:
        raise SystemExit(f'{label}: cannot locate unique function: {anchor!r}')
    op=s.find('{',start)
    if op<0:raise SystemExit(f'{label}: missing opening brace')
    quote=None;escape=False;depth=0
    for idx in range(op,len(s)):
        ch=s[idx]
        if quote:
            if escape:escape=False
            elif ch=='\\':escape=True
            elif ch==quote:quote=None
        elif ch in ('"',"'"):quote=ch
        elif ch=='{':depth+=1
        elif ch=='}':
            depth-=1
            if depth==0:
                print('PASS',label)
                return s[:start]+replacement+s[idx+1:]
    raise SystemExit(f'{label}: unmatched braces')

chat=pkg/'ChatActivity.java'
s=chat.read_text()
s=replace_once(s,
 'TextView bal=label("💎 "+testCoins,13,0xffffdf6b,true);',
 'TextView bal=label(cloudMode?"💎 Secure wallet":"💎 "+testCoins+" TEST",13,0xffffdf6b,true);',
 'label online private chat wallet accurately')

s=replace_once(s,
 'if(sendDirectGift620(giftNames620[selected[0]],giftIcons620[selected[0]],giftCosts620[selected[0]],qty[0],total)){bal.setText("💎 "+testCoins);dialog.dismiss();}',
 'if(sendDirectGift620(giftNames620[selected[0]],giftIcons620[selected[0]],giftCosts620[selected[0]],qty[0],total)){if(!cloudMode)bal.setText("💎 "+testCoins+" TEST");dialog.dismiss();}',
 'dismiss submitted secure send without pretending local wallet funds changed')

s=method_replace(s,
 '    private boolean sendDirectGift620(',
 r'''    private boolean directGiftPending972;

    private boolean sendDirectGift620(String name,String icon,int unit,int qty,int total){
        if(isBlocked()){
            Toast.makeText(this,"Unblock this user before sending a gift",Toast.LENGTH_SHORT).show();
            return false;
        }
        final String from972=me==null?null:me.getUid();
        if(!cloudMode){
            if(unit<1||qty<1||qty>100||((long)unit*qty)!=total||total<1||total>100000){
                Toast.makeText(this,"Invalid TEST gift quantity",Toast.LENGTH_SHORT).show();
                return false;
            }
            if(testCoins<total){
                Toast.makeText(this,"Not enough TEST coins",Toast.LENGTH_SHORT).show();
                return false;
            }
            testCoins-=total;
            mainPrefs().edit().putInt("coins",testCoins).apply();
            String display972=icon+" "+name+" ×"+qty+" • TEST coins "+total;
            saveLocal(display972,"gift","",true);
            Toast.makeText(this,"TEST gift preview only. No real coins or VIP awarded.",
                Toast.LENGTH_LONG).show();
            return true;
        }
        if(!KingSocialGift972.valid(from972,peerUid,unit,qty,total)||
           db==null||chatId==null||chatId.isEmpty()||isFinishing()||isDestroyed()){
            Toast.makeText(this,
                "Real gift needs two signed-in accounts and a secure wallet total under 100,000 coins",
                Toast.LENGTH_LONG).show();
            return false;
        }
        if(directGiftPending972){
            Toast.makeText(this,"Gift verification already in progress",Toast.LENGTH_SHORT).show();
            return false;
        }
        directGiftPending972=true;
        final String recipient972=peerUid,threadId972=chatId;
        final String fromName972=myName==null?"KING User":myName;
        final String receiverName972=peerName==null?"KING User":peerName;
        final String giftText972=icon+" "+name+" ×"+qty+" • 💎"+total;
        Toast.makeText(this,"Checking secure gift transfer…",Toast.LENGTH_SHORT).show();
        CloudBackend.sendGift(recipient972,name+" x"+qty,total,(ok,msg)->runOnUiThread(()->{
            directGiftPending972=false;
            if(isFinishing()||isDestroyed())return;
            if(!ok){
                Toast.makeText(this,"Gift NOT sent. Wallet unchanged: "+msg,
                    Toast.LENGTH_LONG).show();
                return;
            }
            // The Cloud Function has already committed the wallet_ledger and
            // verified spend. Only now do we publish the chat gift message.
            Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
            final com.google.firebase.firestore.DocumentReference thread972=
                db.collection("direct_threads").document(threadId972);
            final java.util.Map<String,Object> chatUpdate972=new HashMap<>();
            chatUpdate972.put("lastMessage","🎁 "+name+" ×"+qty);
            chatUpdate972.put("lastSenderUid",from972);
            chatUpdate972.put("updatedAt",FieldValue.serverTimestamp());
            thread972.get().addOnSuccessListener(existing972->{
                if(isFinishing()||isDestroyed())return;
                boolean exists972=existing972.exists();
                if(exists972){
                    java.util.List<String> peers972=(java.util.List<String>)existing972.get("members");
                    if(peers972==null||!peers972.contains(from972)||!peers972.contains(recipient972)){
                        Toast.makeText(this,
                            "Gift paid, but chat members changed. Check secure Wallet history.",
                            Toast.LENGTH_LONG).show();
                        return;
                    }
                }else{
                    java.util.List<String> peers972=new java.util.ArrayList<>();
                    peers972.add(from972);peers972.add(recipient972);
                    chatUpdate972.put("members",peers972);
                    java.util.Map<String,Object> names972=new HashMap<>();
                    names972.put(from972,fromName972);
                    names972.put(recipient972,receiverName972);
                    chatUpdate972.put("memberNames",names972);
                }
                thread972.set(chatUpdate972,SetOptions.merge())
                    .addOnSuccessListener(v->{
                        Map<String,Object> giftMsg972=new HashMap<>();
                        giftMsg972.put("senderUid",from972);
                        giftMsg972.put("recipientUid",recipient972);
                        giftMsg972.put("senderName",fromName972);
                        giftMsg972.put("text",giftText972);
                        giftMsg972.put("type","gift");
                        giftMsg972.put("giftName",name);
                        giftMsg972.put("giftIcon",icon);
                        giftMsg972.put("giftQty",qty);
                        giftMsg972.put("giftUnitCost",unit);
                        giftMsg972.put("giftValue",total);
                        giftMsg972.put("createdAt",FieldValue.serverTimestamp());
                        thread972.collection("messages").add(giftMsg972)
                            .addOnFailureListener(e->Toast.makeText(this,
                                "Gift paid, but chat receipt did not sync. Check Wallet ledger.",
                                Toast.LENGTH_LONG).show());
                    })
                    .addOnFailureListener(e->Toast.makeText(this,
                        "Gift paid, but chat thread sync failed. Check Wallet ledger.",
                        Toast.LENGTH_LONG).show());
            }).addOnFailureListener(e->Toast.makeText(this,
                "Gift paid, but chat cannot open. Check Wallet ledger.",
                Toast.LENGTH_LONG).show());
        }));
        return true;
    }''',
 'server-confirm private Chat gifting; no TEST debit, fake chat gift or local VIP')

chat.write_text(s)

# The existing VIP Activity reflected only local SharedPreferences activity
# points even for signed-in accounts. Prefer read-only server-wallet VIP.
p=pkg/'KingVipVisualActivity.java'
s=p.read_text()
s=replace_once(s,
 '    @Override public void onCreate(Bundle b){super.onCreate(b);build();}',
 r'''    private boolean verifiedWallet972;
    private boolean walletUnavailable972;
    private int verifiedLevel972;
    private long verifiedPoints972;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        com.google.firebase.auth.FirebaseUser account972=
            com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if(account972==null){
            build();return; // Guest preview is not authoritative.
        }
        TextView waiting972=tv("Verifying VIP with KING Plus secure wallet…",
            17,Color.WHITE,true);
        waiting972.setBackgroundColor(0xff081d2d);
        waiting972.setGravity(Gravity.CENTER);
        setContentView(waiting972);
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("wallets").document(account972.getUid()).get()
            .addOnSuccessListener(wallet972->{
                if(isFinishing()||isDestroyed())return;
                Long p972=wallet972.getLong("vipPoints");
                Long level972=wallet972.getLong("vipLevel");
                long points972=p972==null?0L:p972;
                int predicted972=KingSocialGift972.levelFromPoints(points972);
                if(points972>=0&&points972<=1000000000L&&
                   (level972==null||KingSocialGift972.verifiedVip(level972.intValue(),points972))){
                    verifiedPoints972=points972;
                    verifiedLevel972=predicted972;
                    verifiedWallet972=true;
                }else walletUnavailable972=true;
                build();
            })
            .addOnFailureListener(e->{
                if(isFinishing()||isDestroyed())return;
                walletUnavailable972=true;build();
            });
    }''',
 'VIP screen verifies Firebase wallet-owned VIP instead of local test-point level')

s=replace_once(s,
 'LevelSystem.Snapshot s=LevelSystem.read(this);int vip=s.vipLevel;boolean pink=vip>=25;',
 'LevelSystem.Snapshot s=LevelSystem.read(this);int vip=verifiedWallet972?verifiedLevel972:0;long shownVipPoints972=verifiedWallet972?verifiedPoints972:0L;boolean pink=vip>=25;',
 'VIP badge comes from authoritative wallet only')
s=replace_once(s,
 'long from=LevelSystem.vipThreshold(vip),to=s.nextVipPoints();int pc=vip>=LevelSystem.MAX_VIP?100:(int)Math.max(0,Math.min(100,(s.vipPoints-from)*100/Math.max(1,to-from)));',
 'long from=LevelSystem.vipThreshold(vip),to=LevelSystem.vipThreshold(Math.min(LevelSystem.MAX_VIP,vip+1));int pc=vip>=LevelSystem.MAX_VIP?100:(int)Math.max(0,Math.min(100,(shownVipPoints972-from)*100/Math.max(1,to-from)));',
 'verified spend used for VIP progress bar')
s=replace_once(s,
 'TextView note=tv("No billing enabled • VIP progression uses KING Plus activity/test points",11,0xff9da6b6,false);',
 'TextView note=tv(verifiedWallet972?"Verified VIP • secure server wallet":walletUnavailable972?"VIP verification unavailable • check connection. TEST points do not grant VIP":"Guest preview only • sign in to verify VIP",11,0xff9da6b6,false);',
 'VIP status clearly differentiates server verified and guest preview')

s=replace_once(s,
 'TextView more=tv("See More  ›",12,0xffcbd8ff,true);more.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);',
 'TextView more=tv("See More  ›",12,0xffcbd8ff,true);more.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);more.setOnClickListener(v->new android.app.AlertDialog.Builder(this).setTitle("VIP benefits").setMessage("VIP progress is verified from the secure wallet. Gifts and rewards depend on the account and verified balance. TEST points and local previews never unlock real paid VIP.").setPositiveButton("OK",null).show());',
 'VIP benefits See More opens working details instead of inert text')
s=replace_once(s,
 'TextView info=tv("ⓘ",20,Color.WHITE,false);info.setGravity(Gravity.CENTER);',
 'TextView info=tv("ⓘ",20,Color.WHITE,false);info.setGravity(Gravity.CENTER);info.setOnClickListener(v->new android.app.AlertDialog.Builder(this).setTitle("Verified VIP").setMessage("Your VIP level is calculated using server-confirmed gifts in the secure wallet. If verification is unavailable, the screen does not display TEST points as a paid VIP level.").setPositiveButton("OK",null).show());',
 'VIP info icon now explains verified progression')
p.write_text(s)

gradle=root/'app/build.gradle'
s=gradle.read_text()
old="versionCode 162; versionName '9.7.1-secure-live-gifts-emoji'"
if s.count(old)!=1:raise SystemExit('Expected successful v9.7.1 source')
gradle.write_text(s.replace(old,"versionCode 163; versionName '9.7.2-verified-vip-private-chat-gifts'",1))
print("PASS KING Plus v9.7.2 secure Chat gifts and verified VIP patched")
