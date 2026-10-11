#!/usr/bin/env python3
"""Stage 3 of five: audit real multiplayer turn/score security.
This is a read-only source investigation, not a gameplay completion claim.
"""
from pathlib import Path
p=Path('/tmp/finalgames/app/src/main/java/com/kingplus/social')
for file,patterns in [
 ('OnlineLudoActivity.java',["roll","dice","move","nextTurn","turnUid","winner","gameOver","transaction","runTransaction","Source.SERVER","getCurrentUser","ludo_matches","phase","player","onDestroy"]),
 ('RoomGameActivity.java',["game_moves","game_state","game_votes","winner","score","gameId","onDestroy","countDown","role","turn","Firebase","listener"]),
 ('KingRoomDice997.java',["SecureRandom","addSnapshotListener","actorUid","dice_room_997"]),
 ('KingOfflineLudoActivity.java',["winner","turn","player","move"])
]:
 f=p/file
 if not f.exists():print('MISSING',file);continue
 ls=f.read_text().splitlines()
 print('\nGAME_FILE',file,'TOTAL_LINES',len(ls))
 for term in patterns:
  hits=[i for i,l in enumerate(ls) if term.lower() in l.lower()]
  print('TERM',term,'COUNT',len(hits),'LINES',[x+1 for x in hits[:14]])
  for i in hits[:2]:print(f'{i+1:05}: {ls[i][:600]}')
rules=Path('/tmp/finalgames/firestore.rules').read_text().splitlines()
for term in ['match /ludo_matches/{matchCode}', 'match /game_moves/{playerUid}', 'match /game_state/{stateId}', 'match /game_votes/{playerUid}']:
 indexes=[i for i,l in enumerate(rules) if term in l]
 for i in indexes:
  print('\nRULE',term,i+1)
  for j in range(i,min(len(rules),i+28)):print(f'{j+1:05}: {rules[j][:500]}')
print('STAGE3_GAMEPLAY_AUDIT_COMPLETE')
