#!/usr/bin/env bash
set -euo pipefail

bash tools/build_v480.sh

base64 -d tools/party_v490.patch.gz.b64 | gzip -d > /tmp/party_v490.patch
patch --dry-run -p0 < /tmp/party_v490.patch
patch -p0 < /tmp/party_v490.patch

python3 - <<'PY'
from pathlib import Path
import re
p=Path('app/build.gradle')
s=p.read_text(encoding='utf-8')
s=re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 72; versionName '4.9.0'", s)
p.write_text(s, encoding='utf-8')
Path('CHANGELOG-v4.9.0.txt').write_text(
    'KING Plus v4.9.0 — Party Room Full Feature Stage\n\n'
    'Extends v4.8.0 shared-room music with the reference-inspired Party Room experience requested from supplied screenshots. Adds original premium KING Plus gradient room backgrounds, Templates/Events center, quick-message chips, a real send control, categorized emoji/sticker panels, no-billing VIP sticker gating, host/no-host 8/10/12 seat layouts, real gift-event counter, Billboard, Channel/Group panel, Administrator management (up to five co-host administrators), room-local entrance-effect preferences, room robot actions and expanded room management. Existing Play Center, gift shop, live Firebase seats/members/events, private rooms, moderation, multi-song playlists and shared room music are preserved. Demo users/messages are no longer inserted into local rooms. No billing was added; TEST OTP 123456 is unchanged.\n',
    encoding='utf-8'
)
PY

python3 tools/production_smoke_check.py
node --check functions/index.js
node --check functions/v3.js
gradle :app:assembleDebug --no-daemon

mkdir -p release_v490
cp app/build/outputs/apk/debug/app-debug.apk release_v490/KING-Plus-v4.9.0-party-room-full-feature-no-billing-test-otp.apk
zip -qr release_v490/KING-Plus-v4.9.0-party-room-full-feature-source.zip . \
  -x '.git/*' 'app/build/*' 'release/*' 'release_v430/*' 'release_v431/*' 'release_v440/*' 'release_v441/*' 'release_v450/*' 'release_v460/*' 'release_v470/*' 'release_v480/*' 'release_v490/*'
