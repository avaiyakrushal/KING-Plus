#!/usr/bin/env python3
from pathlib import Path
import sys
root=Path(sys.argv[1]); p=root/'app/src/main/java/com/kingplus/social'
for name in ['RoomGameActivity.java']:
 rows=(p/name).read_text().splitlines()
 for a,b in [(580,635),(644,675),(97,130),(220,248)]:
  print('###',name,a,b)
  for i in range(a-1,min(b,len(rows))):
   print(f'{i+1}: {rows[i][:3500]}')
f=(root/'firestore.rules').read_text().splitlines()
for i,l in enumerate(f):
 if 'match /game_state/{stateId}' in l or 'match /game_ready/' in l:
  print('### FIRESTORE',i+1)
  for j in range(i,min(i+100,len(f))):print(f'{j+1}: {f[j][:1500]}')
