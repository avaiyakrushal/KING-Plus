#!/usr/bin/env python3
from pathlib import Path
import re,sys
root=Path(sys.argv[1]);pkg=root/'app/src/main/java/com/kingplus/social'
p=pkg/'PartyActivity.java';s=p.read_text();ln=s.splitlines()
terms=['private void unregisterMember(', 'void unregisterMember(', 'private void cleanupStaleRoom910(', 'private void startHeartbeat900(', 'private void heartbeat900(', 'private void listenRoom(', 'private void listenMembers(', 'private void attachRoomListeners(', 'private void bindRoomListeners(', 'private void openCloudRoom(', 'private void finishMemberJoin921(', 'private void ensureRoomMembership900(', 'private void setOnlineState900(', 'private void renderParty(', 'private void checkCrowdCapacity930(', 'private void openRoomDoc891(', 'memberNames.clear(', 'memberUids.clear(', 'collection("members").addSnapshotListener', 'collection("members").limit(', 'whereEqualTo("lastSeenAt"', 'memberNames.add(', 'joinedAt', 'lastSeenAt', 'memberSeen', 'membersListener =', 'membersListener=', 'roomId=null', 'roomId = null', 'memberCount', 'roomJoinGeneration966', 'startHeartbeat900();', 'onDestroy(){']
seen=set()
print('=== PARTY TARGETED RANGES ===')
for term in terms:
 matches=[m.start() for m in re.finditer(re.escape(term),s)]
 print(f'TERM {term!r} COUNT={len(matches)}')
 for at in matches[:4]:
  ix=s.count('\n',0,at)
  if ix in seen:continue
  print(f'BEGIN {term!r} AT {ix+1}')
  n=35 if ('private void' in term or 'void unregister' in term or 'membersListener' in term or 'collection("members")' in term) else 9
  for j in range(max(0,ix-2),min(len(ln),ix+n)):
   if j in seen:continue
   print(f'{j+1}: {ln[j][:1000]}')
   seen.add(j)
print('=== FIRESTORE MEMBERS RULES ===')
rule=(root/'firestore.rules').read_text().splitlines()
for i,l in enumerate(rule):
 if 'match /live_rooms/{roomId}' in l:
  for j in range(i,min(i+148,len(rule))):
   print(f'{j+1}: {rule[j][:1000]}')
print('=== CONFIG ===')
g=(root/'app/build.gradle').read_text()
for line in g.splitlines():
 if 'versionCode' in line or 'versionName' in line:print(line)
