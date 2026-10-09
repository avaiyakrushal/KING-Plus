#!/usr/bin/env python3
"""KING Plus v9.7.3: complete no-billing mode for Party Gifts, Chat Gifts and VIP preview.

Restore the last successful v9.7.2 Android artifact, retain online rooms,
live emoji, chats, family, Follow, Ludo and crash guards. No Cloud Functions,
Blaze Billing, paid wallet or money-related purchases are used in the new Gift
interaction. FREE decorative gifts are Firestore messages/events, with zero value.
"""
from pathlib import Path
import shutil,sys

root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
shutil.copy2(Path(__file__).with_name('KingFreeGift973.java'),pkg/'KingFreeGift973.java')

def once(s,old,new,label):
    count=s.count(old)
    if count!=1:
        raise SystemExit(f'{label}: marker expected once; found {count}: {old[:120]!r}')
    print('PASS',label)
    return s.replace(old,new,1)

def function_replace(s,signature,new,label):
    pos=s.find(signature)
    if pos<0 or s.count(signature)!=1:
        raise SystemExit(f'{label}: unique method signature not found')
    start=s.find('{',pos)
    if start<0:raise SystemExit(f'{label}: missing opening brace')
    depth=0;quote=None;esc=False
    for i in range(start,len(s)):
        ch=s[i]
        if quote:
            if esc:esc=False
            elif ch=='\\':esc=True
            elif ch==quote:quote=None
        else:
            if ch in ('"',"'"):quote=ch
            elif ch=='{':depth+=1
            elif ch=='}':
                depth-=1
                if depth==0:
                    print('PASS',label)
                    return s[:pos]+new+s[i+1:]
    raise SystemExit(f'{label}: unterminated function')

party=pkg/'PartyActivity.java'
s=party.read_text()
s=function_replace(s,
    '    private void sendGiftQuantity(',
    r'''    private long lastFreeGift973;
    private void sendGiftQuantity(String targetUid,String targetName,String gift,
            String icon,int unitCost,int quantity,int totalCost){
        final String sender973=user==null?null:user.getUid();
        final long now973=System.currentTimeMillis();
        if(!cloudRoom){
            if(quantity<1||quantity>KingFreeGift973.MAX_QTY){toast("FREE gift quantity: 1 to 9");return;}
            // This is explicitly an offline preview, never a charged gift.
            showGiftEffect(safeName(),targetName,gift,icon,quantity,0);
            toast("FREE Gift preview • no coins or VIP points");
            return;
        }
        if(!KingFreeGift973.allowed(sender973,targetUid,quantity,lastFreeGift973,
                now973,memberSeen&&db!=null&&!isFinishing()&&!isDestroyed())){
            toast("Choose another Party member; max 9 FREE gifts, 1.5 seconds between sends");
            return;
        }
        lastFreeGift973=now973;
        // Firestore room events are allowed on Spark's free quota without
        // Cloud Functions. This is a zero-value decorative animation only.
        // No real wallet, local coins, VIP progress or payment call occurs.
        addGiftEvent(targetUid,targetName,gift,icon,0,quantity,KingFreeGift973.giftValue());
    }''','replace paid Party Gifts with zero-value free cosmetic events')

s=once(s,
 'String text971=actor971+" sent "+KingGiftSafety971.giftMessage(gift,icon,quantity)+" to "+recipient971;',
 'String text971=actor971+" sent FREE "+KingGiftSafety971.giftMessage(gift,icon,quantity)+" to "+recipient971;',
 'clearly label every Party cosmetic gift FREE')

s=once(s,
 'event971.put("giftValue",totalCost);',
 'event971.put("freeGift",true);\n        event971.put("giftValue",0);',
 'Party gift events never claim a wallet transfer')
s=once(s,
 'event971.put("giftUnitCost",unitCost);',
 'event971.put("giftUnitCost",0);',
 'Party gift unit cost is zero')

s=once(s,
 'return giftIcons[i]+"  "+giftNames[i]+"  •  x"+Math.max(1,giftQuantity)+"  •  💎"+compactNumber(total)+"\nTo: "+giftTargetName;',
 'return giftIcons[i]+"  "+giftNames[i]+"  •  x"+Math.max(1,giftQuantity)+"  • FREE (no coins)"+"\nTo: "+giftTargetName;',
 'Gift selection shows FREE instead of fake currency')

