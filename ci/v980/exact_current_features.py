#!/usr/bin/env python3
from pathlib import Path
import sys
p=Path(sys.argv[1])/'app/src/main/java/com/kingplus/social'
for filename,ranges in {
  'MainActivity.java':[(965,1000),(1125,1195),(1350,1395),(1860,1882)],
  'PartyActivity.java':[(2668,2745),(3445,3510),(4590,4613),(2840,2898),(2345,2440)],
  'KingCosmetics.java':[(1,126)],
  'KingLudoLobbyActivity.java':[(1,45)]
}.items():
 f=p/filename
 if not f.exists():continue
 lines=f.read_text().splitlines()
 print("===== FILE",filename,len(lines))
 for a,b in ranges:
  print("===== RANGE",a,b)
  for i in range(a-1,min(len(lines),b)):
   print(f"{i+1}: {lines[i][:2100]}")
