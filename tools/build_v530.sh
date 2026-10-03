#!/usr/bin/env bash
set -euo pipefail

# Apply earlier source stages, then v5.3 realtime emoji and v5.3.2 compact Party UI.
# Intermediate Gradle builds/source archives are skipped; only the final APK is compiled.
mkdir -p app/build/outputs/apk/debug
: > app/build/outputs/apk/debug/app-debug.apk

gradle() {
  echo "[v5.3.2 fast CI] skipping intermediate Gradle build: $*"
}
zip() {
  echo "[v5.3.2 fast CI] skipping intermediate source archive"
}
export -f gradle zip

bash tools/build_v520.sh

unset -f gradle
unset -f zip
rm -rf app/build

python3 tools/run_prepare_party_v530.py
python3 tools/fix_live_emoji_v531.py
python3 tools/prepare_party_ui_v532.py

python3 - <<'PY'
from pathlib import Path
import re
p=Path('app/build.gradle')
s=p.read_text(encoding='utf-8')
s=re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 78; versionName '5.3.2'", s)
p.write_text(s, encoding='utf-8')
Path('CHANGELOG-v5.3.2.txt').write_text(
    'KING Plus v5.3.2 — Compact 8-Seat Party Room UI + Realtime Live Emoji\n\n'
    'Applies the supplied green Party Room layout direction: compact header with online count/share/menu, rank and heart badges, exactly eight compact mic seats in two rows, safety/chat area, quick message chips and a white bottom composer. Keeps v5.3.1 full-screen realtime live emoji sender/listener animation, seat invite/kick/moderation controls, no-billing behavior and TEST OTP unchanged.\n',
    encoding='utf-8'
)
PY

python3 tools/production_smoke_check.py
python3 - <<'PY'
from pathlib import Path
s=Path('app/src/main/java/com/kingplus/social/PartyActivity.java').read_text(encoding='utf-8')
required=[
    'ROOM_GREEN_V532','displaySeatsV532 = 8','addQuickChipV532',
    'liveOverlayLpV531','senderSafeV531','private void sendLiveEmojiV530(',
    'liveEmojiListenerV530=','dV530.put("emoji",emojiV530)'
]
missing=[x for x in required if x not in s]
if missing: raise SystemExit('v5.3.2 Party UI/live emoji verification failed: '+', '.join(missing))
print('v5.3.2 compact Party UI + live emoji sender/receiver/overlay verified')
PY
node --check functions/index.js
node --check functions/v3.js
gradle :app:assembleDebug --no-daemon

mkdir -p release_v532
cp app/build/outputs/apk/debug/app-debug.apk release_v532/KING-Plus-v5.3.2-compact-party-live-emoji-no-billing-test-otp.apk
zip -qr release_v532/KING-Plus-v5.3.2-compact-party-live-emoji-source.zip . \
  -x '.git/*' 'app/build/*' 'release/*' 'release_v430/*' 'release_v431/*' 'release_v440/*' 'release_v441/*' 'release_v450/*' 'release_v460/*' 'release_v470/*' 'release_v480/*' 'release_v490/*' 'release_v500/*' 'release_v510/*' 'release_v520/*' 'release_v530/*' 'release_v531/*' 'release_v532/*'