s=once(s,
 'giftBalanceLabel=pill("💎 "+localCoins,0xff302d40,null);',
 'giftBalanceLabel=pill("FREE • No billing",0xff302d40,null);',
 'Gift Shop shows no billing')
s=once(s,
 'int[] qs={1,9,49,99,499};',
 'int[] qs={1,3,9};',
 'restrict FREE Gift quantity to a reasonable range')

old='''int total=giftCosts[giftSelectedIndex]*giftQuantity;if(total>localCoins){toast("Not enough test diamonds for this gift combo");return;}sendGiftQuantity(giftTargetUid,giftTargetName,giftNames[giftSelectedIndex],giftIcons[giftSelectedIndex],giftCosts[giftSelectedIndex],giftQuantity,total);if(giftBalanceLabel!=null)giftBalanceLabel.setText("💎 "+localCoins);'''
new='''sendGiftQuantity(giftTargetUid,giftTargetName,giftNames[giftSelectedIndex],giftIcons[giftSelectedIndex],0,giftQuantity,0);if(giftBalanceLabel!=null)giftBalanceLabel.setText("FREE • No billing");'''
s=once(s,old,new,'Gift Shop Send does not test or spend coins')

s=once(s,
 'final boolean unlocked=myVip>=vipNeed;',
 'final boolean unlocked=true;',
 'allow all decorative gift effects without paid VIP')
s=once(s,
 'tv(selected?"✓ SELECTED":(vipNeed>0?(unlocked?"VIP ✓":"VIP "+vipNeed):""),9,',
 'tv(selected?"✓ SELECTED":"FREE",9,',
 'Gift Grid never advertises paid VIP requirements')
s=once(s,
 'TextView cost=tv("💎 "+giftCosts[i],9,',
 'TextView cost=tv("FREE",9,',
 'Gift Grid shows FREE rather than coin price')

party.write_text(s)

chat=pkg/'ChatActivity.java'
s=chat.read_text()
s=once(s,
 'TextView bal=label(cloudMode?"💎 Secure wallet":"💎 "+testCoins+" TEST",13,0xffffdf6b,true);',
 'TextView bal=label("FREE • No billing",13,0xffffdf6b,true);',
 'Chat Gift dialog notifies no payment')
s=once(s,
 'TextView co=label("◇ "+giftCosts620[idx],9,0xffaaa4b7,false);',
 'TextView co=label("FREE",9,0xffaaa4b7,false);',
 'Chat Gift cards display FREE')
s=once(s,
 'int[] qs={1,9,49,99};',
 'int[] qs={1,3,9};',
 'limit Chat decorative Gift quantity')
s=once(s,
 'if(!cloudMode)bal.setText("💎 "+testCoins+" TEST");',
 'bal.setText("FREE • No billing");',
 'Chat Gift success never shows a paid wallet')

