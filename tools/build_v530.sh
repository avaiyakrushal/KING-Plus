#!/usr/bin/env bash
set -euo pipefail

bash tools/build_v520.sh
python3 tools/run_prepare_party_v530.py

python3 - <<'PY'
from pathlib import Path
import re
p=Path('app/build.gradle')
s=p.read_text(encoding='utf-8')
s=re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 76; versionName '5.3.0'", s)
p.write_text(s, encoding='utf-8')
Path('CHANGELOG-v5.3.0.txt').write_text(
    'KING Plus v5.3.0 — Live Emoji + Seat Invite/Kick Stage\n\n'
    'Builds on v5.2.0 real-data cleanup. Adds a BoloHi-inspired KING Plus Party lobby Home/Create-room icon, room-wide synchronized live emoji animation through Firestore events, host/co-host seat Invite with member Accept/Decline, and direct occupied-seat moderation for mute/unmute, remove from seat, kick from room and ban. No billing changes were added; existing no-billing behavior and TEST OTP flow remain unchanged.\n',
    encoding='utf-8'
)
PY

python3 tools/production_smoke_check.py
node --check functions/index.js
node --check functions/v3.js
gradle :app:assembleDebug --no-daemon

mkdir -p release_v530
cp app/build/outputs/apk/debug/app-debug.apk release_v530/KING-Plus-v5.3.0-live-emoji-seat-controls-no-billing-test-otp.apk
zip -qr release_v530/KING-Plus-v5.3.0-live-emoji-seat-controls-source.zip . \
  -x '.git/*' 'app/build/*' 'release/*' 'release_v430/*' 'release_v431/*' 'release_v440/*' 'release_v441/*' 'release_v450/*' 'release_v460/*' 'release_v470/*' 'release_v480/*' 'release_v490/*' 'release_v500/*' 'release_v510/*' 'release_v520/*' 'release_v530/*'
