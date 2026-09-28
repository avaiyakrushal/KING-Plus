from pathlib import Path
import re

MAIN = Path('app/src/main/java/com/kingplus/social/MainActivity.java')
BUILD = Path('app/build.gradle')

src = MAIN.read_text(encoding='utf-8')

marker = '    private void walletPage(){\n'
if 'private void productionReadinessPage()' not in src:
    helpers = '''    private void productionReadinessPage(){
        screen="production_readiness";
        base("KING Plus v3.0","Security, notifications, billing and release readiness");
        text("🔐 Server wallet & gifts",19,Color.WHITE,true);
        text("Production wallet writes stay blocked from normal clients. Gifts use trusted Cloud Functions, idempotent operation IDs and server ledger entries.",13,MUTED,false);
        text("🔔 Push notifications",19,Color.WHITE,true);
        text("FCM supports gift, room invite, live-room message, direct-message and follow notification paths once Cloud Functions are deployed.",13,MUTED,false);
        text("💳 Google Play recharge",19,Color.WHITE,true);
        text("Play Billing never credits coins locally. A completed purchase token is sent to the backend for Google Play verification before server wallet credit.",13,MUTED,false);
        button("💳 Open Play Recharge",PURPLE,()->BillingManager.showRecharge(this));
        button("🌐 Open Live Cloud",CARD,this::openLiveCloud);
        button("🛡 Open Admin Dashboard",CARD,this::openAdminDashboard);
        text("Release candidate note: the CI APK/AAB is signed with the stable KING Plus test key so it can be tested consistently. A Play production upload key and Play App Signing are still required for store release.",12,0xffffd768,true);
        button("Back to Settings",CARD,this::settingsPage);
    }

'''
    src = src.replace(marker, helpers + marker)

settings_needle = '        button("🛡 Admin Dashboard",CARD,this::openAdminDashboard);\n'
if 'Production Readiness' not in src and settings_needle in src:
    src = src.replace(settings_needle, settings_needle + '        button("🚀 Production Readiness",PURPLE,this::productionReadinessPage);\n')

wallet_needle = '        button("💳 Recharge Center • TEST",CARD,this::rechargeCenterPage);\n'
if 'Google Play Recharge' not in src and wallet_needle in src:
    src = src.replace(wallet_needle, wallet_needle + '        button("💳 Google Play Recharge • VERIFY ON SERVER",PURPLE,()->BillingManager.showRecharge(this));\n')

MAIN.write_text(src, encoding='utf-8')

gradle = BUILD.read_text(encoding='utf-8')
if "com.android.billingclient:billing" not in gradle:
    gradle = gradle.replace("dependencies {\n", "dependencies {\n    implementation 'com.android.billingclient:billing:9.1.0'\n")
gradle = re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 40; versionName '3.0.0'", gradle)

if "release {" not in gradle:
    gradle = gradle.replace(
        "    buildTypes { debug { signingConfig signingConfigs.debug } }",
        '''    buildTypes {
        debug { signingConfig signingConfigs.debug }
        release {
            signingConfig signingConfigs.debug
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }'''
    )
BUILD.write_text(gradle, encoding='utf-8')

print('Prepared KING Plus v3.0.0 wallet security + push + Play Billing + polish + RC release')
