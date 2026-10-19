from pathlib import Path

root = Path(__file__).resolve().parents[2]
main = (root / "app/src/main/java/com/kingplus/social/MainActivity.java").read_text()
deep = (root / "app/src/main/java/com/kingplus/social/KingDeepFlowActivity.java").read_text()
gradle = (root / "app/build.gradle").read_text()


checkin = main[main.index("private void dailyCheckInPage()") : main.index("private void missionsPage()")]
missions = main[main.index("private void missionsPage()") : main.index("private void renderHome(")]
for source in (checkin, missions):
    assert "not active in this TEST build" in source
    assert "Open Tasks & Check-in" in source
    assert 'openDeepFlow810("task_center")' in source
    assert "coinBalance=" not in source
    assert "appendTransaction(" not in source
    assert "addXp(" not in source
assert "cannot mark a check-in or add coins/XP" in checkin
assert "No mission can claim coins or XP here" in missions

# Client activity must not manufacture mission completion or XP for ranks.
assert "incrementMission(" not in main
assert "addXp(" not in main
assert "private int getMissionValue(String key){return 0;}" in main

# The profile Task Center must continue to be explicit that it is not a live reward service.
assert 'private void taskCenter(){hero("✅ Tasks & Check-in"' in deep
assert "completion is not tracked in this TEST build" in deep
assert "No daily reward is claimable until a verified KING Plus server task/reward service is enabled." in deep

print("PASS check-in and missions cannot fabricate local coins, mission claims, or XP")
