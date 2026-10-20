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

# Activity task shortcuts remain unrewarded; only the new verified check-in claims XP.
assert 'getHttpsCallable("claimDailyCheckin")' in deep
assert 'getHttpsCallable("getDailyCheckinStatus")' in deep
assert "other task rewards stay disabled" in deep
assert "No mission can claim coins or XP here" in missions

print("PASS check-in and missions cannot fabricate local coins, mission claims, or XP")
