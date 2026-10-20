from pathlib import Path
root = Path(__file__).resolve().parents[2]
main = (root / 'app/src/main/java/com/kingplus/social/MainActivity.java').read_text()
menu = (root / 'app/src/main/java/com/kingplus/social/KingDeepFlowActivity.java').read_text()
access = (root / 'app/src/main/java/com/kingplus/social/KingAgencyAccess9982.java').read_text()
activity = (root / 'app/src/main/java/com/kingplus/social/KingAgency9982Activity.java').read_text()
gradle = (root / 'app/build.gradle').read_text()
assert 'KingAgencyAccess9982.check(this,allowed->' in main
assert 'if(allowed&&menuIndex>=0)' in main
assert 'KingAgencyAccess9982.check(this,allowed->{' in menu
assert 'if(allowed&&!isFinishing())row("🏢","Agency & Agent Recharge"' in menu
assert 'getIdToken(false)' in access and 'token.getClaims().get("admin")' in access
assert 'king_agencies' in access and 'king_agents' in access and 'Source.SERVER' in access
assert 'boolean admin, activeAgency, activeAgent' in activity
assert 'versionCode 375' in gradle and '10.0.96-me-profile-progress' in gradle
print('PASS: Agency entry points are shown only for server-authorized Super Admin, active Agency, or active Agent accounts')
