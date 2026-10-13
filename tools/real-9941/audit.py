#!/usr/bin/env python3
from pathlib import Path
import sys
root=Path(sys.argv[1]);s=root/'app/src/main/java/com/kingplus/social'
def dump(file,start,end):
  ls=(s/file).read_text().splitlines();print(f"\n### {file} {start}-{end}")
  for i in range(start-1,min(len(ls),end)):
    print(f"{i+1:04}: {ls[i][:1600]}")
def find(file, needles,ctx=4,cap=18):
  ls=(s/file).read_text().splitlines();print(f"\n### SEARCH {file}")
  hits=[i for i,x in enumerate(ls) if any(k in x for k in needles)]
  print("COUNT",len(hits))
  for i in hits[:cap]:
    for j in range(max(0,i-ctx),min(len(ls),i+ctx+1)):print(f"{j+1:04}: {ls[j][:900]}")
find('PartyActivity.java',['addSnapshotListener','showGiftEffect(', 'giftId','giftAudio','KingGiftSound993','giftListener','giftPlayed','giftQueue'],4,45)
dump('PartyActivity.java',3100,3180)
dump('RoomGameActivity.java',80,144)
dump('RoomGameActivity.java',520,603)
dump('KingSheepRace9925.java',1,35)
rules=(root/'firestore.rules').read_text().splitlines()
for i,x in enumerate(rules):
 if 'match /events/' in x or 'match /messages/' in x or 'match /game_ready/' in x or 'match /game_moves/' in x or 'match /game_state/' in x:
  print("\n### RULES",i+1)
  for k in range(i,min(i+51,len(rules))):print(f"{k+1:04}: {rules[k][:1100]}")

dump('PartyActivity.java',2355,2405)
dump('PartyActivity.java',5645,5695)
dump('PartyActivity.java',3595,3625)
