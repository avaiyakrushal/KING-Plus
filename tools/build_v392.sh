#!/usr/bin/env bash
set -euo pipefail

python3 tools/prepare_v28_build.py
python3 tools/prepare_v29_build.py
python3 tools/prepare_v30_build.py
python3 tools/prepare_v301_hardening.py
python3 tools/prepare_test_build.py
python3 tools/prepare_party_v311.py
python3 tools/prepare_me_copy_v312.py
python3 tools/prepare_party_copy_v320.py
python3 tools/prepare_party_security_v321.py
python3 tools/prepare_party_password_v322.py
python3 tools/prepare_games_v330.py
python3 tools/prepare_party_complete_v340.py
python3 tools/prepare_social_v350.py

apply_patch () {
  local src="$1"
  local out="$2"
  base64 -d "$src" | gzip -d > "$out"
  patch --dry-run -p0 < "$out"
  patch -p0 < "$out"
}

apply_patch tools/room_options_v360.patch.gz.b64 /tmp/room-options.patch
apply_patch tools/room_live_v370.patch.gz.b64 /tmp/room-live.patch
apply_patch tools/room_real_v380.patch.gz.b64 /tmp/room-real.patch
apply_patch tools/real_social_v390.patch.gz.b64 /tmp/real-social.patch
apply_patch tools/party_unified_v391.patch.gz.b64 /tmp/party-unified.patch
apply_patch tools/game_messages_v392.patch.gz.b64 /tmp/game-messages.patch

python3 - <<'PY'
from pathlib import Path
import re
p=Path('app/build.gradle')
s=p.read_text()
s=re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 57; versionName '3.9.2'", s)
p.write_text(s)
Path('CHANGELOG-v3.9.2.txt').write_text(
    'KING Plus v3.9.2 — BoloHi-style Game + Messages\n\n'
    'Refines Game and Messages pages to match the unified BoloHi-inspired navigation/layout used by Party, while preserving real Firebase social data, no-billing mode and TEST OTP 123456.\n'
)
PY

python3 tools/production_smoke_check.py
node --check functions/index.js
node --check functions/v3.js
gradle :app:assembleDebug --no-daemon

mkdir -p release
cp app/build/outputs/apk/debug/app-debug.apk release/KING-Plus-v3.9.2-bolohi-game-messages-no-billing-test-otp.apk
zip -qr release/KING-Plus-v3.9.2-bolohi-game-messages-source.zip . -x '.git/*' 'app/build/*' 'release/*'
