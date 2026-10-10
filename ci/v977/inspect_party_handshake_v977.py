#!/usr/bin/env python3
"""Inspect the screen 'Connecting to Party... Verifying your account and room access'
in successfully built KING Plus v9.7.6 sources for timeout and membership hang."""
from pathlib import Path
import re,sys
root=Path(sys.argv[1]);pkg=root/'app/src/main/java/com/kingplus/social'
items=list(pkg.glob('*.java'))
words=['Connecting to Party','Verifying your account and room access',
 'Joining party','joinMemberThenOpen','checkCrowdCapacity','showPartyConnecting','CloudRoom',
 'party-connect','joinLoading','party-loading','joinRoomByCode','verifyRoomAccess',
 'openCloudRoom','memberSeen','roomJoinGeneration966','onFailure','onStop',
 'onDestroy','startActivity','PartyActivity','FirebaseUser']
for p in items:
 s=p.read_text(errors='replace')
 hit=[w for w in words if w.lower() in s.lower()]
 if not hit:continue
 if not any(k in p.name for k in ['Party','Main','Splash','Room','Home']) and not any('Connecting to Party'==x for x in hit):continue
 print('=== FILE',p.name,'LINES',s.count('\n')+1,'HITS',','.join(hit))
 lines=s.splitlines()
 spots=[]
 for i,l in enumerate(lines):
  if any(q.lower() in l.lower() for q in words):
   spots.append(i)
 for i in spots[:140]:
  if any(k.lower() in lines[i].lower() for k in ['Connecting to Party','Verifying your account','joinMemberThenOpen','checkCrowdCapacity','joinRoomByCode','verifyRoomAccess','showPartyConnecting','openCloudRoom','roomJoinGeneration966']):
   print('=== ANCHOR',i+1,lines[i][:1800])
   for j in range(max(0,i-5),min(len(lines),i+22)):
    print(f'{j+1}: {lines[j][:1600]}')
print('=== CANDIDATE CLASSES',[(f.name,len(f.read_text(errors='replace'))) for f in items if 'Party' in f.name or 'Room' in f.name])

party=(pkg/'PartyActivity.java').read_text().splitlines()
for a,b in [(180,270),(730,835),(855,955),(1002,1060),(1325,1415),(1800,1835),(4540,4646)]:
 print('=== EXACT RANGE',a,b)
 for i in range(a-1,min(len(party),b)):
  print(f'{i+1}: {party[i][:1500]}')
