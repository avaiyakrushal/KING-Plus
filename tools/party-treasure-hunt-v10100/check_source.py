from pathlib import Path
root=Path(__file__).resolve().parents[2]
java=root/'app/src/main/java/com/kingplus/social'
party=(java/'PartyActivity.java').read_text()
game=(java/'RoomGameActivity.java').read_text()
roster=(java/'KingPartyRoundRoster9972.java').read_text()
rules=(root/'firestore.rules').read_text()
assert 'if("Treasure Hunt".equals(name)){openRoomGames740();return;}' in party
assert 'treasureChest10100' in game and 'treasureControls10100()' in game
assert 'KingTreasureHuntRules10100.hit' in game
assert '"treasure"' in roster
assert "['Chest 1','Chest 2','Chest 3']" in rules
assert 'request.resource.data.score == 0' in rules
assert "request.resource.data.get('treasureChest',0)" in rules
print('PASS live Party Treasure Hunt route, Ready roster, one-pick and no-paid-prize rules')
