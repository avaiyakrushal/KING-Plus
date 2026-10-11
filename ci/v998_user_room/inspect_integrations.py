#!/usr/bin/env python3
"""Source-based review of remaining five-part KING Plus gaps. No private credentials."""
from pathlib import Path
import re
p=Path('/tmp/king998/app/src/main/java/com/kingplus/social')
def method(name,term,limit=100):
 f=p/name
 if not f.exists():
  print('MISSING_FILE',name);return
 lines=f.read_text().splitlines()
 ix=[i for i,l in enumerate(lines) if term in l]
 print('\nMETHOD',name,term,[i+1 for i in ix[:10]])
 for i in ix[:2]:
  for j in range(max(0,i-2),min(len(lines),i+limit)):
   print(f'{j+1:05} {lines[j][:650]}')
for x in [
 ('MainActivity.java','private void syncPublicProfile(',70),
 ('SocialActivity.java','private void runSearch()',120),
 ('SocialActivity.java','private void toggleFollow(',64),
 ('SocialActivity.java','private void loadDiscover()',55),
 ('SocialActivity.java','private void joinFullRoom996(',38),
 ('SocialActivity.java','private void verifyOwnProfile996(',50),
 ('PartyActivity.java','private void openRoomDoc891(',50),
 ('PartyActivity.java','private void joinRoomByCode964(',78),
 ('PartyActivity.java','private void showGiftEffect(',50),
 ('KingRoomDice997.java','addSnapshotListener(',32),
 ('KingPublicProfileActivity.java','Room Charisma',35),
 ('RoomGameActivity.java','private void startGame(',45)
]:method(*x)
rules=Path('/tmp/king998/firestore.rules').read_text().splitlines()
for lo,hi in [(337,357),(565,576),(130,140),(542,565)]:
 print('\nRULES',lo,hi)
 for i in range(lo-1,min(hi,len(rules))):print(f'{i+1:04} {rules[i][:450]}')
print('\nAUDIT_V998_DONE')
