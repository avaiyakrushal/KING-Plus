#!/usr/bin/env bash
set -euo pipefail

bash tools/build_v441.sh
python3 tools/prepare_party_room_v450.py
python3 tools/fix_v450_java_strings.py
python3 tools/production_smoke_check.py
node --check functions/index.js
node --check functions/v3.js
gradle :app:assembleDebug --no-daemon

mkdir -p release_v450
cp app/build/outputs/apk/debug/app-debug.apk release_v450/KING-Plus-v4.5.0-party-room-reference-no-billing-test-otp.apk
zip -qr release_v450/KING-Plus-v4.5.0-party-room-reference-source.zip . \
  -x '.git/*' 'app/build/*' 'release/*' 'release_v430/*' 'release_v431/*' 'release_v440/*' 'release_v441/*' 'release_v450/*'
