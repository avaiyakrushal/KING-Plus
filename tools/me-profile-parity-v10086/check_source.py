from pathlib import Path

root = Path(__file__).resolve().parents[2]
gradle = (root / 'app/build.gradle').read_text()
flow = (root / 'app/src/main/java/com/kingplus/social/KingDeepFlowActivity.java').read_text()
main = (root / 'app/src/main/java/com/kingplus/social/MainActivity.java').read_text()
workflow = (root / '.github/workflows/king-v10077-party-rejoin-choice.yml').read_text()

assert 'versionCode 368' in gradle
assert "10.0.89-me-profile-editor" in gradle
assert 'row("🎁","My Gift Wall","View gifts received by this signed-in account","gift_wall");' in flow
assert 'row("🕘","Gift History","View server-recorded sent and received gifts","gift_history");' in flow
assert 'row("✅","Daily Check-in & Missions"' in flow and '"task_center"' in flow
assert 'row("⚙","Settings & Privacy"' in flow and '"settings"' in flow
assert 'row("🔐","Account & Sign-in"' in flow and '"settings_account"' in flow
assert 'fetchAccountLedger9983(true,true)' in flow
assert 'profileWalletCoins=coins;' in main and 'coins.setOnClickListener(v->openDeepFlow810("gift_wall"));' in main
assert 'liveMeGift10064.setOnClickListener(v->openDeepFlow810("gift_wall"));' in main
assert 'fav.setText("My Videos")' in main and 'fav.setOnClickListener(v->videoPage())' in main
assert 'case "settings_account":settingsAccount();break;' in flow
assert 'case "task_center":taskCenter();break;' in flow
assert 'python3 tools/me-profile-parity-v10086/check_source.py' in workflow
print('PASS: Profile Center now opens account Gift Wall, Gift History, Missions, Settings and Account routes')
