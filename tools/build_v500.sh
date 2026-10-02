#!/usr/bin/env bash
set -euo pipefail

bash tools/build_v490.sh

base64 -d tools/party_v500.patch.gz.b64 | gzip -d > /tmp/party_v500.patch
patch --dry-run -p0 < /tmp/party_v500.patch
patch -p0 < /tmp/party_v500.patch

python3 - <<'PY'
from pathlib import Path
import re
p=Path('app/build.gradle')
s=p.read_text(encoding='utf-8')
s=re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 73; versionName '5.0.0'", s)
p.write_text(s, encoding='utf-8')
Path('CHANGELOG-v5.0.0.txt').write_text(
    'KING Plus v5.0.0 — Party Room Real State Stage\n\n'
    'Builds on v4.9.0 Party Room Full Feature. Room music is now backed by a persistent Firestore music_playlist plus a durable music_state/current document, so host/co-host playlists survive room reopen and shared playback state is not dependent on scanning transient room events. Added moderator-only playlist writes, per-room cloud playlist delete support, secure Firestore rules for music state/playlist and future room invites/settings/templates/announcements, plus backend-only gift/sticker ledgers. Room level and Heart level now update from real room activity and gift value instead of staying hard-coded at level 1. Existing no-billing mode and TEST OTP 123456 remain unchanged.\n',
    encoding='utf-8'
)
PY

python3 tools/production_smoke_check.py
node --check functions/index.js
node --check functions/v3.js
gradle :app:assembleDebug --no-daemon

mkdir -p release_v500
cp app/build/outputs/apk/debug/app-debug.apk release_v500/KING-Plus-v5.0.0-party-room-real-state-no-billing-test-otp.apk
zip -qr release_v500/KING-Plus-v5.0.0-party-room-real-state-source.zip . \
  -x '.git/*' 'app/build/*' 'release/*' 'release_v430/*' 'release_v431/*' 'release_v440/*' 'release_v441/*' 'release_v450/*' 'release_v460/*' 'release_v470/*' 'release_v480/*' 'release_v490/*' 'release_v500/*'
