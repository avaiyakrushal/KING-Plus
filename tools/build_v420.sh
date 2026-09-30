#!/usr/bin/env bash
set -euo pipefail

python3 tools/prepare_v28_build.py
python3 tools/prepare_v29_build.py
python3 tools/prepare_v30_build.py
python3 tools/prepare_v301_hardening.py
python3 tools/prepare_test_build.py
python3 tools/prepare_party_v311.py
python3 tools/prepare_me_copy_v312.py
python3 tools/prepare_party_copy_v320.py
python3 tools/prepare_party_security_v321.py
python3 tools/prepare_party_password_v322.py
python3 tools/prepare_games_v330.py
python3 tools/prepare_party_complete_v340.py
python3 tools/prepare_social_v350.py

apply_patch () {
  local src="$1"
  local out="$2"
  base64 -d "$src" | gzip -d > "$out"
  patch --dry-run -p0 < "$out"
  patch -p0 < "$out"
}

apply_patch tools/room_options_v360.patch.gz.b64 /tmp/room-options.patch
apply_patch tools/room_live_v370.patch.gz.b64 /tmp/room-live.patch
apply_patch tools/room_real_v380.patch.gz.b64 /tmp/room-real.patch
apply_patch tools/real_social_v390.patch.gz.b64 /tmp/real-social.patch
apply_patch tools/party_unified_v391.patch.gz.b64 /tmp/party-unified.patch

cp tools/InboxActivity_v392.java app/src/main/java/com/kingplus/social/InboxActivity.java
patch --dry-run -p0 < tools/games_v392.patch
patch -p0 < tools/games_v392.patch

apply_patch tools/party_complete_v400.patch.gz.b64 /tmp/party-complete-v400.patch
apply_patch tools/party_visual_v410.patch.gz.b64 /tmp/party-visual-v410.patch
python3 tools/prepare_party_create_fix_v411.py
python3 tools/prepare_google_login_v420.py

# Fail the build if the stable APK signing key is not registered in google-services.json.
python3 - <<'PY'
from pathlib import Path
import subprocess, re, json

ks = Path('app/kingplus-ci-debug.keystore')
if not ks.exists():
    raise SystemExit('Missing stable CI signing keystore')
out = subprocess.check_output([
    'keytool','-list','-v','-keystore',str(ks),'-storepass','android',
    '-alias','androiddebugkey','-keypass','android'
], stderr=subprocess.STDOUT, text=True)
m = re.search(r'SHA1:\s*([0-9A-Fa-f:]+)', out)
if not m:
    raise SystemExit('Could not read APK signing SHA-1')
sha = m.group(1).replace(':','').lower()

cfg = json.loads(Path('app/google-services.json').read_text())
clients = cfg.get('client', [])
android = [c for c in clients if c.get('client_info',{}).get('android_client_info',{}).get('package_name') == 'com.kingplus.social']
if not android:
    raise SystemExit('google-services.json has no com.kingplus.social Android client')
hashes = set()
has_web = False
for c in android:
    for o in c.get('oauth_client', []):
        if o.get('client_type') == 3:
            has_web = True
        h = o.get('android_info',{}).get('certificate_hash')
        if h:
            hashes.add(h.replace(':','').lower())
print('KING Plus APK signing SHA-1:', ':'.join(sha[i:i+2] for i in range(0,len(sha),2)).upper())
print('Registered Android OAuth SHA-1 count:', len(hashes))
if sha not in hashes:
    raise SystemExit('Current APK signing SHA-1 is NOT registered in google-services.json')
if not has_web:
    raise SystemExit('google-services.json has no OAuth web client for Firebase Google ID tokens')
print('Google/Gmail OAuth signing configuration verified')
PY

python3 tools/production_smoke_check.py
node --check functions/index.js
node --check functions/v3.js
gradle :app:assembleDebug --no-daemon

mkdir -p release
cp app/build/outputs/apk/debug/app-debug.apk release/KING-Plus-v4.2.0-google-gmail-login-no-billing-test-otp.apk
zip -qr release/KING-Plus-v4.2.0-google-gmail-login-source.zip . -x '.git/*' 'app/build/*' 'release/*'
