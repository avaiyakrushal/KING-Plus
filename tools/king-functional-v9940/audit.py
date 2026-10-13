#!/usr/bin/env python3
from pathlib import Path
import sys
r=Path(sys.argv[1]);java=r/'app/src/main/java/com/kingplus/social'
print("VERSION",next((x for x in (r/'app/build.gradle').read_text().splitlines() if "versionCode" in x),"unknown"))
def scan(filename,terms,context=11,limit=12):
 p=java/filename
 if not p.exists():return
 a=p.read_text().splitlines()
 print("\n###",filename,len(a))
 positions=[i for i,l in enumerate(a) if any(x in l for x in terms)]
 seen=set()
 for pos in positions[:limit]:
  for k in range(max(0,pos-context),min(len(a),pos+context+1)):
   if k in seen:continue
   seen.add(k)
   print(f'{k+1}: {a[k][:700]}')
scan('RoomGameActivity.java',['sheepRound9925','sheepTick9925','sheepControls','"sheep".equals','sheepTap','sheepClock','KingSheepRace9925'],6,30)
scan('KingSheepRace9925.java',['class','public static'],30,7)
scan('PartyActivity.java',['giftSound','giftEffect','giftQueue','giftId','giftValue','gift991','onGift','listenGift','showGift'],4,30)
scan('KingGiftSound993.java',['class','play(', 'pending9932'],14,12)
rules=(r/'firestore.rules').read_text().splitlines()
for i,line in enumerate(rules):
 if any(x in line for x in ['match /game_moves/', 'match /game_state/', 'match /events/', 'match /game_ready/']):
  print("\n### FIRESTORE",i+1)
  for j in range(i,min(len(rules),i+71)):print(f'{j+1}: {rules[j][:480]}')

for name,lo,hi in [
 ("RoomGameActivity.java",32,60),
 ("RoomGameActivity.java",75,120),
 ("RoomGameActivity.java",217,315),
 ("RoomGameActivity.java",435,604),
 ("PartyActivity.java",3140,3185)
]:
 p=java/name
 a=p.read_text().splitlines()
 print('\n### SPECIFIC',name,lo,hi)
 for j in range(lo-1,min(len(a),hi)): print(f'{j+1}: {a[j][:1400]}')
