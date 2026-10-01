#!/usr/bin/env bash
set -euo pipefail

bash tools/build_v450.sh

cat tools/discover_v460.part00 tools/discover_v460.part01 tools/discover_v460.part02 tools/discover_v460.part03 > /tmp/discover_v460.b64
base64 -d /tmp/discover_v460.b64 | gzip -d > /tmp/discover_v460.patch
patch --dry-run -p0 < /tmp/discover_v460.patch
patch -p0 < /tmp/discover_v460.patch

python3 - <<'PY'
from pathlib import Path
import re
p=Path('app/build.gradle')
s=p.read_text(encoding='utf-8')
s=re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 69; versionName '4.6.0'", s)
p.write_text(s, encoding='utf-8')
Path('CHANGELOG-v4.6.0.txt').write_text(
    'KING Plus v4.6.0 — Discover Complete\n\n'
    'Adds a dedicated Discover screen inspired by the supplied reference screenshots: ranking hero, contribution/charisma cards, live activity recommendations, Game Master, Chat Talent, Recommended/Nearby people, real follow actions, direct live-room opening, My Influence, medal thresholds and Data Record. Uses real Firebase public profiles, follows and live rooms; discover activity scores are synced per signed-in user through discover_stats. No fake people or fake live rooms are inserted. KING Plus branding remains. No billing; TEST OTP 123456 unchanged.\n',
    encoding='utf-8'
)
PY

python3 tools/production_smoke_check.py
node --check functions/index.js
node --check functions/v3.js
gradle :app:assembleDebug --no-daemon

mkdir -p release_v460
cp app/build/outputs/apk/debug/app-debug.apk release_v460/KING-Plus-v4.6.0-discover-complete-no-billing-test-otp.apk
zip -qr release_v460/KING-Plus-v4.6.0-discover-complete-source.zip . \
  -x '.git/*' 'app/build/*' 'release/*' 'release_v430/*' 'release_v431/*' 'release_v440/*' 'release_v441/*' 'release_v450/*' 'release_v460/*'
