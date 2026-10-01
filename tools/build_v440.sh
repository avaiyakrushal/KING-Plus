#!/usr/bin/env bash
set -euo pipefail

bash tools/build_v431.sh
python3 tools/prepare_party_room_v440.py
python3 tools/production_smoke_check.py
node --check functions/index.js
node --check functions/v3.js
gradle :app:assembleDebug --no-daemon

mkdir -p release_v440
cp app/build/outputs/apk/debug/app-debug.apk release_v440/KING-Plus-v4.4.0-party-room-complete-no-billing-test-otp.apk
zip -qr release_v440/KING-Plus-v4.4.0-party-room-complete-source.zip . \
  -x '.git/*' 'app/build/*' 'release/*' 'release_v430/*' 'release_v431/*' 'release_v440/*'
