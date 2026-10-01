#!/usr/bin/env bash
set -euo pipefail

bash tools/build_v460.sh
python3 tools/prepare_music_v470.py

python3 - <<'PY'
from pathlib import Path
import re
p=Path('app/build.gradle')
s=p.read_text(encoding='utf-8')
s=re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 70; versionName '4.7.0'", s)
p.write_text(s, encoding='utf-8')
Path('CHANGELOG-v4.7.0.txt').write_text(
    'KING Plus v4.7.0 — Persistent Multi-song Room Music\n\n'
    'Adds a reference-inspired Party room music experience: full Playlist screen, search, edit/remove, ADD MUSIC, Android multi-select audio picker, persistent per-user playlist using persistable document URIs, real song filenames, previous/play-pause/next controls, volume slider, automatic next-track playback and a now-playing strip. Songs remain in the playlist after app restart while the original local files remain accessible on the device. Existing KING Plus Party, Discover, Firebase, no-billing and TEST OTP 123456 behavior remains unchanged.\n',
    encoding='utf-8'
)
PY

python3 tools/production_smoke_check.py
node --check functions/index.js
node --check functions/v3.js
gradle :app:assembleDebug --no-daemon

mkdir -p release_v470
cp app/build/outputs/apk/debug/app-debug.apk release_v470/KING-Plus-v4.7.0-multi-song-music-no-billing-test-otp.apk
zip -qr release_v470/KING-Plus-v4.7.0-multi-song-music-source.zip . \
  -x '.git/*' 'app/build/*' 'release/*' 'release_v430/*' 'release_v431/*' 'release_v440/*' 'release_v441/*' 'release_v450/*' 'release_v460/*' 'release_v470/*'
