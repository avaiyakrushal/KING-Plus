#!/usr/bin/env python3
"""KING Plus v9.7.6 — unified Cloudflare D1 Wallet, no Google Cloud Billing.

Based exclusively on successfully compiled v9.7.5 Android source. Keeps
Gmail, Mobile OTP, FREE Gifts, VIP screens, Family, Chat, Ludo and Party stable.
The old Recharge screen queried Firestore /wallets but the low-cost Razorpay
backend stores trusted Diamonds in Cloudflare D1. This patch routes the Wallet
and VIP UI to the *same* verified D1 /wallet endpoint, marks unavailable
balances as unavailable (never invented zero or TEST), and hardens OTP URL
validation + bounded JSON/redirect handling.

Paid recharge remains OFF. Backend hosting and approved SMS/merchant providers
need owner setup; compilation is not proof of live provider availability.
"""
from pathlib import Path
import shutil,sys

root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
for f in ('KingBackend976.java','KingWallet976Activity.java','KingWalletRules976.java'):
    shutil.copy2(Path(__file__).with_name(f),pkg/f)
    print('PASS added',f)

def replace_once(s,old,new,label):
    n=s.count(old)
    if n!=1:raise SystemExit(f'{label}: expected one source marker, found {n}: {old[:110]!r}')
    print('PASS',label)
    return s.replace(old,new,1)

def replace_method(s,start_text,replacement,label):
    a=s.find(start_text)
    if a<0 or s.count(start_text)!=1:
        raise SystemExit(f'{label}: missing or repeated method marker: {start_text}')
    b=s.find('{',a);depth=0;quote=None;esc=False
    for i in range(b,len(s)):
        c=s[i]
        if quote:
            if esc:esc=False
            elif c=='\\':esc=True
            elif c==quote:quote=None
        elif c in ('"',"'"):quote=c
        elif c=='{':depth+=1
        elif c=='}':
            depth-=1
            if depth==0:
                print('PASS',label)
                return s[:a]+replacement+s[i+1:]
    raise SystemExit(f'{label}: unmatched method braces')

main=pkg/'MainActivity.java'
s=main.read_text()
s=replace_method(s,'    private void rechargeCenterPage()',
'''    private void rechargeCenterPage(){
        startActivity(new Intent(this,KingWallet976Activity.class));
    }''',
'Main Recharge routes to one verified Cloudflare D1 Wallet, not divergent Firebase wallet')
main.write_text(s)

vip=pkg/'KingVipVisualActivity.java'
s=vip.read_text()
s=replace_method(s,'    @Override public void onCreate(Bundle b)',
r'''    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        com.google.firebase.auth.FirebaseUser signed976=
            com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if(signed976==null){
            verifiedWallet972=false;
            walletUnavailable972=false;
            build();
            return;
        }
        TextView waiting976=tv("Checking verified Diamond Recharge VIP…",17,Color.WHITE,true);
        waiting976.setBackgroundColor(0xff081d2d);
        waiting976.setGravity(Gravity.CENTER);
        setContentView(waiting976);
        KingBackend976.fetchWallet(this,(wallet976,error976)->{
            if(isFinishing()||isDestroyed())return;
            if(wallet976!=null){
                verifiedWallet972=true;
                walletUnavailable972=false;
                verifiedPoints972=wallet976.rechargeTotal;
                verifiedLevel972=wallet976.vipLevel;
            }else{
                verifiedWallet972=false;
                walletUnavailable972=true;
            }
            build();
        });
    }''',
'VIP only renders server-confirmed recharge total and VIP from the same D1 wallet')
s=replace_once(s,
 'TextView note=tv(verifiedWallet972?"VIP increases only after verified Diamond Recharge • Gifts never increase VIP":walletUnavailable972?"VIP check unavailable • no unverified upgrade is shown":"Recharge VIP preview • signed-in account required",11,0xff9da6b6,false);',
 'TextView note=tv(verifiedWallet972?"VIP is based only on verified Diamond Recharge • Gifts do not add VIP":walletUnavailable972?"Verified VIP is unavailable until secure Cloudflare Wallet is configured":"Recharge VIP preview; sign in to view real progress",11,0xff9da6b6,false);',
'VIP never displays unverified recharge balance as real')
vip.write_text(s)

phone=pkg/'KingPhoneOtp975Activity.java'
s=phone.read_text()
s=replace_method(s,'    private String normalizedServer975()',
 '''    private String normalizedServer975(){
        return KingBackend976.validatedWorkerUrl(server.getText().toString());
    }''',
'OTP endpoint limited to intended Cloudflare kingplus-lowcost worker name')
s=replace_method(s,'    private JSONObject post975(',
r'''    private JSONObject post975(String base,String route,JSONObject data)throws Exception{
        if(base==null||!base.equals(KingBackend976.validatedWorkerUrl(base)))
            throw new SecurityException("Unverified backend URL");
        HttpURLConnection conn=null;
        try{
            conn=(HttpURLConnection)new URL(base+route).openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type","application/json");
            byte[] payload=data.toString().getBytes(StandardCharsets.UTF_8);
            if(payload.length>4096)throw new SecurityException("OTP request too large");
            try(OutputStream out=conn.getOutputStream()){out.write(payload);}
            return KingBackend976.boundedResponse(conn);
        }finally{if(conn!=null)conn.disconnect();}
    }''',
'OTP cannot leak codes or custom auth JWT through redirects or partial JSON reads')
s=replace_once(s,
 'message("Enter your HTTPS Cloudflare Worker address and Turnstile site key.");',
 'message("Enter https://kingplus-lowcost.<owner>.workers.dev and its Turnstile site key.");',
'Explain owner-approved Worker subdomain requirement')
phone.write_text(s)

manifest=root/'app/src/main/AndroidManifest.xml'
m=manifest.read_text()
m=replace_once(m,
 '<activity android:name=".KingPhoneOtp975Activity" android:exported="false" />',
 '<activity android:name=".KingPhoneOtp975Activity" android:exported="false" />\n        <activity android:name=".KingWallet976Activity" android:exported="false" />',
'Register verified Wallet Activity as internal non-exported screen')
manifest.write_text(m)

gradle=root/'app/build.gradle'
g=gradle.read_text()
g=replace_once(g,
 "versionCode 166; versionName '9.7.5-cloudflare-mobile-otp-preparation'",
 "versionCode 167; versionName '9.7.6-unified-verified-wallet'",
 'Release versionCode 167 based on last successful v9.7.5')
gradle.write_text(g)
print('PASS v9.7.6: one server-verified Wallet for Diamond balance, VIP and OAuth/Mobile identities; no billing enabled')
