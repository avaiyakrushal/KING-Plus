from pathlib import Path

root = Path(__file__).resolve().parents[2]
gradle = (root / 'app/build.gradle').read_text()
flow = (root / 'app/src/main/java/com/kingplus/social/KingDeepFlowActivity.java').read_text()
admin = (root / 'app/src/main/java/com/kingplus/social/AdminActivity.java').read_text()
main = (root / 'app/src/main/java/com/kingplus/social/MainActivity.java').read_text()
backend = (root / 'functions/index.js').read_text()
tests = (root / '.github/workflows/king-v10077-party-rejoin-choice.yml').read_text()

assert 'versionCode 380' in gradle and '10.0.101-party-events-live' in gradle
assert 'getClaims().get("admin")' in flow and 'Owner Console' in flow
assert 'getOwnerConsoleSnapshot' in admin and 'updateOwnerAppSettings' in admin
assert 'accounts:' in backend and 'openReports:' in backend and 'agencies:' in backend
assert 'getPublicAppNotice' in backend and 'loadOwnerAppNotice10095' in main
assert "permission-denied" in backend and "request.auth.token.admin !== true" in backend
assert 'owner-settings.test.js' in tests
assert 'KING-Plus-v10.0.101-Party-Events-TEST.apk' in tests
print('PASS: claim-gated owner console, server settings, public notice and release metadata')
