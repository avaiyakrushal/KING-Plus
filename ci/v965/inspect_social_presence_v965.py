#!/usr/bin/env python3
"""Small exact-file audit for v9.6.4 realtime-follow / room presence milestone."""
from pathlib import Path
import sys,re
root=Path(sys.argv[1]);pkg=root/'app/src/main/java/com/kingplus/social'
spec={
 'MainActivity.java':['private void refreshSocial','private void loadSocial','private void profile()','private void onDestroy()','profileFollowersNumber','loadProfileVisitors940','private void social','private void home()'],
 'SocialActivity.java':['@Override public void onCreate','private void toggleFollow','private void openProfile','private void showPerson','private void loadFollowers','private void loadFollowing','private void runSearch','private void loadDiscover','private void renderProfiles','private void onDestroy','private void addPerson'],
 'KingPublicProfileActivity.java':['@Override public void onCreate','private void load()','private void toggleFollow','private void checkFollow','private void loadSocial','private void refreshSocial','publicFollowers940','private void onDestroy','private String followId'],
 'PartyActivity.java':['private void finishMemberJoin921','private void joinMemberThenOpen891','private void checkCrowdCapacity930','private void registerMember','private void unregisterMember','private void renderParty()','private void clearListeners()','private void rebuildMemberStrip','private void onDestroy','private void listenRooms','private void showJoiningRoom964'],
}
for name,keys in spec.items():
 p=pkg/name
 print('\n\n########',name,'########')
 s=p.read_text()
 lines=s.splitlines()
 print('LINES',len(lines),'CHARS',len(s))
 hits=[]
 for key in keys:
  positions=[m.start() for m in re.finditer(re.escape(key),s,re.I)]
  print('MATCH',repr(key),'COUNT',len(positions))
  for pos in positions[:2]:
   lineno=s.count('\n',0,pos)
   hits.append((lineno,key))
 seen=set()
 for index,term in hits:
  # explicit compact source ranges to let agent patch exact marker
  end=min(len(lines), index+17 if 'MainActivity' in name else index+31)
  print('---',term,'LINE',index+1,'---')
  for i in range(max(0,index-2),end):
   if i not in seen:
    print(f'{i+1} | {lines[i][:360]}')
    seen.add(i)
for path in ['firestore.rules','app/build.gradle']:
 s=(root/path).read_text();print('########',path,'########')
 if path.endswith('rules'):
  for q in ['match /follows/{followId}', 'match /public_profiles/{uid}', 'match /live_rooms/{roomId}']:
   k=s.index(q); print(s[k:k+1150])
 else:
  for row in s.splitlines():
   if 'versionCode' in row or 'versionName' in row:print(row)