s=function_replace(s,
 '    private boolean sendDirectGift620(',
 r'''    private boolean sendDirectGift620(String name,String icon,int unit,int qty,int total){
        if(isBlocked()){
            Toast.makeText(this,"Unblock this person first",Toast.LENGTH_SHORT).show();
            return false;
        }
        final String sender973=me==null?null:me.getUid();
        if(qty<1||qty>KingFreeGift973.MAX_QTY){
            Toast.makeText(this,"FREE Gift quantity must be 1–9",Toast.LENGTH_SHORT).show();
            return false;
        }
        final String giftText973=icon+" "+name+" ×"+qty+" • FREE decorative gift (no coins)";
        if(!cloudMode){
            // Local cosmetic preview doesn't claim any Cloud delivery or wallet activity.
            saveLocal(giftText973,"text","",true);
            Toast.makeText(this,"Local FREE Gift preview",Toast.LENGTH_SHORT).show();
            return true;
        }
        if(!KingFreeGift973.validDirect(sender973,peerUid,qty)||db==null||
           chatId==null||chatId.isEmpty()||isFinishing()||isDestroyed()){
            Toast.makeText(this,"Sign in to send a FREE Gift",Toast.LENGTH_SHORT).show();
            return false;
        }
        final String target973=peerUid;
        final String thread973=chatId;
        Map<String,Object> thread973Data=new HashMap<>();
        java.util.ArrayList<String> members973=new java.util.ArrayList<>();
        members973.add(sender973);members973.add(target973);
        thread973Data.put("members",members973);
        Map<String,Object> memberNames973=new HashMap<>();
        memberNames973.put(sender973,myName==null?"KING User":myName);
        memberNames973.put(target973,peerName==null?"KING User":peerName);
        thread973Data.put("memberNames",memberNames973);
        thread973Data.put("lastMessage",giftText973);
        thread973Data.put("lastSenderUid",sender973);
        thread973Data.put("updatedAt",FieldValue.serverTimestamp());
        final com.google.firebase.firestore.DocumentReference doc973=
            db.collection("direct_threads").document(thread973);
        doc973.set(thread973Data,SetOptions.merge()).addOnSuccessListener(v->{
            if(isFinishing()||isDestroyed())return;
            Map<String,Object> gift973=new HashMap<>();
            gift973.put("senderUid",sender973);
            gift973.put("recipientUid",target973);
            gift973.put("senderName",myName==null?"KING User":myName);
            gift973.put("text",giftText973);
            gift973.put("type","text");
            gift973.put("freeGift",true);
            gift973.put("giftValue",0);
            gift973.put("createdAt",FieldValue.serverTimestamp());
            doc973.collection("messages").add(gift973)
                .addOnSuccessListener(saved973->Toast.makeText(this,
                    "FREE Gift delivered • no coins used",Toast.LENGTH_SHORT).show())
                .addOnFailureListener(err973->Toast.makeText(this,
                    "FREE Gift was NOT delivered. Try again when online.",
                    Toast.LENGTH_LONG).show());
        }).addOnFailureListener(err973->{
            if(!isFinishing()&&!isDestroyed())
                Toast.makeText(this,"FREE Gift was NOT sent; Chat unavailable",
                    Toast.LENGTH_LONG).show();
        });
        return true;
    }''',
 'Private Chat gifts become Firestore text decorations without wallet or Cloud Function')

chat.write_text(s)

vip=pkg/'KingVipVisualActivity.java'
s=vip.read_text()
s=function_replace(s,
 '    @Override public void onCreate(Bundle b)',
 r'''    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        // Spark / no-billing mode. Paid VIP is not available and Cloud Functions
        // cannot grant verified VIP while Cloud Billing is disabled.
        verifiedWallet972=false;
        walletUnavailable972=false;
        build();
    }''',
 'VIP screen is a preview, no Cloud wallet calls')
s=once(s,
 'TextView note=tv(verifiedWallet972?"Verified VIP • secure server wallet":walletUnavailable972?"VIP verification unavailable • check connection. TEST points do not grant VIP":"Guest preview only • sign in to verify VIP",11,0xff9da6b6,false);',
 'TextView note=tv("FREE preview only • Paid VIP is paused • No billing",11,0xff9da6b6,false);',
 'VIP screen clearly labels paid upgrades paused')
s=once(s,
 'TextView unlock=tv("Upgrade VIP to unlock more awesome rewards",13,0xffd6d6e5,true);',
 'TextView unlock=tv("Paid VIP upgrades are paused • No payment required",13,0xffd6d6e5,true);',
 'VIP upgrade action no longer implies payment')
s=once(s,
 'VIP progress is verified from the secure wallet. Gifts and rewards depend on the account and verified balance. TEST points and local previews never unlock real paid VIP.',
 'Paid VIP is paused in free mode. FREE Gifts, Family and Chat have no money transfer or billing. Paid benefits are previews only.',
 'VIP See More accurately explains free-only preview')
vip.write_text(s)

build=root/'app/build.gradle'
s=build.read_text()
old="versionCode 163; versionName '9.7.2-verified-vip-private-chat-gifts'"
if s.count(old)!=1:raise SystemExit('Last successful v9.7.2 expected')
build.write_text(s.replace(old,
 "versionCode 164; versionName '9.7.3-no-billing-free-gifts'",1))
print('PASS KING Plus v9.7.3 free gifts without any paid transfer or billing')
