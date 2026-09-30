#!/usr/bin/env bash
set -euo pipefail

# Build the verified v4.2.0 Gmail/Firebase base first.
bash tools/build_v420.sh

# Add the real-people connection stage on top of that generated source.
python3 tools/prepare_real_people_v430.py

python3 tools/production_smoke_check.py
node --check functions/index.js
node --check functions/v3.js
gradle :app:assembleDebug --no-daemon

mkdir -p release_v430
cp app/build/outputs/apk/debug/app-debug.apk release_v430/KING-Plus-v4.3.0-real-people-connect-no-billing-test-otp.apk
zip -qr release_v430/KING-Plus-v4.3.0-real-people-connect-source.zip . \
  -x '.git/*' 'app/build/*' 'release/*' 'release_v430/*'
