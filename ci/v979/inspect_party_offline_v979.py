#!/usr/bin/env python3
"""Investigate why 2 real phones cannot join same Firebase Party in v9.7.8."""
from pathlib import Path
import re,sys
root=Path(sys.argv[1]);p=root/'app/src/main/java/com/kingplus/social'
files=[p/'PartyActivity.java',p/'MainActivity.java',p/'KingNetwork.java',p/'KingPhoneOtp975Activity.java',p/'KingPartyJoinTimeout977.java']
patterns={
'PartyActivity.java':[
'private void joinMemberThenOpen891()', 'private void checkCrowdCapacity930(', 
'private void showPartyJoinFailure977(', 'private void roomJoinError891(', 
'private void openCloudRoom(', 'private void showJoiningRoom964(',
'private void startPartyJoinTimeout977(', 'private void renderLobby(',
'private void joinRoomByCode964(', 'private void retryRoomCreateLegacy921(',
'private void createCloudRoom', 'private void openRoomDoc891(',
'private void initFirebase(', 'db=FirebaseFirestore', 'db=FirebaseFirestore.getInstance',
'KingNetwork.online(', 'enableNetwork(', 'disableNetwork(', 'FirebaseFirestoreSettings',
'addSnapshotListener(', 'SOURCE.SERVER','Source.SERVER',
'FirebaseAuth.getInstance().getCurrentUser()','roomId=',
'getApplicationContext()', 'connectivity', 'roomId=id.trim()', 'cloudRoom=true',
'getIdToken(', 'memberSeen=false', 'showPartyJoinFailure977', 'private void ensureRoomMembership900('
],
'MainActivity.java':['disableNetwork(', 'enableNetwork(', 'setFirestoreSettings(', 'FirebaseFirestoreSettings',
'FirebaseFirestore.getInstance(', 'FirebaseApp.initializeApp(', 'KingNetwork',
'private void renderParty', 'new Intent(this,PartyActivity.class)', 'getIdToken('],
'KingNetwork.java':['public static boolean online','ConnectivityManager','NET_CAPABILITY_VALIDATED']
}
for file in files:
 if not file.exists():continue
 lines=file.read_text(errors='replace').splitlines()
 print('\n===',file.name,'LINES',len(lines),'===')
 pats=patterns.get(file.name,[])
 for k in pats:
  hits=[i for i,line in enumerate(lines) if k in line]
  print('KEY',repr(k),'HITS',len(hits), 'LOC',[h+1 for h in hits[:28]])
  maxshow=4 if k.startswith('private void') or 'joinRoomByCode' in k else 2
  for at in hits[:maxshow]:
   count=65 if k.startswith('private void joinMemberThenOpen') else 42 if k.startswith('private void checkCrowd') or k.startswith('private void showPartyJoinFailure') else 22
   print('BEGIN_WINDOW',at+1)
   for j in range(max(0,at-2),min(len(lines),at+count)):
    print(f'{j+1}: {lines[j][:1400]}')
print('\n=== ANDROID MANIFEST Firebase network permissions ===')
m=(root/'app/src/main/AndroidManifest.xml').read_text().splitlines()
for i,line in enumerate(m,1):
 if any(x in line for x in ['INTERNET','ACCESS_NETWORK_STATE','application','usesCleartextTraffic']):
  print(i,line)
