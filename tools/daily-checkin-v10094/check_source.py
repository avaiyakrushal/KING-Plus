from pathlib import Path
root=Path(__file__).resolve().parents[2]
app=(root/"app/src/main/java/com/kingplus/social/KingDeepFlowActivity.java").read_text()
service=(root/"functions/daily-checkin-service.js").read_text()
functions=(root/"functions/v3.js").read_text()
rules=(root/"firestore.rules").read_text()
workflow=(root/".github/workflows/king-v10077-party-rejoin-choice.yml").read_text()
gradle=(root/"app/build.gradle").read_text()
assert 'getHttpsCallable("claimDailyCheckin")' in app and 'getHttpsCallable("getDailyCheckinStatus")' in app
assert 'Claim account-synced daily Activity XP' in app
assert "Today's check-in is already claimed." in app
assert 'nonredeemable and never changes Diamonds' in app
assert "timeZone:'Asia/Kolkata'" in service and 'XP_PER_CHECKIN=10' in service
assert 'tx.set(event' in service and 'old.lastClaimDay===day||receipt.exists' in service
assert 'const getDailyCheckinStatus=onCall' in functions and 'const claimDailyCheckin=onCall' in functions
assert "request.auth.token?.firebase?.sign_in_provider==='anonymous'" in functions
assert 'match /king_daily_checkins/{uid}' in rules and 'allow create, update, delete: if false;' in rules
assert 'me-profile-checkin-v10094.test.mjs' in workflow and 'daily-checkin-service.test.js' in workflow
assert 'versionCode 376' in gradle and "10.0.97-me-privacy-progress" in gradle
assert "versionCode='376'" in workflow and 'KING-Plus-v10.0.97-Me-Profile-TEST.apk' in workflow
print('PASS: daily check-in is account-scoped, Kolkata-day based, idempotent, and awards nonredeemable XP only')
