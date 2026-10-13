#!/usr/bin/env python3
from pathlib import Path
import sys
r=Path(sys.argv[1]);p=r/'app/src/main/java/com/kingplus/social'
def seg(n,lo,hi):
 a=(p/n).read_text().splitlines();print(f'\n## DETAIL {n} {lo}-{hi}')
 for i in range(lo-1,min(hi,len(a))):print(f'{i+1}: {a[i][:2200]}')
seg('PartyActivity.java',2372,2412)
seg('PartyActivity.java',3110,3182)
seg('PartyActivity.java',3688,3720)
seg('PartyActivity.java',5680,5750)
seg('PartyActivity.java',5810,5908)
seg('KingGiftSound993.java',1,167)
seg('RoomGameActivity.java',118,130)
seg('RoomGameActivity.java',270,288)
for name in ['PartyActivity.java','KingGiftSound993.java','RoomGameActivity.java']:
 a=(p/name).read_text().splitlines()
 for term in ['showGiftEffect(', 'displayConfirmedGift9942','confirmGiftReceived9941','giftFxActive971','play(this,gift)','onPause','onResume','getMetadata','KingGiftSound993']:
  hit=[i+1 for i,s in enumerate(a) if term in s]
  if hit:print("HIT",name,term,hit[:25])
