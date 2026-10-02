#!/usr/bin/env bash
set -euo pipefail

bash tools/build_v510.sh
python3 tools/prepare_real_data_v520.py

python3 - <<'PY'
from pathlib import Path
import re
p=Path('app/build.gradle')
s=p.read_text(encoding='utf-8')
s=re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 75; versionName '5.2.0'", s)
p.write_text(s, encoding='utf-8')
Path('CHANGELOG-v5.2.0.txt').write_text(
    'KING Plus v5.2.0 — Real Data Cleanup Stage\n\n'
    'Builds on v5.1.0 Party Interaction. Removes user-facing seeded/demo Party rooms and people from legacy MainActivity paths, routes stale Party/search/people entry points to the Firebase-backed PartyActivity and SocialActivity, removes fake leaderboard totals and fake Family/Agency member counts, and stops inserting sample notifications when no real activity exists. Signed-in Party remains backed by real Firebase rooms/member counts and existing Room Ranking/Gift History/Follower flows. Existing no-billing mode and TEST OTP 123456 remain unchanged.\n',
    encoding='utf-8'
)
PY

python3 tools/production_smoke_check.py
node --check functions/index.js
node --check functions/v3.js
gradle :app:assembleDebug --no-daemon

mkdir -p release_v520
cp app/build/outputs/apk/debug/app-debug.apk release_v520/KING-Plus-v5.2.0-real-data-cleanup-no-billing-test-otp.apk
zip -qr release_v520/KING-Plus-v5.2.0-real-data-cleanup-source.zip . \
  -x '.git/*' 'app/build/*' 'release/*' 'release_v430/*' 'release_v431/*' 'release_v440/*' 'release_v441/*' 'release_v450/*' 'release_v460/*' 'release_v470/*' 'release_v480/*' 'release_v490/*' 'release_v500/*' 'release_v510/*' 'release_v520/*'
