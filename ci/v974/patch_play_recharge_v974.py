#!/usr/bin/env python3
"""v9.7.4: Build real Google Play Recharge integration without enabling payments.

Restore last known-good compiled v9.7.3 APK source, replace TEST recharge route
with Play Billing one-time product screen (disabled until backend + merchant
credentials are verified). Only server-verified receipts may affect Diamonds
and recharge-only VIP; no client writes to wallets or paid VIP.
"""
from pathlib import Path
import shutil,sys
root=Path(sys.argv[1]);pkg=root/'app/src/main/java/com/kingplus/social'
shutil.copy2(Path(__file__).with_name('KingRecharge974Activity.java'),pkg/'KingRecharge974Activity.java')

def once(s,old,new,label):
 n=s.count(old)
 if n!=1:raise SystemExit(f'{label}: expected one source marker got {n}: {old[:115]!r}')
 print('PASS',label)
 return s.replace(old,new,1)

def method_replace(s,start_text,new,label):
 a=s.find(start_text)
 if a<0 or s.count(start_text)!=1:raise SystemExit(f'{label}: missing or duplicate method')
 b=s.find('{',a);depth=0;in_string=None;esc=False
 for i in range(b,len(s)):
  c=s[i]
  if in_string:
   if esc:esc=False
   elif c=='\\':esc=True
   elif c==in_string:in_string=None
  elif c in ('"',"'"):in_string=c
  elif c=='{':depth+=1
  elif c=='}':
   depth-=1
   if depth==0:
    print('PASS',label)
    return s[:a]+new+s[i+1:]
 raise SystemExit(f'{label}: no closing method brace')

main=pkg/'MainActivity.java'
s=main.read_text()
s=method_replace(s,'    private void rechargeCenterPage()',
 '''    private void rechargeCenterPage(){
        startActivity(new Intent(this,KingRecharge974Activity.class));
    }''',
 'Route Recharge Center to verified Google Play (currently safely disabled), not TEST local coin purchase')

s=once(s,
 'button("Recharge",CARD,()->new AlertDialog.Builder(this).setTitle("Recharge").setMessage("Real-money recharge stays disabled in this no-billing build. The server wallet is real; paid recharge will be enabled only after Play products and server verification are activated.").setPositiveButton("OK",null).show());',
 'button("💎 Recharge Diamonds",CARD,this::rechargeCenterPage);',
 'Link My Wallet Recharge to Play Billing product screen')

s=once(s,
 'TextView bal=text("💎 "+coinBalance+" Diamonds",28,Color.WHITE,true); text("Verified server balance",14,MUTED,false);',
 'TextView bal=text("💎 Checking verified balance…",28,Color.WHITE,true); text("Only server-verified Diamonds can be spent on paid Gifts",14,MUTED,false);',
 'Never mislabel local TEST coins as purchased Diamonds while wallet is loading')
main.write_text(s)

vip=pkg/'KingVipVisualActivity.java'
s=vip.read_text()
s=method_replace(s,'    @Override public void onCreate(Bundle b)',
 r'''    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        com.google.firebase.auth.FirebaseUser account974=
            com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if(account974==null){
            verifiedWallet972=false;
            walletUnavailable972=false;
            build();return;
        }
        TextView waiting974=tv("Loading verified Recharge VIP…",17,Color.WHITE,true);
        waiting974.setBackgroundColor(0xff081d2d);
        waiting974.setGravity(Gravity.CENTER);
        setContentView(waiting974);
        final String uid974=account974.getUid();
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("wallets").document(uid974).get()
            .addOnSuccessListener(wallet974->{
                if(isFinishing()||isDestroyed())return;
                com.google.firebase.auth.FirebaseUser now974=
                    com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                if(now974==null||!uid974.equals(now974.getUid()))return;
                Long verifiedRecharge974=wallet974.getLong("rechargeDiamondsTotal");
                Long storedVip974=wallet974.getLong("vipLevel");
                long points974=verifiedRecharge974==null?0L:verifiedRecharge974;
                int verifiedLevel974=KingSocialGift972.levelFromPoints(points974);
                if(points974>=0 && points974<=1000000000L &&
                   (storedVip974==null || storedVip974.intValue()==verifiedLevel974)){
                    verifiedWallet972=true;
                    verifiedPoints972=points974;
                    verifiedLevel972=verifiedLevel974;
                }else walletUnavailable972=true;
                build();
            })
            .addOnFailureListener(e->{
                if(isFinishing()||isDestroyed())return;
                walletUnavailable972=true;
                build();
            });
    }''',
 'VIP Level is read from server-confirmed Recharge total, never local TEST points')
s=once(s,
 'TextView note=tv("FREE preview only • Paid VIP is paused • No billing",11,0xff9da6b6,false);',
 'TextView note=tv(verifiedWallet972?"VIP increases only after verified Diamond Recharge • Gifts never increase VIP":walletUnavailable972?"VIP check unavailable • no unverified upgrade is shown":"Recharge VIP preview • signed-in account required",11,0xff9da6b6,false);',
 'Explain recharge-only VIP, not TEST gift points')
s=once(s,
 'TextView unlock=tv("Paid VIP upgrades are paused • No payment required",13,0xffd6d6e5,true);',
 'TextView unlock=tv(KingRecharge974Activity.LIVE_RECHARGE?"Recharge verified Diamonds to increase VIP":"VIP Recharge setup pending • payments disabled",13,0xffd6d6e5,true);',
 'VIP shows safe activation status')
vip.write_text(s)

g=root/'app/build.gradle'
s=g.read_text()
s=once(s,"versionCode 164; versionName '9.7.3-no-billing-free-gifts'",
 "versionCode 165; versionName '9.7.4-play-diamond-recharge-preparation'",
 'Bump v9.7.4 to preserve installed app data on update')
needle="dependencies {"
s=once(s,needle,needle+"\n    implementation 'com.android.billingclient:billing:8.3.0'",
 'Include official Google Play Billing client library')
g.write_text(s)
manifest=root/'app/src/main/AndroidManifest.xml'
s=manifest.read_text()
s=once(s,
 '<activity android:name=".KingVipVisualActivity" android:exported="false" />',
 '<activity android:name=".KingVipVisualActivity" android:exported="false" />\n        <activity android:name=".KingRecharge974Activity" android:exported="false" />',
 'Register internal non-exported Recharge Activity')
manifest.write_text(s)
print('PASS KING Plus v9.7.4 client safely prepared, user payments remain disabled')
