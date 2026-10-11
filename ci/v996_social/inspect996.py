#!/usr/bin/env python3
from pathlib import Path
root=Path('/tmp/king996')
p=root/'app/src/main/java/com/kingplus/social'
print('SOCIAL_AUDIT_996_BEGIN')
for name in ['SocialActivity.java','KingSocialIdentity965.java','ChatActivity.java','KingRoomInvite959.java','PartyActivity.java','KingGiftSound993.java']:
 f=p/name
 if not f.exists():print('MISSING',name);continue
 lines=f.read_text().splitlines()
 print('\nFILE',name,'LINES',len(lines))
 if name=='SocialActivity.java':
  patterns=['private void','public void','search','Search','Follow','follow','public_profiles','profile','save','whereEqualTo','collection(','onCreate','cloudReady','intent','uid','UID','King ID','refresh','set(']
  # print full file in chunks but capped to 700 lines, first 500 lines primary
  for i,l in enumerate(lines):
   if i<520:print(f'{i+1:05} {l[:370]}')
 elif name=='KingSocialIdentity965.java' or name=='KingRoomInvite959.java':
  for i,l in enumerate(lines[:250]):print(f'{i+1:05} {l[:400]}')
 elif name=='ChatActivity.java':
  for i,l in enumerate(lines):
   if any(x in l for x in ['shareRoom()', 'KINGROOM:', 'directRoomId', 'sendText(', 'room992','last_live_room','onCreate(', 'showChat(', 'watchMessages','addSnapshotListener']):
    print(f'{i+1:05} {l[:450]}')
 elif name=='PartyActivity.java':
  indices=[i for i,l in enumerate(lines) if any(x in l for x in ['directRoomId','openRoomDoc891(','openCloudRoom(','createCloudRoom','createRoom(', 'roomId=', 'ownerUid=', 'showLobbySearch(', 'memberSeen=true','roomReady', 'partyRoom', 'last_live_room_', 'private void showGiftEffect(', 'private void sendGiftQuantity('])]
  for i in indices[:75]:
   for j in range(max(0,i-1),min(len(lines),i+5)):print(f'{j+1:05} {lines[j][:470]}')
 elif name=='KingGiftSound993.java':
  for i,l in enumerate(lines[:190]):print(f'{i+1:05} {l[:350]}')
f=root/'firestore.rules'
print('\nFIRESTORE RULES')
if f.exists():
  lines=f.read_text().splitlines()
  print('LINES',len(lines))
  for i,l in enumerate(lines):
   if i<460:print(f'{i+1:05} {l[:500]}')
else:print('MISSING FIRESTORE RULES')
print('SOCIAL_AUDIT_996_END')
