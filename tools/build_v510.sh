#!/usr/bin/env bash
set -euo pipefail

bash tools/build_v500.sh

base64 -d tools/party_v510.patch.gz.b64 | gzip -d > /tmp/party_v510.patch
patch --dry-run -p0 < /tmp/party_v510.patch
patch -p0 < /tmp/party_v510.patch

python3 - <<'PY'
from pathlib import Path
import re
p=Path('app/build.gradle')
s=p.read_text(encoding='utf-8')
s=re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 74; versionName '5.1.0'", s)
p.write_text(s, encoding='utf-8')
Path('CHANGELOG-v5.1.0.txt').write_text(
    'KING Plus v5.1.0 — Party Interaction Stage\n\n'
    'Builds on v5.0.0 Party Room Real State. Adds a synchronized Firestore game_state/current document, room-wide Play Center banners, host/co-host game start control, persistent active room template state, and real multi-user room voting with one vote document per signed-in member and live result counting. Guess It, Truth or Dare, Pass the Bomb, Wheel Challenge, Party Wheel, and Room Battle now publish or consume shared room game state instead of acting only as local dialogs. Fan Box now links to Charisma/Gift Senders, Gift History, and Room Ranking. Existing no-billing mode and TEST OTP 123456 remain unchanged.\n',
    encoding='utf-8'
)
PY

python3 tools/production_smoke_check.py
node --check functions/index.js
node --check functions/v3.js
gradle :app:assembleDebug --no-daemon

mkdir -p release_v510
cp app/build/outputs/apk/debug/app-debug.apk release_v510/KING-Plus-v5.1.0-party-interaction-no-billing-test-otp.apk
zip -qr release_v510/KING-Plus-v5.1.0-party-interaction-source.zip . \
  -x '.git/*' 'app/build/*' 'release/*' 'release_v430/*' 'release_v431/*' 'release_v440/*' 'release_v441/*' 'release_v450/*' 'release_v460/*' 'release_v470/*' 'release_v480/*' 'release_v490/*' 'release_v500/*' 'release_v510/*'
