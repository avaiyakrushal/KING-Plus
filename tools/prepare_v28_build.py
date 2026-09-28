from pathlib import Path
import re

MAIN = Path('app/src/main/java/com/kingplus/social/MainActivity.java')
BUILD = Path('app/build.gradle')
MANIFEST = Path('app/src/main/AndroidManifest.xml')

src = MAIN.read_text(encoding='utf-8')

needle = '''        try {
            FirebaseApp app = FirebaseApp.initializeApp(this);
            if (app != null) firebaseAuth = FirebaseAuth.getInstance();
        } catch (Exception ignored) { firebaseAuth = null; }
'''
replacement = needle + '''        PushNotifications.initialize(this);
        if (firebaseAuth != null && firebaseAuth.getCurrentUser() != null) PushNotifications.refreshToken();
'''
if 'PushNotifications.initialize(this);' not in src:
    src = src.replace(needle, replacement)

old_session = '''    private void saveLocalSession(String name, String provider) {
        displayName=name; getPreferences(0).edit().putString("name",name).putString("login_provider",provider).apply(); home();
    }
'''
new_session = '''    private void saveLocalSession(String name, String provider) {
        displayName=name; getPreferences(0).edit().putString("name",name).putString("login_provider",provider).apply();
        if (CloudSync.isSignedIn()) PushNotifications.refreshToken();
        home();
    }
'''
src = src.replace(old_session, new_session)

old_report = '''    private void reportDialog(String who){
        String[] reasons={"Spam","Harassment","Inappropriate content","Fake account","Other"};
        new AlertDialog.Builder(this).setTitle("Report "+who).setItems(reasons,(d,w)->{int count=getPreferences(0).getInt("reports",0)+1;getPreferences(0).edit().putInt("reports",count).apply();Toast.makeText(this,"Report saved for review",Toast.LENGTH_SHORT).show();}).setNegativeButton("Cancel",null).show();
    }
'''
new_report = '''    private void reportDialog(String who){
        String[] reasons={"Spam","Harassment","Inappropriate content","Fake account","Other"};
        new AlertDialog.Builder(this).setTitle("Report "+who).setItems(reasons,(d,w)->{
            String reason=reasons[w];
            int count=getPreferences(0).getInt("reports",0)+1;
            String entry=System.currentTimeMillis()+" | "+who+" | "+reason;
            String old=getPreferences(0).getString("report_history","");
            getPreferences(0).edit().putInt("reports",count).putString("report_history",entry+"\\n"+old).apply();
            CloudSync.submitReport(this,who,reason,(ok,message)->runOnUiThread(()->Toast.makeText(this,message,Toast.LENGTH_LONG).show()));
        }).setNegativeButton("Cancel",null).show();
    }
'''
src = src.replace(old_report, new_report)

marker = '    private void walletPage(){\n'
if 'private void cloudSafetyCenter()' not in src:
    helpers = '''    private void cloudSafetyCenter(){
        screen="cloud_safety"; base("Cloud & Safety Center","KING Plus v2.8 foundation");
        text(CloudSync.isSignedIn()?"☁ Firebase account connected":"☁ Local/test session • cloud actions need real Firebase sign-in",16,Color.WHITE,true);
        button("☁ Sync profile + TEST wallet snapshot",PURPLE,()->CloudSync.syncProfileAndTestWallet(this,getPreferences(0),displayName,coinBalance,giftCount,receivedGiftCount,(ok,message)->runOnUiThread(()->Toast.makeText(this,message,Toast.LENGTH_LONG).show())));
        button("🔔 Enable / refresh push notifications",CARD,()->{PushNotifications.requestPermission(this);Toast.makeText(this,"Push notification setup requested",Toast.LENGTH_SHORT).show();});
        button("🛡 Moderation & Safety",CARD,this::moderationCenterPage);
        button("💳 Recharge Center",CARD,this::rechargeCenterPage);
        text("Cloud profile sync, FCM token registration and cloud report submission are wired. Real push delivery still needs messages sent through Firebase/your backend.",13,MUTED,false);
        button("Back to Settings",CARD,this::settingsPage);
    }

    private void moderationCenterPage(){
        screen="moderation"; base("Moderation & Safety","Block, report and review foundation");
        Set<String> blocked=new HashSet<>(getPreferences(0).getStringSet("blocked",new HashSet<>()));
        int reports=getPreferences(0).getInt("reports",0);
        text("🚫 Blocked users: "+blocked.size(),18,Color.WHITE,true);
        text("⚑ Reports submitted locally: "+reports,18,Color.WHITE,true);
        String history=getPreferences(0).getString("report_history","");
        if(history.isEmpty()) text("No local report history yet.",14,MUTED,false);
        else {
            text("Recent local report audit",16,Color.WHITE,true);
            String[] rows=history.split("\\n");
            for(int i=0;i<Math.min(rows.length,8);i++) if(!rows[i].trim().isEmpty()) text(rows[i],12,MUTED,false);
        }
        text("Signed-in Firebase users also submit reports to the Firestore moderation queue. Reading/updating that queue is denied to normal app clients by the included starter security rules.",13,MUTED,false);
        button("Privacy controls",CARD,this::privacySafetyPage);
        button("Back",CARD,this::cloudSafetyCenter);
    }

    private void rechargeCenterPage(){
        screen="recharge"; base("Recharge Center","TEST purchase foundation • no real money charged");
        text("Current balance: 💎 "+coinBalance+" coins",24,Color.WHITE,true);
        button("TEST +100 coins • ₹10 display price",CARD,()->testRecharge(100,10));
        button("TEST +600 coins • ₹50 display price",CARD,()->testRecharge(600,50));
        button("TEST +1300 coins • ₹100 display price",PURPLE,()->testRecharge(1300,100));
        String history=getPreferences(0).getString("test_recharge_history","");
        if(!history.isEmpty()) { text("Test receipts",17,Color.WHITE,true); String[] rows=history.split("\\n"); for(int i=0;i<Math.min(rows.length,5);i++) if(!rows[i].trim().isEmpty()) text(rows[i],11,MUTED,false); }
        text("PRODUCTION LOCK: this build cannot charge money. Real recharge requires Play Billing/store products plus trusted backend receipt verification and server-authoritative coin crediting.",13,0xffffd768,true);
        button("Back to Wallet",CARD,this::walletPage);
    }

    private void testRecharge(int coins,int rupees){
        RechargeManager.simulateTestRecharge(getPreferences(0),coins,rupees,(newBalance,receipt)->{
            coinBalance=newBalance;
            appendTransaction("+"+coins+" TEST coins • recharge simulator • NO MONEY CHARGED");
            Toast.makeText(this,"TEST balance updated • no money charged",Toast.LENGTH_LONG).show();
            rechargeCenterPage();
        });
    }

'''
    src = src.replace(marker, helpers + marker)

