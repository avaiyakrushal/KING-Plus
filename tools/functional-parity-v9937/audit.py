#!/usr/bin/env python3
"""Read-only audit of verified full KING v9.9.36 APK source; never dump credentials."""
from pathlib import Path
import sys,re
root=Path(sys.argv[1])
src=root/'app/src/main/java/com/kingplus/social'
for filename in ['RoomGameActivity.java','KingRoomDice997.java','KingGameRoutes980.java','GamePlayActivity.java','KingGiftCenterActivity.java','KingGiftSound993.java']:
 p=src/filename
 if not p.exists():continue
 ls=p.read_text().splitlines()
 print('\n##',filename,'lines',len(ls))
 if filename in ('RoomGameActivity.java','KingRoomDice997.java','KingGameRoutes980.java'):
  limit=350
  for i,s in enumerate(ls[:limit]):
   if any(k in s for k in ['AIza','private_key','api_key','google_app_id','client_secret']):continue
   print(f'{i+1:4} {s[:300]}')
 else:
  wanted=('roomId','gameId','playGame','gameState','gift','sender','recipient','transaction','sendGift','Cloud','GIFT','grant','award','balance','sound')
  n=0
  for i,s in enumerate(ls):
   if any(k in s for k in wanted):
    print(f'{i+1:4} {s[:285]}')
    n+=1
    if n>=105:break
p=src/'PartyActivity.java';ls=p.read_text().splitlines()
for pattern in ['private void openRoomGames740','private void handlePartyGame','private void openGame','private void sendGift','private void openGift','private void onGift','gameStateListener','private void simpleRaceEvent','gameState','KingRoomDice997']:
 hits=[i for i,s in enumerate(ls) if pattern in s]
 print('\n## PartyActivity pattern',pattern,'count',len(hits))
 for j in hits[:2]:
  for k in range(max(0,j-5),min(j+21,len(ls))):
   print(f'{k+1:4} {ls[k][:325]}')
rules=(root/'firestore.rules').read_text().splitlines()
for p in ['match /games/{','match /game','match /gift','match /events/{']:
 for j,s in enumerate(rules):
  if p in s:
   print('\n## Firestore',p,'line',j+1)
   for k in range(max(0,j-3),min(j+35,len(rules))):print(f'{k+1:4} {rules[k][:280]}')
print('\n@@END_AUDIT@@')

# Concrete interactions: Party gifting/fx event listener and dice scoring.
def segment(file,start,end):
 ls=(src/file).read_text().splitlines()
 print('\n## DETAIL',file,start,end)
 for k in range(start-1,min(end,len(ls))):print(f'{k+1:4} {ls[k][:450]}')
segment('PartyActivity.java',3070,3170)
segment('PartyActivity.java',5620,5670)
segment('KingRoomDice997.java',70,123)
segment('RoomGameActivity.java',200,300)
segment('RoomGameActivity.java',340,420)
