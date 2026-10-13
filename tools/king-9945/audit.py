#!/usr/bin/env python3
from pathlib import Path
import sys
root=Path(sys.argv[1]);p=root/'app/src/main/java/com/kingplus/social'
def show(fn,a,b):
 f=p/fn;lines=f.read_text().splitlines()
 print('\n# REAL',fn,'lines',len(lines),'range',a,b)
 for idx in range(max(0,a-1),min(b,len(lines))):
  print(f'{idx+1}: {lines[idx][:2200]}')
def find(fn,terms,margin=3,cap=30):
 lines=(p/fn).read_text().splitlines()
 print('\n# FIND',fn)
 seen=set()
 for term in terms:
  hits=[i for i,v in enumerate(lines) if term in v]
  print('SEARCH',term,'HITS',[i+1 for i in hits[:30]])
  for n in hits[:cap]:
   for idx in range(max(0,n-margin),min(len(lines),n+margin+1)):
    if idx in seen:continue
    seen.add(idx)
    print(f'{idx+1}: {lines[idx][:2400]}')
show('RoomGameActivity.java',270,317)
show('RoomGameActivity.java',330,435)
show('RoomGameActivity.java',460,590)
find('RoomGameActivity.java',['finishRound()', 'score', 'RPS', 'rps', 'Rock', 'paper', 'winner','winReason','matchType','type.equals','setOnClickListener'])
find('GamePlayActivity.java',['rps', 'ludo', 'ttt', 'rock', 'winner'],2,15)
find('KingGiftDeliveryQueue9944.java',['offer','poll','clear'],6,15)

java=(p/'RoomGameActivity.java').read_text().splitlines()
for i in (650,651,652,659,660,661,662,663,664,665,666,667,668,669,670,671):
 if i<len(java):
  print('\n## RESULT_EXACT',i+1)
  line=java[i]
  for off in range(0,len(line),1000):print(f"CHUNK {off}: {line[off:off+1000]}")
rules=(root/'firestore.rules').read_text().splitlines()
for i,t in enumerate(rules):
 if 'match /game_moves/{playerUid}' in t:
  for idx in range(i,min(i+86,len(rules))):
   print('RULE_LINE',idx+1,rules[idx][:1100])