old_wallet = '''        button("Open Gift Catalog",PURPLE,this::giftCatalogPage);
        button("📜 Transaction History",CARD,this::transactionHistoryPage);
        button("📅 Daily Check-in",CARD,this::dailyCheckInPage);
        text("Real-money recharge/payout is intentionally disabled until a verified payment backend is connected.",13,MUTED,false);
'''
new_wallet = '''        button("Open Gift Catalog",PURPLE,this::giftCatalogPage);
        button("💳 Recharge Center • TEST",CARD,this::rechargeCenterPage);
        button("☁ Sync cloud profile + TEST wallet snapshot",CARD,()->CloudSync.syncProfileAndTestWallet(this,getPreferences(0),displayName,coinBalance,giftCount,receivedGiftCount,(ok,message)->runOnUiThread(()->Toast.makeText(this,message,Toast.LENGTH_LONG).show())));
        button("📜 Transaction History",CARD,this::transactionHistoryPage);
        button("📅 Daily Check-in",CARD,this::dailyCheckInPage);
        text("Production paid balance remains locked until a trusted payment backend verifies store receipts. The cloud wallet write in this test build is only a non-authoritative snapshot.",13,MUTED,false);
'''
src = src.replace(old_wallet, new_wallet)

settings_needle = '        button("Privacy & Safety",CARD,this::privacySafetyPage);\n'
if 'Cloud & Safety Center' not in src:
    src = src.replace(settings_needle, settings_needle + '        button("☁ Cloud & Safety Center",CARD,this::cloudSafetyCenter);\n        button("🔔 Push notification permission",CARD,()->PushNotifications.requestPermission(this));\n')

profile_needle = '        cardLine(list,"💎 My Wallet","Coins, gifts and transaction history",this::walletPage);'
if 'Cloud profile, push & moderation' not in src:
    src = src.replace(profile_needle, profile_needle + ' cardLine(list,"☁ Cloud & Safety","Cloud profile, push & moderation foundation",this::cloudSafetyCenter);')

MAIN.write_text(src, encoding='utf-8')

gradle = BUILD.read_text(encoding='utf-8')
if "firebase-firestore" not in gradle:
    gradle = gradle.replace("    implementation 'com.google.firebase:firebase-auth'\n", "    implementation 'com.google.firebase:firebase-auth'\n    implementation 'com.google.firebase:firebase-firestore'\n    implementation 'com.google.firebase:firebase-messaging'\n")
gradle = re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 33; versionName '2.8.0'", gradle)
BUILD.write_text(gradle, encoding='utf-8')

manifest = MANIFEST.read_text(encoding='utf-8')
if 'POST_NOTIFICATIONS' not in manifest:
    manifest = manifest.replace('    <uses-permission android:name="android.permission.RECORD_AUDIO" />', '    <uses-permission android:name="android.permission.RECORD_AUDIO" />\n    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />')
if 'KingMessagingService' not in manifest:
    manifest = manifest.replace('        <activity android:name=".MainActivity" android:exported="true">', '        <service android:name=".KingMessagingService" android:exported="false">\n            <intent-filter><action android:name="com.google.firebase.MESSAGING_EVENT" /></intent-filter>\n        </service>\n        <activity android:name=".MainActivity" android:exported="true">')
MANIFEST.write_text(manifest, encoding='utf-8')

print('Prepared KING Plus v2.8.0 cloud/push/safety/recharge foundation')
