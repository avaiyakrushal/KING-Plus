from pathlib import Path
import re

MAIN = Path('app/src/main/java/com/kingplus/social/MainActivity.java')
BUILD = Path('app/build.gradle')
MANIFEST = Path('app/src/main/AndroidManifest.xml')

src = MAIN.read_text(encoding='utf-8')

# v2.9 uses the real Firebase mobile OTP path from the base source. The workflow intentionally
# does not run prepare_test_build.py before this script.
marker = '    private void walletPage(){\n'
if 'private void openLiveCloud()' not in src:
    helpers = '''    private void openLiveCloud(){
        if(!CloudSync.isSignedIn()){
            new AlertDialog.Builder(this).setTitle("Real Firebase sign-in required")
                .setMessage("Live rooms, cloud chat, server wallet, push invites and admin tools require a real Firebase Google/phone account. The v2.9 build uses the real mobile OTP path.")
                .setPositiveButton("OK",null).show();
            return;
        }
        startActivity(new Intent(this,LiveCloudActivity.class));
    }
    private void openAdminDashboard(){
        if(!CloudSync.isSignedIn()){
            Toast.makeText(this,"Sign in with Firebase first",Toast.LENGTH_LONG).show();
            return;
        }
        startActivity(new Intent(this,AdminActivity.class));
    }

'''
    src = src.replace(marker, helpers + marker)

profile_needle = 'cardLine(list,"☁ Cloud & Safety","Cloud profile, push & moderation foundation",this::cloudSafetyCenter);'
if 'Live rooms, real-time chat & voice' not in src and profile_needle in src:
    src = src.replace(profile_needle, profile_needle + ' cardLine(list,"🌐 Live Cloud","Live rooms, real-time chat & voice",this::openLiveCloud);')

settings_needle = '        button("☁ Cloud & Safety Center",CARD,this::cloudSafetyCenter);\n'
if 'Live Cloud Rooms & Chat' not in src and settings_needle in src:
    src = src.replace(settings_needle, settings_needle + '        button("🌐 Live Cloud Rooms & Chat",PURPLE,this::openLiveCloud);\n        button("🛡 Admin Dashboard",CARD,this::openAdminDashboard);\n')

MAIN.write_text(src, encoding='utf-8')

gradle = BUILD.read_text(encoding='utf-8')
if "firebase-functions" not in gradle:
    gradle = gradle.replace("    implementation 'com.google.firebase:firebase-auth'\n", "    implementation 'com.google.firebase:firebase-auth'\n    implementation 'com.google.firebase:firebase-functions'\n")
gradle = re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 34; versionName '2.9.0'", gradle)
BUILD.write_text(gradle, encoding='utf-8')

manifest = MANIFEST.read_text(encoding='utf-8')
if 'MODIFY_AUDIO_SETTINGS' not in manifest:
    manifest = manifest.replace('    <uses-permission android:name="android.permission.RECORD_AUDIO" />', '    <uses-permission android:name="android.permission.RECORD_AUDIO" />\n    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />')
activity_block = '''        <activity android:name=".VoiceWebActivity" android:exported="false" />
        <activity android:name=".AdminActivity" android:exported="false" />
        <activity android:name=".LiveCloudActivity" android:exported="false" />
'''
if 'LiveCloudActivity' not in manifest:
    manifest = manifest.replace('        <activity android:name=".MainActivity" android:exported="true">', activity_block + '        <activity android:name=".MainActivity" android:exported="true">')
MANIFEST.write_text(manifest, encoding='utf-8')

print('Prepared KING Plus v2.9.0 real auth + live cloud + voice + wallet + push + admin foundation')
