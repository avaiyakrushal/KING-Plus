#!/usr/bin/env python3
from pathlib import Path
import sys,re
root=Path(sys.argv[1]);p=root/'app/src/main/java/com/kingplus/social/PartyActivity.java'
text=p.read_text(); lines=text.splitlines()
terms=['private void joinMemberThenOpen891(', 'private void checkCrowdCapacity930(', 'private void finishMemberJoin921(', 'private void retryMinimalMember921(', 'private void openRoomDoc891(', 'private void clearListeners(', 'private void setRoom', 'private void joinRoom', 'private void leaveRoom', 'private void renderLobby(', 'private void roomJoinError891(', 'private boolean isOwner(', 'void unregisterMember(', 'private void onDestroy(', 'private void handleRoom', 'checkCrowdCapacity930(', 'roomId=null', 'roomId = null', 'roomId=']
for q in terms:
 hits=[m.start() for m in re.finditer(re.escape(q),text)]
 print('SEARCH',repr(q),'COUNT',len(hits))
 for pos in hits[:5]:
  line=text.count('\n',0,pos)
  print('---',q,'LINE',line+1,'---')
  for y in range(max(0,line-2),min(len(lines),line+35)):
   print(f'{y+1}: {lines[y][:900]}')
print('=== imports and fields ===')
for i in range(min(190,len(lines))):print(f'{i+1}: {lines[i][:800]}')
