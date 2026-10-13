#!/usr/bin/env python3
from pathlib import Path
import sys
r=Path(sys.argv[1]);s=r/'app/src/main/java/com/kingplus/social'
print('VERSION', next((x for x in (r/'app/build.gradle').read_text().splitlines() if 'versionCode' in x),'MISSING'))
names=[p.name for p in s.glob('*.java') if any(k in p.name.lower() for k in ['ludo','sheep','knife','gift','party','roomgame'])]
print('FILES',names)
def pull(file,keys,limit=14,offset=7):
 p=s/file
 if not p.exists():print('MISSING',file);return
 lines=p.read_text().splitlines()
 print('\n###',file,'TOTAL_LINES',len(lines))
 ids=[i for i,l in enumerate(lines) if any(k.lower() in l.lower() for k in keys)]
 print('HITS',len(ids))
 for j in ids[:limit]:
  print('ANCHOR',j+1,lines[j][:300])
  for i in range(max(0,j-offset),min(len(lines),j+offset+1)):
   print(f'{i+1}: {lines[i][:650]}')
pull('RoomGameActivity.java',['ludo','knife','sheep','onDestroy','onStop','onPause','submit(','startRound('],limit=24,offset=4)
for p in names:
 if p=='RoomGameActivity.java':continue
 if 'ludo' in p.lower() or 'knife' in p.lower() or 'sheep' in p.lower():pull(p,['oncreate','submit','roll','dice','score','firebase','game_moves','game_state','runTransaction','setOnClickListener','private void','listener'],limit=25,offset=3)
pull('PartyActivity.java',['openPartyLudo','openRoomGames740','knife','sheep','showGiftEffect','private void displayGift','sendGiftQuantity','addGiftEvent'],limit=22,offset=3)
rules=(r/'firestore.rules').read_text().splitlines()
for i,l in enumerate(rules):
 if any(z in l for z in ('match /game_moves/', 'match /game_state/', 'match /game_ready/')):
  print('\n## Firestore',i+1)
  for j in range(i,min(i+75,len(rules))):print(f'{j+1}: {rules[j][:500]}')
