#!/usr/bin/env bash
set -euo pipefail

bash tools/build_v470.sh
python3 tools/prepare_music_v480.py

python3 - <<'PY'
from pathlib import Path
import re
p=Path('app/build.gradle')
s=p.read_text(encoding='utf-8')
s=re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 71; versionName '4.8.0'", s)
p.write_text(s, encoding='utf-8')
Path('CHANGELOG-v4.8.0.txt').write_text(
    'KING Plus v4.8.0 — Shared Room Music Sync\n\n'
    'Extends v4.7.0 persistent multi-song playlists with real cloud-room shared playback. Host/co-host selected local audio plays immediately on the host, uploads through the existing authenticated Firebase Storage media path when eligible, and publishes play/pause/next/previous/stop position state through the existing live-room event channel. Other signed-in room members automatically stream the same uploaded track and approximately seek to the shared room position. Guest volume stays local. Host/co-host controls remain authoritative. Songs over the current 12 MB media-rule limit or failed uploads continue locally without crashing the room. No billing flow was added; TEST OTP 123456 remains unchanged.\n',
    encoding='utf-8'
)
PY

python3 tools/production_smoke_check.py
node --check functions/index.js
node --check functions/v3.js
gradle :app:assembleDebug --no-daemon

mkdir -p release_v480
cp app/build/outputs/apk/debug/app-debug.apk release_v480/KING-Plus-v4.8.0-shared-room-music-no-billing-test-otp.apk
zip -qr release_v480/KING-Plus-v4.8.0-shared-room-music-source.zip . \
  -x '.git/*' 'app/build/*' 'release/*' 'release_v430/*' 'release_v431/*' 'release_v440/*' 'release_v441/*' 'release_v450/*' 'release_v460/*' 'release_v470/*' 'release_v480/*'
