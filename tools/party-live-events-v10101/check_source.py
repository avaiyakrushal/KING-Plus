from pathlib import Path

root = Path(__file__).resolve().parents[2]
java = root / "app/src/main/java/com/kingplus/social"
party = (java / "PartyActivity.java").read_text()
main = (java / "MainActivity.java").read_text()

for event, target in {
    '"Crazy Fruits"': 'openRoomGames740("Slot Clash")',
    '"Lucky Slot"': 'openRoomGames740("Slot Clash")',
    '"Chicken Run"': 'openRoomGames740("Live Sheep Sprint")',
    '"Animal PK"': 'openRoomGames740("Crazy Zoo")',
}.items():
    assert event in party and target in party, (event, target)

assert 'startRoomQuizDialog10101();' in party
assert 'publishGameState("vote","❓ Room Quiz: "+q,"",extras);' in party
assert 'options.add(a);' in party and 'options.contains(a)' in party
assert '❓ Room Quiz Results' in party
assert 'if("Draw & Guess".equals(name)){openRoomGames740("Draw & Guess");return;}' in party
assert 'if(name.contains("Ludo")){openRoomGames740("Ludo");return;}' in party
assert 'if(name.contains("Dominoes")){openRoomGames740("Domino Match");return;}' in party
assert 'if(name.contains("SHEEP")){openRoomGames740("Live Sheep Sprint");return;}' in party
assert 'if("Bingo".equals(name)){openRoomGames740("Bingo");return;}' in party
assert 'private void home() {' in main and 'openPartyActivity();' in main
assert 'private void renderHome(String category) {' in main
assert 'openPartyActivity();' in main[main.index('private void renderHome(String category) {'):main.index('private String[][] roomData')]
for stale in ('fruitEvent();return;', 'chickenRunEvent();return;', 'animalPkEvent();return;', 'luckySlotEvent();return;'):
    assert stale not in party, stale
print("PASS Party Events use shared multiplayer games, Room Quiz uses live votes, templates open matching games, and Home shares the live Party lobby")
