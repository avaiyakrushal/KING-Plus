#!/usr/bin/env python3
from pathlib import Path
import sys,re
root=Path(sys.argv[1]);p=root/'app/src/main/java/com/kingplus/social'
files=['MainActivity.java','PartyActivity.java','KingNetwork.java','KingRoomCallback982.java']
for f in files:
 q=p/f
 if not q.exists():print('MISSING',f);continue
 s=q.read_text(errors='replace');l=s.splitlines()
 print('===',f,'LEN',len(l))
 if f=='MainActivity.java':
  for idx,row in enumerate(l):
   if any(q in row for q in [
     'openParty','PartyActivity.class','showMain','renderHome','showSettings','profilePage',
     'mainHome','dashboardPage','menu', 'support','rechargeCenterPage()', 'Room ID',
     'setContentView', 'private void home', 'button("','Button ', 'btn(', 'private void render',
     'KingNetwork', 'private void showProfile','support'
   ]):
    print('HIT',idx+1,row[:1600])
 elif f=='PartyActivity.java':
  for idx,row in enumerate(l):
   if any(q in row for q in [
     'showPartyJoinFailure977(', 'Copy connection report','showPartyJoinFailure',
     'private void renderLobby(', 'private void openCloudRoom(','joinRoomByCode964',
     'private void show','setContentView','private String shortId(',
     'private void renderParty(', 'KingNetwork.online'
   ]):
    print('HIT',idx+1,row[:1400])
 else:
  print(s[:13000])
print('=== MAIN FIRST 240 ===')
l=(p/'MainActivity.java').read_text(errors='replace').splitlines()
for i,x in enumerate(l[:240],1):print(f'{i}: {x[:1800]}')
print('=== MANIFEST ===')
l=(root/'app/src/main/AndroidManifest.xml').read_text().splitlines()
for i,x in enumerate(l,1):
 if '<activity' in x or '<uses-permission' in x:print(f'{i}: {x[:800]}')
print('=== APP VERSION ===')
for x in (root/'app/build.gradle').read_text().splitlines():
 if 'versionCode' in x or 'versionName' in x:print(x)
