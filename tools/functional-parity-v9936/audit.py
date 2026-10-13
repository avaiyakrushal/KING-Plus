#!/usr/bin/env python3
"""Read-only audit of the actual KING full release, not stale starter repo source."""
import re,sys
from pathlib import Path
root=Path(sys.argv[1])
src=root/'app/src/main/java/com/kingplus/social'
print("VERSION:",(root/'app/build.gradle').read_text().split('versionName')[-1][:60])
names=sorted(p.name for p in src.glob('*Game*.java'))+sorted(p.name for p in src.glob('*Room*.java'))
print("GAME_AND_ROOM_JAVA_CLASSES:",names[:60])
for name,terms in {
'PartyActivity.java':['setRoomPasswordDialog','tryPrivateEntry','verifyRoomPassword','grantRoom','roomAccess','grant.put(','access.put(','playRoomGame','openRoomGames740','roomGames','addEvent(','seat','Mic'],
'KingNative3DRoom.java':['Renderer','onTouch','motionX','motionDance','Unity','smooth('],
'KingGiftSound993.java':['play(','Thread','pending9932'],
'KingGiftCenterActivity.java':['sendGift','LIVE_RECHARGE','PAID_VIP_ACTIVE'],
'KingDeepFlowActivity.java':['designLibrary9934','drawableLibrary9935','row(','games','party']
}.items():
 path=src/name
 if not path.exists():continue
 ls=path.read_text().splitlines()
 print('\n## FILE',name,'LINES',len(ls))
 indexes=[]
 for i,line in enumerate(ls):
  if any(t in line for t in terms):indexes.append(i)
 print('MATCHES',len(indexes))
 for i in indexes[:60]:
  print(str(i+1)+': '+ls[i][:280])
 if name=='PartyActivity.java':
  for needle in ('private void setRoomPasswordDialog','private void tryPrivateEntry','private void setPrivateRoom','private void simpleRaceEvent'):
   hits=[j for j,v in enumerate(ls) if needle in v]
   for j in hits[:1]:
    print('--- CONTEXT FOR',needle)
    for k in range(max(0,j-5),min(len(ls),j+77)):
     print(str(k+1)+': '+ls[k][:250])
rules=(root/'firestore.rules').read_text().splitlines()
print('\n## FIRESTORE RULES',len(rules))
for needle in ['function roomAccess','match /live_rooms/{roomId}','match /access/{','match /events/{','match /games/{','match /follows/{','function roomModerator']:
 hits=[i for i,v in enumerate(rules) if needle in v]
 for j in hits[:2]:
  print('---',needle, 'line',j+1)
  for k in range(max(0,j-5),min(len(rules),j+30)):print(f'{k+1}: {rules[k][:180]}')
