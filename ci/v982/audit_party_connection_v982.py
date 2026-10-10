#!/usr/bin/env python3
"""Source-grounded v9.8.2 audit; inspect latest green v9.8.1 without APK decompile."""
from pathlib import Path
import re,sys
root=Path(sys.argv[1]);pkg=root/'app/src/main/java/com/kingplus/social'
p=pkg/'PartyActivity.java';s=p.read_text();rows=s.splitlines()
targets=[
'private void joinMemberThenOpen891(', 'private void reconnectPartyFirestore979(',
'private boolean currentReconnect979(', 'private void showPartyJoinFailure977(',
'private void showJoiningRoom964(', 'private void roomJoinError891(',
'private void joinRoomByCode964(', 'private void openCloudRoom(',
'private void enterLiveParty', 'private void registerMembers',
'private void createCloudRoom', 'private void openRoomDoc891(',
'private void renderParty(', 'private void setOnlineState900(',
'private void attachCloudRoom(', 'private void stopPartyJoinTimeout977(',
'private void ensureRoomMembership900(',
'private void registerNetwork', 'onResume()', 'onDestroy()',
'private void clearListeners(', 'private void renderLobby(',
]
print('PARTY_TOTAL_LINES',len(rows))
def method(sig,cap=10500):
 pos=s.find(sig)
 if pos<0:return None
 begin=s.find('{',pos)
 if begin<0:return None
 depth=0;quote=None;escaped=False
 for j in range(begin,len(s)):
  ch=s[j]
  if quote:
   if escaped: escaped=False
   elif ch=='\\':escaped=True
   elif ch==quote:quote=None
  elif ch in "'\"":quote=ch
  elif ch=='{':depth+=1
  elif ch=='}':
   depth-=1
   if depth==0:return s[pos:min(j+1,pos+cap)]
 return None
for x in targets:
 occurrence=s.count(x)
 print('FUNCTION',repr(x),'COUNT',occurrence)
 if occurrence==1:
  m=method(x,9500)
  print('START_LINE',s.count('\n',0,s.index(x))+1)
  print('BODY\n',m)
print('=== KEY FIXMARKERS ===')
for needle in ['roomJoinGeneration966','partyReconnectTimeout979',
'partyAutoRepairUsed979','roomJoinTimeout977','partyJoinHandler977',
'partyJoinError891','KingNetwork.watch','showPartyJoinFailure977',
'partyReconnecting979','memberSeen=true',
'get(com.google.firebase.firestore.Source.SERVER)']:
 inds=[i+1 for i,r in enumerate(rows) if needle in r]
 print('KEY',needle,'COUNT',len(inds),'LINES',inds[:25])
print('=== OTHER CONNECTION HELPER ===')
for fn in ['KingNetwork.java','KingPartyConnection979.java','KingPartyJoin977.java','KingPartyJoin966.java']:
 f=pkg/fn
 if f.exists():
  print('FILE',fn)
  print(f.read_text()[:9000])
print('=== VERSION ===')
print('\n'.join(l for l in (root/'app/build.gradle').read_text().splitlines() if 'versionCode' in l or 'versionName' in l))
