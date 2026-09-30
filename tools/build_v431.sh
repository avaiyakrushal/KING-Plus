#!/usr/bin/env bash
set -euo pipefail

bash tools/build_v430.sh
python3 tools/prepare_room_profile_v431.py
python3 tools/production_smoke_check.py
node --check functions/index.js
node --check functions/v3.js
gradle :app:assembleDebug --no-daemon

mkdir -p release_v431
cp app/build/outputs/apk/debug/app-debug.apk release_v431/KING-Plus-v4.3.1-room-profile-id-no-billing-test-otp.apk
zip -qr release_v431/KING-Plus-v4.3.1-room-profile-id-source.zip . \
  -x '.git/*' 'app/build/*' 'release/*' 'release_v430/*' 'release_v431/*'
