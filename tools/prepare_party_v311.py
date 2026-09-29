from pathlib import Path
import re

MAIN = Path('app/src/main/java/com/kingplus/social/MainActivity.java')
MANIFEST = Path('app/src/main/AndroidManifest.xml')
BUILD = Path('app/build.gradle')

src = MAIN.read_text(encoding='utf-8')

# Route the Party tab into the dedicated BoloHi-style PartyActivity.
if 'private void openPartyActivity()' not in src:
    marker = '    private void home() {\n'
    helper = '''    private void openPartyActivity() {
        Intent i = new Intent(this, PartyActivity.class);
        i.putExtra("displayName", displayName);
        startActivity(i);
    }

'''
    src = src.replace(marker, helper + marker)

src = src.replace(
    'n.setOnClickListener(v->{if(k==0)home();else if(k==1)games();else if(k==2)discover();else if(k==3)messages();else profile();});',
    'n.setOnClickListener(v->{if(k==0)openPartyActivity();else if(k==1)games();else if(k==2)discover();else if(k==3)messages();else profile();});'
)

# When returning from PartyActivity, restore the requested bottom tab.
old_start = '        if (displayName.isEmpty()) login(); else home();\n'
new_start = '''        if (displayName.isEmpty()) login();
        else {
            int openTab = getIntent() == null ? 0 : getIntent().getIntExtra("openTab", 0);
            if (openTab == 1) games();
            else if (openTab == 2) discover();
            else if (openTab == 3) messages();
            else if (openTab == 4) profile();
            else home();
        }
'''
src = src.replace(old_start, new_start)

# Add a clear entry point on Settings as well.
settings_needle = '        button("🔔 Push notification permission",CARD,()->PushNotifications.requestPermission(this));\n'
if 'BoloHi-style Party Center' not in src and settings_needle in src:
    src = src.replace(settings_needle, settings_needle + '        button("🎤 BoloHi-style Party Center",PURPLE,this::openPartyActivity);\n')

MAIN.write_text(src, encoding='utf-8')

manifest = MANIFEST.read_text(encoding='utf-8')
if 'PartyActivity' not in manifest:
    manifest = manifest.replace(
        '        <activity android:name=".MainActivity" android:exported="true">',
        '        <activity android:name=".PartyActivity" android:exported="false" />\n        <activity android:name=".MainActivity" android:exported="true">'
    )
MANIFEST.write_text(manifest, encoding='utf-8')

gradle = BUILD.read_text(encoding='utf-8')
gradle = re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 42; versionName '3.1.1'", gradle)
BUILD.write_text(gradle, encoding='utf-8')

print('Prepared KING Plus v3.1.1 BoloHi-style Party complete build')
