#!/usr/bin/env python3
"""Inspect actual built v9.6.1 KING Plus game functionality before choosing v9.6.2 stage."""
from pathlib import Path
import re,sys
r=Path(sys.argv[1]);pkg=r/'app/src/main/java/com/kingplus/social'
targets=['OnlineLudoActivity.java','KingLudoLobbyActivity.java','RoomGameActivity.java','GamePlayActivity.java','PartyActivity.java']
patterns={
 'OnlineLudoActivity.java':['addSnapshotListener','runTransaction','dice','roll','move','turn','join','onCreate','Firestore','create','send','update'],
 'KingLudoLobbyActivity.java':['onCreate','start','join','roll','RoomGameActivity','OnlineLudoActivity'],
 'RoomGameActivity.java':['addSnapshotListener','runTransaction','dice','roll','move','turn','join','onCreate','Firestore','game_state','game_moves','GamePlayActivity'],
 'GamePlayActivity.java':['addSnapshotListener','runTransaction','dice','roll','move','turn','join','onCreate','Firestore','game_state'],
 'PartyActivity.java':['RoomGameActivity.class','OnlineLudoActivity.class','KingLudoLobbyActivity.class','game_moves','game_state','joinRoomGame','gamePanel','showGame']
}
for fn in targets:
 p=pkg/fn
 print('\\n===============',fn,'EXISTS',p.exists(),'===============')
 if not p.exists():continue
 lines=p.read_text(errors='replace').splitlines()
 print('LINE_COUNT',len(lines))
 found=[]
 for ix,s in enumerate(lines):
  if any(k.lower() in s.lower() for k in patterns[fn]):
   found.append(ix)
 print('MATCHES',len(found))
 if fn=='PartyActivity.java':
  for ix in found[:45]:print(f'{ix+1}: {lines[ix][:380]}')
 else:
  # Exhaustive small files, only significant sections of big files.
  if len(lines)<=340:
   for i,s in enumerate(lines):print(f'{i+1}: {s[:600]}')
  else:
   shown=set()
   for ix in found[:110]:
    for y in range(max(0,ix-2),min(len(lines),ix+3)):
     if y not in shown:
      print(f'{y+1}: {lines[y][:630]}')
      shown.add(y)
print('\\n=============== FIRESTORE RULES GAME ===============')
rules=(r/'firestore.rules').read_text().splitlines()
for i,s in enumerate(rules):
 if 'game_' in s or 'ludo' in s.lower():
  print(f'{i+1}: '+s[:330])
