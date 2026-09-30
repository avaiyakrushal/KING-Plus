#!/usr/bin/env bash
set -euo pipefail

bash tools/build_v411.sh
python3 tools/prepare_google_login_v412.py

python3 tools/production_smoke_check.py
node --check functions/index.js
node --check functions/v3.js
gradle :app:assembleDebug --no-daemon

rm -rf release_v412
mkdir -p release_v412
cp app/build/outputs/apk/debug/app-debug.apk release_v412/KING-Plus-v4.1.2-google-login-no-billing-test-otp.apk
zip -qr release_v412/KING-Plus-v4.1.2-google-login-source.zip . -x '.git/*' 'app/build/*' 'release/*' 'release_v412/*'
