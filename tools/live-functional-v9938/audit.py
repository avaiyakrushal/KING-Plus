#!/usr/bin/env python3
"""Read-only audit of actual released v9.9.37 code used for Android build."""
from pathlib import Path
import sys
r=Path(sys.argv[1]);s=r/'app/src/main/java/com/kingplus/social'
def section(path,lo,hi):
 p=Path(path);ls=p.read_text().splitlines()
 print(f'\n### {p.name}:{lo}-{hi}')
 for i in range(lo-1,min(hi,len(ls))):
  ln=ls[i]
  if any(k in ln.lower() for k in ['api_key','client_secret','private_key']):continue
  print(f'{i+1}: {ln[:2500]}')
section(r/'firestore.rules',116,175)
section(r/'firestore.rules',330,370)
section(r/'firestore.rules',548,645)
section(s/'RoomGameActivity.java',85,125)
section(s/'RoomGameActivity.java',253,280)
section(s/'RoomGameActivity.java',425,520)
section(s/'PartyActivity.java',3070,3160)
section(s/'PartyActivity.java',5640,5655)
section(s/'KingRoomDice997.java',70,130)
print('\n### Party Game room/State references')
ls=(s/'RoomGameActivity.java').read_text().splitlines()
for n,x in enumerate(ls):
 if any(k in x for k in ['private void submit(','private void startRound(','private void processTttMove864','private void resolve','private void settle','private void toggleReady750','private void openPartyLudo862','private void onResume','private void onPause']):
  print(f'{n+1}: {x[:3100]}')
print('\n### Gift widget inputs')
ls=(s/'PartyActivity.java').read_text().splitlines()
for i,l in enumerate(ls):
 if any(k in l for k in ['showGiftEffect(', 'private void gift','KingGiftSound993.play(', 'private void giftShop', 'private void liveGift']):
  print(f'{i+1}: {l[:900]}')
