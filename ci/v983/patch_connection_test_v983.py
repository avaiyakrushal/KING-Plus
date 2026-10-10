#!/usr/bin/env python3
"""KING Plus v9.8.3 – on-phone multi-device Firestore connection diagnostics.

Start from successfully compiled v9.8.2 artifact, never old tracked /app.
Provide a non-destructive, privacy-safe test of Firebase token refresh,
network, server-only Room Code lookup and same-user membership read.
Reachable from Settings Help and directly from Party Join failure.
"""
from pathlib import Path
import shutil,sys

root=Path(sys.argv[1])
pkg=root/'app/src/main/java/com/kingplus/social'
for fn in ['KingConnection983Activity.java','KingConnectionDiagnostics983.java']:
    shutil.copy2(Path(__file__).with_name(fn),pkg/fn)

def change(src,old,new,label):
    count=src.count(old)
    if count!=1:raise SystemExit(f'{label}: expected one marker found {count}: {old[:130]!r}')
    print('PASS',label)
    return src.replace(old,new,1)

main=pkg/'MainActivity.java'
s=main.read_text()
s=change(s,
 'button("Login & OTP help",CARD,this::authDiagnostics910);',
 '''button("Login & OTP help",CARD,this::authDiagnostics910);
        button("🔌 Test Firebase / Party Room connection",CARD,
            ()->startActivity(new Intent(this,KingConnection983Activity.class)));''',
 'Expose server connection diagnostics in Settings Help & Feedback')
main.write_text(s)

party=pkg/'PartyActivity.java'
s=party.read_text()
s=change(s,
 '''        retry977.setOnClickListener(v->{
            if(isFinishing()||isDestroyed())return;''',
 '''        TextView testConnection983=tv("🔌  Test Firebase / Party connection",15,Color.WHITE,true);
        testConnection983.setGravity(Gravity.CENTER);
        testConnection983.setBackgroundColor(0xff3c516b);
        LinearLayout.LayoutParams testParams983=new LinearLayout.LayoutParams(-1,dp(52));
        testParams983.setMargins(0,dp(10),0,0);
        root977.addView(testConnection983,testParams983);
        testConnection983.setOnClickListener(v->{
            if(isFinishing()||isDestroyed())return;
            Intent intent983=new Intent(this,KingConnection983Activity.class);
            if(roomId!=null&&!roomId.isEmpty())
                intent983.putExtra("roomInput983",roomId);
            startActivity(intent983);
        });
        retry977.setOnClickListener(v->{
            if(isFinishing()||isDestroyed())return;''',
 'Party Join failure links to same-account Firebase server test and copies full room id')
party.write_text(s)

manifest=root/'app/src/main/AndroidManifest.xml'
s=manifest.read_text()
s=change(s,
 '<activity android:name=".KingRecharge974Activity" android:exported="false" />',
 '<activity android:name=".KingRecharge974Activity" android:exported="false" />\n        <activity android:name=".KingConnection983Activity" android:exported="false" />',
 'Register internal-only Firebase diagnostic Activity')
manifest.write_text(s)

gradle=root/'app/build.gradle'
s=gradle.read_text()
s=change(s,
 "versionCode 173; versionName '9.8.2-party-room-session-isolation'",
 "versionCode 174; versionName '9.8.3-on-phone-firebase-connection-test'",
 'Upgrade Android version without uninstalling v9.8.2')
gradle.write_text(s)
print('PASS v9.8.3 multi-phone read-only diagnostic screen integrated')
