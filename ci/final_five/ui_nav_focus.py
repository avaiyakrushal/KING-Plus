#!/usr/bin/env python3
from pathlib import Path
p=Path('/tmp/kingui/app/src/main/java/com/kingplus/social')
for file,ranges in {
 'MainActivity.java':[(127,166),(815,832),(1290,1333),(2017,2048)],
 'PartyActivity.java':[(310,345),(421,464),(570,610),(3878,3912),(3910,4016),(5285,5337)],
 'CommunityHubActivity.java':[(42,69)],
 'SocialActivity.java':[(78,151)]
}.items():
 f=p/file
 if not f.exists():continue
 s=f.read_text().splitlines()
 for a,b in ranges:
  print('\n===',file,a,b)
  for i in range(a-1,min(b,len(s))):print(f"{i+1:05} {s[i][:1300]}")
