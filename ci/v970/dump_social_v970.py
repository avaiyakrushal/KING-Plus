#!/usr/bin/env python3
from pathlib import Path
import sys
p=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social'
for name in ['SocialActivity.java','KingPublicProfileActivity.java']:
 f=p/name
 print('===== FILE',name,'=====')
 for i,l in enumerate(f.read_text().splitlines(),1):
  print(str(i)+": "+l)
print("===== MAIN LIFECYCLE / PUBLIC PROFILE =====")
l=(p/'MainActivity.java').read_text().splitlines()
for q in ['syncPublicProfile','showProfile','loadRealProfileData','startProfileFollowUpdates965','onResume()','onPause()']:
 inds=[i for i,x in enumerate(l) if q in x]
 for at in inds[:2]:
  print('KEY',q,'LINE',at+1)
  for j in range(max(0,at-3),min(len(l),at+20)):
   print(str(j+1)+": "+l[j][:600])
