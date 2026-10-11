#!/usr/bin/env python3
from pathlib import Path
p=Path('/tmp/kingfeature/app/src/main/java/com/kingplus/social')
for f in ['KingMessagingService.java','CloudSync.java','CommunityHubActivity.java','MainActivity.java','KingLoginBackdropView.java']:
 x=p/f
 if not x.exists():continue
 lines=x.read_text().splitlines()
 print('FILE',f,len(lines))
 if f=='CommunityHubActivity.java':
  searches=['private void family(){','private void familyJoinCreate(){','private void joinFamily','private void showFamily(','private void addMoment(']
 elif f=='MainActivity.java':
  searches=['private void login(){','button("f  Facebook','Continue with Google','private void mainScreen(']
 else:searches=[]
 if searches:
  for pat in searches:
   for i,l in enumerate(lines):
    if pat in l:
     print('BLOCK',f,pat,'LINE',i+1)
     for j in range(max(0,i-2),min(i+15,len(lines))):
      print(f'{j+1:05} {lines[j][:1700]}')
     break
 else:
  for i,l in enumerate(lines[:160]):
   print(f'{i+1:05} {l[:1500]}')
