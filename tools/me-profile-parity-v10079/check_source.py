from pathlib import Path

root = Path(__file__).resolve().parents[2]
main = (root / "app/src/main/java/com/kingplus/social/MainActivity.java").read_text()
deep = (root / "app/src/main/java/com/kingplus/social/KingDeepFlowActivity.java").read_text()
gradle = (root / "app/build.gradle").read_text()


# Later profile refactors moved the old sideMenu section; verify its routes across the activity.
drawer = main
for item in (
    "Square / Moments", "Coin Mall", "Data Center", "Privilege Pack",
    "Privilege Shop", "VIP Level", "Noble Center", "Family",
    "User Level Center", "Wallet", "Web Diamond Recharge",
    "Gift Wall / Share Gift", "Social Media", "Invite Friends",
    "Settings", "Help & Customer Support", "Rules and Policies",
    "All Me Options",
):
    assert item in drawer, f"missing Me drawer item: {item}"
for route in ("coin_mall", "data_center", "web_diamond", "social_center", "profile_menu", "settings", "help", "rules"):
    assert f'openDeepFlow810("{route}")' in drawer, f"missing drawer route: {route}"
assert "startActivity(new Intent(this,KingGiftCenterActivity.class))" in drawer

menu = deep[deep.index("private void profileMenu()") : deep.index("private void dataCenter()")]
for item in (
    "Account & status", "Agency & Agent Recharge", "User Level Center", "VIP Level",
    "Noble Center", "Wallet", "Coin Mall", "Web Diamond", "Square / Moments",
    "Social Media", "Visitors", "Family", "Relationship / CP", "Invite Friends",
    "Profile Center", "My Gallery", "Followers & Friends", "Backpack / Collection", "Notifications",
    "Friend Broadcast", "Share Gift / Gift Center", "My Gift Wall",
    "Privilege Pack", "Privilege Shop", "Rankings", "Tasks / Check-in",
    "Events", "Settings", "Help & Customer Support", "Rules and Policies",
):
    assert item in menu, f"missing All Me Options item: {item}"

for route in ("data_center", "coin_mall", "web_diamond"):
    assert f'case "{route}"' in deep, f"missing nested route handler: {route}"
assert "private void dataCenter()" in deep
assert 'case "profile_center"' in deep
assert 'case "settings_privacy"' in deep and 'case "settings_notifications"' in deep
assert 'case "detail_reports":go("report_history")' in deep
assert 'case "report_history":reportHistory9981()' in deep
assert 'case "detail_help_safety":safetyHelp9981()' in deep
assert 'case "detail_blocked":openMeCloud9984("blocked")' in deep
assert 'case "detail_rules_privacy":policyPage9981("privacy")' in deep
assert 'case "detail_rules_terms":policyPage9981("terms")' in deep
assert 'case "detail_rules_community":go("rules")' in deep
assert "Official privacy-policy web URL is not configured" in deep
assert "Official Terms of Service URL is not configured" in deep
assert 'private void openNotifications9981()' in deep
assert 'putExtra("openNotifications10010",true)' in deep
assert 'mainPrefs.getString("report_history_"+owner' in deep
assert 'getPreferences(0).getString(historyKey' in main
assert 'getPreferences(0).putString("report_history",' not in main
assert 'private void taskCenter(){hero("✅ Tasks & Check-in"' in deep
assert 'completion is not tracked in this TEST build' in deep
assert '+" XP not yet verified"' not in deep
print("PASS Me drawer parity: Bolo Hi menu shortcuts map into KING Plus pages; nested profile and data-center routes are present")
