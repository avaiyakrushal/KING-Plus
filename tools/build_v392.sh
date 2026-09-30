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

# v3.9.2 is intentionally stored as plain source/patch files so the CI path
# cannot be damaged by a large base64+gzip payload.
cp tools/InboxActivity_v392.java app/src/main/java/com/kingplus/social/InboxActivity.java
patch --dry-run -p0 < tools/games_v392.patch
patch -p0 < tools/games_v392.patch

python3 - <<'PY'
from pathlib import Path
import re
p=Path('app/build.gradle')
s=p.read_text()
s=re.sub(r"versionCode\s+\d+;\s+versionName\s+'[^']+'", "versionCode 57; versionName '3.9.2'", s)
p.write_text(s)
Path('CHANGELOG-v3.9.2.txt').write_text(
    'KING Plus v3.9.2 — BoloHi-style Game + Messages\n\n'
    'Messages uses compact conversation rows, real Firebase direct threads, search, followers/friends/notification shortcuts and the standard bottom navigation. Game uses the Today/Games/Party header, Hot/LUDO/Party/Team tabs, BoloHi-inspired two-column cards, Ludo Master, Sheep Fight, Werewolf, Spy Game, Draw & Guess, Bingo, Crazy Zoo, history/stats and voice game-room entry. No-billing and TEST OTP 123456 remain unchanged.\n'
)
PY

python3 tools/production_smoke_check.py
node --check functions/index.js
node --check functions/v3.js
gradle :app:assembleDebug --no-daemon

mkdir -p release
cp app/build/outputs/apk/debug/app-debug.apk release/KING-Plus-v3.9.2-bolohi-game-messages-no-billing-test-otp.apk
zip -qr release/KING-Plus-v3.9.2-bolohi-game-messages-source.zip . -x '.git/*' 'app/build/*' 'release/*'
