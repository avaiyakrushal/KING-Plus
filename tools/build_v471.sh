#!/usr/bin/env bash
set -euo pipefail

bash tools/build_v470.sh
python3 tools/prepare_shared_music_v471.py

python3 - <<'PY'
from pathlib import Path
import re
p=Path('app/build.gradle')
s=p.read_text(encoding='utf-8')
s=re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 71; versionName '4.7.1'", s)
p.write_text(s, encoding='utf-8')
Path('CHANGELOG-v4.7.1.txt').write_text(
    'KING Plus v4.7.1 — Shared Room Music Sync\n\n'
    'Extends v4.7.0 multi-song playlists with host-synchronized room music for direct HTTPS audio sources. Host play, pause, resume, previous/next and current playback position are synchronized through the existing live room Firestore document, so signed-in room members can hear the same shared track without Firebase Storage or billing. Device-local songs remain persistent and playable on the host device; local content URIs are not uploaded or exposed to other users. Guests keep independent volume control while shared playback controls remain host-owned. No billing; TEST OTP 123456 unchanged.\n',
    encoding='utf-8'
)
PY

python3 tools/production_smoke_check.py
node --check functions/index.js
node --check functions/v3.js
gradle :app:assembleDebug --no-daemon

mkdir -p release_v471
cp app/build/outputs/apk/debug/app-debug.apk release_v471/KING-Plus-v4.7.1-shared-room-music-no-billing-test-otp.apk
zip -qr release_v471/KING-Plus-v4.7.1-shared-room-music-source.zip . \
  -x '.git/*' 'app/build/*' 'release/*' 'release_v430/*' 'release_v431/*' 'release_v440/*' 'release_v441/*' 'release_v450/*' 'release_v460/*' 'release_v470/*' 'release_v471/*'
