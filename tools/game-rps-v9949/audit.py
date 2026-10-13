#!/usr/bin/env python3
from pathlib import Path
import sys
r=Path(sys.argv[1]);p=r/'app/src/main/java/com/kingplus/social'
for n in ['RoomGameActivity.java']:
 lines=(p/n).read_text().splitlines()
 print('## FILE',n,'LEN',len(lines))
 for x in ['renderMoves()', 'renderMoves(', 'listenMoves', 'currentMoves','game_moves','finishRound','resultFor(', 'type.equals("rps")', '"rps".equals','roundId','status']:
  hits=[i for i,line in enumerate(lines) if x in line]
  print('FIND',x,hits[:15])
  for i in hits[:4]:
   for k in range(max(0,i-4),min(len(lines),i+13)):print(f'{k+1}: {lines[k][:2000]}')
 for a,b in [(265,365),(520,590),(665,725)]:
  print(f'## VIEW {a}-{b}')
  for i in range(a-1,min(b,len(lines))):print(f'{i+1}: {lines[i][:2400]}')
