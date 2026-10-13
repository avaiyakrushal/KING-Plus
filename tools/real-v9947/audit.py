#!/usr/bin/env python3
from pathlib import Path
import sys
r=Path(sys.argv[1]);src=r/'app/src/main/java/com/kingplus/social'
p=src/'RoomGameActivity.java'
lines=p.read_text().splitlines()
print('VERSION',next((s for s in (r/'app/build.gradle').read_text().splitlines() if 'versionCode' in s),None))
for a,b in [(25,77),(127,176),(265,330),(552,660),(660,740)]:
 print('\n## Game',a,b)
 for i in range(a-1,min(len(lines),b)):print(f'{i+1}: {lines[i][:2100]}')
for term in ['cancelRound','stopRound','finishRound','startRound','controls.addView','readyButton','ready750','gameStateVerified9943','participantUids','onDestroy','listenReady','renderControls','finishRole','onPause']:
 indexes=[i for i,x in enumerate(lines) if term in x]
 print('FIND',term,[(i+1,lines[i][:350]) for i in indexes[:15]])
rules=(r/'firestore.rules').read_text().splitlines()
for i,row in enumerate(rules):
 if 'match /game_state/{stateId}' in row or 'match /game_ready/{playerUid}' in row:
  print('\n## Firestore',i+1)
  for j in range(i,min(i+23,len(rules))):print(f'{j+1}: {rules[j][:1100]}')
