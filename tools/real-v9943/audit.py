#!/usr/bin/env python3
from pathlib import Path
import sys
r=Path(sys.argv[1]);s=r/'app/src/main/java/com/kingplus/social'
for fn in ['RoomGameActivity.java','KingRoomDice997.java','KingGiftSound993.java','PartyActivity.java']:
 p=s/fn;l=p.read_text().splitlines()
 print('\n### FILE',fn,'lines',len(l))
 if fn=='PartyActivity.java':
  terms=['private void attachGameState','private void sendGiftQuantity','private void showGiftEffect','private void refreshSupporters','private void attachV530RealtimeHelpers','private void clearListeners','gameStateListener','giftReceiptListener9941','micTogglePending968','private void toggleMic']
 elif fn=='RoomGameActivity.java':
  terms=['private void listenState','private void listenMoves','private void render','private void sendMove','private void processTttMove','private void process','onStop','onResume','onDestroy','private void refresh','private void watch','addSnapshotListener','getMetadata']
 elif fn=='KingRoomDice997.java':terms=['addSnapshotListener','onStop','setOnClickListener','Source.SERVER']
 else:terms=['static void play','static void enqueue','queue','pending','Audio','prepare','release']
 for term in terms:
  hits=[i for i,t in enumerate(l) if term in t]
  print('SEARCH',term,[(i+1,l[i][:160]) for i in hits[:8]])
  for i in hits[:2]:
   for j in range(max(0,i-4),min(len(l),i+21)):print(f'{j+1}: {l[j][:1300]}')
print('\n### FIRESTORE')
l=(r/'firestore.rules').read_text().splitlines()
for i,t in enumerate(l):
 if any(v in t for v in ['match /game_moves/','match /game_state/','match /events/','match /gift_receipts/']):
  print('RULE',i+1)
  for j in range(i,min(len(l),i+82)):print(f'{j+1}: {l[j][:1050]}')

p=s/'RoomGameActivity.java';l=p.read_text().splitlines()
print('\n### ROOM_GAME_EXACT')
for i in list(range(35,70))+list(range(120,130))+list(range(200,230))+list(range(274,289))+list(range(595,635)):
 if i<len(l):print(f'{i+1}: {l[i][:3000]}')
